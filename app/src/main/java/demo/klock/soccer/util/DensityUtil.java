package demo.klock.soccer.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Method;

/**
 * Created by chengxin on 3/2/15.
 */
public class DensityUtil {

    private static final String TAG = "DensityUtil";

    private volatile static float SCREEN_HEIGHT;
    private volatile static float SCREEN_WIDTH;

    public static float getScreenWidth(Resources resources) {
        if (SCREEN_WIDTH == 0) {
            SCREEN_WIDTH = resources.getDisplayMetrics().widthPixels;
        }
        return SCREEN_WIDTH;
    }

    public static float getScreenHeight(Resources resources) {
        if (SCREEN_HEIGHT == 0) {
            SCREEN_HEIGHT = resources.getDisplayMetrics().heightPixels;
        }
        return SCREEN_HEIGHT;
    }

    /**获取虚拟功能键高度 */
    public static int getVirtualBarHeigh(Context context) {
        int vh = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vh;
    }

    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return dp * scale;
    }

    public static float sp2px(Resources resources, float sp) {
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    /**
     * 获取缩放倍数
     */
    public static int getScaledNum(Context context){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) scale;
    }

    public static int convertDpToPx(Context context, int dp) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
    }

    public static int convertDpToPx(View view, int dp) {
        Resources r = view.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
    }
}
