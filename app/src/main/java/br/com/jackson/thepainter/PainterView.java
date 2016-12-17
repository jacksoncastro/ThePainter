package br.com.jackson.thepainter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

/**
 * Created by jackson on 17/12/16.
 */

public class PainterView extends View {

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    private float brushSize;

    private float lastBrushSize;

    private boolean erase;

    private ToolType currentTool = ToolType.BRUSH;

    public PainterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // initializing view
        initializingPainter();
    }

    private void initializingPainter() {
        drawPath = new Path();
        drawPaint = new Paint(paintColor);

        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;

        drawPaint.setStrokeWidth(brushSize);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (currentTool == ToolType.SPRAY) {
                    sprayMoveTo(touchX, touchY);
                } else {
                    drawPaint.setStyle(Paint.Style.STROKE);
                    drawPath.moveTo(touchX, touchY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentTool == ToolType.SPRAY) {
                    sprayLineTo(touchX, touchY);
                } else {
                    drawPaint.setStyle(Paint.Style.STROKE);
                    drawPath.lineTo(touchX, touchY);
                }
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    private void sprayLineTo(float touchX, float touchY) {
        int dotsToDrawAtATime = 10;
        double brushRadius = 10.0;

        drawPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        for (int i = 0; i < dotsToDrawAtATime; i++) {
            // Get the location to draw to
            Random random = new Random();

            float x = (float) (touchX + random.nextGaussian() * brushRadius);
            float y = (float) (touchY + random.nextGaussian() * brushRadius);

            drawCanvas.drawPoint(x, y, drawPaint);
        }
    }

    private void sprayMoveTo(float touchX, float touchY) {

        int dotsToDrawAtATime = 10;
        double brushRadius = 50.0;

        drawPaint.setStrokeWidth(10f);
        for (int i = 0; i < dotsToDrawAtATime; i++) {
            // Get the location to draw to
            Random random = new Random();

            float x = (float) (touchX + random.nextGaussian() * brushRadius);
            float y = (float) (touchY + random.nextGaussian() * brushRadius);

            drawPath.moveTo(x, y);
        }
    }

    public void setColor(int newColor) {
        invalidate();
        paintColor = newColor;
        drawPaint.setColor(paintColor);
    }

    public void setBrushSize(float newSize) {
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());
        brushSize = pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }

    public void setLastBrushSize(float lastSize) {
        lastBrushSize=lastSize;
    }

    public float getLastBrushSize() {
        return lastBrushSize;
    }

    public void setErase(boolean erase){
        this.erase = erase;

        if(erase) {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        } else {
            drawPaint.setXfermode(null);
        }
    }

    public void startNew() {
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public ToolType getCurrentTool() {
        return currentTool;
    }

    public void setCurrentTool(ToolType currentTool) {
        this.currentTool = currentTool;
    }
}
