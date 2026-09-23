package com.domainwatch.app;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class RdapClient {
 public static JSONObject check(String domain){
  domain=domain==null?"":domain.trim().toLowerCase(Locale.ROOT);
  if(domain.endsWith(".tr")) return checkTrWhois(domain);
  return checkRdap(domain);
 }

 private static JSONObject base(String domain){
  JSONObject o=new JSONObject();
  try{o.put("domain",domain);o.put("lastChecked",now());}catch(Exception ignored){}
  return o;
 }

 private static JSONObject checkTrWhois(String domain){
  JSONObject out=base(domain);
  Socket socket=null;
  try{
   socket=new Socket();
   socket.connect(new InetSocketAddress("whois.trabis.gov.tr",43),10000);
   socket.setSoTimeout(12000);
   OutputStream os=socket.getOutputStream();
   os.write((domain+"\r\n").getBytes("UTF-8"));
   os.flush();

   String raw=readWhois(socket.getInputStream());
   String low=raw.toLowerCase(Locale.ROOT);

   out.put("source","TRABIS WHOIS");
   out.put("whoisServer","whois.trabis.gov.tr");

   if(raw.trim().isEmpty()){
    out.put("availability","unknown");
    out.put("error","TRABIS boş yanıt döndürdü");
    return out;
   }

   if(isExplicitNoMatch(low)){
    out.put("availability","available");
    out.put("statuses",new JSONArray());
    out.put("nameservers",new JSONArray());
    return out;
   }

   out.put("availability","registered");
   out.put("registrar",firstValue(raw,new String[]{
     "Registrar Name","Registrar","Organization Name","Organization"
   }));
   out.put("created",parseWhoisDate(firstValue(raw,new String[]{
     "Created on","Creation Date","Created","Registration Date"
   })));
   out.put("expires",parseWhoisDate(firstValue(raw,new String[]{
     "Expires on","Expiry Date","Expiration Date","Expire Date","Expires"
   })));
   out.put("updated",parseWhoisDate(firstValue(raw,new String[]{
     "Last Updated","Updated Date","Last Update","Updated"
   })));
   out.put("statuses",extractStatuses(raw));
   out.put("nameservers",extractTrNameservers(raw));
   return out;
  }catch(Exception e){
   try{
    out.put("availability","unknown");
    out.put("source","TRABIS WHOIS");
    out.put("error",e.getClass().getSimpleName());
   }catch(Exception ignored){}
   return out;
  }finally{
   if(socket!=null)try{socket.close();}catch(Exception ignored){}
  }
 }

 private static boolean isExplicitNoMatch(String low){
  return low.contains("no match found") ||
         low.contains("no match") ||
         low.contains("not found") ||
         low.contains("no entries found") ||
         low.contains("domain not found") ||
         low.contains("kayıt bulunamad") ||
         low.contains("kayit bulunamad");
 }

 private static String firstValue(String raw,String[] keys){
  String[] lines=raw.split("\\r?\\n");
  for(String key:keys){
   String kl=key.toLowerCase(Locale.ROOT);
   for(String line:lines){
    String t=line.trim();
    String tl=t.toLowerCase(Locale.ROOT);
    if(tl.startsWith(kl.toLowerCase(Locale.ROOT))){
     int idx=t.indexOf(':');
     if(idx>=0&&idx<t.length()-1)return cleanValue(t.substring(idx+1));
     if(t.length()>key.length())return cleanValue(t.substring(key.length()));
    }
   }
  }
  return "";
 }

 private static String cleanValue(String s){
  s=s==null?"":s.trim();
  while(s.startsWith(".")||s.startsWith(":")||s.startsWith("-"))s=s.substring(1).trim();
  return s;
 }

 private static JSONArray extractStatuses(String raw){
  JSONArray a=new JSONArray();
  HashSet<String> seen=new HashSet<>();
  String[] lines=raw.split("\\r?\\n");
  for(String line:lines){
   String t=line.trim();
   String l=t.toLowerCase(Locale.ROOT);
   if(l.startsWith("domain status")||l.startsWith("status")||
      l.contains("transfer status")||l.contains("freeze status")||l.contains("frozen")){
    int idx=t.indexOf(':');
    String v=idx>=0?cleanValue(t.substring(idx+1)):t;
    if(!v.isEmpty()&&seen.add(v.toLowerCase(Locale.ROOT)))a.put(v);
   }
  }
  return a;
 }

 private static JSONArray extractTrNameservers(String raw){
  JSONArray a=new JSONArray();
  HashSet<String> seen=new HashSet<>();
  String[] lines=raw.split("\\r?\\n");
  boolean inBlock=false;
  for(String line:lines){
   String t=line.trim();
   String l=t.toLowerCase(Locale.ROOT);
   if(l.startsWith("** domain servers")||l.startsWith("** name servers")){inBlock=true;continue;}
   if(inBlock&&t.startsWith("**"))inBlock=false;

   String host="";
   if(l.startsWith("name server")){
    int idx=t.indexOf(':'); host=idx>=0?cleanValue(t.substring(idx+1)):cleanValue(t.substring("name server".length()));
   } else if(inBlock&&!t.isEmpty()){
    String[] parts=t.split("\\s+");
    if(parts.length>0)host=parts[0];
   }
   host=host.replaceAll("\\.$","");
   if(host.matches("(?i)[a-z0-9._-]+\\.[a-z]{2,}$")&&seen.add(host.toLowerCase(Locale.ROOT)))a.put(host);
  }
  return a;
 }

 private static String parseWhoisDate(String value){
  if(value==null||value.trim().isEmpty())return "";
  String v=value.trim().replaceAll("\\.$","");
  String[] pats={
    "yyyy-MMM-dd","yyyy-MM-dd","dd.MM.yyyy","dd-MM-yyyy",
    "yyyy/MM/dd","dd/MM/yyyy",
    "yyyy-MMM-dd HH:mm:ss","yyyy-MM-dd HH:mm:ss",
    "dd.MM.yyyy HH:mm:ss","yyyy-MM-dd'T'HH:mm:ssX"
  };
  for(String p:pats){
   try{
    SimpleDateFormat f=new SimpleDateFormat(p,Locale.US);
    f.setLenient(false);
    Date d=f.parse(v);
    if(d!=null){
     SimpleDateFormat out=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
     out.setTimeZone(TimeZone.getTimeZone("UTC"));
     return out.format(d);
    }
   }catch(Exception ignored){}
  }
  return v;
 }

 private static JSONObject checkRdap(String domain){
  JSONObject out=base(domain);
  HttpURLConnection con=null;
  try{
   URL url=new URL("https://rdap.org/domain/"+domain);
   con=(HttpURLConnection)url.openConnection();
   con.setConnectTimeout(12000);
   con.setReadTimeout(12000);
   con.setRequestProperty("Accept","application/rdap+json, application/json");
   con.setRequestProperty("User-Agent","DomainWatch-Android/1.0");
   int code=con.getResponseCode();

   out.put("source","RDAP");
   out.put("http",code);

   if(code==404){
    out.put("availability","available");
    return out;
   }
   if(code<200||code>=300){
    out.put("availability","unknown");
    return out;
   }

   JSONObject rdap=new JSONObject(read(con.getInputStream()));
   out.put("availability","registered");
   out.put("registrar",registrar(rdap));
   out.put("statuses",rdap.optJSONArray("status")!=null?rdap.optJSONArray("status"):new JSONArray());
   out.put("created",event(rdap,"registration"));
   out.put("updated",firstEvent(rdap,new String[]{"last changed","last update of RDAP database","last update"}));
   out.put("expires",firstEvent(rdap,new String[]{"expiration","expiry"}));
   out.put("nameservers",nameservers(rdap));
   return out;
  }catch(Exception e){
   try{out.put("availability","unknown");out.put("source","RDAP");out.put("error",e.getClass().getSimpleName());}catch(Exception ignored){}
   return out;
  }finally{
   if(con!=null)con.disconnect();
  }
 }

 private static String registrar(JSONObject r){
  JSONArray entities=r.optJSONArray("entities");
  if(entities==null)return "";
  for(int i=0;i<entities.length();i++){
   JSONObject e=entities.optJSONObject(i);if(e==null)continue;
   JSONArray roles=e.optJSONArray("roles");boolean reg=false;
   if(roles!=null)for(int j=0;j<roles.length();j++)if("registrar".equalsIgnoreCase(roles.optString(j)))reg=true;
   if(!reg)continue;
   JSONArray v=e.optJSONArray("vcardArray");
   if(v!=null&&v.length()>1){
    JSONArray props=v.optJSONArray(1);
    if(props!=null)for(int j=0;j<props.length();j++){
     JSONArray p=props.optJSONArray(j);
     if(p!=null&&"fn".equalsIgnoreCase(p.optString(0)))return p.optString(3);
    }
   }
   String h=e.optString("handle");if(!h.isEmpty())return h;
  }
  return "";
 }

 private static String event(JSONObject r,String a){
  JSONArray x=r.optJSONArray("events");if(x==null)return "";
  for(int i=0;i<x.length();i++){
   JSONObject e=x.optJSONObject(i);
   if(e!=null&&a.equalsIgnoreCase(e.optString("eventAction")))return e.optString("eventDate");
  }
  return "";
 }

 private static String firstEvent(JSONObject r,String[] actions){
  for(String s:actions){String v=event(r,s);if(!v.isEmpty())return v;}
  return "";
 }

 private static JSONArray nameservers(JSONObject r){
  JSONArray out=new JSONArray(),a=r.optJSONArray("nameservers");
  if(a==null)return out;
  for(int i=0;i<a.length();i++){
   JSONObject n=a.optJSONObject(i);
   if(n!=null)out.put(n.optString("ldhName"));
  }
  return out;
 }

 private static String read(InputStream in)throws Exception{
  BufferedReader br=new BufferedReader(new InputStreamReader(in,"UTF-8"));
  StringBuilder sb=new StringBuilder();String line;
  while((line=br.readLine())!=null)sb.append(line);
  return sb.toString();
 }

 private static String readWhois(InputStream in)throws Exception{
  BufferedReader br=new BufferedReader(new InputStreamReader(in,"UTF-8"));
  StringBuilder sb=new StringBuilder();String line;
  while((line=br.readLine())!=null)sb.append(line).append("\n");
  return sb.toString();
 }

 private static String now(){
  SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);
  f.setTimeZone(TimeZone.getTimeZone("UTC"));
  return f.format(new Date());
 }
}
