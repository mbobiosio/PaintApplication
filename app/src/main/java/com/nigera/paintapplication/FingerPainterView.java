/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nigera.paintapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FingerPainterView extends View {

    private Context context;
    private Canvas canvas;
    private Paint paint;
    private Bitmap bitmap;
    private Path path;
    private Uri uri;
    private float shapeX;
    private float shapeY;

    public static ArrayList<CircleCoordinatesModels> circleCoordinatesModelsArrayList = new ArrayList<>();

    //private ArrayList<Path> paths = new ArrayList<Path>();
    //private ArrayList<Path> undonePaths = new ArrayList<Path>();
    List<Pair<Path, Integer>> paths = new ArrayList<Pair<Path,Integer>>();
    List<Pair<Path, Integer>> undonePaths = new ArrayList<Pair<Path,Integer>>();



    public Bitmap getBitmapFromCanvas(){
        return this.getDrawingCache();
    }

    public FingerPainterView(Context context) {
        super(context); // application context
        init(context);
    }

    public FingerPainterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FingerPainterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.context = context;

        path = new Path();
        paint = new Paint();

        // default brush style and colour
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(20);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setARGB(255, 0, 0, 0);
    }

    public void setBrush(Paint.Cap brush) {
        paint.setStrokeCap(brush);
    }

    public Paint.Cap getBrush() {
        return paint.getStrokeCap();
    }

    public void setBrushWidth(int width) {
        paint.setStrokeWidth(width);
    }

    public int getBrushWidth() {
        return (int) paint.getStrokeWidth();
    }

    public void setColour(int colour) {
        paint.setColor(colour);
    }

    public int getColour() {
        return paint.getColor();
    }

    public void load(Uri uri) {
        this.uri = uri;
    }

//    private boolean isInsideCircle(int x, int y){
//        if (((x - center_x)^2 + (y - center_y)^2) < radius^2)
//            return true;
//        return false;
//    }


    public void drawCircle() {

        paint.setColor(MainActivity.brushColor);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(shapeX, shapeY, MainActivity.circleSize, paint);
        path.reset();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        // save superclass view state
        bundle.putParcelable("superState", super.onSaveInstanceState());

        try {
            // save bitmap to temporary cache file to overcome binder transaction size limit
            File f = File.createTempFile("fingerpaint", ".png", context.getCacheDir());
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(f));
            // save temporary filename to bundle
            bundle.putString("tempfile", f.getAbsolutePath());
        } catch (IOException e) {
            Log.e("FingerPainterView", e.toString());
        }
        return bundle;
    }


    public void writeTextinCanvas(){
        Paint paint = new Paint();
        //canvas.drawPaint(paint);
        paint.setColor(MainActivity.brushColor);
        paint.setTextSize(MainActivity.painTextSize);
        canvas.drawText(MainActivity.painText, 75, 150, paint);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            try {
                // load cache file from bundle stored filename
                File f = new File(bundle.getString("tempfile"));
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                // need to copy the bitmap to create a mutable version
                bitmap = b.copy(b.getConfig(), true);
                b.recycle();
                f.delete();
            } catch (IOException e) {
                Log.e("FingerPainterView", e.toString());
            }

            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // canvas is white with a bitmap with alpha channel drawn over the top
//        canvas.drawColor(Color.WHITE);
        //canvas.drawColor(0x00AAAAAA);
        this.setDrawingCacheEnabled(true);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        // show current drawing path
//        canvas.drawPath(path, paint);


        for (Pair<Path,Integer> path_clr : paths){
            paint.setColor(path_clr.second);
            canvas.drawPath( path_clr.first, paint);
        }
        canvas.drawPath(path, paint);


    }

    int bitmapWidth;
    int bitmapHeight;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // called after the activity has been created when the view is inflated
        bitmapHeight = h;
        bitmapWidth = w;
        if (bitmap == null) {
            if (uri != null) {
                try {
                    // attempt to load the uri provided, scale to fit our canvas
                    InputStream stream = context.getContentResolver().openInputStream(uri);
                    Bitmap bm = BitmapFactory.decodeStream(stream);
                    bitmap = Bitmap.createScaledBitmap(bm, Math.max(w, h), Math.max(w, h), false);
                    stream.close();
                    bm.recycle();


                } catch (IOException e) {
                    Log.e("FingerPainterView", e.toString());
                }
            } else {
                // create a square bitmap so is drawable even after rotation to landscape
                bitmap = Bitmap.createBitmap(Math.max(w, h), Math.max(w, h), Bitmap.Config.ARGB_8888);
            }
        }
        canvas = new Canvas(bitmap);
        //canvas.drawBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.background),0,0,paint);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.background), 0, 0, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        shapeX = x;
        shapeY = y;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.reset();
                path.moveTo(x, y);
                path.lineTo(x, y);
                undonePaths.clear();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                //canvas.drawPath(path, paint);
                //path.reset();

                if (MainActivity.inDrawCircle) {
                    paths.add(new Pair(path, MainActivity.brushColor));
                    path = new Path();
                    drawCircle();

                } else {
                    paths.add(new Pair(path, MainActivity.brushColor));
                    path = new Path();
                    canvas.drawPath(path, paint);
                    path.reset();
                }

                invalidate();
                break;
        }
        return true;
    }

    public void eraseThePaintLines() {

        if (paths.size()>0)
        {
            undonePaths.add(paths.remove(paths.size()-1));
            invalidate();
        }


//        canvas = new Canvas();
//        bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
//        canvas.setBitmap(bitmap);
    }
}