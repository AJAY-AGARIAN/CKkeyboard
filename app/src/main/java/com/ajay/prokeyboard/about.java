package com.ajay.prokeyboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class about extends AppCompatActivity
{
    private ImageView adImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        AdManager.init(this);
        AdManager.loadBanner(this, R.id.about_banner_ad);

        Button attribution = findViewById(R.id.attribution);
        attribution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(about.this, attribution.class));
            }
        });
        Button adfree = findViewById(R.id.adfree);
        adfree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.ajay.paidCk"));
                startActivity(intent);
            }
        });
        adImg = findViewById(R.id.ad);
        if (adImg != null) adImg.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.atmegame.com/?utm_source=AjayApps&utm_medium=AjayApps"));
            startActivity(intent);
        });
        Button rate = findViewById(R.id.rate);
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID));
                startActivity(intent);
            }
        });
        Button feedback = findViewById(R.id.feedback);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Email = new Intent(Intent.ACTION_SEND);
                Email.setType("text/email");
                Email.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.feedback_email) });
                Email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                Email.putExtra(Intent.EXTRA_TEXT, "Dear Developer,");
                startActivity(Intent.createChooser(Email, "Send Feedback over E-mail:"));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdManager.resumeBanner(this, R.id.about_banner_ad);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AdManager.pauseBanner(this, R.id.about_banner_ad);
    }

    @Override
    protected void onDestroy() {
        AdManager.destroyBanner(this, R.id.about_banner_ad);
        super.onDestroy();
    }
}
