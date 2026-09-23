package com.domainwatch.app;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
public class MainActivity extends Activity {
 private LinearLayout cards;private EditText input;private TextView status;private Button refresh;
 private static final Pattern DOMAIN=Pattern.compile("^(?=.{1,253}$)(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,63}$");
 @Override public void onCreate(Bundle b){super.onCreate(b);buildUi();DomainMonitorJob.schedule(this);DomainMonitorJob.createChannel(this);askNotifications();render();}
 private void buildUi(){
  int pad=dp(16);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(pad,pad,pad,pad);root.setBackgroundColor(Color.rgb(246,247,249));
  TextView title=t("Domain Nöbetçisi",28,true);root.addView(title);
  TextView sub=t("Birden fazla domaini takip et • süre ve RDAP bilgilerini gör",14,false);sub.setTextColor(Color.DKGRAY);sub.setPadding(0,0,0,dp(12));root.addView(sub);
  LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
  input=new EditText(this);input.setHint("ornek.com");input.setSingleLine(true);row.addView(input,new LinearLayout.LayoutParams(0,dp(52),1));
  Button add=new Button(this);add.setText("EKLE");row.addView(add,new LinearLayout.LayoutParams(dp(92),dp(52)));root.addView(row);
  refresh=new Button(this);refresh.setText("TÜMÜNÜ ŞİMDİ KONTROL ET");root.addView(refresh,new LinearLayout.LayoutParams(-1,dp(50)));
  status=t("Hazır",13,false);status.setTextColor(Color.DKGRAY);status.setPadding(0,dp(4),0,dp(8));root.addView(status);
  ScrollView sv=new ScrollView(this);cards=new LinearLayout(this);cards.setOrientation(LinearLayout.VERTICAL);sv.addView(cards);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
  add.setOnClickListener(v->addDomain());refresh.setOnClickListener(v->refreshAll());
 }
 private void addDomain(){String d=input.getText().toString().trim().toLowerCase(Locale.ROOT).replace("https://","").replace("http://","");int slash=d.indexOf('/');if(slash>=0)d=d.substring(0,slash);if(d.startsWith("www."))d=d.substring(4);if(!DOMAIN.matcher(d).matches()){toast("Geçerli bir domain yaz.");return;}boolean added=DomainStore.add(this,d);input.setText("");render();if(added)refreshOne(d);else toast("Bu domain zaten listede.");}
 private void refreshAll(){List<JSONObject> items=DomainStore.load(this);if(items.isEmpty()){toast("Önce domain ekle.");return;}status.setText("Kontrol ediliyor…");refresh.setEnabled(false);new Thread(()->{for(JSONObject o:items){JSONObject fresh=RdapClient.check(o.optString("domain"));DomainStore.replace(this,fresh);runOnUiThread(this::render);}runOnUiThread(()->{status.setText("Kontrol tamamlandı");refresh.setEnabled(true);});}).start();}
 private void refreshOne(String d){status.setText(d+" kontrol ediliyor…");new Thread(()->{JSONObject f=RdapClient.check(d);DomainStore.replace(this,f);runOnUiThread(()->{render();status.setText("Hazır");});}).start();}
 private void render(){cards.removeAllViews();List<JSONObject> items=DomainStore.load(this);if(items.isEmpty()){TextView e=t("Henüz domain eklenmedi.",16,false);e.setPadding(0,dp(22),0,0);cards.addView(e);return;}for(JSONObject o:items)cards.addView(card(o));}
 private View card(JSONObject o){
  LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),dp(14),dp(16),dp(14));
  android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(14));bg.setStroke(dp(1),Color.rgb(220,223,228));box.setBackground(bg);
  LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.setMargins(0,0,0,dp(12));box.setLayoutParams(bp);
  String av=o.optString("availability","unknown"),icon="available".equals(av)?"🟢":"registered".equals(av)?"🔴":"🟡";
  LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);TextView name=t(icon+"  "+o.optString("domain"),20,true);head.addView(name,new LinearLayout.LayoutParams(0,-2,1));
  Button del=new Button(this);del.setText("SİL");del.setTextSize(12);head.addView(del,new LinearLayout.LayoutParams(dp(72),dp(44)));box.addView(head);del.setOnClickListener(v->{DomainStore.remove(this,o.optString("domain"));render();});
  String availability="available".equals(av)?"Kayıt için uygun görünüyor":"registered".equals(av)?"Kayıtlı":"Durum belirsiz";addLine(box,"Durum",availability);
  String exp=o.optString("expires");addLine(box,"Bitiş tarihi",fmt(exp));addLine(box,"Kalan süre",remaining(exp,av));addLine(box,"Registrar",empty(o.optString("registrar")));
  addLine(box,"Durum kodları",join(o.optJSONArray("statuses")));addLine(box,"Kayıt tarihi",fmt(o.optString("created")));addLine(box,"Son güncelleme",fmt(o.optString("updated")));
  addLine(box,"Name server",join(o.optJSONArray("nameservers")));addLine(box,"Son kontrol",fmt(o.optString("lastChecked")));
  TextView note=t("Not: Sürenin dolması domainin hemen boşa düştüğü anlamına gelmez. redemptionPeriod / pendingDelete gibi durumları da kontrol et.",12,false);note.setTextColor(Color.GRAY);note.setPadding(0,dp(8),0,0);box.addView(note);return box;
 }
 private void addLine(LinearLayout box,String k,String v){TextView tv=t(k+":  "+(v==null||v.isEmpty()?"—":v),14,false);tv.setPadding(0,dp(3),0,dp(3));box.addView(tv);}
 private String join(JSONArray a){if(a==null||a.length()==0)return "—";StringBuilder s=new StringBuilder();for(int i=0;i<a.length();i++){if(i>0)s.append(", ");s.append(a.optString(i));}return s.toString();}
 private String empty(String s){return s==null||s.isEmpty()?"—":s;}
 private String fmt(String iso){if(iso==null||iso.isEmpty())return "—";try{Instant i=Instant.parse(iso);return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault()).format(i);}catch(Exception e){return iso;}}
 private String remaining(String exp,String av){if("available".equals(av))return "Boşa düşmüş görünüyor";if(exp==null||exp.isEmpty())return "—";try{long sec=Duration.between(Instant.now(),Instant.parse(exp)).getSeconds();if(sec<=0)return "Süre geçmiş; silinme aşaması ayrıca kontrol edilmeli";long d=sec/86400,h=(sec%86400)/3600,m=(sec%3600)/60;return d+" gün "+h+" saat "+m+" dk";}catch(Exception e){return "—";}}
 private TextView t(String s,int sp,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(Color.rgb(28,30,34));if(bold)v.setTypeface(null,1);return v;}
 private int dp(int v){return(int)(v*getResources().getDisplayMetrics().density+0.5f);}private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
 private void askNotifications(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},11);}
}