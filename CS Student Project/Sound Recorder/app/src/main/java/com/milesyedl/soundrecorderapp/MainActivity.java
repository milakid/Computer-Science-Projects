package com.milesyedl.soundrecorderapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_AUDIO_PERMISSION_CODE = 101;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    ImageView ibRecord;
    ImageView ibPlay;
    TextView tvTime;
    TextView tvRecordingPath;
    ImageView ivSimplebg;
    boolean isRecording = false;
    boolean isPlaying = false;
    Handler handler;

    ExecutorService executorService = Executors.newSingleThreadExecutor();


    int seconds = 0;
    String path = null;
    LottieAnimationView lavPlaiyng;
    int dummySeconds = 0;
    int playableSeconds = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        ibRecord = findViewById(R.id.ib_record);
        ibPlay = findViewById(R.id.ib_play);
        tvTime = findViewById(R.id.tv_time);
        tvRecordingPath = findViewById(R.id.tv_recording_path);
        ivSimplebg = findViewById(R.id.iv_simple_bg);
        lavPlaiyng = findViewById(R.id.lav_playing);
        mediaPlayer = new MediaPlayer();


        ibRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkRecordingPermission())
                {

                    if(!isRecording)
                    {
                        isRecording = true;
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                mediaRecorder = new MediaRecorder();
                                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                                mediaRecorder.setOutputFile(getRecordingFilePath());
                                path = getRecordingFilePath();

                                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                                try {
                                    mediaRecorder.prepare();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                mediaRecorder.start();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ivSimplebg.setVisibility(View.VISIBLE);
                                        lavPlaiyng.setVisibility(View.GONE);
                                        tvRecordingPath.setText(getRecordingFilePath());
                                        playableSeconds = 0;
                                        seconds= 0;
                                        dummySeconds= 0;
                                        ibRecord.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.recording_active));
                                        runTimer();
                                    }
                                });
                            }
                        });
                    }
                    else
                    {
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                mediaRecorder.start();
                                mediaRecorder.stop();
                                mediaRecorder = null;
                                playableSeconds = seconds;
                                dummySeconds=seconds;
                                seconds=0;
                                isRecording=false;


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ivSimplebg.setVisibility(View.VISIBLE);
                                        lavPlaiyng.setVisibility(View.GONE);
                                        handler.removeCallbacksAndMessages(null);
                                        ibRecord.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.recording_in_active));
                                    }
                                });
                            }
                        });
                    }
                }
                else {
                    requestRecordingPermission();
                }
            }
        });

        ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isPlaying)
                {
                    if(path!=null)
                    {
                        try {
                            mediaPlayer.setDataSource(getRecordingFilePath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "NO RECORDING FOUND", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.start();
                    isPlaying=true;
                    ibPlay.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.recording_pause));
                    ivSimplebg.setVisibility(View.GONE);
                    lavPlaiyng.setVisibility(View.VISIBLE);
                    runTimer();
                }
                else
                {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer=null;

                    mediaPlayer = new MediaPlayer();
                    isPlaying = false;
                    seconds = 0;
                    handler.removeCallbacksAndMessages(null);
                    ivSimplebg.setVisibility(View.VISIBLE);
                    lavPlaiyng.setVisibility(View.GONE);
                    ibPlay.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.recording_play));

                }

            }
        });

    }

    private void runTimer() {
        handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int minutes = (seconds%360)/60;
                int secs = seconds%60;
                String time= String.format(Locale.getDefault(), "%02d:%02d", minutes,secs);
                tvTime.setText(time);
                if(isRecording || (isPlaying && playableSeconds!= -1))
                {
                    seconds++;
                    playableSeconds--;

                    if(playableSeconds==-1 && isPlaying)
                    {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        isPlaying=false;
                        mediaPlayer=null;
                        mediaPlayer = new MediaPlayer();
                        playableSeconds = dummySeconds;
                        seconds=0;
                        handler.removeCallbacksAndMessages(null);
                        ivSimplebg.setVisibility(View.VISIBLE);
                        lavPlaiyng.setVisibility(View.GONE);
                        ibPlay.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.recording_play));
                        return;

                    }
                }
                handler.postDelayed(this,1000);

            }
        });
    }

    private String getRecordingFilePath() {
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File music = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(music, "TestFile" + ".mp3");
        return file.getPath();
    }

    private void requestRecordingPermission()
    {
        ActivityCompat.requestPermissions
                (MainActivity.this, new String[]
                        {Manifest.permission.RECORD_AUDIO},REQUEST_AUDIO_PERMISSION_CODE);

    }

    public boolean checkRecordingPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED)
        {
            requestRecordingPermission();
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
                                             @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_AUDIO_PERMISSION_CODE)
        {
            if(grantResults.length>0)
            {
                boolean permissionToRecording=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if(permissionToRecording)
                {
                    Toast.makeText(getApplicationContext(), "Permission given", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}