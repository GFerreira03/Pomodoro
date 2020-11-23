package br.com.gabrielferreira.pomodoro;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

	private static final long POMODORO_TIME = 5000; //1500000
	private static final long SHORT_BREAK = 300000; //300000
	private static final long LONG_BREAK = 1800000; //1800000

    public int[] sound;
    public int alarm;

    private long timeLeft;

	private boolean isCounting = false;
	private boolean restTime = false;
	private int pomodoroCount = 0;

	private TextView timerTxt, pomodoroCountTxt;
	private TextView shortBreakTxt, longBreakTxt, pomodoroTxt;

    MediaPlayer mediaPlayer;
    SoundPool soundPool;
    CountDownTimer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		soundPool = new SoundPool(2, AudioManager.STREAM_ALARM, 0);

		timerTxt = findViewById(R.id.timer);
		pomodoroCountTxt = findViewById(R.id.pomodoroCount);

        Button startButton = findViewById(R.id.startBtn);
        Button resetButton = findViewById(R.id.resetBtn);
        ImageButton configButton = findViewById(R.id.configBtn);

        pomodoroTxt = findViewById(R.id.pomodoroTxt);
        shortBreakTxt = findViewById(R.id.shortTxt);
        longBreakTxt = findViewById(R.id.longTxt);

        if (alarm == 0){
            alarm = R.raw.kabuki;
        }
        
		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
			    if (!isCounting){
                    isCounting = true;
                    if (!restTime) {
                        timeLeft = POMODORO_TIME;
                        pomodoroTxt.setTextColor(Color.parseColor("#FA646D"));
                        shortBreakTxt.setTextColor(Color.parseColor("#C1BDBE"));
                        longBreakTxt.setTextColor(Color.parseColor("#C1BDBE"));
                        startTimer();
                        pomodoroCount += 1;
                        pomodoroCountTxt.setText(String.format(Locale.getDefault(), "Pomodoros: %01d", pomodoroCount));
                    } else if (pomodoroCount < 4) {
                        timeLeft = SHORT_BREAK;
                        pomodoroTxt.setTextColor(Color.parseColor("#C1BDBE"));
                        shortBreakTxt.setTextColor(Color.parseColor("#FA646D"));
                        longBreakTxt.setTextColor(Color.parseColor("#C1BDBE"));
                        startTimer();
                    } else {
                        timeLeft = LONG_BREAK;
                        pomodoroTxt.setTextColor(Color.parseColor("#C1BDBE"));
                        shortBreakTxt.setTextColor(Color.parseColor("#C1BDBE"));
                        longBreakTxt.setTextColor(Color.parseColor("#FA646D"));
                        startTimer();
                        pomodoroCount = 0;
                        pomodoroCountTxt.setText(String.format(Locale.getDefault(), "Pomodoros: %01d", pomodoroCount));
                    }
                } else {
			        Toast.makeText(getApplicationContext(), "Aguarde o término da contagem", Toast.LENGTH_SHORT).show();
                }
			}
		});

		resetButton.setOnClickListener(new View.OnClickListener(){
		    @Override
            public void onClick(View view) {
		        confirmDialog();
            }
        });

		configButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                configDialog();
            }
        });
	}

    /**
     * Controla o timer.
     */
	private void startTimer(){
        timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long l) {
                timeLeft = l;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                isCounting = false;
                restTime = !restTime;
                try {
                    playSound(alarm);
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
	}

    /**
     * Atualiza o texto do contador.
     */
	private void updateTimerText(){
		int minutes = (int) (timeLeft / 1000) / 60;
		int seconds = (int) (timeLeft / 1000) % 60;

		String timeText = String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);
		timerTxt.setText(timeText);
	}

    /**
     * Reseta o contador e o texto.
     */
	private void resetTimer(){
	    timer.cancel();
	    isCounting = false;
	    restTime = false;
        pomodoroCount = 0;
        timerTxt.setText(String.format(Locale.getDefault(),"25:00"));
        pomodoroCountTxt.setText(String.format(Locale.getDefault(),"Pomodoro: 0"));
    }

    private void confirmDialog(){
        final AlertDialog.Builder confirmBox = new AlertDialog.Builder(this);
        confirmBox.setTitle("Reiniciar");
        confirmBox.setMessage("Ao tocar em sim, a contagem reiniciará. Deseja continuar?");
        confirmBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, "Contagem reiniciada", Toast.LENGTH_SHORT).show();
                resetTimer();
            }
        });
        confirmBox.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        confirmBox.show();
    }

    private void configDialog(){
        final CharSequence[] items = {"Kabuki", "End game"};
        sound = new int[]{R.raw.kabuki, R.raw.end_game};
        int selItem = 0;
        AlertDialog.Builder configDialog = new AlertDialog.Builder(this);

        configDialog
                .setTitle("Selecionar som")
                .setSingleChoiceItems(items, selItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        playSound(sound[i]);
                        alarm = sound[i];
                    }
                })
                .create()
                .show();
    }

    private void playSound(int res) {
	    if (mediaPlayer == null){
            mediaPlayer = MediaPlayer.create(this,res);
        } else {
            mediaPlayer.reset();
            mediaPlayer = MediaPlayer.create(this,res);
            mediaPlayer.start();
        }
    }

}