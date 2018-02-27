package com.example.gocode.flashaction;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int TIPO_SENSOR = Sensor.TYPE_ACCELEROMETER;
    private ImageButton image_button_liga_e_desliga_processo;
    private SensorManager sensorManager;
    private Sensor sensor;
    private Boolean temFlash;
    private Boolean flashDesligado = true;
    private Boolean shakeServiceDesligado;
    private int count = 0;
    private static final String SERVICE_STATE = "serviceState";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        image_button_liga_e_desliga_processo = findViewById(R.id.image_button_liga_e_desliga_processo);
        temFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(TIPO_SENSOR);

        if (savedInstanceState != null) {
            shakeServiceDesligado = savedInstanceState.getBoolean(SERVICE_STATE);
            if (shakeServiceDesligado) {
                image_button_liga_e_desliga_processo.setImageResource(R.drawable.shake_flashlight_off_image_button);
            } else {
                image_button_liga_e_desliga_processo.setImageResource(R.drawable.shake_flashlight_on_image_button);
            }
        }

        if (MyService.running) {
            shakeServiceDesligado = false;
            image_button_liga_e_desliga_processo.setImageResource(R.drawable.shake_flashlight_on_image_button);
        } else {
            shakeServiceDesligado = true;
            image_button_liga_e_desliga_processo.setImageResource(R.drawable.shake_flashlight_off_image_button);
        }

        if (sensor == null) {
            Toast.makeText(MainActivity.this, "Sensor não disponível", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!temFlash) {
            Toast.makeText(MainActivity.this, "Seu dispositivo não tem flash", Toast.LENGTH_SHORT).show();
            finish();
        }

        image_button_liga_e_desliga_processo.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if (shakeServiceDesligado) {
                    startService(new Intent(MainActivity.this, MyService.class));
                    Toast.makeText(MainActivity.this, "Shake ativado", Toast.LENGTH_SHORT).show();
                    image_button_liga_e_desliga_processo.setImageResource(R.drawable.shake_flashlight_on_image_button);
                    shakeServiceDesligado = false;
                    Log.e("service", "Service iniciado");
                } else {
                    stopService(new Intent(MainActivity.this, MyService.class));
                    Toast.makeText(MainActivity.this, "Shake desativado", Toast.LENGTH_SHORT).show();
                    image_button_liga_e_desliga_processo.setImageResource(R.drawable.shake_flashlight_off_image_button);
                    shakeServiceDesligado = true;
                    Log.e("service", "Service foi parado");
                }
            }

        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        count++;
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    if (count == 2) {
                        if (flashDesligado) {
                            try {
                                CameraManager cameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
                                for (String id : cameraManager.getCameraIdList()) {

                                    // Turn on the flash if camera has one
                                    if (cameraManager.getCameraCharacteristics(id)
                                            .get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            cameraManager.setTorchMode(id, true);
                                        }
                                        flashDesligado = false;
                                    }
                                }
                            } catch (CameraAccessException e) {
                                Log.e("tag", "Failed to interact with camera.", e);
                                Toast.makeText(getApplicationContext(), "Torch Failed: " + e.getMessage(), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        } else {
                            try {
                                CameraManager cameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
                                for (String id : cameraManager.getCameraIdList()) {

                                    // Turn on the flash if camera has one
                                    if (cameraManager.getCameraCharacteristics(id)
                                            .get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            cameraManager.setTorchMode(id, false);
                                        }
                                        flashDesligado = true;
                                    }
                                }
                            } catch (CameraAccessException e) {
                                Log.e("tag", "Failed to interact with camera.", e);
                                Toast.makeText(getApplicationContext(), "Torch Failed: " + e.getMessage(), Toast.LENGTH_SHORT)
                                        .show();
                            }

                        }
                        count = 0;
                    }
                }
            }, 500);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SERVICE_STATE, shakeServiceDesligado);
        super.onSaveInstanceState(outState);
    }
}

