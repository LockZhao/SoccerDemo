package demo.klock.soccer;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import java.math.BigDecimal;

import demo.klock.soccer.view.BezierView;
import demo.klock.soccer.view.SoccerView;
import soccer.klock.demo.soccerdemo.R;

public class MainActivity extends AppCompatActivity {

    private static final int DURATION = 1000;

    private BezierView   bazierView;
    private SoccerView   soccerView;
    private TextView     tvStart;
    private AnimatorPath path;
    private TextView     tvRatio;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bazierView = findViewById(R.id.bazier_view);
        soccerView = findViewById(R.id.soccer_view);
        tvStart = findViewById(R.id.tv_start);
        tvRatio = findViewById(R.id.tv_ratio);

        /*soccerView.setZOrderOnTop(true);
        soccerView.getHolder().setFormat(PixelFormat.TRANSLUCENT);*/

        bazierView.setOnPathUpdateListener(new BezierView.OnPathUpdateListener() {
            @Override
            public void onPathUpdate (int startX, int startY, int startControlX, int startControlY, int endX, int endY, int endControlX, int endControlY) {
                int xOffset = (int) getResources().getDimension(R.dimen.soccer_size) / 2;
                int yOffset = (int) getResources().getDimension(R.dimen.soccer_size) / 2;
                path = new AnimatorPath();
                path.moveTo(startX - xOffset, startY - yOffset);
                path.thirdBesselCurveTo(startControlX - xOffset, startControlY - yOffset,
                        endControlX - xOffset, endControlY - yOffset,
                        endX - xOffset, endY - yOffset);

                double delta = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
                String[] ratios = new String[4];
                ratios[0] = formatDouble((startControlX - startX) / delta);
                ratios[1] = formatDouble((startControlY - startY) / delta);
                ratios[2] = formatDouble((endControlX - startX) / delta);
                ratios[3] = formatDouble((endControlY - startY) / delta);
                String str = String.format("(%s,%s,%s,%s)", ratios[0], ratios[1], ratios[2], ratios[3]);
                Log.i("ratio", str);
                tvRatio.setText(str);
            }

            public String formatDouble (double v) {
                return new BigDecimal(v).setScale(3, BigDecimal.ROUND_HALF_UP).toString();
            }
        });

        tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (path == null)
                    return;
                startAnimatorPath(soccerView, "pathPoint", path);
            }
        });
    }

    private void startAnimatorPath (View view, String propertyName, AnimatorPath path) {
        ObjectAnimator anim1 = ObjectAnimator.ofObject(view, propertyName, new PathEvaluator(), path.getPoints().toArray());
        anim1.setInterpolator(new AccelerateDecelerateInterpolator());
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(view, "rotation", 0, 3600);
        anim2.setInterpolator(new LinearInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(anim1, anim2);
        animatorSet.setDuration(DURATION);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart (Animator animation) {
                onAnimationBegin();
            }

            @Override
            public void onAnimationEnd (Animator animation) {
                onAnimationStop();
            }

            @Override
            public void onAnimationCancel (Animator animation) {
                onAnimationStop();
            }

            @Override
            public void onAnimationRepeat (Animator animation) {

            }
        });
        animatorSet.start();
    }

    private void onAnimationBegin () {
        Log.i("TIME_INTERVAL", "onAnimationStart: ");
        bazierView.setPathVisible(View.INVISIBLE);
        soccerView.setAnimStart(true);
    }

    private void onAnimationStop () {
        Log.i("TIME_INTERVAL", "onAnimationStop: ");
        bazierView.setPathVisible(View.VISIBLE);
        soccerView.setAnimStart(false);
    }
}
