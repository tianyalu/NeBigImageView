package com.sty.ne.bigimageview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.Nullable;

/**
 * Created by tian on 2019/9/30.
 */

public class BigImageView extends View implements GestureDetector.OnGestureListener, View.OnTouchListener{
    private final Rect mRect;  // 图片显示视窗，当前=屏幕宽高
    private final BitmapFactory.Options mOptions;
    private final GestureDetector mGestureDetector;
    private final Scroller mScroller;
    private int mImageWidth;
    private int mImageHeight;
    private BitmapRegionDecoder mDecoder;
    private int mViewWidth;
    private int mViewHeight;
    private float mScale;
    private Bitmap mBitmap;

    public BigImageView(Context context) {
        this(context, null);
    }

    public BigImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BigImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 第一步，设置BigImageView所需要的一些成员变量
        mRect = new Rect();
        // 内存复用
        mOptions = new BitmapFactory.Options();
        // 手势识别
        mGestureDetector = new GestureDetector(context, this);
        // 滚动类
        mScroller = new Scroller(context);
        setOnTouchListener(this);
    }

    // 第二步，设置图片，得到图片的信息
    public void setImage(InputStream is) {
        // 获取图片宽和高，注意：不能将图片整个加载到内存
        mOptions.inJustDecodeBounds = true; // 设置为true时， 解码bitmap时可以只返回其宽、高和Mime类型而不必为其申请内存，从而节约内存空间
        BitmapFactory.decodeStream(is, null, mOptions);
        mImageWidth = mOptions.outWidth;
        mImageHeight = mOptions.outHeight;

        // 开启复用
        mOptions.inMutable = true;
        // 设置格式为RGB565-->节约一半内存
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        mOptions.inJustDecodeBounds = false;

        // 区域解码器
        try {
            mDecoder = BitmapRegionDecoder.newInstance(is, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestLayout();
    }

    // 第三步，开始测量，得到view的宽高，测量加载的图片到底缩放成什么样子
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 得到view的宽高
        mViewWidth = getMeasuredWidth();
        mViewHeight = getMeasuredHeight();

        // 确定加载图片的区域
        mRect.left = 0;
        mRect.top = 0;
        mRect.right = mImageWidth;
        // 计算缩放因子
        mScale = mViewWidth / (float) mImageWidth;
        // 计算缩放后要显示图像的高度（图像宽度缩放到和屏幕一样宽，高度也要随之缩放）
        mRect.bottom = (int) (mViewHeight / mScale);  // mViewWidth/mImageWidth = mViewHeight/mImageHeight
    }

    // 第四步，画出具体的内容
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 判断解码器是不是为null，如果解码器没拿到，表示没有设置过图片
        if(mDecoder == null) {
            return;
        }
        // 真正复用内存 // 复用的bitmap必须跟即将解码的bitmap尺寸一样
        mOptions.inBitmap = mBitmap;
        // 指定解码区域
        mBitmap = mDecoder.decodeRegion(mRect, mOptions);
        // 得到一个矩阵进行缩放，相当于得到view的大小
        Matrix matrix = new Matrix();
        matrix.setScale(mScale, mScale);
        canvas.drawBitmap(mBitmap, matrix, null);
    }

    // 第五步，处理点击事件
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // 直接将事件交给手势事件处理
        return mGestureDetector.onTouchEvent(event);
    }

    // 第六步，手按下去
    @Override
    public boolean onDown(MotionEvent e) {
        // 如果移动没有停止，强行到停止
        if(!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }
        // 继续接收后续事件
        return true;
    }


    /**
     *  // 第七步，处理滑动事件
     * @param e1  开始事件，手指按下去，开始获取坐标
     * @param e2  获取当前事件坐标
     * @param distanceX  x轴移动的距离
     * @param distanceY  y轴移动的距离
     * @return
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // 上下移动的时候，mRect需要改变显示的区域
        mRect.offset(0, (int)distanceY);
        // 移动时，处理到达顶部和底部的情况
        if(mRect.bottom > mImageHeight) {
            mRect.bottom = mImageHeight;
            mRect.top = mImageHeight - (int)(mViewHeight / mScale);
        }
        if(mRect.top < 0) {
            mRect.top = 0;
            mRect.bottom = (int)(mViewHeight / mScale);
        }
        invalidate();
        return false;
    }

    // 第八步，处理惯性问题
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        mScroller.fling(0, mRect.top, 0, (int)-velocityY, 0, 0, 0,
                mImageHeight - (int)(mViewHeight / mScale));
        return false;
    }

    // 第九步，处理计算结果
    @Override
    public void computeScroll() {
        if(mScroller.isFinished()) {
            return;
        }
        if(mScroller.computeScrollOffset()) {
            mRect.top = mScroller.getCurrY();
            mRect.bottom = mRect.top + (int)(mViewHeight/mScale);
            invalidate();
        }
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

}
