package com.example.imagehammer;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.night.hammer.Hammer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Hammer.setDebug(true);
    }

    public void cut_image(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 1; i++) {
                        String mBasePath = getExternalFilesDir(Environment.DIRECTORY_DCIM).getAbsolutePath() + File.separator + "Test" + i + ".JPG";
                        Hammer.withCropping()
                                .setImageSize(720, 360)
                                .onLaunch(new File(mBasePath));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    public void compress_image(View view) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 1; i++) {
                        String mBasePath = getExternalFilesDir(Environment.DIRECTORY_DCIM).getAbsolutePath() + File.separator + "Test" + i + ".JPG";
                        Hammer.withCompress()
                                .setImageMaxLength(100 * 1024)
                                .setImageMaxSize(480, 480)
                                .setStep(5)
                                .onLaunch(new File(mBasePath));
                    }
                } catch (Exception e) {

                }
            }
        }).start();
    }

    public void save_image(View view) {
        String mBasePath = getExternalFilesDir(Environment.DIRECTORY_DCIM).getAbsolutePath() + File.separator;
        File file = new File(mBasePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        new Thread(() -> {
            for (int i = 0; i < 1; i++) {
                File mImageFile = new File(mBasePath + "Test" + i + ".JPG");
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
                try (FileOutputStream fos = new FileOutputStream(mImageFile)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                } catch (IOException e) {

                }
            }
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "文件写入成功", Toast.LENGTH_LONG));
        }).start();
    }
}