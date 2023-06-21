package com.hanny.listenpushmsg.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.RemoteViews;

import com.hanny.listenpushmsg.bean.NotifyTopBean;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressLint("OverrideAbstract")
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotifyService extends NotificationListenerService {

    public static String SEND_MSG_BROADCAST = "notify_msg";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i("消息发送了--->", "消息onNotificationPosted");
        super.onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.e("消息发送了--->", "消息onNotificationRemoved");
        super.onNotificationRemoved(sbn);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap) {
        Log.e("消息发送了--->", "消息onNotificationPosted22222");
        String packageName = sbn.getPackageName();
        if(!packageName.equals("com.eg.android.AlipayGphone")&&!packageName.equals("com.tencent.mm")){
            Log.i("其他应用的通知","无需理会");
            return;
        }
        String content = null;
        //获取通知栏消息中的文字
        if (sbn.getNotification().tickerText != null) {
            content = sbn.getNotification().tickerText.toString();
        }
        //如果获取文字失败,通过反射获取
        NotifyTopBean notifyTopBean=null;
        if (content == null) {
            Map<String, Object> notiInfo = getNotiInfo(sbn.getNotification());
            if (null != notiInfo) {
                notifyTopBean=new NotifyTopBean();
//            content = notiInfo.get("title") + ":" + notiInfo.get("text");
                notifyTopBean.setTitle(String.valueOf(notiInfo.get("title")));
                notifyTopBean.setContent(String.valueOf(notiInfo.get("text")));
                notifyTopBean.setTime(System.currentTimeMillis());
            }
        }else{
            notifyTopBean=new NotifyTopBean();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                Bundle extras = sbn.getNotification().extras;
                if (extras != null) {
                    //包名
                    String pkg = sbn.getPackageName();
                    // 获取通知标题
                    String title = extras.getString(Notification.EXTRA_TITLE, "");
                    // 获取通知内容
                    String contents = extras.getString(Notification.EXTRA_TEXT, "");
                    Log.i("NotifycationData", String.format("收到通知，包名：%s，标题：%s，内容：%s", pkg, title, content));
                    notifyTopBean.setTitle(title);
                    notifyTopBean.setContent(contents);
                    notifyTopBean.setPackageName(packageName);
                }else{
                    notifyTopBean.setTitle(""+sbn.getPackageName());
                    notifyTopBean.setContent(content);
                    notifyTopBean.setPackageName(packageName);
                }

            }else{
                notifyTopBean.setTitle(""+sbn.getPackageName());
                notifyTopBean.setContent(content);
                notifyTopBean.setPackageName(packageName);
            }
            notifyTopBean.setTime(System.currentTimeMillis());
        }
        if (notifyTopBean == null) {
            return;
        }
        //测试
//        notifyTopBean.setTitle("你已成功收款");
//        notifyTopBean.setTitle("你已成功收款0.1元(朋友到店)");
        Intent intent = new Intent();
        intent.putExtra("msg", notifyTopBean);
        intent.setAction(SEND_MSG_BROADCAST);
        sendBroadcast(intent);
    }

    private Map<String, Object> getNotiInfo(Notification notification) {
        int key = 0;
        if (notification == null)
            return null;
        RemoteViews views = notification.contentView;
        if (views == null)
            return null;
        Class secretClass = views.getClass();

        try {
            Map<String, Object> text = new HashMap<>();

            Field outerFields[] = secretClass.getDeclaredFields();
            for (int i = 0; i < outerFields.length; i++) {
                if (!outerFields[i].getName().equals("mActions"))
                    continue;

                outerFields[i].setAccessible(true);

                ArrayList<Object> actions = (ArrayList<Object>) outerFields[i].get(views);
                for (Object action : actions) {
                    Field innerFields[] = action.getClass().getDeclaredFields();
                    Object value = null;
                    Integer type = null;
                    for (Field field : innerFields) {
                        field.setAccessible(true);
                        if (field.getName().equals("value")) {
                            value = field.get(action);
                        } else if (field.getName().equals("type")) {
                            type = field.getInt(action);
                        }
                    }
                    // 经验所得 type 等于9 10为短信title和内容，不排除其他厂商拿不到的情况
                    if (type != null && (type == 9 || type == 10)) {
                        if (key == 0) {
                            text.put("title", value != null ? value.toString() : "");
                        } else if (key == 1) {
                            text.put("text", value != null ? value.toString() : "");
                        } else {
                            text.put(Integer.toString(key), value != null ? value.toString() : null);
                        }
                        key++;
                    }
                }
                key = 0;

            }
            return text;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
