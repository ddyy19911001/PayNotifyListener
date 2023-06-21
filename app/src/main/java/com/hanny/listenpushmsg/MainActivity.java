package com.hanny.listenpushmsg;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hanny.listenpushmsg.bean.NotifyTopBean;
import com.hanny.listenpushmsg.service.ForegroundService;
import com.hanny.listenpushmsg.service.NotifyService;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_ID = 1;
    private TextView tvMsg;
    private Intent mForegroundService;
    private TextView tvMoneyMsg;
    private boolean isStartDoing;
    private Button btStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startForegroundService();
        //开始监听
        btStart = findViewById(R.id.btStart);
        findViewById(R.id.btStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEnable(MainActivity.this)) {
                    openSetting(MainActivity.this);
                    toggleNotificationListenerService(MainActivity.this);
                }
                toggleNotificationListenerService();
            }
        });
        tvMsg = findViewById(R.id.tvMsg);
        tvMoneyMsg = findViewById(R.id.tvMoneyMsg);
        registBroadCast();
        toggleNotificationListenerService();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!isEnable(MainActivity.this)) {
            isStartDoing=false;
        }else{
            isStartDoing=true;
        }
        updateBtText();
    }

    private void updateBtText() {
        if(isStartDoing){
            btStart.setTextColor(Color.parseColor("#44aa55"));
            btStart.setText("监控正在运行");
        }else{
            btStart.setTextColor(Color.parseColor("#282828"));
            btStart.setText("点击开始监控");
        }
    }

    public void stopForegroundService(){
        //停止服务
        mForegroundService = new Intent(this, ForegroundService.class);
        stopService(mForegroundService);
    }


    public void startForegroundService(){
        //启动服务
        if (!ForegroundService.serviceIsLive) {
            // Android 8.0使用startForegroundService在前台启动新服务
            mForegroundService = new Intent(this, ForegroundService.class);
            mForegroundService.putExtra("Foreground", "This is a foreground service.");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(mForegroundService);
            } else {
                startService(mForegroundService);
            }
        } else {
            Log.i( "监控服务：","前台服务正在运行中...");
        }
    }


    private void openSetting(Context context) {
        try {
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            } else {
                intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            }
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    private boolean isEnable(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopForegroundService();
    }

    private void toggleNotificationListenerService(Context context) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(context, NotifyService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(
                new ComponentName(context, NotifyService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private void registBroadCast() {
        IntentFilter filter = new IntentFilter(NotifyService.SEND_MSG_BROADCAST);
        registerReceiver(receiver, filter);
    }


    private void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName(this, NotifyService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(
                new ComponentName(this, NotifyService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }


    public SimpleDateFormat format=new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotifyTopBean msg = (NotifyTopBean) intent.getSerializableExtra("msg");
            //过滤其他垃圾通知，只保留微信与支付宝
            String bt=msg.getTitle();
            if (bt.contains("微信支付") || bt.contains("收款通知")||bt.contains("你已成功收款")) {
                //输出run控制台打印
                Log.i("收款到账消息：", bt);
                //提取字符串里的整数与小数点
                //Pattern p = Pattern.compile("\\d+");
                Pattern p = Pattern.compile("(([1-9]\\d*)|0)(\\.(\\d){0,2})?");
                Matcher m = p.matcher(bt);
                m.find();
                //金额参数
                String amount = m.group();
                //厂家标题
                String title = bt.toString();
                msg.setAmount(amount);
                Log.i(title+"到账金额：----提取数字---->", amount);
                openVipOrGiveMoney(msg);
            }
            if(msg!=null&&!msg.equals("")) {
                if(!TextUtils.isEmpty(tvMsg.getText())){
                    tvMsg.append("\n\n");
                }
                tvMsg.append(format.format(msg.getTime())+"("+msg.getTitle()+")");
                tvMsg.append("\n内容："+msg.getContent());
            }
        }
    };


    /**
     * 接到了收款信息，给用户开通或赠送积分等
     * @param msg
     */
    private void openVipOrGiveMoney(NotifyTopBean msg) {
        tvMoneyMsg.append(msg.getTitle()+"\n--->（合计收到："+msg.getAmount()+"元）\n\n");
    }
}
