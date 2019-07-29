package com.ajay.prokeyboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class slideadapter extends PagerAdapter {
    Context context;
    LayoutInflater layoutInflater;
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
        return view==(RelativeLayout)o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        container.removeView((RelativeLayout)object);
    }
}
