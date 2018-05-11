package demo.klock.soccer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import demo.klock.soccer.util.DensityUtil;
import soccer.klock.demo.soccerdemo.R;

/**
 * Created by ZhaoRongZhi on 2018-05-03
 *
 * @descr
 */
public class BezierView extends RelativeLayout {

    private ViewDragHelper dragHelper;
    private int finalLeft = -1, finalTop = -1;
    private ImageView ivStart, ivEnd, ivStartControl, ivEndControl;
    private RelativeLayout       rlContent;
    private OnPathUpdateListener listener;

    private Paint pathPaint, startPaint, endPaint;
    private int[] startPoint, endPoint, startControlPoint, endControlPoint;
    private int   isPathVisible;
    private Path path;


    public interface OnPathUpdateListener {
        void onPathUpdate (int startX, int startY, int startControlX, int startControlY,
                           int endX, int endY, int endControlX, int endControlY);
    }

    public void setOnPathUpdateListener (OnPathUpdateListener listener) {
        this.listener = listener;
    }

    public BezierView (Context context) {
        this(context, null);
    }

    public BezierView (Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BezierView (Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init (Context context) {
        setWillNotDraw(false);  // 告诉ViewGroup需要绘制以调用onDraw方法
        View view = LayoutInflater.from(context).inflate(R.layout.bezier_layout, this, true);
        ivStart = view.findViewById(R.id.iv_start);
        ivEnd = view.findViewById(R.id.iv_end);
        ivStartControl = view.findViewById(R.id.iv_start_control);
        ivEndControl = view.findViewById(R.id.iv_end_control);
        rlContent = view.findViewById(R.id.rl_content);

        startPaint = buildPaint(getResources().getColor(R.color.start_control_point), 1);
        pathPaint = buildPaint(Color.GREEN, 3);
        endPaint = buildPaint(getResources().getColor(R.color.end_control_point), 1);

        dragHelper = ViewDragHelper.create(rlContent, 1f, new ViewDragHelper.Callback() {

            @Override
            public boolean tryCaptureView (View child, int pointerId) {
                return child instanceof ImageView;
            }

            @Override
            public void onViewCaptured (View capturedChild, int activePointerId) {
                super.onViewCaptured(capturedChild, activePointerId);
            }

            @Override
            public int clampViewPositionHorizontal (View child, int left, int dx) {
                return left;
            }

            @Override
            public int clampViewPositionVertical (View child, int top, int dy) {
                return top;
            }

            @Override
            public int getViewHorizontalDragRange (View child) {
                return getMeasuredWidth() - child.getMeasuredWidth();
            }

            @Override
            public int getViewVerticalDragRange (View child) {
                return getMeasuredHeight() - child.getMeasuredHeight();
            }

            @Override
            public void onViewPositionChanged (@NonNull View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                invalidate();
            }

            @Override
            public void onViewReleased (View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                int viewWidth = releasedChild.getWidth();
                int viewHeight = releasedChild.getHeight();
                int curLeft = releasedChild.getLeft();
                int curTop = releasedChild.getTop();

                finalTop = curTop < 0 ? 0 : curTop;
                finalLeft = curLeft < 0 ? 0 : curLeft;
                if ((finalTop + viewHeight) > getHeight()) {
                    finalTop = getHeight() - viewHeight;
                }

                if ((finalLeft + viewWidth) > getWidth()) {
                    finalLeft = getWidth() - viewWidth;
                }
                dragHelper.settleCapturedViewAt(finalLeft, finalTop);
                onPathUpdate();
            }
        });
    }

    public double getPathLength () {
        if (path != null) {
            return new PathMeasure(path, false).getLength();
        }
        return 0;
    }

    public void setPathVisible (int pathVisible) {
        isPathVisible = pathVisible;
        ivStart.setVisibility(pathVisible);
        ivStartControl.setVisibility(pathVisible);
        ivEndControl.setVisibility(pathVisible);
        invalidate();
    }

    private void onPathUpdate () {
        if (listener != null) {
            listener.onPathUpdate(getCenterPoint(ivStart)[0], getCenterPoint(ivStart)[1],
                    getCenterPoint(ivStartControl)[0], getCenterPoint(ivStartControl)[1],
                    getCenterPoint(ivEnd)[0], getCenterPoint(ivEnd)[1],
                    getCenterPoint(ivEndControl)[0], getCenterPoint(ivEndControl)[1]);
        }
    }

    @Override
    protected void onLayout (boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        onPathUpdate();
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

    @Override
    protected void onDraw (Canvas canvas) {
        super.onDraw(canvas);
        if (isPathVisible != VISIBLE) {
            return;
        }
        startPoint = getCenterPoint(ivStart);
        endPoint = getCenterPoint(ivEnd);
        startControlPoint = getCenterPoint(ivStartControl);
        endControlPoint = getCenterPoint(ivEndControl);

        path = new Path();
        path.moveTo(startPoint[0], startPoint[1]);
        path.cubicTo(startControlPoint[0], startControlPoint[1], endControlPoint[0], endControlPoint[1], endPoint[0], endPoint[1]);
        canvas.drawPath(path, pathPaint);
        canvas.drawLine(startPoint[0], startPoint[1], startControlPoint[0], startControlPoint[1], startPaint);
        canvas.drawLine(endPoint[0], endPoint[1], endControlPoint[0], endControlPoint[1], endPaint);
    }

    /**
     * 获取view中心点坐标
     */
    private int[] getCenterPoint (View v) {
        return new int[]{(v.getLeft() + v.getRight()) / 2, (v.getTop() + v.getBottom()) / 2};
    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            dragHelper.cancel();
            return false;
        }
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        dragHelper.processTouchEvent(event);
        return true;
    }

}
