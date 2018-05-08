package demo.klock.soccer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;

import demo.klock.soccer.view.BezierView;
import soccer.klock.demo.soccerdemo.R;

public class MainActivity extends AppCompatActivity {

    private static final int DURATION = 1000;

    private BezierView   bazierView;
    private TextView     tvStart;
    private ImageView    ivSoccer;
    private AnimatorPath path;
    private TextView     tvRatio;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bazierView = findViewById(R.id.bazier_view);
        tvStart = findViewById(R.id.tv_start);
        ivSoccer = findViewById(R.id.iv_soccer);
        tvRatio = findViewById(R.id.tv_ratio);

        bazierView.setOnPathUpdateListener(new BezierView.OnPathUpdateListener() {
            @Override
            public void onPathUpdate (int startX, int startY, int startControlX, int startControlY, int endX, int endY, int endControlX, int endControlY) {
                int xOffset = ivSoccer.getWidth() / 2;
                int yOffset = ivSoccer.getHeight() / 2;
                path = new AnimatorPath();
                path.moveTo(startX - xOffset, startY - yOffset);
                path.thirdBesselCurveTo(startControlX - xOffset, startControlY - yOffset,
                        endControlX - xOffset, endControlY - yOffset,
                        endX - xOffset, endY - yOffset);
                ivSoccer.setVisibility(View.GONE);

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
                ivSoccer.setVisibility(View.VISIBLE);
                startAnimatorPath(ivSoccer, "ivSoccer", path);
            }
        });
    }

    public void setIvSoccer (PathPoint newLoc) {
        ivSoccer.setTranslationX(newLoc.mX);
        ivSoccer.setTranslationY(newLoc.mY);
    }

    private void startAnimatorPath (View view, String propertyName, AnimatorPath path) {
        ObjectAnimator anim1 = ObjectAnimator.ofObject(this, propertyName, new PathEvaluator(), path.getPoints().toArray());
        anim1.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator anim2 = ObjectAnimator.ofFloat(ivSoccer, "rotation", 0, 3600);
        anim2.setInterpolator(new LinearInterpolator());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(anim1, anim2);
        animatorSet.setDuration(1000);
        animatorSet.start();
    }
}
