package demo.klock.soccer;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
    private TextView     tvStart, tvScore, tvRatio;
    private AnimatorPath path;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bazierView = findViewById(R.id.bazier_view);
        soccerView = findViewById(R.id.soccer_view);
        tvStart = findViewById(R.id.tv_start);
        tvStart = findViewById(R.id.tv_start);
        tvRatio = findViewById(R.id.tv_ratio);
        tvScore = findViewById(R.id.tv_score);

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

//                path.lineTo(200, 200);

                double delta = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
                String[] ratios = new String[4];
                ratios[0] = formatDouble((startControlX - startX) / delta, 3);
                ratios[1] = formatDouble((startControlY - startY) / delta, 3);
                ratios[2] = formatDouble((endControlX - startX) / delta, 3);
                ratios[3] = formatDouble((endControlY - startY) / delta, 3);
                String str = String.format("(%s,%s,%s,%s)", ratios[0], ratios[1], ratios[2], ratios[3]);
                Log.i("ratio", str);
                tvRatio.setText(str);
            }
        });

        initStartBtn();
    }

    private void initStartBtn () {
        tvStart.setText("Start");
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
        // 贝塞尔曲线路径动画
        PathEvaluator pathEvaluator = new PathEvaluator();
        TimeInterpolator interpolator = new LinearInterpolator();
        ObjectAnimator pathAnim = ObjectAnimator.ofObject(view, propertyName, pathEvaluator, path.getPoints().toArray());
        pathAnim.setInterpolator(interpolator);
        pathAnim.setDuration(DURATION);

        // 贝塞尔曲线结束后的路径动画， 先取得95%时间位置点的位置，计算最后5% Duration里的足球移动速率
        // 之后根据贝塞尔曲线终点切线（控制点）的反方向计算出延伸出屏幕外的动画及持续时间
        float mid = 0.95f;
        PathPoint endPoint = path.getPoints().get(1);
        PathPoint midPoint = pathEvaluator.evaluate(interpolator.getInterpolation(mid), path.getPoints().get(0), endPoint);
        int offset = (int) getResources().getDimension(R.dimen.soccer_size);
        PathPoint vanishPoint = calVanishPoint(view.getLeft() - offset, view.getTop() - offset, view.getRight(), view.getBottom(), midPoint, endPoint);
        int vanishDuration = (int)(DURATION * (distanceBetween(vanishPoint.mX, vanishPoint.mY, endPoint.mX, endPoint.mY)
                / distanceBetween(vanishPoint.mX, vanishPoint.mY, endPoint.mX, endPoint.mY)));
        AnimatorPath vanishPath = new AnimatorPath();
        vanishPath.moveTo(endPoint.mX, endPoint.mY);
        vanishPath.lineTo(0, 0);
        ObjectAnimator vanishAnim = ObjectAnimator.ofObject(view, propertyName, new PathEvaluator(), vanishPath.getPoints().toArray());
        vanishAnim.setInterpolator(new LinearInterpolator());
        vanishAnim.setDuration(vanishDuration);

        // 旋转动画
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(view, "rotation", 0, 3600);
        rotateAnim .setInterpolator(new LinearInterpolator());
        rotateAnim.setDuration(DURATION + vanishDuration);

        // 两个路径动画的播放集合
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(pathAnim, vanishAnim);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart (Animator animation) {
                onAnimationBegin(animation);
            }

            @Override
            public void onAnimationEnd (Animator animation) {
                onAnimationStop(animation);
            }

            @Override
            public void onAnimationCancel (Animator animation) {
                onAnimationStop(animation);
            }

            @Override
            public void onAnimationRepeat (Animator animation) {

            }
        });

        animatorSet.start();
        rotateAnim.start();
    }

    private void onAnimationBegin (final Animator animation) {
        Log.i("TIME_INTERVAL", "onAnimationStart: ");
        final long startTime = System.currentTimeMillis();
        bazierView.setPathVisible(View.INVISIBLE);
        soccerView.setAnimStart(true);

        tvScore.setVisibility(View.GONE);
        tvStart.setText("Kick！！");
        tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                double score = 100 - Math.abs(System.currentTimeMillis() - startTime - DURATION) / 1.5d;
                tvScore.setText("Score: " + formatDouble(score < 0 ? 0 : score, 1));
                tvScore.setVisibility(View.VISIBLE);
                if (animation.isRunning()) {
                    animation.cancel();
                }
                initStartBtn();
            }
        });
    }

    private void onAnimationStop (Animator animation) {
        Log.i("TIME_INTERVAL", "onAnimationStop: ");
        bazierView.setPathVisible(View.VISIBLE);
        soccerView.setAnimStart(false);
    }

    /**
     * 给定一个区域的四个l、t、r、b坐标值，及区域内两点p1、p2,计算两点的延长线于区域四周的交点坐标
     */
    private PathPoint calVanishPoint (float left, float top, float right, float bottom, PathPoint p1, PathPoint p2) {
        return PathPoint.moveTo(0, 0);
    }

    /**
     * 计算两点间距离
     */
    private double distanceBetween (float p1x, float p1y, float p2x, float p2y) {
        return Math.sqrt(Math.pow(Math.abs(p1x - p2x), 2) + Math.pow(Math.abs(p1y - p2y), 2));
    }

    public String formatDouble (double v, int scale) {
        return new BigDecimal(v).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }
}
