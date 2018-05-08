package demo.klock.soccer.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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
    private PathView             pathView;
    private OnPathUpdateListener listener;

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
        View view = LayoutInflater.from(context).inflate(R.layout.bezier_layout, this, true);
        ivStart = view.findViewById(R.id.iv_start);
        ivEnd = view.findViewById(R.id.iv_end);
        ivStartControl = view.findViewById(R.id.iv_start_control);
        ivEndControl = view.findViewById(R.id.iv_end_control);
        rlContent = view.findViewById(R.id.rl_content);
        pathView = view.findViewById(R.id.path_view);

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
                updatePath();
            }
        });
    }

    private void updatePath () {
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
        refreshPathView();
    }

    @Override
    public void invalidate () {
        super.invalidate();
        refreshPathView();
    }

    public void refreshPathView () {
        pathView.setPoints(getCenterPoint(ivStart)[0], getCenterPoint(ivStart)[1],
                getCenterPoint(ivStartControl)[0], getCenterPoint(ivStartControl)[1],
                getCenterPoint(ivEnd)[0], getCenterPoint(ivEnd)[1],
                getCenterPoint(ivEndControl)[0], getCenterPoint(ivEndControl)[1]);
    }

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
