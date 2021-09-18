package com.ajay.prokeyboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class settings extends AppCompatActivity {

    private AdView mAdView;
    SeekBar seekBar;
    RadioGroup colorRadioGroup;
    RadioGroup layoutRadioGroup;
    final String RADIO_INDEX_COLOUR = "RADIO_INDEX_COLOUR";
    final String RADIO_INDEX_LAYOUT = "RADIO_INDEX_LAYOUT";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        seekBar = findViewById(R.id.seekBar);

        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progressChangedValue = seekBar.getProgress();
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    progressChangedValue = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Write code to perform some action when touch is started.
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    SavePreferences("SIZE", progressChangedValue);
                }
            });
        }
       colorRadioGroup = (RadioGroup) findViewById(R.id.radioGroupColour);
       colorRadioGroup.setOnCheckedChangeListener(radioGroupColour);

        layoutRadioGroup = (RadioGroup) findViewById(R.id.keylayouts);
        layoutRadioGroup.setOnCheckedChangeListener(radioGroupLayout);

        LoadPreferences();

        //ads
        //ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adViewSetting);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    //color
        RadioGroup.OnCheckedChangeListener  radioGroupColour= new RadioGroup.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    RadioButton checkedRadioButtonColour = (RadioButton) colorRadioGroup.findViewById(checkedId);
                    int checkedIndexColour = colorRadioGroup.indexOfChild(checkedRadioButtonColour);
                    SavePreferences(RADIO_INDEX_COLOUR, checkedIndexColour);
                }
            };
    //layout
    RadioGroup.OnCheckedChangeListener  radioGroupLayout= new RadioGroup.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    RadioButton checkedRadioButtonLayout = (RadioButton) layoutRadioGroup.findViewById(checkedId);
                    int checkedIndexLayout = layoutRadioGroup.indexOfChild(checkedRadioButtonLayout);
                    SavePreferences(RADIO_INDEX_LAYOUT, checkedIndexLayout);
                }
            };

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

    public void LoadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);

        int savedRadioColour = sharedPreferences.getInt(RADIO_INDEX_COLOUR, 0);
        RadioButton savedCheckedRadioButtonColour = (RadioButton) colorRadioGroup.getChildAt(savedRadioColour);
        savedCheckedRadioButtonColour.setChecked(true);

        int savedRadioLayout = sharedPreferences.getInt(RADIO_INDEX_LAYOUT, 0);
        RadioButton savedCheckedRadioButtonLayout = (RadioButton) layoutRadioGroup.getChildAt(savedRadioLayout);
        savedCheckedRadioButtonLayout.setChecked(true);

        int setPreview = sharedPreferences.getInt("PREVIEW", 1);
        int setSound = sharedPreferences.getInt("SOUND", 1);
        int setVibrator = sharedPreferences.getInt("VIBRATE", 1);
        int setSize     = sharedPreferences.getInt("SIZE"   , 1);

        CheckBox preview = (CheckBox) findViewById(R.id.preview);
        CheckBox sound = (CheckBox) findViewById(R.id.sound);
        CheckBox vibrate = (CheckBox) findViewById(R.id.vibrate);
        SeekBar size = (SeekBar) findViewById(R.id.seekBar);

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

        size.setProgress(setSize);
    }

    public void closeKeyboard(View v) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

    }

}