package com.domainwatch.app;
import android.app.*;
import android.app.job.*;
import android.content.*;
import android.os.Build;
import org.json.JSONObject;
import java.util.List;

public class DomainMonitorJob extends JobService {
 public static final int JOB_ID=47021;
 public static final String CHANNEL="domain_alerts";

 public static void schedule(Context c){
  try{
   createChannel(c);
   JobScheduler js=(JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
   if(js==null) return;
   JobInfo info=new JobInfo.Builder(JOB_ID,new ComponentName(c,DomainMonitorJob.class))
     .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
     .setPersisted(true)
     .setPeriodic(JobInfo.getMinPeriodMillis())
     .build();
   js.schedule(info);
  }catch(Throwable ignored){}
 }

 @Override public boolean onStartJob(JobParameters p){
  try{
   new Thread(()->{
    try{
     List<JSONObject> items=DomainStore.load(this);
     for(JSONObject old:items){
      String d=old.optString("domain");
      String before=old.optString("availability","unknown");
      JSONObject fresh=RdapClient.check(d);
      PricingClient.enrich(this,fresh);
      DomainStore.replace(this,fresh);
      handleNotifications(old,fresh,d,before);
     }
    }catch(Throwable ignored){}
    finally{try{jobFinished(p,false);}catch(Throwable ignored){}}
   }).start();
   return true;
  }catch(Throwable t){return false;}
 }

 @Override public boolean onStopJob(JobParameters p){return true;}

 private void handleNotifications(JSONObject old, JSONObject fresh, String d, String before){
  try{
   android.content.SharedPreferences p=getSharedPreferences("notify_settings",MODE_PRIVATE);
   if(!p.getBoolean("enabled",true))return;
   String av=fresh.optString("availability","unknown");
   if(p.getBoolean("available",true)&&!"available".equals(before)&&"available".equals(av)){
    notifyCustom("Domain boşa düştü",d+" kayıt için uygun görünüyor.",Math.abs(d.hashCode())); return;
   }
   String statuses=fresh.optJSONArray("statuses")!=null?fresh.optJSONArray("statuses").toString().toLowerCase():"";
   String oldStatuses=old.optJSONArray("statuses")!=null?old.optJSONArray("statuses").toString().toLowerCase():"";
   if(p.getBoolean("pendingDelete",true)&&statuses.contains("pendingdelete")&&!oldStatuses.contains("pendingdelete")){
    notifyCustom("Domain pendingDelete aşamasında",d+" silinme aşamasına girdi.",Math.abs((d+"pd").hashCode()));
   }
   if(p.getBoolean("redemption",true)&&statuses.contains("redemptionperiod")&&!oldStatuses.contains("redemptionperiod")){
    notifyCustom("Domain redemptionPeriod aşamasında",d+" redemptionPeriod durumuna girdi.",Math.abs((d+"rp").hashCode()));
   }
   if(p.getBoolean("expiring",true)){
    String exp=fresh.optString("expires");
    if(exp!=null&&!exp.isEmpty()){
     try{
      long days=java.time.Duration.between(java.time.Instant.now(),java.time.Instant.parse(exp)).toDays();
      String key="expwarn_"+d;
      if(days>=0&&days<=7&&!p.getBoolean(key,false)){
       notifyCustom("Domain süresi yaklaşıyor",d+" için yaklaşık "+days+" gün kaldı.",Math.abs((d+"ex").hashCode()));
       p.edit().putBoolean(key,true).apply();
      }
     }catch(Throwable ignored){}
    }
   }
  }catch(Throwable ignored){}
 }

 private void notifyCustom(String title,String text,int id){
  try{
   createChannel(this);
   Intent i=new Intent(this,MainActivity.class);
   PendingIntent pi=PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
   Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
   b.setSmallIcon(android.R.drawable.stat_notify_more).setContentTitle(title).setContentText(text).setAutoCancel(true).setContentIntent(pi).setPriority(Notification.PRIORITY_HIGH);
   NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
   if(nm!=null)nm.notify(id,b.build());
  }catch(Throwable ignored){}
 }

 private void notifyAvailable(String d){
  try{
   createChannel(this);
   Intent i=new Intent(this,MainActivity.class);
   PendingIntent pi=PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
   Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
   b.setSmallIcon(android.R.drawable.stat_notify_more)
    .setContentTitle("Domain boşa düştü")
    .setContentText(d+" kayıt için uygun görünüyor.")
    .setAutoCancel(true)
    .setContentIntent(pi)
    .setPriority(Notification.PRIORITY_HIGH);
   NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
   if(nm!=null)nm.notify(Math.abs(d.hashCode()),b.build());
  }catch(Throwable ignored){}
 }

 public static void createChannel(Context c){
  try{
   if(Build.VERSION.SDK_INT>=26){
    NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
    if(nm!=null)nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Domain uyarıları",NotificationManager.IMPORTANCE_HIGH));
   }
  }catch(Throwable ignored){}
 }
}
