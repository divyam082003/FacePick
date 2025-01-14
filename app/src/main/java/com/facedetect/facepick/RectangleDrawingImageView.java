package com.facedetect.facepick;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;
import java.util.ArrayList;
import java.util.List;

public class RectangleDrawingImageView extends AppCompatImageView {
    private Paint paint;
    private List<RectF> rectangles;
    private int originalImageWidth;
    private int originalImageHeight;

    public RectangleDrawingImageView(Context context) {
        super(context);
        init();
    }

    public RectangleDrawingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RectangleDrawingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        rectangles = new ArrayList<>();
    }
    public void setOriginalImageSize(int width, int height) {
        originalImageWidth = width;
        originalImageHeight = height;
    }


    public void addRectangles(List<FaceDetectionResult> dataList) {
        for (FaceDetectionResult data : dataList) {
            int x = data.getX();
            int y = data.getY();
            int width = data.getWidth();
            int height = data.getHeight();

            // Adjust coordinates to match image view dimensions
            float imageViewWidth = getWidth();
            float imageViewHeight = getHeight();
            float scaleX = imageViewWidth / originalImageWidth; // originalImageWidth is the width of the image being displayed
            float scaleY = imageViewHeight / originalImageHeight; // originalImageHeight is the height of the image being displayed
            float adjustedX = x * scaleX;
            float adjustedY = y * scaleY;
            float adjustedWidth = width * scaleX;
            float adjustedHeight = height * scaleY;

            rectangles.add(new RectF(adjustedX, adjustedY, adjustedX + adjustedWidth, adjustedY + adjustedHeight));
        }
        invalidate();
    }

    public void clearRectangles() {
        rectangles.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (RectF rect : rectangles) {
            canvas.drawRect(rect, paint);
        }
    }
}

