package com.ajay.prokeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

public class settings extends AppCompatActivity {

    SeekBar seekBar;
    RadioGroup colorRadioGroup;
    RadioGroup layoutRadioGroup;
    RadioGroup capsLockRadioGroup;
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

        capsLockRadioGroup = findViewById(R.id.capsLockStyleGroup);
        capsLockRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int index = capsLockRadioGroup.indexOfChild(capsLockRadioGroup.findViewById(checkedId));
            SavePreferences("CAPS_LOCK_STYLE", index);
        });

        AdManager.init(this);
        AdManager.loadBanner(this, R.id.settings_banner_ad);

        LoadPreferences();

        // Set up checkbox listeners
        CheckBox soundCheckBox = findViewById(R.id.sound);
        CheckBox vibrateCheckBox = findViewById(R.id.vibrate);
        CheckBox previewCheckBox = findViewById(R.id.preview);
        CheckBox autoCloseBracketsCheckBox = findViewById(R.id.autoCloseBrackets);
        CheckBox autoCloseQuotesCheckBox = findViewById(R.id.autoCloseQuotes);

        soundCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SavePreferences("SOUND", isChecked ? 1 : 0);
        });

        vibrateCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SavePreferences("VIBRATE", isChecked ? 1 : 0);
        });

        previewCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SavePreferences("PREVIEW", isChecked ? 1 : 0);
        });

        autoCloseBracketsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SavePreferences("AUTO_CLOSE_BRACKETS", isChecked ? 1 : 0);
        });

        autoCloseQuotesCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SavePreferences("AUTO_CLOSE_QUOTES", isChecked ? 1 : 0);
        });
    }

    // color
    RadioGroup.OnCheckedChangeListener radioGroupColour = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            RadioButton checkedRadioButtonColour = (RadioButton) colorRadioGroup.findViewById(checkedId);
            int checkedIndexColour = colorRadioGroup.indexOfChild(checkedRadioButtonColour);
            SavePreferences(RADIO_INDEX_COLOUR, checkedIndexColour);
        }
    };
    // layout
    RadioGroup.OnCheckedChangeListener radioGroupLayout = new RadioGroup.OnCheckedChangeListener() {

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
        } else
            SavePreferences("PREVIEW", 0);
        closeKeyboard(v);
    }

    public void sound(View v) {
        CheckBox preview = findViewById(R.id.sound);
        if (preview.isChecked()) {
            SavePreferences("SOUND", 1);
        } else
            SavePreferences("SOUND", 0);
        closeKeyboard(v);
    }

    public void vibrate(View v) {
        CheckBox preview = findViewById(R.id.vibrate);
        if (preview.isChecked()) {
            SavePreferences("VIBRATE", 1);
        } else
            SavePreferences("VIBRATE", 0);
        closeKeyboard(v);
    }

    public void LoadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);

        int savedRadioColour = sharedPreferences.getInt(RADIO_INDEX_COLOUR, 0);
        if (savedRadioColour >= 0 && savedRadioColour < colorRadioGroup.getChildCount()) {
            RadioButton btn = (RadioButton) colorRadioGroup.getChildAt(savedRadioColour);
            if (btn != null) btn.setChecked(true);
        }

        int savedRadioLayout = sharedPreferences.getInt(RADIO_INDEX_LAYOUT, 0);
        if (savedRadioLayout >= 0 && savedRadioLayout < layoutRadioGroup.getChildCount()) {
            RadioButton btn = (RadioButton) layoutRadioGroup.getChildAt(savedRadioLayout);
            if (btn != null) btn.setChecked(true);
        }

        int setPreview = sharedPreferences.getInt("PREVIEW", 1);
        int setSound = sharedPreferences.getInt("SOUND", 1);
        int setVibrator = sharedPreferences.getInt("VIBRATE", 1);
        int setSize = sharedPreferences.getInt("SIZE", 1);
        int setAutoCloseBrackets = sharedPreferences.getInt("AUTO_CLOSE_BRACKETS", 0);
        int setAutoCloseQuotes = sharedPreferences.getInt("AUTO_CLOSE_QUOTES", 0);
        int setCapsLockStyle = sharedPreferences.getInt("CAPS_LOCK_STYLE", 0);

        CheckBox preview = (CheckBox) findViewById(R.id.preview);
        CheckBox sound = (CheckBox) findViewById(R.id.sound);
        CheckBox vibrate = (CheckBox) findViewById(R.id.vibrate);
        CheckBox autoCloseBrackets = (CheckBox) findViewById(R.id.autoCloseBrackets);
        CheckBox autoCloseQuotes = (CheckBox) findViewById(R.id.autoCloseQuotes);
        SeekBar size = (SeekBar) findViewById(R.id.seekBar);

        preview.setChecked(setPreview == 1);
        sound.setChecked(setSound == 1);
        vibrate.setChecked(setVibrator == 1);
        autoCloseBrackets.setChecked(setAutoCloseBrackets == 1);
        autoCloseQuotes.setChecked(setAutoCloseQuotes == 1);
        size.setProgress(setSize);

        if (setCapsLockStyle >= 0 && setCapsLockStyle < capsLockRadioGroup.getChildCount()) {
            RadioButton btn = (RadioButton) capsLockRadioGroup.getChildAt(setCapsLockStyle);
            if (btn != null) btn.setChecked(true);
        }
    }

    public void closeKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdManager.resumeBanner(this, R.id.settings_banner_ad);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AdManager.pauseBanner(this, R.id.settings_banner_ad);
    }

    @Override
    protected void onDestroy() {
        AdManager.destroyBanner(this, R.id.settings_banner_ad);
        super.onDestroy();
    }
}
