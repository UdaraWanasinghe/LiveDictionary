package com.udara.developer.livedictionary;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class CropView extends View {
    Canvas tempCanvas;
    Bitmap tempBitmap;

    Paint maskPaint;
    Paint framePaint;
    Paint cornerPaint;
    Paint finalPaint;
    Paint boundingBoxPaint;
    Paint textPaint;

    //colors
    int maskColor;
    int cornerColor;
    int frameColor;

    //dimensions
    public int viewWidth, viewHeight;
    public float frameHalfWidth;
    public float frameHalfHeight;
    public int minHalfWidth, minHalfHeight, maxHalfWidth, maxHalfHeight;
    public int cornerRadius, cornerWidth, cornerOffset;

    //rect
    public RectF cornerRect;
    public RectF frameRect;

    private float screenDensity;

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOnTouchListener(new View.OnTouchListener() {
            float startHalfWidth;
            float startHalfHeight;

            float currentHalfWidth;
            float currentHalfHeight;

            float downX;
            float downY;

            float currentX;
            float currentY;

            //touch areas
            final int CORNER_TOP_LEFT = 0;
            final int CORNER_BOTTOM_LEFT = 1;
            final int CORNER_TOP_RIGHT = 2;
            final int CORNER_BOTTOM_RIGHT = 3;
            final int LEFT_SIDE = 4;
            final int RIGHT_SIDE = 5;
            final int TOP_SIDE = 6;
            final int BOTTOM_SIDE = 7;

            int touchArea = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();

                        startHalfWidth = frameHalfWidth;
                        startHalfHeight = frameHalfHeight;

                        float left = frameRect.left;
                        float top = frameRect.top;
                        float right = frameRect.right;
                        float bottom = frameRect.bottom;

                        float offset = cornerOffset * 3;

                        if (downX >= left - offset && downX <= right + offset && downY >= top - offset && downY <= bottom + offset) {

                            if (downX <= left + offset && downY <= top + offset) {
                                touchArea = CORNER_TOP_LEFT;

                            } else if (downX <= left + offset && downY >= bottom - offset) {
                                touchArea = CORNER_BOTTOM_LEFT;

                            } else if (downX >= right - offset && downY <= top + offset) {
                                touchArea = CORNER_TOP_RIGHT;

                            } else if (downX >= right - offset && downY >= bottom - offset) {
                                touchArea = CORNER_BOTTOM_RIGHT;

                            } else if (downX <= left + offset) {
                                touchArea = LEFT_SIDE;

                            } else if (downX >= right - offset) {
                                touchArea = RIGHT_SIDE;

                            } else if (downY <= top + offset) {
                                touchArea = TOP_SIDE;

                            } else if (downY >= bottom - offset) {
                                touchArea = BOTTOM_SIDE;

                            }
                        }

                        break;

                    case MotionEvent.ACTION_MOVE:
                        currentX = event.getX();
                        currentY = event.getY();

                        switch (touchArea) {
                            case CORNER_TOP_LEFT:
                                currentHalfWidth = startHalfWidth - currentX + downX;
                                currentHalfHeight = startHalfHeight - currentY + downY;
                                frameHalfWidth = currentHalfWidth > maxHalfWidth ? maxHalfWidth :
                                        currentHalfWidth < minHalfWidth ? minHalfWidth : currentHalfWidth;
                                frameHalfHeight = currentHalfHeight > maxHalfHeight ? maxHalfHeight :
                                        currentHalfHeight < minHalfHeight ? minHalfHeight : currentHalfHeight;
                                v.invalidate();
                                break;

                            case CORNER_BOTTOM_LEFT:
                                currentHalfWidth = startHalfWidth - currentX + downX;
                                currentHalfHeight = startHalfHeight + currentY - downY;
                                frameHalfWidth = currentHalfWidth > maxHalfWidth ? maxHalfWidth :
                                        currentHalfWidth < minHalfWidth ? minHalfWidth : currentHalfWidth;
                                frameHalfHeight = currentHalfHeight > maxHalfHeight ? maxHalfHeight :
                                        currentHalfHeight < minHalfHeight ? minHalfHeight : currentHalfHeight;
                                v.invalidate();
                                break;

                            case CORNER_TOP_RIGHT:
                                currentHalfWidth = startHalfWidth + currentX - downX;
                                currentHalfHeight = startHalfHeight - currentY + downY;
                                frameHalfWidth = currentHalfWidth > maxHalfWidth ? maxHalfWidth :
                                        currentHalfWidth < minHalfWidth ? minHalfWidth : currentHalfWidth;
                                frameHalfHeight = currentHalfHeight > maxHalfHeight ? maxHalfHeight :
                                        currentHalfHeight < minHalfHeight ? minHalfHeight : currentHalfHeight;
                                v.invalidate();
                                break;

                            case CORNER_BOTTOM_RIGHT:
                                currentHalfWidth = startHalfWidth + currentX - downX;
                                currentHalfHeight = startHalfHeight + currentY - downY;
                                frameHalfWidth = currentHalfWidth > maxHalfWidth ? maxHalfWidth :
                                        currentHalfWidth < minHalfWidth ? minHalfWidth : currentHalfWidth;
                                frameHalfHeight = currentHalfHeight > maxHalfHeight ? maxHalfHeight :
                                        currentHalfHeight < minHalfHeight ? minHalfHeight : currentHalfHeight;
                                v.invalidate();
                                break;

                            case LEFT_SIDE:
                                currentHalfWidth = startHalfWidth - currentX + downX;
                                frameHalfWidth = currentHalfWidth > maxHalfWidth ? maxHalfWidth :
                                        currentHalfWidth < minHalfWidth ? minHalfWidth : currentHalfWidth;
                                v.invalidate();
                                break;

                            case RIGHT_SIDE:
                                currentHalfWidth = startHalfWidth + currentX - downX;
                                frameHalfWidth = currentHalfWidth > maxHalfWidth ? maxHalfWidth :
                                        currentHalfWidth < minHalfWidth ? minHalfWidth : currentHalfWidth;
                                v.invalidate();
                                break;
                            case TOP_SIDE:
                                currentHalfHeight = startHalfHeight - currentY + downY;
                                frameHalfHeight = currentHalfHeight > maxHalfHeight ? maxHalfHeight :
                                        currentHalfHeight < minHalfHeight ? minHalfHeight : currentHalfHeight;
                                v.invalidate();
                                break;

                            case BOTTOM_SIDE:
                                currentHalfHeight = startHalfHeight + currentY - downY;
                                frameHalfHeight = currentHalfHeight > maxHalfHeight ? maxHalfHeight :
                                        currentHalfHeight < minHalfHeight ? minHalfHeight : currentHalfHeight;
                                v.invalidate();
                                break;

                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        touchArea = -1;
                        break;

                }
                return true;
            }
        });

        screenDensity = context.getResources().getDisplayMetrics().density;

        //colors
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.colorMask);
        cornerColor = resources.getColor(R.color.colorCorners);
        frameColor = resources.getColor(R.color.colorFrame);

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setColor(maskColor);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        cornerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cornerPaint.setColor(cornerColor);

        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setColor(frameColor);
        framePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        finalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        boundingBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boundingBoxPaint.setColor(Color.RED);
        boundingBoxPaint.setStrokeWidth(1 * screenDensity);
        boundingBoxPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setTextSize(screenDensity * 10);
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);

        cornerRect = new RectF();
        frameRect = new RectF();

        cornerRadius = (int) (screenDensity * 10);
        cornerWidth = (int) (screenDensity * 4);
        cornerOffset = (int) (screenDensity * 20);

        minHalfWidth = minHalfHeight = (int) (cornerRadius * 2 + 8 * screenDensity);

        frameHalfWidth = minHalfWidth * 2;
        frameHalfHeight = minHalfHeight;

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        tempBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        tempCanvas = new Canvas(tempBitmap);

        viewWidth = w;
        viewHeight = h;
        viewHalfWidth = w / 2;
        viewHalfHeight = h / 2;

        maxHalfWidth = (int) ((w - 8 * screenDensity) / 2f);
        maxHalfHeight = (int) ((h - 8 * screenDensity) / 2f);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        frameRect = getFrameRect();

        int left = (int) frameRect.left;
        int top = (int) frameRect.top;
        int right = (int) frameRect.right;
        int bottom = (int) frameRect.bottom;

        if (drawBoundingBox) {
            for (RectData rect : boundingBoxList) {
                rect.rect.offset(left, top);
                canvas.drawRect(rect.rect, boundingBoxPaint);
                if (rect.definitions != null) {
                    canvas.drawText(rect.definitions.get(0), rect.rect.left, rect.rect.top, textPaint);
                }
            }
            drawBoundingBox = false;

        }

        //draw canvas background
        tempCanvas.drawRect(0, 0, viewWidth, viewHeight, maskPaint);

        //draw corners
        cornerRect.set(left - cornerWidth, top - cornerWidth, right + cornerWidth, bottom + cornerWidth);
        tempCanvas.drawRoundRect(cornerRect, cornerRadius, cornerRadius, cornerPaint);
        tempCanvas.drawRect(left - cornerOffset, top + cornerOffset, right + cornerOffset, bottom - cornerOffset, maskPaint);
        tempCanvas.drawRect(left + cornerOffset, top - cornerOffset, right - cornerOffset, bottom + cornerOffset, maskPaint);

        //draw frame
        tempCanvas.drawRoundRect(frameRect, cornerRadius, cornerRadius, framePaint);

        //draw final
        canvas.drawBitmap(tempBitmap, 0, 0, finalPaint);

        super.onDraw(canvas);
    }

    int viewHalfWidth;
    int viewHalfHeight;

    public synchronized RectF getFrameRect() {
        frameRect.set(viewHalfWidth - frameHalfWidth, viewHalfHeight - frameHalfHeight,
                viewHalfWidth + frameHalfWidth, viewHalfHeight + frameHalfHeight);
        return frameRect;
    }

    boolean drawBoundingBox = false;
    ArrayList<RectData> boundingBoxList;

    public void drawBoundingBoxes(ArrayList<RectData> boundingBoxList) {
        this.boundingBoxList = boundingBoxList;
        drawBoundingBox = true;
        invalidate();
    }

    public float[] getCropViewDimen() {
        float[] dimens = new float[6];
        dimens[0] = this.viewWidth;
        dimens[1] = this.viewHeight;
        dimens[2] = this.frameRect.width();
        dimens[3] = this.frameRect.height();
        dimens[4] = this.frameRect.left;
        dimens[5] = this.frameRect.top;

        return dimens;
    }

}
