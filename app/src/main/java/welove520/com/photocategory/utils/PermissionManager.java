package welove520.com.photocategory.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * 6.0+ 权限检测
 * Created by Jne
 * Date: 2016/4/1
 * Time: 16:30
 */
public class PermissionManager {
    /**
     * 权限检测
     * 如果没有 打开授权弹窗
     *
     * @param activity 传Activity
     */
    public static void checkPermissions(String[] permissions, int requestCode, Activity activity) {
        ArrayList<String> permissionList = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        if (permissionList.size() > 0) {
            // 没有权限
            // 因为requestPermissions方法只在ActivityCompat中，所以只能在activity中调用
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    /**
     * 检测是否含有全部的权限
     */
    public static boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }


    /**
     * 启动应用的设置
     */
    private static void startAppSettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    /**
     * 在没有界面时(在server、类方法中) 只能判断是否有权限
     * 比如：DeviceInfoUtil中 getDeviceId()
     *
     * @return true 有权限 false 没权限
     */
    public static boolean checkSelfPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}