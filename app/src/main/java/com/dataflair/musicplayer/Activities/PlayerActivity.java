package com.dataflair.musicplayer.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.dataflair.musicplayer.MainActivity;
import com.dataflair.musicplayer.R;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity {
    LinearLayout linearLayout;
    ImageView back_btn;

    SpeechRecognizer speechRecognizer;
    Intent speechIntent;
    String keeper = "";

    String mode = "ON";

    Button btnPlay, btnNext, btnPrevious, btnShuffle;
    TextView txtSongName, txtSongStart, txtSongEnd;
    SeekBar seekMusicBar;
    BarVisualizer barVisualizer;
    ImageView imageView;
    String songName;

    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateSeekBar;
    Handler handler;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        init();
        VoicePermission();
//        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(PlayerActivity.this);
//        speechIntent= new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//
//        speechRecognizer.setRecognitionListener(new RecognitionListener() {
//            @Override
//            public void onReadyForSpeech(Bundle params) {
//
//            }
//            @Override
//            public void onBeginningOfSpeech() {
//
//            }
//            @Override
//            public void onRmsChanged(float rmsdB) {
//
//            }
//            @Override
//            public void onBufferReceived(byte[] buffer) {
//
//            }
//            @Override
//            public void onEndOfSpeech() {
//
//            }
//            @Override
//            public void onError(int error) {
//
//            }
//            @Override
//            public void onResults(Bundle results) {
//                ArrayList<String> matchFound = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//                if (matchFound != null){
//                    keeper = matchFound.get(0);
//
//                    if (keeper.contentEquals ("drop")) {
//                        Toast.makeText(getApplicationContext(), "Command = " + keeper, Toast.LENGTH_SHORT).show();
//                        btnPlay.setBackgroundResource(R.drawable.pause_icon);
//                        mediaPlayer.pause();
//                    } else if (keeper.contentEquals("play")) {
//                        Toast.makeText(getApplicationContext(), "Command = " + keeper, Toast.LENGTH_SHORT).show();
//                        btnPlay.setBackgroundResource(R.drawable.play_icon);
//                        mediaPlayer.start();
//                    }else if (keeper.contentEquals("next please")) {
//                        Toast.makeText(getApplicationContext(), "Command = " + keeper, Toast.LENGTH_SHORT).show();
//                        if(!mediaPlayer.isPlaying()){
//                            btnPlay.setBackgroundResource(R.drawable.play_icon);
//                        }
//                        mediaPlayer.stop();
//                        mediaPlayer.release();
//                        position = ((position + 1) % mySongs.size());
//                        Uri uri1 = Uri.parse(mySongs.get(position).toString());
//                        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri1);
//                        songName = mySongs.get(position).getName();
//                        txtSongName.setText(songName);
//                        mediaPlayer.start();
//                        songEndTime();
//                    }else if (keeper.contentEquals("back")) {
//                        Toast.makeText(getApplicationContext(), "Command = " + keeper, Toast.LENGTH_SHORT).show();
//                        mediaPlayer.stop();
//                        mediaPlayer.release();
//                        position = ((position - 1) % mySongs.size());
//                        if (position < 0)
//                            position = mySongs.size() - 1;
//                        Uri uri1 = Uri.parse(mySongs.get(position).toString());
//                        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri1);
//                        songName = mySongs.get(position).getName();
//                        txtSongName.setText(songName);
//                        mediaPlayer.start();
//                    }
//                }
//
//            }
//            @Override
//            public void onPartialResults(Bundle partialResults) {
//
//            }
//            @Override
//            public void onEvent(int eventType, Bundle params) {
//
//            }
//        });
//        linearLayout = findViewById(R.id.linearlayout);
//        linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                Toast.makeText(PlayerActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
//                speechRecognizer.startListening(speechIntent);
//                keeper = "";
//                return false;
//            }
//        });

        back_btn = findViewById(R.id.back_btn);
        back_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Intent intent = new Intent(PlayerActivity.this, MainActivity.class);
                startActivity(intent);
                return false;
            }
        });



        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getIntegerArrayList("songs");
        String textContent = intent.getStringExtra("songname");
        position = bundle.getInt("pos");
        txtSongName.setSelected(true);

        Uri uri = Uri.parse(mySongs.get(position).toString());
        songName = mySongs.get(position).getName();
        txtSongName.setText(songName);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        songEndTime();

        visualizer();


        updateSeekBar = new Thread() {
            @Override
            public void run() {
                int TotalDuration = mediaPlayer.getDuration();
                int CurrentPosition = 0;

                while (CurrentPosition < TotalDuration) {
                    try {
                        sleep(500);
                        CurrentPosition = mediaPlayer.getCurrentPosition();
                        seekMusicBar.setProgress(CurrentPosition);

                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        };


        seekMusicBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.start();
        playCycle();
        seekMusicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());

            }
        });


        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNext.performClick();
            }
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position + 1) % mySongs.size());
                Uri uri1 = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri1);
                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();
                songEndTime();
                startAnimation(imageView, 360f);
                visualizer();
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position - 1) % mySongs.size());
                if (position < 0)
                    position = mySongs.size() - 1;

                Uri uri1 = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri1);
                songName = mySongs.get(position).getName();
                txtSongName.setText(songName);
                mediaPlayer.start();
                songEndTime();
                startAnimation(imageView, -360f);
                visualizer();
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffle();
            }
        });

    }

    private void init(){
        btnPlay = (Button) findViewById(R.id.BtnPlay);
        btnNext = (Button) findViewById(R.id.BtnNext);
        btnPrevious = (Button) findViewById(R.id.BtnPrevious);
        btnShuffle = findViewById(R.id.BtnShuffle);
        txtSongName = (TextView) findViewById(R.id.SongTxt);
        txtSongStart = (TextView) findViewById(R.id.TxtSongStart);
        txtSongEnd = (TextView) findViewById(R.id.TxtSongEnd);
        seekMusicBar = (SeekBar) findViewById(R.id.SeekBar);
        barVisualizer = findViewById(R.id.wave);
        imageView = (ImageView) findViewById(R.id.MusicImage);
        handler = new Handler();


    }

    private void play(){
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    btnPlay.setBackgroundResource(R.drawable.play_icon);
                    mediaPlayer.pause();

                } else {
                    btnPlay.setBackgroundResource(R.drawable.pause_icon);
                    mediaPlayer.start();
                    playCycle();

                    TranslateAnimation moveAnim = new TranslateAnimation(-25, 25, -25, 25);
                    moveAnim.setInterpolator(new AccelerateInterpolator());
                    moveAnim.setDuration(600);
                    moveAnim.setFillEnabled(true);
                    moveAnim.setFillAfter(true);
                    moveAnim.setRepeatMode(Animation.REVERSE);
                    moveAnim.setRepeatCount(1);
                    imageView.startAnimation(moveAnim);
                    visualizer();
                }
            }
        });
    }

    private void shuffle(){
//        Fisher Yates Algo
        Random rand = new Random();
        int n = mySongs.size();

        for (int i = n - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);

            // Swap songs[i] and songs[j]
            Collections.swap(mySongs, i, j);
        }
    }

    private void VoicePermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(! (ContextCompat.checkSelfPermission(PlayerActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)){
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }

    public void startAnimation(View view, Float degree) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, degree);
        objectAnimator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();
    }

    public String createDuration(int duration) {
        String time = "";
        String secondsString;

        //Converting total duration into time
        int minutes = (int) (duration % 3600000) / 60000;
        int seconds = (int) ((duration % 3600000) % 60000 / 1000);

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10)
            secondsString = "0" + seconds;
        else
            secondsString = "" + seconds;

        time = time + minutes + ":" + secondsString;

        // Return timer String;
        return time;


    }

    private  void playCycle(){
        try {
            seekMusicBar.setProgress(mediaPlayer.getCurrentPosition());
            String currentTime = createDuration(mediaPlayer.getCurrentPosition());
            txtSongStart.setText(currentTime);

            if (mediaPlayer.isPlaying()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        playCycle();

                    }
                };
                handler.postDelayed(runnable, 100);
            }

        }
        catch (Exception e){
            e.printStackTrace();

        }

    }
    public void visualizer() {
        int audioSessionId = mediaPlayer.getAudioSessionId();
        if (audioSessionId != -1) {
            barVisualizer.setAudioSessionId(audioSessionId);
        }
    }

    public void songEndTime() {
        String endTime = createDuration(mediaPlayer.getDuration());
        txtSongEnd.setText(endTime);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (barVisualizer != null)
            barVisualizer.release();
    }


}