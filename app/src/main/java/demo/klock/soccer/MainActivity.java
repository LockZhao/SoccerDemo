package demo.klock.soccer;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import java.math.BigDecimal;

import demo.klock.soccer.util.DensityUtil;
import demo.klock.soccer.view.BezierView;
import demo.klock.soccer.view.SoccerView;
import soccer.klock.demo.soccerdemo.R;

public class MainActivity extends AppCompatActivity {

    private static final int DURATION = 1200;

    private BezierView bazierView;
    private SoccerView soccerView;
    private TextView   tvStart, tvScore, tvRatio;
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
                // 显示贝塞尔曲线四个点位的坐标
                double delta = distanceBetween(endX, startX, endY, startY);
                String[] ratios = new String[4];
                ratios[0] = formatDouble((startControlX - startX) / delta, 3);
                ratios[1] = formatDouble((startControlY - startY) / delta, 3);
                ratios[2] = formatDouble((endControlX - startX) / delta, 3);
                ratios[3] = formatDouble((endControlY - startY) / delta, 3);
                String str = String.format("(%s,%s,%s,%s)", ratios[0], ratios[1], ratios[2], ratios[3]);
                tvRatio.setText(str);

                // 计算曲线结束后的消失点
                int xOffset = (int) getResources().getDimension(R.dimen.soccer_size) / 2;
                int yOffset = (int) getResources().getDimension(R.dimen.soccer_size) / 2;

                int left = soccerView.getLeft() - xOffset * 2;
                int top = soccerView.getTop() - yOffset * 2;
                int right = soccerView.getRight();
                int bottom = soccerView.getBottom();

                float[] vanishPoint;
                if (endControlX > endX) {
                    if (endControlY > endY) {
                        vanishPoint = calVanishPoint(endControlX, endControlY, endX, endY, left, top);
                    } else {
                        vanishPoint = calVanishPoint(endControlX, endControlY, endX, endY, left, bottom);
                    }
                } else {
                    if (endControlY > endY) {
                        vanishPoint = calVanishPoint(endControlX, endControlY, endX, endY, right, top);
                    } else {
                        vanishPoint = calVanishPoint(endControlX, endControlY, endX, endY, right, bottom);
                    }
                }

                // 构建AnimatorPath路径，并赋值节点所在位置的长度百分比
                path = new AnimatorPath();
                path.moveTo(startX - xOffset, startY - yOffset);
                path.thirdBesselCurveTo(startControlX - xOffset, startControlY - yOffset,
                        endControlX - xOffset, endControlY - yOffset,
                        endX - xOffset, endY - yOffset);
                path.lineTo(vanishPoint[0], vanishPoint[1]);
            }
        });

        initStartBtn();
    }

    /**
     * 计算路径消失点坐标
     */
    private float[] calVanishPoint (float p1X, float p1Y, float p2X, float p2Y, float vanishX, float vanishY) {
        float[] vanishPoint = new float[2];
        if (p1X == p2X) {
            vanishPoint[0] = p1X;
            vanishPoint[1] = vanishY;
        } else if (p1Y == p2Y) {
            vanishPoint[0] = vanishX;
            vanishPoint[1] = p1Y;
        } else {
            float ratio = (p2Y - p1Y) / (p2X - p1X);
            float diffX = Math.abs(p2X - vanishX);
            float diffY = Math.abs(p2Y - vanishY);
            if (Math.abs(diffX * ratio) < diffY) {
                ratio = (p2Y - p1Y) / Math.abs(p2X - p1X);
                vanishPoint[0] = vanishX;
                vanishPoint[1] = p2Y + diffX * ratio;
            } else {
                ratio = Math.abs(p2Y - p1Y) / (p2X - p1X);
                vanishPoint[0] = p2X + diffY / ratio;
                vanishPoint[1] = vanishY;
            }
        }
        return vanishPoint;
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
        double bazierLength = bazierView.getPathLength();
        double lineLength = distanceBetween(path.getPoints().get(1).mX, path.getPoints().get(1).mY, path.getPoints().get(2).mX, path.getPoints().get(2).mY);
        double ratio = bazierLength / (bazierLength + lineLength);

        ObjectAnimator anim1 = ObjectAnimator.ofObject(view, propertyName, new PathEvaluator(), path.getPoints().toArray());
        anim1.setInterpolator(new CustomInterpolator((float)ratio));
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(view, "rotation", 0, 3600);
        anim2.setInterpolator(new LinearInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(anim1, anim2);
        animatorSet.setDuration(DURATION);
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
    }

    private void onAnimationBegin (final Animator animation) {
        Log.i("TIME_INTERVAL", "onAnimationStart: ");
        bazierView.setPathVisible(View.INVISIBLE);
        soccerView.setAnimStart(true);

        tvScore.setVisibility(View.GONE);
        tvStart.setText("Kick！！");
        tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                double score = 0;
                if (path != null && soccerView.getPathPoint() != null) {
                    PathPoint endPoint = path.getPoints().get(1);
                    double distance = distanceBetween(endPoint.mX, endPoint.mY, soccerView.getPathPoint().mX, soccerView.getPathPoint().mY);
                    double range = DensityUtil.convertDpToPx(v, 40);
                    score = (range - distance) * (100 / range);
                }
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
     * 计算两点间距离
     */
    private double distanceBetween (float p1x, float p1y, float p2x, float p2y) {
        return Math.sqrt(Math.pow(Math.abs(p1x - p2x), 2) + Math.pow(Math.abs(p1y - p2y), 2));
    }

    public String formatDouble (double v, int scale) {
        return new BigDecimal(v).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }
}
