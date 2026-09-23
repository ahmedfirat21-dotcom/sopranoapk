package com.domainwatch.app;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
public class RdapClient {
 public static JSONObject check(String domain){
  JSONObject out=new JSONObject();try{out.put("domain",domain);}catch(Exception ignored){}
  HttpURLConnection con=null;
  try{
   URL url=new URL("https://rdap.org/domain/"+domain);con=(HttpURLConnection)url.openConnection();con.setConnectTimeout(12000);con.setReadTimeout(12000);
   con.setRequestProperty("Accept","application/rdap+json, application/json");con.setRequestProperty("User-Agent","DomainWatch-Android/1.0");
   int code=con.getResponseCode();
   if(code==404){out.put("availability","available");out.put("http",code);out.put("lastChecked",now());return out;}
   if(code<200||code>=300){out.put("availability","unknown");out.put("http",code);out.put("lastChecked",now());return out;}
   JSONObject rdap=new JSONObject(read(con.getInputStream()));out.put("availability","registered");out.put("http",code);out.put("registrar",registrar(rdap));
   out.put("statuses",rdap.optJSONArray("status")!=null?rdap.optJSONArray("status"):new JSONArray());
   out.put("created",event(rdap,"registration"));out.put("updated",firstEvent(rdap,new String[]{"last changed","last update of RDAP database","last update"}));
   out.put("expires",firstEvent(rdap,new String[]{"expiration","expiry"}));out.put("nameservers",nameservers(rdap));out.put("lastChecked",now());return out;
  }catch(Exception e){try{out.put("availability","unknown");out.put("error",e.getClass().getSimpleName());out.put("lastChecked",now());}catch(Exception ignored){}return out;}
  finally{if(con!=null)con.disconnect();}
 }
 private static String registrar(JSONObject r){JSONArray entities=r.optJSONArray("entities");if(entities==null)return "";for(int i=0;i<entities.length();i++){JSONObject e=entities.optJSONObject(i);if(e==null)continue;JSONArray roles=e.optJSONArray("roles");boolean reg=false;if(roles!=null)for(int j=0;j<roles.length();j++)if("registrar".equalsIgnoreCase(roles.optString(j)))reg=true;if(!reg)continue;JSONArray v=e.optJSONArray("vcardArray");if(v!=null&&v.length()>1){JSONArray props=v.optJSONArray(1);if(props!=null)for(int j=0;j<props.length();j++){JSONArray p=props.optJSONArray(j);if(p!=null&&"fn".equalsIgnoreCase(p.optString(0)))return p.optString(3);}}String h=e.optString("handle");if(!h.isEmpty())return h;}return "";}
 private static String event(JSONObject r,String a){JSONArray x=r.optJSONArray("events");if(x==null)return "";for(int i=0;i<x.length();i++){JSONObject e=x.optJSONObject(i);if(e!=null&&a.equalsIgnoreCase(e.optString("eventAction")))return e.optString("eventDate");}return "";}
 private static String firstEvent(JSONObject r,String[] actions){for(String s:actions){String v=event(r,s);if(!v.isEmpty())return v;}return "";}
 private static JSONArray nameservers(JSONObject r){JSONArray out=new JSONArray(),a=r.optJSONArray("nameservers");if(a==null)return out;for(int i=0;i<a.length();i++){JSONObject n=a.optJSONObject(i);if(n!=null)out.put(n.optString("ldhName"));}return out;}
 private static String read(InputStream in)throws Exception{BufferedReader br=new BufferedReader(new InputStreamReader(in));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line);return sb.toString();}
 private static String now(){SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US);f.setTimeZone(TimeZone.getTimeZone("UTC"));return f.format(new Date());}
}