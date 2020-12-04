package br.com.gabrielferreira.pomodoro;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

	private static final long POMODORO_TIME = 1500000; //1500000
	private static final long SHORT_BREAK = 300000; //300000
	private static final long LONG_BREAK = 1800000; //1800000

    public static final String ALARM_KEY = "alarm";
    public static final String DEFAULT_START = "defaultStart";

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
    Button resetButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		soundPool = new SoundPool(2, AudioManager.STREAM_ALARM, 0);

		timerTxt = findViewById(R.id.timer);
		pomodoroCountTxt = findViewById(R.id.pomodoroCount);

        Button startButton = findViewById(R.id.startBtn);
        resetButton = findViewById(R.id.resetBtn);
        ImageButton configButton = findViewById(R.id.configBtn);

        pomodoroTxt = findViewById(R.id.pomodoroTxt);
        shortBreakTxt = findViewById(R.id.shortTxt);
        longBreakTxt = findViewById(R.id.longTxt);
        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        alarm = sharedPreferences.getInt(ALARM_KEY, R.raw.kabuki);
        resetButton.setEnabled(false);
        resetButton.setVisibility(View.INVISIBLE);

		startButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
                resetButton.setEnabled(true);
                resetButton.setVisibility(View.VISIBLE);
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
		        resetDialog();
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
	    if (mediaPlayer != null)
	        mediaPlayer.stop();

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
                    createNotificationChannel();
                    notificationStart();
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
	@SuppressLint("SetTextI18n")
    private void resetTimer(){
	    timer.cancel();
	    isCounting = false;
	    restTime = false;
        pomodoroCount = 0;
        resetButton.setEnabled(false);
        resetButton.setVisibility(View.INVISIBLE);
        timerTxt.setText("25:00");
        pomodoroCountTxt.setText("Pomodoro: 0");
    }

    /**
     * Inicia e gerencia o a caixa de diálogo de confirmação do reinício da contagem.
     */
    private void resetDialog(){
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

    /**
     * Abre e gerencia a caixa de diálogo de mudança de música.
     */
    private void configDialog(){
        final CharSequence[] items = {"Kabuki", "End game"};
        sound = new int[]{R.raw.kabuki, R.raw.end_game};
        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        int selItem = sharedPreferences.getInt(DEFAULT_START, 0);
        AlertDialog.Builder configDialog = new AlertDialog.Builder(this);

        configDialog
                .setTitle("Selecionar som")
                .setSingleChoiceItems(items, selItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        playSound(sound[i]);
                        alarm = sound[i];
                        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(DEFAULT_START, i);
                        editor.apply();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        saveSound();
                        mediaPlayer.stop();
                    }
                })
                .create()
                .show();
    }

    /**
     * Inicia a música selecionada
     * @param res
     * Recurso de áudio a ser iniciado.
     */
    private void playSound(int res) {
	    if (mediaPlayer == null){
            mediaPlayer = MediaPlayer.create(this,res);
            mediaPlayer.start();
        } else {
            mediaPlayer.reset();
            mediaPlayer = MediaPlayer.create(this,res);
            mediaPlayer.start();
        }
    }

    /**
     * Salva a música selecionada no aparelho.
     */
    private void saveSound (){
        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = sharedPreferences.edit();

	    editor.putInt(ALARM_KEY, alarm);
	    editor.apply();
    }

    private void notificationStart(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "alarmNotification")
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Alarme Pomodoro")
                .setContentText("Ciclo finalizado")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel (){
        NotificationChannel channel = new NotificationChannel("alarmNotification", "alarm notification", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }


}