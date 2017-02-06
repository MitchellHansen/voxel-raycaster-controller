package com.example.mrh.voxel_raycaster_controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class MyView extends View
{
    public float accel_x = 0;
    public float accel_y = 0;
    public float accel_z = 0;

    Paint paint = null;
    public MyView(Context context)
    {
        super(context);
        init(context);

    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        //int x = R.id.x_data_view;
        //int y = R.id.y_data_view;

        int x = (getWidth()/2) + (int)(accel_x * 60);// + com.example.mrh.test2.Accel.getX_dat();
        int y = (getHeight()/2) + (int)(accel_y * 60);
        int radius;
        radius = 15;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.DKGRAY);
        canvas.drawPaint(paint);
        // Use Color.parseColor to define HTML colors
        paint.setColor(Color.parseColor("#CD5C5C"));
        canvas.drawCircle(x, y, radius, paint);
        invalidate();
    }
}