package demo.klock.soccer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import demo.klock.soccer.PathPoint;
import soccer.klock.demo.soccerdemo.R;

/**
 * Created by ZhaoRongZhi on 2018-05-08
 *
 * @descr
 */
public class SoccerView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private final int TIME_PER_FRAME = 1000 / 60;  // 每秒帧数

    private SurfaceHolder mHolder;
    private Canvas        mCanvas;//绘图的画布
    private boolean       mIsDrawing;//控制绘画线程的标志位
    private PathPoint     pathPoint;
    private Thread animThread;
    private Bitmap bmp;

    public SoccerView (Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SoccerView (Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView () {
        mHolder = getHolder();
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
        bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.img_soccer);
    }

    public void setPathPoint (PathPoint pathPoint) {
        this.pathPoint = pathPoint;
    }

    public void setAnimStart (boolean isStart) {
        if (animThread == null || !animThread.isAlive()) {
            animThread = new Thread(this);
            animThread.start();
        }
        this.mIsDrawing = isStart;
    }

    @Override
    public void surfaceCreated (SurfaceHolder holder) {
        mCanvas = mHolder.lockCanvas();
        mCanvas.drawColor(Color.WHITE);
        mHolder.unlockCanvasAndPost(mCanvas);
        setAnimStart(true);
    }

    @Override
    public void surfaceChanged (SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed (SurfaceHolder holder) {
        mIsDrawing = false;
    }

    @Override
    public void run () {
        while (mIsDrawing) {
            if (pathPoint == null) {
                continue;
            }

            /**取得更新之前的时间**/
            long startTime = System.currentTimeMillis();

            synchronized (mHolder) {
                /**拿到当前画布 然后锁定**/
                mCanvas = mHolder.lockCanvas();

                drawView();

                /**绘制结束后解锁显示在屏幕上**/
                mHolder.unlockCanvasAndPost(mCanvas);
            }

            /**取得更新结束的时间**/
            long endTime = System.currentTimeMillis();

            /**计算出一次更新的毫秒数**/
            int diffTime = (int) (endTime - startTime);

            /**确保帧数**/
            while (diffTime <= TIME_PER_FRAME) {
                diffTime = (int) (System.currentTimeMillis() - startTime);
                /**线程等待**/
                Thread.yield();
            }
        }
        // 结束后重置画布
        mCanvas = mHolder.lockCanvas();
        mCanvas.drawColor(Color.WHITE);
        mHolder.unlockCanvasAndPost(mCanvas);
    }

    private void drawView () {
        mCanvas.drawColor(Color.WHITE);
        mCanvas.drawBitmap(bmp, pathPoint.mX, pathPoint.mY, null);
    }
}
