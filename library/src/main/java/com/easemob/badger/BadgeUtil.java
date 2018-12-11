package com.easemob.badger;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.easemob.badger.impl.XiaomiHomeBadger;

import java.util.ArrayList;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;


/**
 * Created by liyuzhao on 16/5/5.
 */
public class BadgeUtil {

    private static final String TAG = BadgeUtil.class.getSimpleName();
    private static List<Integer> notifyIdList = new ArrayList<Integer>();
    private static Badger badger;

    /**
     *
     * @param notification 更新角标一般都是和发送notification并行的.如果不想发notification只是更新角标,这里传null
     * @param notifyID notificationId
     * @param context
     * @param thisNotifyCount notifyID对应的notification的未读总数(XiaoMi 发送的是这个count,XiaoMi 会自动计算所有notification 未读数量)
     * @param count 整个APP所有的未读数量 (其他的是使用这个count)
     */
    public static void sendBadgeNotification(Notification notification, int notifyID, Context context, int thisNotifyCount, int count){
        if (count <= 0){
            count = 0;
        } else {
            count = Math.max(0, Math.min(count, 99));
        }

        if (badger == null){
            String currentHomePackage = getLauncherName(context);
            Log.d(TAG, "currentHomePackage:" + currentHomePackage);
            badger = BadgerType.getBadgerByLauncherName(currentHomePackage);
        }
        if (badger != null && badger instanceof XiaomiHomeBadger) {
            if (!isAppRunningForeground(context)){
                if (notification == null){
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "badge");
//                    builder.setSmallIcon(android.R.color.transparent);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        builder.setBadgeIconType(Notification.BADGE_ICON_NONE);
//                    }
//                    builder.setAutoCancel(true);
//                    builder.setColor(Color.TRANSPARENT);
//                    builder.setNumber(count);
//                    builder.setLocalOnly(true);
//                    builder.setShowWhen(false);
//                    builder.setVisibility(View.GONE);
//                    builder.setLargeIcon(Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888));
                    builder.setOnlyAlertOnce(true);
                    notification = builder.build();
                }
                badger.executeBadge(context, notification, notifyID, thisNotifyCount, count);
                setNotification(notification, notifyID, context, count);
            }
        } else {
            ShortcutBadger.applyCount(context, count);
        }
    }


    /**
     * 重置,清除Badge未读显示数<br/>
     * @param context
     */
    public static void resetBadgeCount(Context context){
        if (badger == null){
            String currentHomePackage = getLauncherName(context);
            Log.d(TAG, "currentHomePackage:" + currentHomePackage);
            badger = BadgerType.getBadgerByLauncherName(currentHomePackage);
        }

        if (badger != null && badger instanceof XiaomiHomeBadger) {
            sendBadgeNotification(null, 0, context, 0, 0);
        }
        ShortcutBadger.removeCount(context);
    }

    /**
     * 获取手机的launcher name
     * @param context
     * @return
     */
    public static String getLauncherName(Context context){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null)
            return resolveInfo.activityInfo.packageName;
        else
            return "";

    }

    static void setNotification(Notification notification, int notificationId, Context context, int count){
        if (notification != null){
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(notificationId);
            ShortcutBadger.applyNotification(context, notification, count);
            mNotificationManager.notify(notificationId, notification);
        }
    }

    static boolean isAppRunningForeground(Context var0) {
        ActivityManager var1 = (ActivityManager)var0.getSystemService(Context.ACTIVITY_SERVICE);

        try {
            List var2 = var1.getRunningTasks(1);
            if (var2 != null && var2.size() >= 1) {
                boolean var3 = var0.getPackageName().equalsIgnoreCase(((ActivityManager.RunningTaskInfo)var2.get(0)).baseActivity.getPackageName());
                return var3;
            } else {
                return false;
            }
        } catch (SecurityException var4) {
            var4.printStackTrace();
            return false;
        }
    }
}
