package demo.klock.soccer;

import android.util.Log;
import android.view.animation.LinearInterpolator;

/**
 * Created by ZhaoRongZhi on 2018-05-11
 *
 * @descr
 */
public class CustomInterpolator extends LinearInterpolator {

    @Override
    public float getInterpolation (float input) {
        Log.i("Interpolator", "getInterpolation: " + input);
        return super.getInterpolation(input);
    }
}
