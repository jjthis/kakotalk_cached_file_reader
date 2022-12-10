package com.Lion.kakao.toolkit;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ImageViewThis extends AppCompatActivity {
    File ePath = new File("sdcard/thisKakaoPic/");
    File eePath;
    int now;
    int list;
    float pressedX;
    ImageView img;
    long ClickTime = 0;

    int firstC = 0;


    @SuppressWarnings("unused")
    private static final float MIN_ZOOM = 1f, MAX_ZOOM = 1f;

    // These matrices will be used to scale points of the image
    Matrix savedMatrix = new Matrix();

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;


    @SuppressLint({"ClickableViewAccessibility", "WrongConstant"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.BLACK);
        Intent intent = getIntent();
        list = MainActivity.list.size();
        now = intent.getExtras().getInt("Data");
        eePath = MainActivity.list.get(now);
        img = new ImageView(this);
        Glide.with(this)
                .load(eePath)
                .into(img);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
        layoutParams.gravity = Gravity.CENTER;
        img.setLayoutParams(layoutParams);

        img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (saveScale == 1.0f) {
                    float distance = 0;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            long clickTime = System.currentTimeMillis();
                            if (clickTime - ClickTime < 300) {
                                if (img.getScaleType() != ImageView.ScaleType.MATRIX)
                                    img.setScaleType(ImageView.ScaleType.MATRIX);
                                zoomIn(event.getX(), event.getY());
                                return true;
                            } else {
                                ClickTime = clickTime;
                                //single
                            }
                            pressedX = event.getX();
                            break;
                        case MotionEvent.ACTION_UP:
                            distance = pressedX - event.getX();
                            break;
                    }
                    //(event.getAction()& MotionEvent.ACTION_MASK) != MotionEvent.ACTION_POINTER_DOWN) {
                    if (Math.abs(distance) < 100) {
                        return false;
                    }
                    if (distance > 0) {
                        if (now != list - 1) {
                            Intent intent = new Intent(ImageViewThis.this, ImageViewThis.class);
                            intent.putExtra("Data", now + 1);
                            startActivity(intent);
                            overridePendingTransition(R.anim.left_in, R.anim.left_out);
                        } else {
                            Toast.makeText(ImageViewThis.this, "This is Last Image", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    } else if (now != 0) {
                        Intent intent = new Intent(ImageViewThis.this, ImageViewThis.class);
                        intent.putExtra("Data", now - 1);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.right_out);
                    } else {
                        Toast.makeText(ImageViewThis.this, "This is First Image", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    finish(); // finish 해주지 않으면 activity가 계속 쌓인다.
                } else {
                    img.setScaleType(ImageView.ScaleType.MATRIX);
                    float scale;

                    dumpEvent(event);
                    // Handle touch events here...

                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:   // first finger down only

                            long clickTime = System.currentTimeMillis();
                            if (clickTime - ClickTime < 300) {
                                if (img.getScaleType() != ImageView.ScaleType.MATRIX)
                                    img.setScaleType(ImageView.ScaleType.MATRIX);
                                zoomOut();
                                return false;
                            } else {
                                ClickTime = clickTime;
                                //single
                            }

                            savedMatrix.set(matrix);
                            if (firstC == 0) {
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inJustDecodeBounds = true;
                                try {
                                    BitmapFactory.decodeStream(
                                            getContentResolver().openInputStream(Uri.fromFile(eePath)),
                                            null,
                                            options);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                float hei = options.outHeight;
                                float wid = options.outWidth;
                                float dec = (float) (hei / wid);
                                float x_pri = 0, y_pri = 0;
                                float def = (float) (16.0 / 9.0);
                                if (dec < def) y_pri = (float) (1920 - (1080 * (dec))) / 2;//16중
                                else if (dec > def) x_pri = (float) (1080 - (1920 / dec)) / 2;//9중
                                start.set(event.getX() - x_pri, event.getY() - y_pri);
                                Log.d(TAG, "mode=DRAG"); // write to LogCat
                                mode = DRAG;
                                firstC = 12;
                                break;
                            } else {
                                start.set(event.getX(), event.getY());
                                Log.d(TAG, "mode=DRAG"); // write to LogCat
                                mode = DRAG;
                                break;
                            }

                        case MotionEvent.ACTION_UP: // first finger lifted

                        case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                            mode = NONE;
                            Log.d(TAG, "mode=NONE");
                            break;

                        case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                            oldDist = spacing(event);
                            Log.d(TAG, "oldDist=" + oldDist);
                            if (oldDist > 5f) {
                                savedMatrix.set(matrix);
                                midPoint(mid, event);
                                mode = ZOOM;
                                Log.d(TAG, "mode=ZOOM");
                            }
                            break;

                        case MotionEvent.ACTION_MOVE:

                            if (mode == DRAG) {
                                matrix.set(savedMatrix);
                                matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                            } else if (mode == ZOOM) {
                                // pinch zooming
                                float newDist = spacing(event);
                                Log.d(TAG, "newDist=" + newDist);
                                if (newDist > 5f) {
                                    matrix.set(savedMatrix);
                                    scale = newDist / oldDist; // setting the scaling of the
                                    // matrix...if scale > 1 means
                                    // zoom in...if scale < 1 means
                                    // zoom out
                                    matrix.postScale(scale, scale, mid.x, mid.y);
                                }
                            }
                            break;
                    }

                    img.setImageMatrix(matrix); // display the transformation on screen
                }
                Log.i(TAG, "onTouch: " + "맞음아아ㅓㄹㄴ아ㅓㅎ");
                return true;
            }
        });
        int strokeWidth = 5; // 5px not dp
        //int roundRadius = 10; // 15px not dp
        int strokeColor = Color.parseColor("#FF4081");
        int fillColor = Color.WHITE;

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int parm = 20;
        param.leftMargin = 30;
        param.rightMargin = 30;
        param.topMargin = 40;

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(fillColor);
        //gd.setCornerRadius(roundRadius);
        gd.setStroke(strokeWidth, strokeColor);
        final LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(1);
        Button btn = new Button(this);
        btn.setBackground(gd);
        btn.setLayoutParams(param);
        btn.setTransformationMethod(null);
        btn.setText("Save this Picture");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ePath.exists()) ePath.mkdir();
                final File imgFile = new File(ePath, eePath.getName() + ".png");
                copyFile(eePath, imgFile);
                MediaScannerConnection.scanFile(ImageViewThis.this, new String[]{imgFile.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Saved this Picture", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                });
            }
        });
        Button btn1 = new Button(this);
        btn1.setBackground(gd);
        btn1.setLayoutParams(param);
        btn1.setTransformationMethod(null);
        btn1.setText("Delete this Picture");
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ImageViewThis.this, "Deleted this Picture", Toast.LENGTH_SHORT).show();
                eePath.delete();
                finish();
            }
        });
        lay.addView(btn);
        lay.addView(btn1);
        AlertDialog.Builder dialog = new AlertDialog.Builder(ImageViewThis.this);
        dialog.setTitle("선택");
        dialog.setView(lay);
        dialog.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog dia = dialog.create();
        img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dia.show();
                return false;
            }
        });
        img.setBackgroundColor(Color.BLACK);
        setContentView(img);
    }

    public Point getScreenSize() {
        Display display = ImageViewThis.this.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }


    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Show an event in the LogCat view, for debugging
     */
    private void dumpEvent(MotionEvent event) {
        String names[] = {"DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?"};
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }

        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }

        sb.append("]");
        Log.d("Touch Events ---------", sb.toString());
    }


    public void copyFile(File path1, File path2) {
        try {
            FileInputStream fis = new java.io.FileInputStream(path1);
            FileOutputStream fos = new java.io.FileOutputStream(path2);
            BufferedInputStream bis = new java.io.BufferedInputStream(fis);
            BufferedOutputStream bos = new java.io.BufferedOutputStream(fos);
            int buf;
            while ((buf = bis.read()) != -1) {
                bos.write(buf);
            }
            bis.close();
            bos.close();
            fis.close();
            fos.close();
        } catch (Exception e) {
        }
    }

    String TAG = "thiss";
    float oldScale = 1.0f;
    float saveScale = 1.0f;
    Matrix matrix = new Matrix();

    public void zoomIn(float x, float y) {
        oldScale = saveScale;
        saveScale *= 2;
        Drawable d = img.getDrawable();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(Uri.fromFile(eePath)),
                    null,
                    options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        float hei = options.outHeight;
        float wid = options.outWidth;
        float dec = (float) (hei / wid);
        float x_pri = 0, y_pri = 0;
        float def = (float) (16.0 / 9.0);
        if (dec < def) y_pri = (float) (1920 - (1080 * (dec)));//16중
        else if (dec > def) x_pri = (float) (1080 - (1920 / dec));//9중
        RectF imageRectF = new RectF(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        RectF viewRectF = new RectF(0, 0, img.getWidth(), img.getHeight());
        matrix.setRectToRect(imageRectF, viewRectF, Matrix.ScaleToFit.CENTER);
        matrix.setScale(saveScale, saveScale, x - x_pri, y - y_pri);
        img.setImageMatrix(matrix);
        img.invalidate();
    }

    public void zoomOut() {
        saveScale = oldScale;
        matrix.setScale(saveScale, saveScale);
        Drawable d = img.getDrawable();
        RectF imageRectF = new RectF(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        RectF viewRectF = new RectF(0, 0, img.getWidth(), img.getHeight());
        matrix.setRectToRect(imageRectF, viewRectF, Matrix.ScaleToFit.CENTER);
        img.setImageMatrix(matrix);
        img.invalidate();
    }
}
