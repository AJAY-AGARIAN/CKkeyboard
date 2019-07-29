package com.ajay.prokeyboard;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private LinearLayout linearLayout;
    private slideadapter sliderAdapter;
    private TextView[] mdots;
    private Button back;
    private Button next;
    private int curruntpg;
    private Button sysbtn;
    private EditText texton;
    private LinearLayout infolayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        back = findViewById(R.id.back);
        next= findViewById(R.id.next);
        sysbtn=findViewById(R.id.click);
        texton=findViewById(R.id.textonboard);
        infolayout=findViewById(R.id.infolayout);

        viewPager = findViewById(R.id.viewpager);
        linearLayout = findViewById(R.id.linelayout);
        sliderAdapter = new slideadapter(this);
        viewPager.setAdapter(sliderAdapter);
        adddot(0);
        viewPager.addOnPageChangeListener(viewlistner);

        next.setOnClickListener(new View.OnClickListener(){
            int count=0;
            @Override
            public void onClick(View v) {

                count++;
                viewPager.setCurrentItem(curruntpg+1);
                /*if(count==3 && next.getText()=="FINISH")
                {
                    startActivity(new Intent(MainActivity.this,MainActivity.class));
                }*/
            }
        });
        sysbtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(curruntpg==0) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);
                    startActivity(intent);
                }
                else if(curruntpg==1){
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showInputMethodPicker();
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(curruntpg-1);
            }
        });
    }
    public void  adddot(int position){
        mdots=new TextView[3];
        linearLayout.removeAllViews();
        for(int i=0;i<mdots.length;i++){
            mdots[i]=new TextView(this);
            mdots[i].setText(Html.fromHtml("&#8226;"));
            mdots[i].setTextSize(35);
            mdots[i].setTextColor(getResources().getColor(R.color.black));
            linearLayout.addView(mdots[i]);
        }
        if(mdots.length>0){
            mdots[position].setTextColor(getResources().getColor(R.color.colorAccent));
        }
    }
    ViewPager.OnPageChangeListener viewlistner= new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            adddot(i);
            curruntpg=i;
            if(i==0){
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

            }else if(i==mdots.length-1){
                next.setEnabled(true);
                back.setEnabled(true);
                back.setVisibility(View.VISIBLE);
                next.setVisibility(View.INVISIBLE);
                next.setText("");
                back.setText("BACK");
                sysbtn.setVisibility(View.GONE);
                texton.setVisibility(View.VISIBLE);
                infolayout.setVisibility(View.VISIBLE);


            }else {
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
            }

        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }

    };
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menuitems, menu);
        return true;
    }

    public void onGroupItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings:
                Intent intent=new Intent(MainActivity.this,settings.class);
                startActivity(intent);
                break;
            case R.id.about:
                Intent intent2=new Intent(MainActivity.this,about.class);
                startActivity(intent2);
                break;
            case R.id.share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Hey! Download Coding Keyboard from Google play store and feel the comfort of coding on mobile. Visit https://codingkeyboard.carrd.co/";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                break;
        }
    }
}
