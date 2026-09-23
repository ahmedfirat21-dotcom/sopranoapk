package com.domainwatch.app;
import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
public class DomainStore {
 private static final String PREF="domains",KEY="items";
 public static synchronized List<JSONObject> load(Context c){
  List<JSONObject> out=new ArrayList<>(); String raw=c.getSharedPreferences(PREF,Context.MODE_PRIVATE).getString(KEY,"[]");
  try{JSONArray a=new JSONArray(raw);for(int i=0;i<a.length();i++)out.add(a.getJSONObject(i));}catch(Exception ignored){} return out;
 }
 public static synchronized void save(Context c,List<JSONObject> items){JSONArray a=new JSONArray();for(JSONObject o:items)a.put(o);c.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putString(KEY,a.toString()).apply();}
 public static synchronized boolean add(Context c,String d){List<JSONObject> items=load(c);for(JSONObject o:items)if(d.equalsIgnoreCase(o.optString("domain")))return false;JSONObject o=new JSONObject();try{o.put("domain",d.toLowerCase());o.put("availability","unknown");o.put("lastChecked","");}catch(Exception ignored){}items.add(0,o);save(c,items);return true;}
 public static synchronized void remove(Context c,String d){List<JSONObject> items=load(c),keep=new ArrayList<>();for(JSONObject o:items)if(!d.equalsIgnoreCase(o.optString("domain")))keep.add(o);save(c,keep);}
 public static synchronized void replace(Context c,JSONObject fresh){String d=fresh.optString("domain");List<JSONObject> items=load(c);for(int i=0;i<items.size();i++)if(d.equalsIgnoreCase(items.get(i).optString("domain"))){items.set(i,fresh);save(c,items);return;}items.add(fresh);save(c,items);}
}