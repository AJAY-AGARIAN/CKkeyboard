package com.ajay.prokeyboard;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class dialogBox extends Dialog {

    public Activity c;
    public Dialog d;
    public Button continueBtn;

    public dialogBox(Activity a) {
        super(a);
        this.c = a;
    }

    // Default constructor required by Android system
    public dialogBox() {
        super(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_box);
        continueBtn = findViewById(R.id.continueBtn);
        continueBtn.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);
            c.startActivity(intent);
            dismiss();
        });
    }
}