package com.hanny.listenpushmsg.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.hanny.listenpushmsg.MainActivity;
import com.hanny.listenpushmsg.R;

public class ForegroundService extends Service {

    private static final String TAG = ForegroundService.class.getSimpleName();
    public static boolean serviceIsLive;
    private int NOTIFICATION_ID=1;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        Notification notification=startQtService();
        //将服务置于启动状态 ,NOTIFICATION_ID指的是创建的通知的ID
        startForeground(NOTIFICATION_ID, notification);
    }


    /**
     * 创建服务通知
     */
    private Notification startQtService() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 唯一的通知通道的id.
        String notificationChannelId = "notification_channel_id_01";

        // Android8.0以上的系统，新建消息通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //用户可见的通道名称
            String channelName = "Foreground Service Notification";
            //通道的重要程度
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, channelName, importance);
            notificationChannel.setDescription("Channel description");
            //LED灯
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            //震动
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId);
        //通知小图标
        builder.setSmallIcon(R.mipmap.ic_launcher);
        //通知标题
        builder.setContentTitle("通知监听服务");
        //通知内容
        builder.setContentText("正在运行");
        //设定通知显示的时间
        builder.setWhen(System.currentTimeMillis());
        builder.setOngoing(true);
        //设定启动的内容
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        //创建通知并返回
        return builder.build();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        // 标记服务启动
        ForegroundService.serviceIsLive = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        // 标记服务关闭
        ForegroundService.serviceIsLive = false;
        // 移除通知
        stopForeground(true);
        super.onDestroy();
    }

}

