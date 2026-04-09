package com.ajay.prokeyboard;

// import android.content.ClipData;          // [CLIPBOARD - disabled]
// import android.content.ClipboardManager;  // [CLIPBOARD - disabled]
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
// import android.view.Gravity;    // [CLIPBOARD - disabled]
import android.view.KeyEvent;
import android.view.View;
// import android.view.ViewGroup;  // [CLIPBOARD - disabled]
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
// import android.widget.LinearLayout;  // [CLIPBOARD - disabled]
// import android.widget.PopupWindow;   // [CLIPBOARD - disabled]
// import android.widget.TextView;      // [CLIPBOARD - disabled]

import com.ajay.prokeyboard.keyboard.CKKeyboardView;
// import com.ajay.prokeyboard.keyboard.ClipboardHistory;  // [CLIPBOARD - disabled]
import com.ajay.prokeyboard.keyboard.KeyData;
import com.ajay.prokeyboard.keyboard.KeyboardDefinitions;

// import java.util.List;  // [CLIPBOARD - disabled]

public class inputMethodService extends InputMethodService
        implements CKKeyboardView.OnKeyListener {

    private CKKeyboardView ckKeyboardView;

    // Current state — persisted across layer/layout switches
    private int currentLayout = KeyboardDefinitions.LAYOUT_QWERTY;
    private int currentLayer = KeyboardDefinitions.LAYER_MAIN;
    private int currentSize = 1; // 0=small, 1=medium, 2=large
    private int currentColor = 0;
    // Shift state — mirrors CKKeyboardView constants
    private int  shiftState    = CKKeyboardView.SHIFT_OFF;
    private long lastShiftTapMs = 0L;
    private static final long DOUBLE_TAP_MS = 400L;

    private boolean ctrlActive = false; // one-shot: armed → next key fires Ctrl+key → released
    private boolean soundOn = true;
    private boolean vibratorOn = true;
    private boolean previewEnabled = true;
    private boolean autoCloseBrackets = false;
    private boolean autoCloseQuotes = false;
    private int capsLockStyle = 0; // 0=gboard (double-tap), 1=laptop (toggle)
    private MediaPlayer mediaPlayer; // Reusable sound player

    // Terminal / code context detection
    private boolean isTerminalContext = false;

    // [CLIPBOARD - disabled]
    // private ClipboardHistory clipboardHistory;
    // private PopupWindow clipboardPopup;

    // Known terminal package names
    private static final String[] TERMINAL_PACKAGES = {
        "com.termux", "jackpal.androidterm", "org.connectbot",
        "com.jcraft.jsch", "com.arachnoid.sshelper", "com.sonelli.juicessh",
        "de.mud.terminal", "com.server.auditor.ssh.client", "com.ericharlow.DroidTerm"
    };

    // ─────────────────────────────────────────────────────────────────────
    // IME lifecycle
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public View onCreateInputView() {
        SharedPreferences pre = getSharedPreferences("MY_SHARED_PREF", MODE_PRIVATE);

        currentLayout = pre.getInt("RADIO_INDEX_LAYOUT", KeyboardDefinitions.LAYOUT_QWERTY);
        currentSize = pre.getInt("SIZE", 1);
        currentLayer = KeyboardDefinitions.LAYER_MAIN;
        shiftState   = CKKeyboardView.SHIFT_OFF;
        ctrlActive   = false;
        soundOn = pre.getInt("SOUND", 1) == 1;
        vibratorOn = pre.getInt("VIBRATE", 1) == 1;
        previewEnabled = pre.getInt("PREVIEW", 1) == 1;
        autoCloseBrackets = pre.getInt("AUTO_CLOSE_BRACKETS", 0) == 1;
        autoCloseQuotes = pre.getInt("AUTO_CLOSE_QUOTES", 0) == 1;
        capsLockStyle = pre.getInt("CAPS_LOCK_STYLE", 0);
        currentColor = pre.getInt("RADIO_INDEX_COLOUR", 0);

        // clipboardHistory = new ClipboardHistory(pre);  // [CLIPBOARD - disabled]

        ckKeyboardView = new CKKeyboardView(this);
        ckKeyboardView.setOnKeyListener(this);
        ckKeyboardView.setPreviewEnabled(previewEnabled);

        applyTheme(currentColor);
        rebuildKeyboard();

        return ckKeyboardView;
    }

    @Override
    public void onStartInput(EditorInfo attr, boolean restarting) {
        super.onStartInput(attr, restarting);
        isTerminalContext = detectTerminalContext(attr);
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
        autoCloseBrackets = pre.getInt("AUTO_CLOSE_BRACKETS", 0) == 1;
        autoCloseQuotes = pre.getInt("AUTO_CLOSE_QUOTES", 0) == 1;
        capsLockStyle = pre.getInt("CAPS_LOCK_STYLE", 0);

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

            // ── Modifier keys — never combined with Ctrl ───────────────────
            case KeyData.CODE_SHIFT: {
                if (capsLockStyle == 1) {
                    // Laptop style: single tap toggles caps lock on/off, no one-shot
                    shiftState = (shiftState == CKKeyboardView.SHIFT_LOCKED)
                            ? CKKeyboardView.SHIFT_OFF
                            : CKKeyboardView.SHIFT_LOCKED;
                } else {
                    // Gboard style (default): off→one-shot, one-shot→off or double-tap→lock, locked→off
                    long now = SystemClock.uptimeMillis();
                    if (shiftState == CKKeyboardView.SHIFT_LOCKED) {
                        shiftState = CKKeyboardView.SHIFT_OFF;
                    } else if (shiftState == CKKeyboardView.SHIFT_ON
                            && (now - lastShiftTapMs) < DOUBLE_TAP_MS) {
                        shiftState = CKKeyboardView.SHIFT_LOCKED;
                    } else if (shiftState == CKKeyboardView.SHIFT_ON) {
                        shiftState = CKKeyboardView.SHIFT_OFF;
                    } else {
                        shiftState = CKKeyboardView.SHIFT_ON;
                    }
                    lastShiftTapMs = now;
                }
                ckKeyboardView.setShiftState(shiftState);
                break;
            }

            case KeyData.CODE_CTRL:
                ctrlActive = !ctrlActive;
                ckKeyboardView.setCtrlActive(ctrlActive);
                break;

            case KeyData.CODE_ESC:
                // Esc itself is never combined — just send it
                sendDownUpKeyEvents(KeyEvent.KEYCODE_ESCAPE);
                break;

            // ── Layer / utility keys ───────────────────────────────────────
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

            // ── Indent / Outdent ───────────────────────────────────────────
            case KeyData.CODE_INDENT:
                indentCurrentLine(ic);
                break;

            case KeyData.CODE_OUTDENT:
                outdentCurrentLine(ic);
                break;

            // ── Undo / Redo ────────────────────────────────────────────────
            case KeyData.CODE_UNDO:
                sendCtrlKey(ic, KeyEvent.KEYCODE_Z);
                break;

            case KeyData.CODE_REDO:
                sendCtrlKey(ic, KeyEvent.KEYCODE_Y);
                break;

            // ── Clipboard history ──────────────────────────────────────────
            // [CLIPBOARD - disabled]
            // case KeyData.CODE_CLIPBOARD:
            //     snapshotClipboard();
            //     showClipboardPopup();
            //     break;

            // ── Keys that work both plain and as Ctrl+key ──────────────────
            case KeyData.CODE_DELETE:
                // Ctrl+Backspace = delete word backward
                if (ctrlActive) sendCtrlKey(ic, KeyEvent.KEYCODE_DEL);
                else sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                break;

            case KeyData.CODE_DONE:
                // Ctrl+Enter used in many editors (run, submit, new line below)
                if (ctrlActive) sendCtrlKey(ic, KeyEvent.KEYCODE_ENTER);
                else if (isTerminalContext) sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
                else handleEnter(ic);
                break;

            case KeyData.CODE_LEFT:
                // Ctrl+Left = jump word left
                if (ctrlActive) sendCtrlKey(ic, KeyEvent.KEYCODE_DPAD_LEFT);
                else sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
                break;

            case KeyData.CODE_RIGHT:
                // Ctrl+Right = jump word right
                if (ctrlActive) sendCtrlKey(ic, KeyEvent.KEYCODE_DPAD_RIGHT);
                else sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
                break;

            case KeyData.CODE_UP:
                // Ctrl+Up = scroll / move line up
                if (ctrlActive) sendCtrlKey(ic, KeyEvent.KEYCODE_DPAD_UP);
                else sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP);
                break;

            case KeyData.CODE_DOWN:
                // Ctrl+Down = scroll / move line down
                if (ctrlActive) sendCtrlKey(ic, KeyEvent.KEYCODE_DPAD_DOWN);
                else sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN);
                break;

            case 9: // Tab
                if (ctrlActive) sendCtrlKey(ic, KeyEvent.KEYCODE_TAB);
                else sendDownUpKeyEvents(KeyEvent.KEYCODE_TAB);
                break;

            // ── Regular character keys ─────────────────────────────────────
            default:
                if (code < 0 || code > 0x10FFFF) break;

                if (ctrlActive) {
                    int keyCode = charToKeyCode(code);
                    if (keyCode != -1) {
                        sendCtrlKey(ic, keyCode);
                    } else {
                        // No mapping for this symbol — just cancel Ctrl
                        ctrlActive = false;
                        ckKeyboardView.setCtrlActive(false);
                    }
                } else {
                    char ch = (char) code;
                    if (shiftState != CKKeyboardView.SHIFT_OFF && Character.isLetter(ch)) {
                        ch = Character.toUpperCase(ch);
                        // One-shot shift: revert after a single letter
                        if (shiftState == CKKeyboardView.SHIFT_ON) {
                            shiftState = CKKeyboardView.SHIFT_OFF;
                            ckKeyboardView.setShiftState(shiftState);
                        }
                    }
                    // In terminal context: suppress auto-pair entirely
                    if (isTerminalContext) {
                        ic.commitText(String.valueOf(ch), 1);
                        break;
                    }
                    // Auto-close on overtype: skip over the closing char if already there
                    if (shouldOvertype(ch)) {
                        CharSequence next = ic.getTextAfterCursor(1, 0);
                        if (next != null && next.length() > 0 && next.charAt(0) == ch) {
                            sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
                            break;
                        }
                    }
                    // Auto-close brackets/quotes: insert pair, cursor placed inside
                    char closing = getAutoPair(ch);
                    if (closing != 0) {
                        ic.commitText(String.valueOf(ch) + closing, 1);
                        // commitText always places cursor after all committed text,
                        // so move left once to land between the pair
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
                    } else {
                        ic.commitText(String.valueOf(ch), 1);
                    }
                }
                break;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Indent / Outdent
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Indents the current line by inserting 4 spaces at the start of the line,
     * then restores the cursor to its original position + 4.
     */
    private void indentCurrentLine(InputConnection ic) {
        CharSequence before = ic.getTextBeforeCursor(2000, 0);
        if (before == null) return;
        int charsBeforeCursor = before.length();
        int lineStart = lastIndexOf(before, '\n') + 1; // 0 if no newline found
        // Move cursor to line start
        int stepsBack = charsBeforeCursor - lineStart;
        if (stepsBack > 0) {
            ic.setSelection(lineStart, lineStart); // not reliable in all editors
            // Fallback: use key events
            for (int i = 0; i < stepsBack; i++) sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
        }
        ic.commitText("    ", 1);
        // Restore cursor: move right stepsBack positions (we inserted 4 chars before)
        for (int i = 0; i < stepsBack; i++) sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
    }

    /**
     * Outdents the current line by removing up to 4 leading spaces from the line start.
     * Cursor is repositioned correctly.
     */
    private void outdentCurrentLine(InputConnection ic) {
        CharSequence before = ic.getTextBeforeCursor(2000, 0);
        if (before == null) return;
        int charsBeforeCursor = before.length();
        int lineStart = lastIndexOf(before, '\n') + 1;

        // Read up to 4 chars at line start to count removable spaces
        CharSequence lineHead = ic.getTextAfterCursor(4 + (charsBeforeCursor - lineStart), 0);
        // We need to read from the line start position, so combine before+after approach
        // Simpler: read current line from 'before'
        String currentLine = before.subSequence(lineStart, charsBeforeCursor).toString();
        int spacesToRemove = 0;
        for (int i = 0; i < currentLine.length() && i < 4; i++) {
            if (currentLine.charAt(i) == ' ') spacesToRemove++;
            else break;
        }
        if (spacesToRemove == 0) return;

        int stepsBack = charsBeforeCursor - lineStart;
        // Move cursor to line start
        for (int i = 0; i < stepsBack; i++) sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
        // Delete the leading spaces
        for (int i = 0; i < spacesToRemove; i++) sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
        // Restore cursor (move back right, but only as far as original position allows)
        int restoreSteps = Math.max(0, stepsBack - spacesToRemove);
        for (int i = 0; i < restoreSteps; i++) sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
    }

    private static int lastIndexOf(CharSequence cs, char c) {
        for (int i = cs.length() - 1; i >= 0; i--) {
            if (cs.charAt(i) == c) return i;
        }
        return -1;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Clipboard history popup  [CLIPBOARD - disabled]
    // ─────────────────────────────────────────────────────────────────────

    // private void snapshotClipboard() {
    //     try {
    //         ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    //         if (cm != null && cm.hasPrimaryClip()) {
    //             ClipData clip = cm.getPrimaryClip();
    //             if (clip != null && clip.getItemCount() > 0) {
    //                 CharSequence text = clip.getItemAt(0).coerceToText(this);
    //                 if (text != null && text.length() > 0) {
    //                     clipboardHistory.addEntry(text.toString());
    //                 }
    //             }
    //         }
    //     } catch (Exception ignored) {}
    // }

    // private void showClipboardPopup() {
    //     if (ckKeyboardView == null) return;
    //     dismissClipboardPopup();
    //     LinearLayout root = new LinearLayout(this);
    //     root.setOrientation(LinearLayout.VERTICAL);
    //     root.setBackgroundColor(0xCC1A1A2E);
    //     LinearLayout header = new LinearLayout(this);
    //     header.setOrientation(LinearLayout.HORIZONTAL);
    //     header.setBackgroundColor(0xFF0D1B2A);
    //     header.setPadding(dp(12), 0, dp(4), 0);
    //     header.setGravity(Gravity.CENTER_VERTICAL);
    //     header.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(40)));
    //     TextView title = new TextView(this);
    //     title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
    //     title.setText("Clipboard");
    //     title.setTextColor(0xFFFFFFFF);
    //     title.setTextSize(14f);
    //     TextView closeBtn = new TextView(this);
    //     closeBtn.setLayoutParams(new LinearLayout.LayoutParams(dp(40), dp(40)));
    //     closeBtn.setText("\u2715");
    //     closeBtn.setTextColor(0xFFFFFFFF);
    //     closeBtn.setTextSize(18f);
    //     closeBtn.setGravity(Gravity.CENTER);
    //     closeBtn.setOnClickListener(v -> dismissClipboardPopup());
    //     header.addView(title);
    //     header.addView(closeBtn);
    //     android.widget.ScrollView scroll = new android.widget.ScrollView(this);
    //     LinearLayout container = new LinearLayout(this);
    //     container.setOrientation(LinearLayout.VERTICAL);
    //     container.setPadding(dp(4), dp(4), dp(4), dp(4));
    //     scroll.addView(container);
    //     List<ClipboardHistory.Entry> entries = clipboardHistory.getEntries();
    //     if (entries.isEmpty()) {
    //         TextView empty = new TextView(this);
    //         empty.setText("No clipboard history yet.");
    //         empty.setTextColor(0xFFAAAAAA);
    //         empty.setPadding(dp(12), dp(12), dp(12), dp(12));
    //         container.addView(empty);
    //     } else {
    //         for (int i = 0; i < entries.size(); i++) {
    //             container.addView(buildEntryRow(entries.get(i), i));
    //         }
    //     }
    //     root.addView(header);
    //     root.addView(scroll);
    //     clipboardPopup = new PopupWindow(root,
    //             ViewGroup.LayoutParams.MATCH_PARENT,
    //             ViewGroup.LayoutParams.WRAP_CONTENT, true);
    //     clipboardPopup.setElevation(8f);
    //     clipboardPopup.showAtLocation(ckKeyboardView, Gravity.BOTTOM, 0, ckKeyboardView.getHeight());
    // }

    // private int dp(int dp) {
    //     return Math.round(dp * getResources().getDisplayMetrics().density);
    // }

    // private View buildEntryRow(ClipboardHistory.Entry entry, int index) {
    //     LinearLayout row = new LinearLayout(this);
    //     row.setOrientation(LinearLayout.HORIZONTAL);
    //     row.setPadding(8, 8, 8, 8);
    //     row.setBackgroundColor(index % 2 == 0 ? 0xFF1A1A2E : 0xFF16213E);
    //     TextView textView = new TextView(this);
    //     textView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
    //     textView.setText(entry.text);
    //     textView.setTextColor(0xFFEEEEEE);
    //     textView.setTextSize(13f);
    //     textView.setMaxLines(2);
    //     textView.setEllipsize(android.text.TextUtils.TruncateAt.END);
    //     textView.setPadding(4, 4, 4, 4);
    //     TextView pinBtn = new TextView(this);
    //     pinBtn.setText(entry.pinned ? "\uD83D\uDCCC" : "\uD83D\uDCCD");
    //     pinBtn.setTextSize(16f);
    //     pinBtn.setPadding(12, 4, 12, 4);
    //     textView.setOnClickListener(v -> {
    //         InputConnection ic = getCurrentInputConnection();
    //         if (ic != null) ic.commitText(entry.text, 1);
    //         dismissClipboardPopup();
    //     });
    //     pinBtn.setOnClickListener(v -> {
    //         clipboardHistory.togglePin(index);
    //         dismissClipboardPopup();
    //         showClipboardPopup();
    //     });
    //     row.addView(textView);
    //     row.addView(pinBtn);
    //     return row;
    // }

    // private void dismissClipboardPopup() {
    //     if (clipboardPopup != null && clipboardPopup.isShowing()) {
    //         clipboardPopup.dismiss();
    //     }
    //     clipboardPopup = null;
    // }

    // ─────────────────────────────────────────────────────────────────────
    // Terminal context detection
    // ─────────────────────────────────────────────────────────────────────

    private boolean detectTerminalContext(EditorInfo attr) {
        if (attr == null) return false;

        // Known terminal package names
        String pkg = attr.packageName;
        if (pkg != null) {
            for (String terminal : TERMINAL_PACKAGES) {
                if (pkg.equals(terminal) || pkg.startsWith(terminal)) return true;
            }
        }

        // inputType == 0 → app opted out of IME suggestions (common in terminals)
        int inputType = attr.inputType;
        if (inputType == 0) return true;

        // Visible password variation — used by some terminal prompts
        int variation = inputType & android.text.InputType.TYPE_MASK_VARIATION;
        if (variation == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) return true;

        return false;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Enter with auto-indent
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Handle Enter: commit newline + current line's indentation.
     * If the line ends with {, (, or :, add one extra indent level (4 spaces).
     */
    private void handleEnter(InputConnection ic) {
        CharSequence before = ic.getTextBeforeCursor(500, 0);
        String indent = "";
        if (before != null) {
            String text = before.toString();
            int lineStart = text.lastIndexOf('\n') + 1;
            String currentLine = text.substring(lineStart);
            // Extract leading whitespace
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < currentLine.length(); i++) {
                char c = currentLine.charAt(i);
                if (c == ' ' || c == '\t') sb.append(c);
                else break;
            }
            indent = sb.toString();
            // Find last non-whitespace char
            int end = currentLine.length() - 1;
            while (end >= 0 && (currentLine.charAt(end) == ' ' || currentLine.charAt(end) == '\t')) end--;
            if (end >= 0) {
                char last = currentLine.charAt(end);
                if (last == '{' || last == '(' || last == ':') {
                    indent += "    ";
                }
            }
        }
        ic.commitText("\n" + indent, 1);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Auto-pair helpers
    // ─────────────────────────────────────────────────────────────────────

    /** Returns the auto-pair closing character if the setting is enabled, else 0. */
    private char getAutoPair(char ch) {
        if (autoCloseBrackets) {
            switch (ch) {
                case '(': return ')';
                case '[': return ']';
                case '{': return '}';
            }
        }
        if (autoCloseQuotes && (ch == '"' || ch == '\'')) {
            return ch;
        }
        return 0;
    }

    /** Returns true if typing this char should skip over it when it already follows the cursor. */
    private boolean shouldOvertype(char ch) {
        switch (ch) {
            case ')': case ']': case '}': return autoCloseBrackets;
            case '"': case '\'': return autoCloseQuotes;
            default: return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Ctrl key helper
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Send a Ctrl+keyCode key event pair with correct timestamps, then
     * deactivate the one-shot Ctrl modifier.
     */
    private void sendCtrlKey(InputConnection ic, int keyCode) {
        long now = SystemClock.uptimeMillis();
        int meta = KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, meta));
        ic.sendKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP,   keyCode, 0, meta));
        ctrlActive = false;
        ckKeyboardView.setCtrlActive(false);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Swipe / drag / long-press handlers
    // ─────────────────────────────────────────────────────────────────────

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

    @Override
    public void onSpacebarCursorDrag(int direction) {
        sendDownUpKeyEvents(direction > 0
                ? KeyEvent.KEYCODE_DPAD_RIGHT
                : KeyEvent.KEYCODE_DPAD_LEFT);
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
        ckKeyboardView.setShiftState(shiftState);
    }

    /**
     * Map the six colour-theme indices to (bg, keyBg, keyPress, text) ARGB values.
     * Colours are taken directly from res/values/colors.xml.
     */
    private void applyTheme(int colorIndex) {
        if (ckKeyboardView == null)
            return;
        switch (colorIndex) {
            case 1: // Grey
                ckKeyboardView.setTheme(0xFFB3B6B7, 0xFFFFFFFF, 0xFFD0D3D4, 0xFF000000);
                break;
            case 2: // Navy
                ckKeyboardView.setTheme(0xFF0D1B2A, 0xFF1B2A3B, 0xFF253545, 0xFFFFFFFF);
                break;
            case 3: // Hacker Green
                ckKeyboardView.setTheme(0xFF0A0A0A, 0xFF1A1A1A, 0xFF004400, 0xFF00FF41);
                break;
            case 4: // AMOLED Black
                ckKeyboardView.setTheme(0xFF000000, 0xFF111111, 0xFF222222, 0xFFFFFFFF);
                break;
            default: // Dark (0)
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
        // dismissClipboardPopup();  // [CLIPBOARD - disabled]
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

    /**
     * Maps a Unicode character code to the matching Android KeyEvent keycode
     * so Ctrl+key events can be dispatched correctly.
     * Returns -1 if there is no known mapping.
     */
    private static int charToKeyCode(int code) {
        // a-z / A-Z
        if (code >= 'a' && code <= 'z') return KeyEvent.KEYCODE_A + (code - 'a');
        if (code >= 'A' && code <= 'Z') return KeyEvent.KEYCODE_A + (code - 'A');
        // 0-9
        if (code >= '0' && code <= '9') return KeyEvent.KEYCODE_0 + (code - '0');
        // Symbol keys present on this keyboard
        switch (code) {
            case 32: return KeyEvent.KEYCODE_SPACE;          // Ctrl+Space (autocomplete)
            case 39: return KeyEvent.KEYCODE_APOSTROPHE;     // Ctrl+'
            case 44: return KeyEvent.KEYCODE_COMMA;          // Ctrl+,
            case 45: return KeyEvent.KEYCODE_MINUS;          // Ctrl+-    (zoom out / fold)
            case 46: return KeyEvent.KEYCODE_PERIOD;         // Ctrl+.
            case 47: return KeyEvent.KEYCODE_SLASH;          // Ctrl+/    (toggle comment)
            case 59: return KeyEvent.KEYCODE_SEMICOLON;      // Ctrl+;
            case 61: return KeyEvent.KEYCODE_EQUALS;         // Ctrl+=    (zoom in)
            case 91: return KeyEvent.KEYCODE_LEFT_BRACKET;   // Ctrl+[    (dedent)
            case 92: return KeyEvent.KEYCODE_BACKSLASH;      // Ctrl+\
            case 93: return KeyEvent.KEYCODE_RIGHT_BRACKET;  // Ctrl+]    (indent)
            case 96: return KeyEvent.KEYCODE_GRAVE;          // Ctrl+`    (open terminal)
            default: return -1;
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
