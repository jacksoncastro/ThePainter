package br.com.jackson.thepainter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private PainterView painterView;

    private ImageButton currPaint;
    private ImageButton drawBtn;
    private ImageButton eraseBtn;
    private ImageButton newBtn;
    private ImageButton saveBtn;
    private ImageButton loadImageBtn;

    private float smallBrush;
    private float mediumBrush;
    private float largeBrush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializingDrawer();
    }

    private void initializingDrawer() {
        painterView = (PainterView)findViewById(R.id.painter);

        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);

        currPaint = (ImageButton)paintLayout.getChildAt(0);

        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        smallBrush = getResources().getInteger(R.integer.small_size);

        mediumBrush = getResources().getInteger(R.integer.medium_size);

        largeBrush = getResources().getInteger(R.integer.large_size);

        drawBtn = (ImageButton)findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(getListenerDrawer());

        painterView.setBrushSize(mediumBrush);

        eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(getListenerErase());

        newBtn = (ImageButton)findViewById(R.id.new_btn);
        newBtn.setOnClickListener(getListenerNew());

        saveBtn = (ImageButton)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder saveDialog = new AlertDialog.Builder(MainActivity.this);
                saveDialog.setTitle("Save drawing");
                saveDialog.setMessage("Save drawing to device Gallery?");
                saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.WRITE_EXTERNAL_STORAGE);
                        } else {
                            saveImage();
                        }
                    }
                });
                saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        dialog.cancel();
                    }
                });
                saveDialog.show();
            }
        });

        loadImageBtn = (ImageButton)findViewById(R.id.load_image_btn);
        loadImageBtn.setOnClickListener(getListenerLoadImage());
    }

    private OnClickListener getListenerLoadImage() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.READ_EXTERNAL_STORAGE);
                } else {
                    loadImageFromGallery();
                }
            }
        };
    }

    private void loadImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (data == null) {
                Toast.makeText(MainActivity.this, "Erro to load image", Toast.LENGTH_SHORT).show();
                return;
            }
            if (requestCode == Constants.PICK_IMAGE && resultCode == Activity.RESULT_OK) {
                loadInputStream(data);
            }

        } catch (IOException e) {
            Log.e(MainActivity.class.getSimpleName(), "Erro to load image from gallery", e);
        }
    }

    private void loadInputStream(Intent data) throws IOException {
        InputStream inputStream = MainActivity.this.getContentResolver().openInputStream(data.getData());
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        try {
            setBackgroundImage(bufferedInputStream);
        } finally {
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private void setBackgroundImage(BufferedInputStream bufferedInputStream) {

        Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);

        if (bitmap != null) {
            BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
            painterView.setBackgroundDrawable(ob);
        }
    }

    private OnClickListener getListenerNew() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder newDialog = new AlertDialog.Builder(MainActivity.this);
                newDialog.setTitle("New drawing");
                newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
                newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        painterView.startNew();
                        dialog.dismiss();
                    }
                });
                newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                newDialog.show();
            }
        };
    }

    private OnClickListener getListenerErase() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog brushDialog = new Dialog(MainActivity.this);
                brushDialog.setTitle("Eraser size:");
                brushDialog.setContentView(R.layout.brush_chooser);

                ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
                smallBtn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        painterView.setErase(true);
                        painterView.setColor(Color.WHITE);
                        painterView.setBrushSize(smallBrush);
                        brushDialog.dismiss();
                        painterView.setErase(false);
                    }
                });

                ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
                mediumBtn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        painterView.setErase(true);
                        painterView.setColor(Color.WHITE);
                        painterView.setBrushSize(mediumBrush);
                        brushDialog.dismiss();
                        painterView.setErase(false);
                    }
                });

                ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
                largeBtn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        painterView.setErase(true);
                        painterView.setColor(Color.WHITE);
                        painterView.setBrushSize(largeBrush);
                        brushDialog.dismiss();
                        painterView.setErase(false);
                    }
                });

                brushDialog.show();
            }
        };
    }

    private OnClickListener getListenerDrawer() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog brushDialog = new Dialog(MainActivity.this);
                brushDialog.setTitle("Brush size:");

                brushDialog.setContentView(R.layout.brush_chooser);

                ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
                smallBtn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        painterView.setBrushSize(smallBrush);
                        painterView.setLastBrushSize(smallBrush);
                        brushDialog.dismiss();
                    }
                });

                ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
                mediumBtn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        painterView.setBrushSize(mediumBrush);
                        painterView.setLastBrushSize(mediumBrush);
                        brushDialog.dismiss();
                    }
                });

                ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
                largeBtn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        painterView.setBrushSize(largeBrush);
                        painterView.setLastBrushSize(largeBrush);
                        brushDialog.dismiss();
                    }
                });

                brushDialog.show();
            }
        };
    }

    public void paintClicked(View view) {
        if(view != currPaint) {
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();
            painterView.setColor(Color.parseColor(color));

            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint=(ImageButton)view;

            painterView.setBrushSize(painterView.getLastBrushSize());
        }
    }

    private void saveImage() {
        painterView.setDrawingCacheEnabled(true);

        String imgSaved = MediaStore.Images.Media.insertImage(
                getContentResolver(), painterView.getDrawingCache(),
                UUID.randomUUID().toString()+".png", "drawing");

        if (imgSaved != null) {
            Toast.makeText(getApplicationContext(), "Drawing saved to Gallery!", Toast.LENGTH_SHORT).show();;
        } else{
            Toast.makeText(getApplicationContext(), "Oops! Image could not be saved.", Toast.LENGTH_SHORT).show();
        }

        painterView.destroyDrawingCache();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   saveImage();
                } else {
                    Toast.makeText(MainActivity.this, "You don't granted permission of write storage for application", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case Constants.READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadImageFromGallery();
                } else {
                    Toast.makeText(MainActivity.this, "You don't granted permission of read storage for application", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}