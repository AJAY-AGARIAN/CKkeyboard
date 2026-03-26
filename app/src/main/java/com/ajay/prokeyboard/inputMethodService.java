package com.ajay.prokeyboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.ajay.prokeyboard.keyboard.CKKeyboardView;
import com.ajay.prokeyboard.keyboard.KeyData;
import com.ajay.prokeyboard.keyboard.KeyboardDefinitions;

public class inputMethodService extends InputMethodService
        implements CKKeyboardView.OnKeyListener {

    private CKKeyboardView ckKeyboardView;

    // Current state — persisted across layer/layout switches
    private int currentLayout = KeyboardDefinitions.LAYOUT_QWERTY;
    private int currentLayer = KeyboardDefinitions.LAYER_MAIN;
    private int currentSize = 1; // 0=small, 1=medium, 2=large
    private int currentColor = 0;
    private boolean capsLock = false;
    private boolean soundOn = true;
    private boolean vibratorOn = true;
    private boolean previewEnabled = true;
    private MediaPlayer mediaPlayer; // Reusable sound player

    // ─────────────────────────────────────────────────────────────────────
    // IME lifecycle
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public View onCreateInputView() {
        SharedPreferences pre = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);

        currentLayout = pre.getInt("RADIO_INDEX_LAYOUT", KeyboardDefinitions.LAYOUT_QWERTY);
        currentSize = pre.getInt("SIZE", 1);
        currentLayer = KeyboardDefinitions.LAYER_MAIN;
        capsLock = false;
        soundOn = pre.getInt("SOUND", 1) == 1;
        vibratorOn = pre.getInt("VIBRATE", 1) == 1;
        previewEnabled = pre.getInt("PREVIEW", 1) == 1;
        currentColor = pre.getInt("RADIO_INDEX_COLOUR", 0);

        ckKeyboardView = new CKKeyboardView(this);
        ckKeyboardView.setOnKeyListener(this);
        ckKeyboardView.setPreviewEnabled(previewEnabled);

        applyTheme(currentColor);
        rebuildKeyboard();

        return ckKeyboardView;
    }

    @Override
    public void onWindowShown() {
        super.onWindowShown();
        reloadSettings();
    }

    private void reloadSettings() {
        SharedPreferences pre = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);

        int newLayout = pre.getInt("RADIO_INDEX_LAYOUT", KeyboardDefinitions.LAYOUT_QWERTY);
        int newSize = pre.getInt("SIZE", 1);
        boolean newSound = pre.getInt("SOUND", 1) == 1;
        boolean newVibrate = pre.getInt("VIBRATE", 1) == 1;
        boolean newPreview = pre.getInt("PREVIEW", 1) == 1;
        int newColor = pre.getInt("RADIO_INDEX_COLOUR", 0);

        // Check if keyboard layout or size changed
        boolean layoutChanged = (newLayout != currentLayout);
        boolean sizeChanged = (newSize != currentSize);

        // Check if preview setting changed
        boolean previewChanged = (newPreview != previewEnabled);

        // Check if theme changed
        boolean colorChanged = (newColor != currentColor);

        // Update state
        currentLayout = newLayout;
        currentSize = newSize;
        soundOn = newSound;
        vibratorOn = newVibrate;
        previewEnabled = newPreview;
        currentColor = newColor;

        // Rebuild keyboard if layout or size changed
        if (ckKeyboardView != null) {
            if (layoutChanged || sizeChanged) {
                rebuildKeyboard();
            }

            // Update preview setting
            if (previewChanged) {
                ckKeyboardView.setPreviewEnabled(previewEnabled);
            }

            // Apply new theme if color changed
            if (colorChanged) {
                applyTheme(currentColor);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // CKKeyboardView.OnKeyListener
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public void onKeyDown(int code) {
        try {
            playSound();
        } catch (Exception e) {
            // Silently ignore sound errors
        }
        try {
            vibrate();
        } catch (Exception e) {
            // Silently ignore vibration errors
        }
    }

    @Override
    public void onKeyUp(int code) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;

        switch (code) {

            case KeyData.CODE_DELETE:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                break;

            case KeyData.CODE_DONE:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
                break;

            case KeyData.CODE_SHIFT:
                capsLock = !capsLock;
                ckKeyboardView.setCapsLock(capsLock);
                break;

            case KeyData.CODE_SWITCH_ARROWS:
                currentLayer = KeyboardDefinitions.LAYER_ARROWS;
                rebuildKeyboard();
                break;

            case KeyData.CODE_SWITCH_MAIN:
                currentLayer = KeyboardDefinitions.LAYER_MAIN;
                rebuildKeyboard();
                break;

            case KeyData.CODE_SETTINGS:
                Intent intent = new Intent(this, settings.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;

            case KeyData.CODE_LEFT:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
                break;

            case KeyData.CODE_RIGHT:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
                break;

            case KeyData.CODE_UP:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP);
                break;

            case KeyData.CODE_DOWN:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN);
                break;

            default:
                // Validate that code is a valid character (not a special control code)
                if (code >= 0 && code <= 0x10FFFF) {
                    char ch = (char) code;
                    if (capsLock && Character.isLetter(ch)) {
                        ch = Character.toUpperCase(ch);
                    }
                    ic.commitText(String.valueOf(ch), 1);
                }
                break;
        }
    }

    @Override
    public void onKeyLongPress(int code) {
        // Long-press on space → show IME picker (matches original behaviour)
        if (code == 32) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.showInputMethodPicker();
        }
    }

    @Override
    public void onSwipeLeft() {
        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
    }

    @Override
    public void onSwipeRight() {
        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
    }

    @Override
    public void onSwipeDown() {
        // Compact mode: hide symbol rows, show only letters
        try {
            currentLayer = KeyboardDefinitions.LAYER_LETTERS_ONLY;
            rebuildKeyboard();
        } catch (Exception e) {
            // Fallback to main layer on error
            currentLayer = KeyboardDefinitions.LAYER_MAIN;
        }
    }

    @Override
    public void onSwipeUp() {
        // Restore full keyboard
        try {
            currentLayer = KeyboardDefinitions.LAYER_MAIN;
            rebuildKeyboard();
        } catch (Exception e) {
            // Log error if needed
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────

    /** Rebuild the keyboard after any layout, layer or size change. */
    private void rebuildKeyboard() {
        if (ckKeyboardView == null)
            return;
        ckKeyboardView.setKeyboardLayout(
                KeyboardDefinitions.build(currentLayout, currentLayer, currentSize));
        ckKeyboardView.setCapsLock(capsLock);
    }

    /**
     * Map the six colour-theme indices to (bg, keyBg, keyPress, text) ARGB values.
     * Colours are taken directly from res/values/colors.xml.
     */
    private void applyTheme(int colorIndex) {
        if (ckKeyboardView == null)
            return;
        switch (colorIndex) {
            case 1: // White
                ckKeyboardView.setTheme(0xFFB3B6B7, 0xFFFFFFFF, 0xFFD0D3D4, 0xFF000000);
                break;
            case 2: // Blue
                ckKeyboardView.setTheme(0xFF2E86C1, 0xFF3498DB, 0xFF1A5276, 0xFFFFFFFF);
                break;
            case 3: // Red
                ckKeyboardView.setTheme(0xFFE74C3C, 0xFFEC7063, 0xFFC0392B, 0xFFFFFFFF);
                break;
            case 4: // Green
                ckKeyboardView.setTheme(0xFF27AE60, 0xFF52BE80, 0xFF1E8449, 0xFFFFFFFF);
                break;
            case 5: // Yellow
                ckKeyboardView.setTheme(0xFFF1C40F, 0xFFF4D03F, 0xFFD4AC0D, 0xFF000000);
                break;
            default: // Dark (0) — default
                ckKeyboardView.setTheme(0xFF17202A, 0xFF283747, 0xFF34495E, 0xFFFFFFFF);
                break;
        }
    }

    private void playSound() {
        if (!soundOn)
            return;
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.keypress_sound);
            }
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            // Handle error silently to avoid crashing
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Release mediaPlayer resources
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                // Silently ignore release errors
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void vibrate() {
        if (!vibratorOn)
            return;
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null)
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(10);
        }
    }
}
