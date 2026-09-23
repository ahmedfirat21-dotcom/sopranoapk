package com.domainwatch.app;
import android.app.*;
import android.app.job.*;
import android.content.*;
import android.os.Build;
import org.json.JSONObject;
import java.util.List;
public class DomainMonitorJob extends JobService {
 public static final int JOB_ID=47021; public static final String CHANNEL="domain_alerts";
 public static void schedule(Context c){
  JobScheduler js=(JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
  JobInfo.Builder b=new JobInfo.Builder(JOB_ID,new ComponentName(c,DomainMonitorJob.class)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true).setPeriodic(15*60*1000L);
  js.schedule(b.build()); createChannel(c);
 }
 @Override public boolean onStartJob(JobParameters p){new Thread(()->{List<JSONObject> items=DomainStore.load(this);for(JSONObject old:items){String d=old.optString("domain"),before=old.optString("availability","unknown");JSONObject fresh=RdapClient.check(d);DomainStore.replace(this,fresh);if(!"available".equals(before)&&"available".equals(fresh.optString("availability")))notifyAvailable(d);}jobFinished(p,false);}).start();return true;}
 @Override public boolean onStopJob(JobParameters p){return true;}
 private void notifyAvailable(String d){createChannel(this);Intent i=new Intent(this,MainActivity.class);PendingIntent pi=PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);b.setSmallIcon(android.R.drawable.stat_notify_more).setContentTitle("Domain boşa düştü").setContentText(d+" kayıt için uygun görünüyor.").setAutoCancel(true).setContentIntent(pi).setPriority(Notification.PRIORITY_HIGH);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(Math.abs(d.hashCode()),b.build());}
 public static void createChannel(Context c){if(Build.VERSION.SDK_INT>=26){NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Domain uyarıları",NotificationManager.IMPORTANCE_HIGH));}}
}