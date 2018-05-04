package demo.klock.soccer.view;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by ZhaoRongZhi on 2018-05-03
 *
 * @descr
 */
public class DragView extends AppCompatImageView {

    float moveX;
    float moveY;

    public DragView (Context context) {
        super(context);
    }

    public DragView (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                moveX = event.getX();
                moveY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float xDistance = event.getX() - moveX;
                float yDistance = event.getY() - moveY;
                offsetLeftAndRight((int)xDistance);
                offsetTopAndBottom((int)yDistance);
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }
}
