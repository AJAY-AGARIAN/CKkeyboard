package com.ajay.prokeyboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SlideAdapter extends RecyclerView.Adapter<SlideAdapter.SlideViewHolder> {
    private Context context;
    private LayoutInflater layoutInflater;

    public int[] sideimages = { R.drawable.enable,
            R.drawable.tap,
            R.drawable.typing
    };

    private String[] ontop = { "Click to enable Coding Keyboard", "Select Coding Keyboard as keyboard",
            "Start typing below to try Coding Keyboard" };

    public SlideAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View view = layoutInflater.inflate(R.layout.slidelayout, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        holder.ontoptext.setText(ontop[position]);
        holder.onbaordimage.setImageResource(sideimages[position]);
    }

    @Override
    public int getItemCount() {
        return sideimages.length;
    }

    public static class SlideViewHolder extends RecyclerView.ViewHolder {
        TextView ontoptext;
        ImageView onbaordimage;

        public SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            ontoptext = itemView.findViewById(R.id.changetext);
            onbaordimage = itemView.findViewById(R.id.imageonboard);
        }
    }
}
