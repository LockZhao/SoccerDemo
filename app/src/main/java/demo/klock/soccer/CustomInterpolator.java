package demo.klock.soccer;

import android.util.Log;
import android.view.animation.LinearInterpolator;

/**
 * Created by ZhaoRongZhi on 2018-05-11
 *
 * @descr
 */
public class CustomInterpolator extends LinearInterpolator {

    private float ratio;

    public CustomInterpolator (float ratio) {
        Log.i("CustomInterpolator", "ratio: " + ratio);
        this.ratio = ratio;
    }

    @Override
    public float getInterpolation (float input) {
        // 两段路径，在返回0.5的时候会切换，由于两段路径的长度不一样，为了保证动画速率一致，需要根据路径转换点的位置ratio变换时间轴
        float ori = input;
        if (input < ratio) {
            input = input * (0.5f / ratio);
        } else {
            input = (input - ratio) * ((1 - 0.5f) / (1 - ratio)) + 0.5f;
        }
        Log.i("CustomInterpolator", ori + " --> " + input);
        return super.getInterpolation(input);
    }
}
