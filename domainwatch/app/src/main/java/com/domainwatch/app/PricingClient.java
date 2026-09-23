package com.domainwatch.app;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.util.Locale;

public class PricingClient {
 public static JSONObject enrich(Context c, JSONObject domainInfo){
  try{
   String token=c.getSharedPreferences("pricing_settings",Context.MODE_PRIVATE).getString("godaddy_pat","");
   if(token==null||token.trim().isEmpty()){
    domainInfo.put("priceSource","GoDaddy API (token gerekli)");
    return domainInfo;
   }
   String domain=domainInfo.optString("domain");
   HttpURLConnection con=(HttpURLConnection)new URL("https://api.godaddy.com/v3/domains/check-availability?domain="+URLEncoder.encode(domain,"UTF-8")).openConnection();
   con.setConnectTimeout(12000);con.setReadTimeout(12000);
   con.setRequestProperty("Authorization","Bearer "+token.trim());
   con.setRequestProperty("Accept","application/json");
   int code=con.getResponseCode();
   domainInfo.put("priceSource","GoDaddy API");
   domainInfo.put("priceHttp",code);
   if(code<200||code>=300){
    domainInfo.put("priceError","HTTP "+code);
    return domainInfo;
   }
   JSONObject j=new JSONObject(read(con.getInputStream()));
   domainInfo.put("godaddyAvailable",j.optBoolean("available",false));
   domainInfo.put("inventory",j.optString("inventory",""));
   JSONArray prices=j.optJSONArray("prices");
   if(prices!=null&&prices.length()>0){
    JSONObject pick=null;
    for(int i=0;i<prices.length();i++){
      JSONObject p=prices.optJSONObject(i);
      if(p==null)continue;
      if("YEAR".equalsIgnoreCase(p.optString("term"))&&p.optInt("period",0)==1){pick=p;break;}
      if(pick==null)pick=p;
    }
    if(pick!=null){
      putMoney(domainInfo,"registrationPrice",pick.optJSONObject("price"));
      putMoney(domainInfo,"renewalPrice",pick.optJSONObject("renewalPrice"));
      putMoney(domainInfo,"firstTermPrice",pick.optJSONObject("firstTermPrice"));
      JSONArray fees=pick.optJSONArray("fees");
      if(fees!=null){
       for(int i=0;i<fees.length();i++){
        JSONObject fee=fees.optJSONObject(i);
        if(fee==null)continue;
        String type=fee.optString("type",fee.optString("name",""));
        if(type.toLowerCase(Locale.ROOT).contains("premium")){
          JSONObject amount=fee.optJSONObject("amount");
          if(amount==null)amount=fee.optJSONObject("price");
          putMoney(domainInfo,"premiumFee",amount);
          domainInfo.put("premiumFeeType",type);
        }
       }
      }
    }
   }
   return domainInfo;
  }catch(Exception e){
   try{domainInfo.put("priceError",e.getClass().getSimpleName());domainInfo.put("priceSource","GoDaddy API");}catch(Exception ignored){}
   return domainInfo;
  }
 }

 private static void putMoney(JSONObject out,String key,JSONObject m){
  if(m==null)return;
  try{
   String cur=m.optString("currencyCode",m.optString("currency",""));
   long val=m.optLong("value",Long.MIN_VALUE);
   if(val!=Long.MIN_VALUE){
    out.put(key,((double)val)/100.0);
    out.put(key+"Currency",cur);
   }
  }catch(Exception ignored){}
 }

 private static String read(InputStream in)throws Exception{
  BufferedReader br=new BufferedReader(new InputStreamReader(in,"UTF-8"));
  StringBuilder sb=new StringBuilder();String line;
  while((line=br.readLine())!=null)sb.append(line);
  return sb.toString();
 }
}
