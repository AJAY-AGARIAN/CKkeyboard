package com.ajay.prokeyboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class slideadapter extends PagerAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    public slideadapter(Context context)
    {
        this.context=context;
    }

    public int[] sideimages={R.drawable.enable,
            R.drawable.tap,
            R.drawable.typing
    };

    private String[] ontop={"Click to enable Coding Keyboard","Select Coding Keyboard as keyboard",
            "Start typing below to try Coding Keyboard"};

    @Override
    public int getCount() {
        return sideimages.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view== o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View view=layoutInflater.inflate(R.layout.slidelayout,container,false);

        TextView ontoptext=(TextView)view.findViewById(R.id.changetext);
        ImageView onbaordimage =(ImageView)view.findViewById(R.id.imageonboard);


        ontoptext.setText(ontop[position]);
        onbaordimage.setImageResource(sideimages[position]);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }
}
