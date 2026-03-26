package com.ajay.prokeyboard;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MainActivity extends AppCompatActivity {

    private AdView mAdView;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private LinearLayout linearLayout;
    private TextView[] mdots;
    private Button back;
    private Button next;
    private Button sysbtn;
    private int currentPage;
    private EditText texton;
    private LinearLayout infolayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        back = findViewById(R.id.back);
        next = findViewById(R.id.next);
        sysbtn = findViewById(R.id.click);
        texton = findViewById(R.id.textonboard);
        infolayout = findViewById(R.id.infolayout);

        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabLayout);
        linearLayout = findViewById(R.id.linelayout);
        SlideAdapter sliderAdapter = new SlideAdapter(this);
        viewPager.setAdapter(sliderAdapter);
        adddot(0);
        viewPager.registerOnPageChangeCallback(viewlistner);

        // Setup TabLayout with ViewPager2 using TabLayoutMediator
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Enable");
                    break;
                case 1:
                    tab.setText("Learn");
                    break;
                case 2:
                    tab.setText("Typing");
                    break;
            }
        }).attach();

        dialogBox cdd = new dialogBox(this);

        next.setOnClickListener(new View.OnClickListener() {
            int count = 0;

            @Override
            public void onClick(View v) {

                count++;
                viewPager.setCurrentItem(currentPage + 1);
                /*
                 * if(count==3 && next.getText()=="FINISH")
                 * {
                 * startActivity(new Intent(MainActivity.this,MainActivity.class));
                 * }
                 */
            }
        });
        sysbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (currentPage == 0) {
                    cdd.show();
                } else if (currentPage == 1) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    assert imm != null;
                    imm.showInputMethodPicker();
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(currentPage - 1);
            }
        });

        // ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    @SuppressWarnings("deprecation")
    public void adddot(int position) {
        mdots = new TextView[3];
        linearLayout.removeAllViews();
        for (int i = 0; i < mdots.length; i++) {
            mdots[i] = new TextView(this);
            Spanned bullet;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                bullet = Html.fromHtml("&#8226;", Html.FROM_HTML_MODE_LEGACY);
            } else {
                bullet = Html.fromHtml("&#8226;");
            }
            mdots[i].setText(bullet);
            mdots[i].setTextSize(35);
            mdots[i].setTextColor(ContextCompat.getColor(this, R.color.black));
            linearLayout.addView(mdots[i]);
        }
        if (mdots.length > 0) {
            mdots[position].setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        }
    }

    ViewPager2.OnPageChangeCallback viewlistner = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            adddot(position);
            currentPage = position;

            if (position == 0) {
                next.setEnabled(true);
                back.setEnabled(false);
                back.setVisibility(View.INVISIBLE);
                next.setVisibility(View.VISIBLE);
                sysbtn.setVisibility(View.VISIBLE);
                next.setText("NEXT");
                back.setText("");
                sysbtn.setText("ENABLE");
                texton.setVisibility(View.GONE);
                infolayout.setVisibility(View.GONE);
                mAdView.setVisibility(View.VISIBLE);

            } else if (position == mdots.length - 1) {
                next.setEnabled(true);
                back.setEnabled(true);
                back.setVisibility(View.VISIBLE);
                next.setVisibility(View.INVISIBLE);
                next.setText("");
                back.setText("BACK");
                sysbtn.setVisibility(View.GONE);
                texton.setVisibility(View.VISIBLE);
                infolayout.setVisibility(View.VISIBLE);
                mAdView.setVisibility(View.GONE);

            } else {
                next.setEnabled(true);
                back.setEnabled(true);
                back.setVisibility(View.VISIBLE);
                next.setVisibility(View.VISIBLE);
                sysbtn.setVisibility(View.VISIBLE);
                next.setText("NEXT");
                back.setText("BACK");
                sysbtn.setText("SELECT");
                texton.setVisibility(View.GONE);
                infolayout.setVisibility(View.GONE);
                mAdView.setVisibility(View.VISIBLE);
            }
        }
    };

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuitems, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(MainActivity.this, settings.class);
                startActivity(intent);
                return true;
            case R.id.about:
                Intent intent2 = new Intent(MainActivity.this, about.class);
                startActivity(intent2);
                return true;
            case R.id.share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Hey! Download Coding Keyboard from Google play store and feel the comfort of coding on mobile. Visit https://play.google.com/store/apps/details?id=com.ajay.prokeyboard to download app now.";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
