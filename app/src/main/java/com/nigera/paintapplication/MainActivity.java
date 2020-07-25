package com.nigera.paintapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    Dialog BrushWidthDialog,CircleWidthDialog,WriteTextDialog;
    FingerPainterView myFingerPainterView;
    public static int brushColor = Color.parseColor("#000000");
    public static String brushType = "Round";
    public static int brushWidth = 10;
    public static int painTextSize = 16;
    public static String painText = "";
    public static int circleSize = 50;
    public static boolean inDrawCircle = false;
    public static boolean isErase = false;

    Button black_button, red_button, green_button, orange_button, purple_button, aqua_button;
    Button draw_line;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BrushWidthDialog != null) {
            BrushWidthDialog.dismiss();
            BrushWidthDialog = null;
        }
        if(CircleWidthDialog!=null){
            CircleWidthDialog.dismiss();
            CircleWidthDialog = null;
        }
        if(WriteTextDialog!=null){
            WriteTextDialog.dismiss();
            WriteTextDialog = null;
        }
    }
    private void initilizePainer(int color, String brush) {
        //Set the Color of Painter
        myFingerPainterView.setColour(color);

        //Set the Brush Type of Painter
        if (brush.equals("Round"))
            myFingerPainterView.setBrush(Paint.Cap.ROUND);
        else
            myFingerPainterView.setBrush(Paint.Cap.SQUARE);


        //Set the width of the Brush
        myFingerPainterView.setBrushWidth(brushWidth);

    }


    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {

            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Log.d("FileSaving",
                        "Error creating media file, check storage permissions: ");// e.getMessage());
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                myFingerPainterView.getBitmapFromCanvas().compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.close();
                Toast.makeText(MainActivity.this, "Your picture has been saved", Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                Log.d("FileSaving", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("FileSaving", "Error accessing file: " + e.getMessage());
            }


        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_LONG).show();
            //"Permissions are important to use this feature"
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Save Button
        findViewById(R.id.save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TedPermission.with(MainActivity.this)
                        .setPermissionListener(permissionlistener)
                        .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .check();

            }
        });

        ////////////////////////////////////////////
        /////////////Write Text Dialog//////////////
        ////////////////////////////////////////////
        WriteTextDialog = new Dialog(MainActivity.this);
        WriteTextDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WriteTextDialog.setContentView(R.layout.write_text_dialog);
        WriteTextDialog.setCancelable(false);
        final EditText text_edt = WriteTextDialog.findViewById(R.id.text_edt);
        final EditText text_size_edt = WriteTextDialog.findViewById(R.id.text_size_edt);

        TextView dialogn_confirmation_text = WriteTextDialog.findViewById(R.id.dialogn_confirmation_text);
        dialogn_confirmation_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                painText = text_edt.getText().toString();
                painTextSize = Integer.parseInt(text_size_edt.getText().toString());
                WriteTextDialog.dismiss();
                myFingerPainterView.writeTextinCanvas();
            }
        });


        //Write Text
        findViewById(R.id.text_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WriteTextDialog.show();
            }
        });

        ////////////////////////////////////////////
        ///////Circle size Selection Dialog/////////
        ////////////////////////////////////////////
        CircleWidthDialog = new Dialog(MainActivity.this);
        CircleWidthDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        CircleWidthDialog.setContentView(R.layout.circle_width_dialog);
        CircleWidthDialog.setCancelable(false);
        final EditText circle_width_edt = CircleWidthDialog.findViewById(R.id.circle_width_edt);
        circle_width_edt.setText(circleSize+"");
        TextView cirlce_dialogn_confirmation = CircleWidthDialog.findViewById(R.id.cirlce_dialogn_confirmation);
        cirlce_dialogn_confirmation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleSize = Integer.parseInt(circle_width_edt.getText().toString());
                CircleWidthDialog.dismiss();
            }
        });

        //Circle Size
        findViewById(R.id.circle_size).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CircleWidthDialog.show();
            }
        });


        ////////////////////////////////////////////
        ///////Brush Width Selection Dialog/////////
        ////////////////////////////////////////////
        BrushWidthDialog = new Dialog(MainActivity.this);
        BrushWidthDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        BrushWidthDialog.setContentView(R.layout.brush_width_dialog);
        BrushWidthDialog.setCancelable(false);
        final EditText brush_width_edt = BrushWidthDialog.findViewById(R.id.brush_width_edt);
        brush_width_edt.setText(brushWidth+"");

        TextView dialognBtn_confirmation = BrushWidthDialog.findViewById(R.id.dialognBtn_confirmation);
        dialognBtn_confirmation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                brushWidth = Integer.parseInt(brush_width_edt.getText().toString());
                myFingerPainterView.setBrushWidth(brushWidth);
                BrushWidthDialog.dismiss();
            }
        });

        //Brush Width
        findViewById(R.id.brush_width).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                brush_width_edt.setText(brushWidth+"");
                BrushWidthDialog.show();
            }
        });

        findViewById(R.id.draw_cirlce).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inDrawCircle = true;
            }
        });
        //Draw Line Button
        findViewById(R.id.draw_line).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inDrawCircle = false;
            }
        });

        //Erase Button
        findViewById(R.id.testing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myFingerPainterView.eraseThePaintLines();

            }
        });

        //Color Button initilization
        colorButtonsInit();



        myFingerPainterView = findViewById(R.id.myFingerPainterViewId);

        initilizePainer(brushColor, brushType);

        myFingerPainterView.setBrushWidth(brushWidth);


    }

    private void colorButtonsInit() {
        //Black Color
        black_button = findViewById(R.id.black_button);
        black_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.brushColor = Color.parseColor("#000000");
                //myFingerPainterView.setColour(MainActivity.brushColor);
                initilizePainer(brushColor, brushType);
            }
        });

        //Red Color
        red_button = findViewById(R.id.red_button);
        red_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.brushColor = Color.parseColor("#fd0000");
                //myFingerPainterView.setColour(MainActivity.brushColor);
                initilizePainer(brushColor, brushType);
            }
        });


        //Green Color
        green_button = findViewById(R.id.green_button);
        green_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.brushColor = Color.parseColor("#04ff00");
                //myFingerPainterView.setColour(MainActivity.brushColor);
                //initilizePainer(brushColor, brushType);
            }
        });


        //Orange Color
        orange_button = findViewById(R.id.orange_button);
        orange_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.brushColor = Color.parseColor("#f55600");
                //myFingerPainterView.setColour(MainActivity.brushColor);
                //initilizePainer(brushColor, brushType);
            }
        });

        //Purple Color
        purple_button = findViewById(R.id.purple_button);
        purple_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.brushColor = Color.parseColor("#fd00d7");
                //myFingerPainterView.setPaintColor(MainActivity.brushColor);
                //initilizePainer(brushColor, brushType);
            }
        });

        //Aqua Color
        aqua_button = findViewById(R.id.aqua_button);
        aqua_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.brushColor = Color.parseColor("#00f7ff");
               // myFingerPainterView.setColour(MainActivity.brushColor);
                //initilizePainer(brushColor, brushType);
            }
        });

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}


