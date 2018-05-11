package demo.klock.soccer;

import android.util.Log;
import android.view.animation.LinearInterpolator;

/**
 * Created by ZhaoRongZhi on 2018-05-11
 *
 * @descr
 */
public class CustomInterpolator extends LinearInterpolator {

    float ratio;

    public CustomInterpolator (float ratio) {
        Log.i("CustomInterpolator", "ratio: " + ratio);
        this.ratio = ratio;
    }

    @Override
    public float getInterpolation (float input) {
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
