package demo.klock.soccer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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

    private SurfaceHolder holder;
    private Canvas        canvas;//绘图的画布
    private boolean       isDrawing;//控制绘画线程的标志位
    private PathPoint     pathPoint;
    private float         rotation;
    private Thread        animThread;
    private Bitmap        bmp;

    public SoccerView (Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public SoccerView (Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView () {
        holder = getHolder();
        holder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
        initBitmap();
    }

    private void initBitmap () {
        Bitmap oriBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.img_soccer);
        int width = oriBitmap.getWidth();
        int height = oriBitmap.getHeight();
        float scaleWidth = getResources().getDimension(R.dimen.soccer_size) / width;
        float scaleHeight = getResources().getDimension(R.dimen.soccer_size) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        bmp = Bitmap.createBitmap(oriBitmap, 0, 0, width, height, matrix, true);
    }

    /**
     * 由ObjectAnimator.ofObject方法回调
     */
    public void setPathPoint (PathPoint pathPoint) {
        this.pathPoint = pathPoint;
    }

    /**
     * 由ObjectAnimator.ofFloat方法回调
     */
    public void setRotation (float rotation) {
        this.rotation = rotation;
    }

    public void setAnimStart (boolean isStart) {
        if (animThread == null || !animThread.isAlive()) {
            animThread = new Thread(this);
            animThread.start();
        }
        this.isDrawing = isStart;
    }

    @Override
    public void surfaceCreated (SurfaceHolder holder) {
        canvas = this.holder.lockCanvas();
        canvas.drawColor(Color.WHITE);
        this.holder.unlockCanvasAndPost(canvas);
        setAnimStart(true);
    }

    @Override
    public void surfaceChanged (SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed (SurfaceHolder holder) {
        isDrawing = false;
    }

    @Override
    public void run () {
        while (isDrawing) {
            if (pathPoint == null) {
                continue;
            }

            /**取得更新之前的时间**/
            long startTime = System.currentTimeMillis();

            synchronized (holder) {
                /**拿到当前画布 然后锁定**/
                canvas = holder.lockCanvas();

                drawView();

                /**绘制结束后解锁显示在屏幕上**/
                holder.unlockCanvasAndPost(canvas);
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
        canvas = holder.lockCanvas();
        canvas.drawColor(Color.WHITE);
        holder.unlockCanvasAndPost(canvas);
    }

    private void drawView () {
        canvas.drawColor(Color.WHITE);
        Matrix matrix = new Matrix();
        int offsetX = bmp.getWidth() / 2;
        int offsetY = bmp.getHeight() / 2;
        matrix.postTranslate(-offsetX, -offsetY);
        matrix.postRotate(rotation);
        matrix.postTranslate(pathPoint.mX + offsetX, pathPoint.mY + offsetY);
        canvas.drawBitmap(bmp, matrix, null);
    }
}
