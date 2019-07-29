package com.ajay.prokeyboard;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;


public class settings extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        LoadPreferences();

    }

    private void SavePreferences(String key, int value) {
        SharedPreferences sharedPreferences = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }
    public void preview(View v) {
        CheckBox preview = findViewById(R.id.preview);
        if (preview.isChecked()) {
            SavePreferences("PREVIEW", 1);
        } else SavePreferences("PREVIEW", 0);
        closeKeyboard(v);

    }

    public void sound(View v) {
        CheckBox preview = findViewById(R.id.sound);
        if (preview.isChecked()) {
            SavePreferences("SOUND", 1);
        } else SavePreferences("SOUND", 0);
        closeKeyboard(v);
    }

    public void vibrate(View v) {
        CheckBox preview = findViewById(R.id.vibrate);
        if (preview.isChecked()) {
            SavePreferences("VIBRATE", 1);
        } else SavePreferences("VIBRATE", 0);
        closeKeyboard(v);
    }
    private void LoadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);

        int setPreview  = sharedPreferences.getInt("PREVIEW", 0);
        int setSound    = sharedPreferences.getInt("SOUND"  , 1);
        int setVibrator = sharedPreferences.getInt("VIBRATE", 1);

        CheckBox preview = (CheckBox) findViewById(R.id.preview);
        CheckBox sound   = (CheckBox) findViewById(R.id.sound);
        CheckBox vibrate = (CheckBox) findViewById(R.id.vibrate);

        if (setPreview == 1)
            preview.setChecked(true);
        else
            preview.setChecked(false);

        if (setSound == 1)
            sound.setChecked(true);
        else
            sound.setChecked(false);

        if (setVibrator == 1)
            vibrate.setChecked(true);
        else
            vibrate.setChecked(false);

    }
    public void closeKeyboard(View v) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

    }

}