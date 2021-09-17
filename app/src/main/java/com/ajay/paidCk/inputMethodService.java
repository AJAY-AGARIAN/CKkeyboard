package com.ajay.paidCk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import java.util.Timer;
import java.util.TimerTask;

public class inputMethodService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
    private KeyboardView keyboardView;
    private Keyboard keyboard;
    private boolean vibratorOn;
    private boolean soundOn;
    private boolean caps = false;
    private Timer LongPressTimer = null;

    @Override
    public View onCreateInputView() {

        SharedPreferences pre = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        int layout=pre.getInt("RADIO_INDEX_LAYOUT",0);
        //color
        switch (pre.getInt("RADIO_INDEX_COLOUR", 0)) {
            case 0:
                keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboardview, null);
                break;
            case 1:
                keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.whitekeyboardview, null);
                break;
            case 2:
                keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.bluekeyboardview, null);
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Dialog window=this.getWindow();
                    window.setNavigationBarColor(Color.parseColor("#20111111"));
                } */
                break;
            case 3:
                keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.redkeyboardview, null);
                break;
            case 4:
                keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.greenkeyboardview, null);
                break;
            case 5:
                keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.yellowkeyboardview, null);
                break;
            default:
                keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboardview, null);
        }
        //size
        int size = pre.getInt("SIZE", 1);
        //if (primaryCode==-1||primaryCode==-5||primaryCode==-4||primaryCode==9)
        //keyboardView.setBackgroundResource(R.drawable.spacebtn);
        if(layout==0) {
            if (size == 0) {
                keyboard = new Keyboard(this, R.xml.keyslayout);
            } else if (size == 1) {
                keyboard = new Keyboard(this, R.xml.keyboardmedium);
            } else if (size == 2) {
                keyboard = new Keyboard(this, R.xml.keyboardlarge);
            } else keyboard = new Keyboard(this, R.xml.keyboardmedium);
        }else if(layout==1){
            if (size == 0) {
                keyboard = new Keyboard(this, R.xml.dvoraksmall);
            } else if (size == 1) {
                keyboard = new Keyboard(this, R.xml.dvorakmedium);
            } else if (size == 2) {
                keyboard = new Keyboard(this, R.xml.dvoraklarge);
            }
        } else if(layout==2){
            if (size == 0) {
                keyboard = new Keyboard(this, R.xml.azertysmall);
            } else if (size == 1) {
                keyboard = new Keyboard(this, R.xml.azertymedium);
            } else if (size == 2) {
                keyboard = new Keyboard(this, R.xml.azertylarge);
            }
        } else if(layout==3){
            if (size == 0) {
                keyboard = new Keyboard(this, R.xml.qwertzsmall);
            } else if (size == 1) {
                keyboard = new Keyboard(this, R.xml.qwertzmedium);
            } else if (size == 2) {
                keyboard = new Keyboard(this, R.xml.qwertzlarge);
            }
        }
        if (pre.getInt("PREVIEW", 1) == 1) {
            keyboardView.setPreviewEnabled(true);
        } else keyboardView.setPreviewEnabled(false);

        if (pre.getInt("SOUND", 1) == 1) {
            soundOn = true;
        } else soundOn = false;

        if (pre.getInt("VIBRATE", 1) == 1) {
            vibratorOn = true;
        } else vibratorOn = false;

        keyboardView.setKeyboard(keyboard);
        keyboardView.setOnKeyboardActionListener(this);

        return keyboardView;

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        SharedPreferences pre = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        int size = pre.getInt("SIZE", 1);
        int layout=pre.getInt("RADIO_INDEX_LAYOUT",0);

        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection != null) {
            switch(primaryCode) {
                case Keyboard.KEYCODE_DELETE:
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                    break;
                case Keyboard.KEYCODE_SHIFT:
                    caps = !caps;
                    keyboard.setShifted(caps);
                    keyboardView.invalidateAllKeys();
                    break;
                case Keyboard.KEYCODE_DONE:
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
                    break;
                case 5005:
                    /*Intent intent2=new Intent(this,settings.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent2);*/
                    if (keyboard!= null) {
                        if(layout==0) {
                            if (size == 0) {
                                keyboard = new Keyboard(this, R.xml.arrows);
                            } else if (size == 1) {
                                keyboard = new Keyboard(this, R.xml.arrowsmedium);
                            } else if (size == 2) {
                                keyboard = new Keyboard(this, R.xml.arrowslarge);
                            }
                        }
                        else if(layout==1){
                            if (size == 0) {
                                keyboard = new Keyboard(this, R.xml.dvorakarrowsmall);
                            } else if (size == 1) {
                                keyboard = new Keyboard(this, R.xml.dvorakarrowmedium);
                            } else if (size == 2) {
                                keyboard = new Keyboard(this, R.xml.dvoraklarge);
                            }
                        }
                        else if(layout==2){
                            if (size == 0) {
                                keyboard = new Keyboard(this, R.xml.azertyarrowsmall);
                            } else if (size == 1) {
                                keyboard = new Keyboard(this, R.xml.azertyarrowmedium);
                            } else if (size == 2) {
                                keyboard = new Keyboard(this, R.xml.azertyarrowlarge);
                            }
                        }else if(layout==3){
                            if (size == 0) {
                                keyboard = new Keyboard(this, R.xml.qwertzarrowsmall);
                            } else if (size == 1) {
                                keyboard = new Keyboard(this, R.xml.qwertzarrowmedium);
                            } else if (size == 2) {
                                keyboard = new Keyboard(this, R.xml.qwertzarrowlarge);
                            }
                        }

                        keyboardView.setKeyboard(keyboard);
                    }
                    break;
                case 5004:
                    if (keyboard != null) {
                        if(layout==0) {
                            if (size == 0) {
                                keyboard = new Keyboard(this, R.xml.keyslayout);
                            } else if (size == 1) {
                                keyboard = new Keyboard(this, R.xml.keyboardmedium);
                            } else if (size == 2) {
                                keyboard = new Keyboard(this, R.xml.keyboardlarge);
                            }
                        }
                        else  if(layout==1){
                            if (size == 0) {
                                keyboard = new Keyboard(this, R.xml.dvoraksmall);
                            } else if (size == 1) {
                                keyboard = new Keyboard(this, R.xml.dvorakmedium);
                            } else if (size == 2) {
                                keyboard = new Keyboard(this, R.xml.dvoraklarge);
                            }
                        }
                        else  if(layout==2){
                            if (size == 0) {
                                keyboard = new Keyboard(this, R.xml.azertysmall);
                            } else if (size == 1) {
                                keyboard = new Keyboard(this, R.xml.azertymedium);
                            } else if (size == 2) {
                                keyboard = new Keyboard(this, R.xml.azertylarge);
                            }
                        } else if(layout==3){
                            if (size == 0) {
                                keyboard = new Keyboard(this, R.xml.qwertzsmall);
                            } else if (size == 1) {
                                keyboard = new Keyboard(this, R.xml.qwertzmedium);
                            } else if (size == 2) {
                                keyboard = new Keyboard(this, R.xml.qwertzlarge);
                            }
                        }
                        keyboardView.setKeyboard(keyboard);
                    }
                    break;
                case 5006:
                    Intent intent2=new Intent(this,settings.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //keyboardView.closing();
                    startActivity(intent2);
                    break;
                case 5000:
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
                    break;
                case 5001:
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN);
                    break;
                case 5002:
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP);
                    break;
                case 5003:
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
                    break;
                default :
                    char code = (char) primaryCode;
                    if(Character.isLetter(code) && caps){
                       code = Character.toUpperCase(code);
                    }
                    inputConnection.commitText(String.valueOf(code), 1);
            }
        }
    }

    @Override
    public void onPress(final int primaryCode) {
        SharedPreferences pre = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);

        if (soundOn) {
            MediaPlayer keypressSoundPlayer = MediaPlayer.create(this, R.raw.keypress_sound);
            keypressSoundPlayer.start();
            keypressSoundPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
        }

        if (vibratorOn) {

            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if(vibrator!=null)
                vibrator.vibrate(10);
        }
            if (primaryCode == -1 || primaryCode == -5 || primaryCode == -4 || primaryCode == 9 || primaryCode == 32 || primaryCode == 5001
                    || primaryCode == 5000 || primaryCode == 5002 || primaryCode == 5003 || primaryCode == 5004 || primaryCode == 5005 || primaryCode == 5006) {
                keyboardView.setPreviewEnabled(false);
            } else if (pre.getInt("PREVIEW", 1) == 1) {
                keyboardView.setPreviewEnabled(true); }
        if (LongPressTimer != null)
            LongPressTimer.cancel();

        LongPressTimer = new Timer();
        LongPressTimer.schedule(new TimerTask() {

            @Override
            public void run() {

                try {

                    Handler uiHandler = new Handler(Looper.getMainLooper());

                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {

                            try {

                                inputMethodService.this.onKeyLongPress(primaryCode);

                            } catch (Exception e) {
                                Log.e(inputMethodService.class.getSimpleName(), "uiHandler.run: " + e.getMessage(), e);
                            }

                        }
                    };

                    uiHandler.post(runnable);

                } catch (Exception e) {
                    Log.e(inputMethodService.class.getSimpleName(), "Timer.run: " + e.getMessage(), e);
                }
            }

        }, ViewConfiguration.getLongPressTimeout());

    }

    @Override
    public void onRelease(int primaryCode) {
        if (LongPressTimer != null)
             LongPressTimer.cancel();

    }

    private void onKeyLongPress(int keyCode) {
        if(keyCode == 32) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm!=null)
                imm.showInputMethodPicker();
        }
    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {
        SharedPreferences pre = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        int color=pre.getInt("RADIO_INDEX_COLOUR", 0);
        int layout=pre.getInt("RADIO_INDEX_LAYOUT",0);
        if (keyboard != null) {
            if(layout==0) {
                keyboard = new Keyboard(this, R.xml.quertyonly);
                keyboardView.setKeyboard(keyboard);
            }
            else if (layout==1){
                keyboard = new Keyboard(this, R.xml.dvorakonly);
                keyboardView.setKeyboard(keyboard);
            }
            else if (layout==2){
                keyboard = new Keyboard(this, R.xml.azertyonly);
                keyboardView.setKeyboard(keyboard);
            }
             else if (layout==3){
                 keyboard = new Keyboard(this, R.xml.qwertzonly);
                 keyboardView.setKeyboard(keyboard);
             }
        }
        keyboardView.setKeyboard(keyboard);
    }
    @Override
    public void swipeUp() {

        SharedPreferences pre = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);
        int size = pre.getInt("SIZE", 1);
        int color=pre.getInt("RADIO_INDEX_COLOUR", 0);
        int layout=pre.getInt("RADIO_INDEX_LAYOUT",0);

        if (keyboard != null) {
            if(layout==0) {
                if (size == 0) {
                    keyboard = new Keyboard(this, R.xml.keyslayout);
                } else if (size == 1) {
                    keyboard = new Keyboard(this, R.xml.keyboardmedium);
                } else if (size == 2) {
                    keyboard = new Keyboard(this, R.xml.keyboardlarge);
                } else {
                    keyboard = new Keyboard(this, R.xml.keyboardmedium);
                }
            }
            else if(layout==1)
            {
                if (size == 0) {
                    keyboard = new Keyboard(this, R.xml.dvoraksmall);
                } else if (size == 1) {
                    keyboard = new Keyboard(this, R.xml.dvorakmedium);
                } else if (size == 2) {
                    keyboard = new Keyboard(this, R.xml.dvoraklarge);
                }
            }
            else if(layout==2)
            {
                if (size == 0) {
                    keyboard = new Keyboard(this, R.xml.azertysmall);
                } else if (size == 1) {
                    keyboard = new Keyboard(this, R.xml.azertymedium);
                } else if (size == 2) {
                    keyboard = new Keyboard(this, R.xml.azertylarge);
                }
            }else if(layout==3){
                if (size == 0) {
                    keyboard = new Keyboard(this, R.xml.qwertzsmall);
                } else if (size == 1) {
                    keyboard = new Keyboard(this, R.xml.qwertzmedium);
                } else if (size == 2) {
                    keyboard = new Keyboard(this, R.xml.qwertzlarge);
                }
            }

            keyboardView.setKeyboard(keyboard);
        }
    }
}
