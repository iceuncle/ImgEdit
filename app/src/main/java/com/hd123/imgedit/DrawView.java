package com.hd123.imgedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;

import com.hd123.imgedit.bean.DrawPath;
import com.hd123.imgedit.bean.DrawType;
import com.hd123.imgedit.bean.TextBean;
import com.hd123.imgedit.utils.ListUtil;
import com.hd123.imgedit.utils.RectUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tianyang on 2017/8/7.
 */

public class DrawView extends ScrollView {
    public static final float TEXT_SIZE_DEFAULT = 60;
    public static final int STICKER_BTN_HALF_SIZE = 30;
    public static final int PADDING = 32;
    public static final int TEXT_TOP_PADDING = 10;
    public static final int CHAR_MIN_HEIGHT = 60;

    private Context mContext;
    private Bitmap basePicture;
    private int pictureWidth;
    private int pictureHeight;
    private View editView;

    private DrawType drawType;
    private List<DrawPath> savePath = new ArrayList<>();


    //涂鸦
    private Paint mPaint;
    private Bitmap mDrawBit;
    private Paint mEraserPaint;
    private int index = 0;
    private Canvas mPaintCanvas = null;
    private float last_x = 0;
    private float last_y = 0;
    private boolean eraser;
    private Path mPath;
    private int mPaintColor;


    //文字
    private TextPaint mTextPaint = new TextPaint();
    private Paint mHelpPaint = new Paint();
    private Rect mTextRect = new Rect();// warp text rect record
    private RectF mHelpBoxRect = new RectF();
    private Rect mDeleteRect = new Rect();//删除按钮位置
    private Rect mRotateRect = new Rect();//旋转按钮位置
    private RectF mDeleteDstRect = new RectF();
    private RectF mRotateDstRect = new RectF();
    private Bitmap mDeleteBitmap;
    private Bitmap mRotateBitmap;
    private int mCurrentMode = IDLE_MODE;
    //控件的几种模式
    private static final int IDLE_MODE = 2;//正常
    private static final int MOVE_MODE = 3;//移动模式
    private static final int ROTATE_MODE = 4;//旋转模式
    private static final int DELETE_MODE = 5;//删除模式
    private EditText mEditText;//输入控件
    public int layout_x = 0;
    public int layout_y = 0;
    public float mRotateAngle = 0;
    public float mScale = 1;
    private boolean isInitLayout = true;
    private boolean isShowHelpBox = true;
    //是否需要自动换行
    private List<String> mTextContents = new ArrayList<String>(2);//存放所写的文字内容
    private String mText;
    //是否开启旋转模式
    private boolean isSuperMode = true;
    private int mTextColor;

    //马赛克
    private Bitmap bmCoverLayer;
    private Bitmap bmMosaicLayer;
    private Paint mosaicPaint;
    private Bitmap bmTouchLayer;


    public DrawView(Context context) {
        super(context);
        initView(context);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void setSuperMode(boolean superMode) {
        isSuperMode = superMode;
    }


    public void setEditText(EditText textView) {
        this.mEditText = textView;
    }

    public void setEditView(View editView) {
        this.editView = editView;
    }


    private void generatorBit() {
        mDrawBit = Bitmap.createBitmap(pictureWidth, pictureHeight, Bitmap.Config.ARGB_4444);
        mPaintCanvas = new Canvas(mDrawBit);
    }

    private void initView(Context context) {
        mContext = context;
        drawType = DrawType.NORMAL;

        mPaint = new Paint();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setPathEffect(new PathEffect());
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);

        mEraserPaint = new Paint();
        mEraserPaint.setAlpha(0);
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setDither(true);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mEraserPaint.setStrokeCap(Paint.Cap.ROUND);
        mEraserPaint.setPathEffect(new PathEffect());
        mEraserPaint.setStrokeWidth(10);


        mDeleteBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.sticker_delete);
        mRotateBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.sticker_rotate);
        mDeleteRect.set(0, 0, mDeleteBitmap.getWidth(), mDeleteBitmap.getHeight());
        mRotateRect.set(0, 0, mRotateBitmap.getWidth(), mRotateBitmap.getHeight());
        mDeleteDstRect = new RectF(0, 0, STICKER_BTN_HALF_SIZE << 1, STICKER_BTN_HALF_SIZE << 1);
        mRotateDstRect = new RectF(0, 0, STICKER_BTN_HALF_SIZE << 1, STICKER_BTN_HALF_SIZE << 1);

        mTextPaint.setColor(Color.RED);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(TEXT_SIZE_DEFAULT);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mHelpPaint.setColor(Color.BLACK);
        mHelpPaint.setStyle(Paint.Style.STROKE);
        mHelpPaint.setAntiAlias(true);
        mHelpPaint.setStrokeWidth(4);


        mosaicPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mosaicPaint.setStyle(Paint.Style.STROKE);
        mosaicPaint.setAntiAlias(true);
        mosaicPaint.setStrokeJoin(Paint.Join.ROUND);
        mosaicPaint.setStrokeCap(Paint.Cap.ROUND);
        mosaicPaint.setPathEffect(new CornerPathEffect(10));
        mosaicPaint.setStrokeWidth(50);
        mosaicPaint.setColor(Color.BLUE);


    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

    }

    public void setBasePicture(Bitmap basePicture) {

        this.basePicture = basePicture;//缓存图片原图
        pictureWidth = basePicture.getWidth();//获取图片宽度
        pictureHeight = basePicture.getHeight();//获取图片高度


        if (mDrawBit == null) {
            generatorBit();
        }

        if (isInitLayout) {
            isInitLayout = false;
            mRotateAngle = 0;
            mScale = 1;
            mTextContents.clear();
        }


        bmCoverLayer = Bitmap.createBitmap(pictureWidth, pictureHeight,
                Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bmCoverLayer);
        Bitmap mosaicBitmap = getMosaic(basePicture);
        canvas.drawBitmap(mosaicBitmap, 0, 0, null);
        canvas.save();
        mosaicBitmap.recycle();
        mosaicBitmap = null;

        bmMosaicLayer = Bitmap.createBitmap(pictureWidth, pictureHeight,
                Bitmap.Config.ARGB_4444);


    }

    public void setDrawType(DrawType drawType) {
        this.drawType = drawType;
    }

    //涂鸦颜色
    public void setPaintColor(int color) {
        this.mPaintColor = color;
        this.mPaint.setColor(color);
        invalidate();
    }

    //涂鸦线宽
    public void setWidth(float width) {
        this.mPaint.setStrokeWidth(width);
    }


    public void setText(String text) {
        isShowHelpBox = true;
        this.mText = text;
        invalidate();
    }

    public void setTextColor(int color) {
        this.mTextColor = color;
        mTextPaint.setColor(color);
        invalidate();
    }

    //设置橡皮擦
    public void setEraser(boolean eraser) {
        this.eraser = eraser;
        mPaint.setColor(eraser ? Color.TRANSPARENT : mPaintColor);
    }

    public Bitmap getPaintBit() {
        return mDrawBit;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (null != basePicture) {//绘制图片原图
            canvas.drawBitmap(basePicture, 0, 0, null);
        }

        if (mDrawBit != null) {
            canvas.drawBitmap(mDrawBit, 0, 0, null);
        }


        if (TextUtils.isEmpty(mText))
            return;

        drawContent(canvas);


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        System.out.println("width = " + getMeasuredWidth() + "     height = " + getMeasuredHeight());
        setMeasuredDimension(pictureWidth, pictureHeight);


    }


    private float moveX = 0;
    private float moveY = 0;
    private float downX = 0;
    private float downY = 0;


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (drawType.equals(DrawType.NORMAL)) {
            return super.onTouchEvent(event);
        }
        //屏蔽父类的touch事件
        getParent().requestDisallowInterceptTouchEvent(true);

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (drawType) {
                    case DRAW:
                        mPath = new Path();
                        mPath.moveTo(x, y);
                        for (int i = savePath.size() - 1; i >= index; i--) {
                            savePath.remove(i);
                        }
                        savePath.add(new DrawPath(DrawType.DRAW, mPath, mPaintColor));
                        index = savePath.size();
                        last_x = x;
                        last_y = y;
                        break;
                    case TEXT:
                        downX = event.getX();
                        downY = event.getY();
                        moveX = 0;
                        moveY = 0;

                        if (mDeleteDstRect.contains(x, y)) {// 删除模式
                            isShowHelpBox = true;
                            mCurrentMode = DELETE_MODE;
                        } else if (mRotateDstRect.contains(x, y)) {// 旋转按钮
                            isShowHelpBox = true;
                            mCurrentMode = ROTATE_MODE;
                            last_x = mRotateDstRect.centerX();
                            last_y = mRotateDstRect.centerY();
                        } else if (mHelpBoxRect.contains(x, y)) {// 移动模式
                            isShowHelpBox = true;
                            mCurrentMode = MOVE_MODE;
                            last_x = x;
                            last_y = y;
                        } else {
                            mCurrentMode = IDLE_MODE;
                            getParent().requestDisallowInterceptTouchEvent(false);
//                            isShowHelpBox = false;
//                            invalidate();
                        }

                        if (mCurrentMode == DELETE_MODE) {// 删除选定贴图
                            mCurrentMode = IDLE_MODE;// 返回空闲状态
                            clearTextContent();
                            invalidate();
                        }
                        break;
                    case MOSAIC:
                        mPath = new Path();
                        mPath.moveTo(x, y);
                        for (int i = savePath.size() - 1; i >= index; i--) {
                            savePath.remove(i);
                        }
                        savePath.add(new DrawPath(DrawType.MOSAIC, mPath, 0));
                        index = savePath.size();
                        last_x = x;
                        last_y = y;
                        break;
                }

            case MotionEvent.ACTION_MOVE:

                switch (drawType) {
                    case DRAW:
                        mPath.lineTo(x, y);
                        mPaintCanvas.drawLine(last_x, last_y, x, y, eraser ? mEraserPaint : mPaint);
                        last_x = x;
                        last_y = y;
                        this.postInvalidate();
                        break;
                    case TEXT:
                        moveX += Math.abs(event.getX() - downX);
                        moveY += Math.abs(event.getY() - downY);
                        downX = event.getX();
                        downY = event.getY();

                        if (mCurrentMode == MOVE_MODE) {// 移动贴图
                            mCurrentMode = MOVE_MODE;
                            float dx = x - last_x;
                            float dy = y - last_y;
                            layout_x += dx;
                            layout_y += dy;
                            invalidate();
                            last_x = x;
                            last_y = y;
                        } else if (mCurrentMode == ROTATE_MODE) {// 旋转 缩放文字操作
                            mCurrentMode = ROTATE_MODE;
                            float dx = x - last_x;
                            float dy = y - last_y;
                            updateRotateAndScale(dx, dy);
                            invalidate();
                            last_x = x;
                            last_y = y;
                        }
                        break;
                    case MOSAIC:
                        mPath.lineTo(x, y);

                        bmTouchLayer = Bitmap.createBitmap(pictureWidth, pictureHeight,
                                Bitmap.Config.ARGB_4444);
                        Canvas canvas = new Canvas();
                        canvas.setBitmap(bmTouchLayer);

                        canvas.drawLine(last_x, last_y, x, y, mosaicPaint);

                        canvas.setBitmap(bmMosaicLayer);
                        canvas.drawARGB(0, 0, 0, 0);
                        canvas.drawBitmap(bmCoverLayer, 0, 0, null);
                        mosaicPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                        canvas.drawBitmap(bmTouchLayer, 0, 0, mosaicPaint);
                        canvas.save();
                        mosaicPaint.setXfermode(null);
                        bmTouchLayer.recycle();
                        bmTouchLayer = null;
                        mPaintCanvas.drawBitmap(bmMosaicLayer, 0, 0, null);
                        mPaintCanvas.save();


                        last_x = x;
                        last_y = y;

                        invalidate();
                        break;
                }
                break;

            case MotionEvent.ACTION_UP:

                switch (drawType) {
                    case TEXT:
                        if (moveX < 20 && moveY < 20 && mHelpBoxRect.contains(x, y)) {
                            mEditText.requestFocus();
                            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                        }

                        if (mCurrentMode == IDLE_MODE && TextUtils.isEmpty(mText)) {
                            editView.setVisibility(VISIBLE);
                            layout_x = (int) x;
                            layout_y = (int) y;

                            mEditText.requestFocus();
                            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                        }

                        break;
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDrawBit != null && !mDrawBit.isRecycled()) {
            mDrawBit.recycle();
        }
    }


    private void drawContent(Canvas canvas) {
        drawText(canvas);

        if (!isSuperMode) {
            return;
        }

        int offsetValue = ((int) mDeleteDstRect.width()) >> 1;
        mDeleteDstRect.offsetTo(mHelpBoxRect.left - offsetValue, mHelpBoxRect.top - offsetValue);
        mRotateDstRect.offsetTo(mHelpBoxRect.right - offsetValue, mHelpBoxRect.bottom - offsetValue);

        RectUtil.rotateRect(mDeleteDstRect, mHelpBoxRect.centerX(),
                mHelpBoxRect.centerY(), mRotateAngle);
        RectUtil.rotateRect(mRotateDstRect, mHelpBoxRect.centerX(),
                mHelpBoxRect.centerY(), mRotateAngle);


        if (!isShowHelpBox) {
            return;
        }

        canvas.save();
        canvas.rotate(mRotateAngle, mHelpBoxRect.centerX(), mHelpBoxRect.centerY());
        canvas.drawRoundRect(mHelpBoxRect, 10, 10, mHelpPaint);
        canvas.restore();


        canvas.drawBitmap(mDeleteBitmap, mDeleteRect, mDeleteDstRect, null);
        canvas.drawBitmap(mRotateBitmap, mRotateRect, mRotateDstRect, null);
    }

    private void drawText(Canvas canvas) {
        TextBean textBean = new TextBean(layout_x, layout_y, mScale, mRotateAngle, mText, mTextColor);
        drawText(canvas, textBean);
    }


    public void drawText(Canvas canvas, TextBean textBean) {
        mTextContents.clear();

        String[] splits = textBean.text.split("\n");
        for (String text : splits) {
            mTextContents.add(text);
        }
        if (ListUtil.isEmpty(mTextContents))
            return;

        int text_height = 0;

        mTextRect.set(0, 0, 0, 0);
        Rect tempRect = new Rect();
        for (int i = 0; i < mTextContents.size(); i++) {
            String text = mTextContents.get(i);
            mTextPaint.getTextBounds(text, 0, text.length(), tempRect);
            text_height = Math.max(CHAR_MIN_HEIGHT, tempRect.height());
            if (tempRect.height() <= 0) {
                tempRect.set(0, 0, 0, text_height);
            }

            RectUtil.rectAddV(mTextRect, tempRect, TEXT_TOP_PADDING);
        }

        mTextRect.offset(textBean.x, textBean.y - text_height);


        mHelpBoxRect.set(mTextRect.left - PADDING, mTextRect.top - PADDING
                , mTextRect.right + PADDING, mTextRect.bottom + PADDING);
        RectUtil.scaleRect(mHelpBoxRect, textBean.scale);

        canvas.save();
        canvas.scale(textBean.scale, textBean.scale, mHelpBoxRect.centerX(), mHelpBoxRect.centerY());
        canvas.rotate(textBean.rotate, mHelpBoxRect.centerX(), mHelpBoxRect.centerY());

        int draw_text_y = textBean.y;
        for (int i = 0; i < mTextContents.size(); i++) {
            canvas.drawText(mTextContents.get(i), textBean.x, draw_text_y, mTextPaint);
            draw_text_y += text_height + TEXT_TOP_PADDING;
        }
        canvas.restore();
    }


    public void clearTextContent() {
        if (mEditText != null) {
            mEditText.setText(null);
        }
    }


    /**
     * 旋转 缩放 更新
     *
     * @param dx
     * @param dy
     */
    public void updateRotateAndScale(final float dx, final float dy) {
        float c_x = mHelpBoxRect.centerX();
        float c_y = mHelpBoxRect.centerY();

        float x = mRotateDstRect.centerX();
        float y = mRotateDstRect.centerY();

        float n_x = x + dx;
        float n_y = y + dy;

        float xa = x - c_x;
        float ya = y - c_y;

        float xb = n_x - c_x;
        float yb = n_y - c_y;

        float srcLen = (float) Math.sqrt(xa * xa + ya * ya);
        float curLen = (float) Math.sqrt(xb * xb + yb * yb);

        float scale = curLen / srcLen;// 计算缩放比

        mScale *= scale;
        float newWidth = mHelpBoxRect.width() * mScale;

        if (newWidth < 70) {
            mScale /= scale;
            return;
        }

        double cos = (xa * xb + ya * yb) / (srcLen * curLen);
        if (cos > 1 || cos < -1)
            return;
        float angle = (float) Math.toDegrees(Math.acos(cos));
        float calMatrix = xa * yb - xb * ya;// 行列式计算 确定转动方向

        int flag = calMatrix > 0 ? 1 : -1;
        angle = flag * angle;

        mRotateAngle += angle;
    }


    //上一步
    public void lastStep() {
        index--;
        if (index < 0) {
            index = 0;
            return;
        }
        resetView();
        invalidate();

        for (int i = 0; i < index; i++) {
            switch (savePath.get(i).type) {
                case DRAW:
                    mPaint.setColor(savePath.get(i).color);
                    mPaintCanvas.drawPath(savePath.get(i).path, eraser ? mEraserPaint : mPaint);
                    break;
                case TEXT:
                    mTextPaint.setColor(savePath.get(i).textBean.color);
                    drawText(mPaintCanvas, savePath.get(i).textBean);
                    break;
                case MOSAIC:

                    bmTouchLayer = Bitmap.createBitmap(pictureWidth, pictureHeight,
                            Bitmap.Config.ARGB_4444);
                    Canvas canvas = new Canvas();
                    canvas.setBitmap(bmTouchLayer);
                    canvas.drawPath(savePath.get(i).path, mosaicPaint);
                    canvas.setBitmap(bmMosaicLayer);
                    canvas.drawARGB(0, 0, 0, 0);
                    canvas.drawBitmap(bmCoverLayer, 0, 0, null);
                    mosaicPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                    canvas.drawBitmap(bmTouchLayer, 0, 0, mosaicPaint);
                    canvas.save();
                    mosaicPaint.setXfermode(null);
                    bmTouchLayer.recycle();
                    bmTouchLayer = null;
                    mPaintCanvas.drawBitmap(bmMosaicLayer, 0, 0, null);
                    mPaintCanvas.save();
                    break;
            }
        }
        this.postInvalidate();
        mPaint.setColor(mPaintColor);
        mTextPaint.setColor(mTextColor);
    }


    public void nextStep() {
        index++;
        if (index > savePath.size()) {
            index = savePath.size();
            return;
        }

        resetView();
        invalidate();

        for (int i = 0; i < index; i++) {
            switch (savePath.get(i).type) {
                case DRAW:
                    mPaint.setColor(savePath.get(i).color);
                    mPaintCanvas.drawPath(savePath.get(i).path, eraser ? mEraserPaint : mPaint);
                    break;
                case TEXT:
                    mTextPaint.setColor(savePath.get(i).textBean.color);
                    drawText(mPaintCanvas, savePath.get(i).textBean);
                    break;
                case MOSAIC:

                    bmTouchLayer = Bitmap.createBitmap(pictureWidth, pictureHeight,
                            Bitmap.Config.ARGB_4444);
                    Canvas canvas = new Canvas();
                    canvas.setBitmap(bmTouchLayer);
                    canvas.drawPath(savePath.get(i).path, mosaicPaint);
                    canvas.setBitmap(bmMosaicLayer);
                    canvas.drawARGB(0, 0, 0, 0);
                    canvas.drawBitmap(bmCoverLayer, 0, 0, null);
                    mosaicPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                    canvas.drawBitmap(bmTouchLayer, 0, 0, mosaicPaint);
                    canvas.save();
                    mosaicPaint.setXfermode(null);
                    bmTouchLayer.recycle();
                    bmTouchLayer = null;
                    mPaintCanvas.drawBitmap(bmMosaicLayer, 0, 0, null);
                    mPaintCanvas.save();
            }
        }
        this.postInvalidate();
        mPaint.setColor(mPaintColor);
        mTextPaint.setColor(mTextColor);
    }


    public void resetView() {
        if (mDrawBit != null && !mDrawBit.isRecycled()) {
            mDrawBit.recycle();
            mDrawBit = null;
        }
        generatorBit();

        if (mEditText != null) {
            mEditText.setText(null);
        }
        layout_x = getMeasuredWidth() / 2;
        layout_y = getMeasuredHeight() / 2;
        mRotateAngle = 0;
        mScale = 1;
        mTextContents.clear();
    }


    public void resetRectView() {
        mEditText.setText("");
        mDeleteRect.set(0, 0, mDeleteBitmap.getWidth(), mDeleteBitmap.getHeight());
        mRotateRect.set(0, 0, mRotateBitmap.getWidth(), mRotateBitmap.getHeight());
        mDeleteDstRect = new RectF(0, 0, STICKER_BTN_HALF_SIZE << 1, STICKER_BTN_HALF_SIZE << 1);
        mRotateDstRect = new RectF(0, 0, STICKER_BTN_HALF_SIZE << 1, STICKER_BTN_HALF_SIZE << 1);
    }

    public void confirmText() {
        editView.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);

        if (TextUtils.isEmpty(mEditText.getText().toString())) {
            return;
        }

        for (int i = savePath.size() - 1; i >= index; i--) {
            savePath.remove(i);
        }

        TextBean textBean = new TextBean(layout_x, layout_y, mScale, mRotateAngle, mEditText.getText().toString().trim(), mTextColor);
        savePath.add(new DrawPath(DrawType.TEXT, textBean));
        index = savePath.size();

        isShowHelpBox = false;
        drawText(mPaintCanvas, textBean);
        resetRectView();
    }


    /**
     * 马赛克效果
     *
     * @param bitmap 原图
     * @return 马赛克图片
     */
    public static Bitmap getMosaic(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int radius = 15;


        Bitmap mosaicBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(mosaicBitmap);

        int horCount = (int) Math.ceil(width / (float) radius);
        int verCount = (int) Math.ceil(height / (float) radius);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        for (int horIndex = 0; horIndex < horCount; ++horIndex) {
            for (int verIndex = 0; verIndex < verCount; ++verIndex) {
                int l = radius * horIndex;
                int t = radius * verIndex;
                int r = l + radius;
                if (r > width) {
                    r = width;
                }
                int b = t + radius;
                if (b > height) {
                    b = height;
                }
                int color = bitmap.getPixel(l, t);
                Rect rect = new Rect(l, t, r, b);
                paint.setColor(color);
                canvas.drawRect(rect, paint);
            }
        }
        canvas.save();

        return mosaicBitmap;
    }


    public void destory() {
        if (basePicture != null) {
            basePicture.recycle();
            basePicture = null;
        }

        if (mDrawBit != null) {
            mDrawBit.recycle();
            mDrawBit = null;
        }
        if (bmMosaicLayer != null) {
            bmMosaicLayer.recycle();
            bmMosaicLayer = null;
        }

        if (bmCoverLayer != null) {
            bmCoverLayer.recycle();
            bmCoverLayer = null;
        }

        if (bmTouchLayer != null) {
            bmTouchLayer.recycle();
            bmTouchLayer = null;
        }

        System.gc();
    }


}
