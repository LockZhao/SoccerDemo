package demo.klock.soccer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import demo.klock.soccer.util.DensityUtil;
import soccer.klock.demo.soccerdemo.R;

/**
 * Created by ZhaoRongZhi on 2018-05-02
 *
 * @descr
 */
public class PathView extends View {

    private Paint pathPaint, startPaint, endPaint;

    private int[] startPoint;
    private int[] endPoint;
    private int[] startControlPoint;
    private int[] endControlPoint;

    public PathView (Context context, AttributeSet attrs) {
        super(context, attrs);
        startPaint = buildPaint(getResources().getColor(R.color.start_control_point), 1);
        pathPaint = buildPaint(Color.GREEN, 3);
        endPaint = buildPaint(getResources().getColor(R.color.end_control_point), 1);
    }

    private Paint buildPaint (int color, int widthDp) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(DensityUtil.convertDpToPx(this, widthDp));
        return paint;
    }

    public void setPoints (int startX, int startY, int startControlX, int startControlY,
                           int endX, int endY, int endControlX, int endControlY) {
        startPoint = new int[]{startX, startY};
        endPoint = new int[]{endX, endY};
        startControlPoint = new int[]{startControlX, startControlY};
        endControlPoint = new int[]{endControlX, endControlY};
        invalidate();
    }

    @Override
    protected void onDraw (Canvas canvas) {
        super.onDraw(canvas);
        try {
            Path path = new Path();
            path.moveTo(startPoint[0], startPoint[1]);
            path.cubicTo(startControlPoint[0], startControlPoint[1], endControlPoint[0], endControlPoint[1], endPoint[0], endPoint[1]);
            canvas.drawPath(path, pathPaint);
            canvas.drawLine(startPoint[0], startPoint[1], startControlPoint[0], startControlPoint[1], startPaint);
            canvas.drawLine(endPoint[0], endPoint[1], endControlPoint[0], endControlPoint[1], endPaint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}