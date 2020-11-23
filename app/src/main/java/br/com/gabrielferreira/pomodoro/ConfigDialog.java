package br.com.gabrielferreira.pomodoro;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Objects;

public class ConfigDialog extends AppCompatDialogFragment{
    public  AudioAttributes audioAttributes;
    public SoundPool alarmPool;
    public RadioGroup rGroup;

    public int kabuki, endGame;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.config, null);

        builder.setView(view).setTitle("Sons");
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int radioButtonID = rGroup.getCheckedRadioButtonId(); //https://stackoverflow.com/questions/6440259/how-to-get-the-selected-index-of-a-radiogroup-in-android
                View radioButton = rGroup.findViewById(radioButtonID);
                int idx = rGroup.indexOfChild(radioButton);

                RadioButton r = (RadioButton) rGroup.getChildAt(idx);
                String selectedtext = r.getText().toString();

                if (selectedtext.equals("Kabuki")){
                    alarmPool.play(kabuki, 1, 1, 0, 0, 1);
                } else if (selectedtext.equals("End game")){
                    alarmPool.play(endGame, 1, 1, 0, 0, 1);
                }

            }
        });

        loadSounds();
        return builder.create();
    }

    /**
     * Cria e popula a lista dropdown.
     */
    private void loadSounds(){
        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        alarmPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();

        kabuki = alarmPool.load(Objects.requireNonNull(getActivity()).getApplicationContext(), R.raw.kabuki, 1);
        endGame = alarmPool.load(getActivity().getApplicationContext(), R.raw.end_game, 1);
    }

}
