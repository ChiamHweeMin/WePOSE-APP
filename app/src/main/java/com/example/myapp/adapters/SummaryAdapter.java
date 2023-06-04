package com.example.myapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapp.R;
import com.example.myapp.interfaces.RecyclerViewClickListener;
import com.example.myapp.models.SummaryModel;

import java.util.ArrayList;
import java.util.Random;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.MyViewHolder> {
    ArrayList<SummaryModel> arrayList;
    Context context;

    final private RecyclerViewClickListener clickListener;

    public SummaryAdapter(Context context, ArrayList<SummaryModel> arrayList, RecyclerViewClickListener clickListener) {
        this.arrayList = arrayList;
        this.context = context;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_summary_item, parent, false);

        final MyViewHolder myViewHolder = new MyViewHolder(view);

//        int[] androidColors = view.getResources().getIntArray(R.array.androidcolors);
//        int randomColors = androidColors[new Random().nextInt(androidColors.length)];

//        myViewHolder.accordian_title.setBackgroundColor(randomColors);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryAdapter.MyViewHolder holder, int position) {
        final String date = arrayList.get(position).getDate();
        final String elapsedTime = arrayList.get(position).getElapsedTime();
        final Integer slouchCount = arrayList.get(position).getSlouchCount();

        holder.dateTv.setText(date);

        if(elapsedTime != null) {
            holder.elapsedTimeTv.setText(String.valueOf(elapsedTime));
        }

        if(slouchCount != null) {
            holder.slouchCountTv.setText(String.valueOf(slouchCount));
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        CardView accordian_title;
        TextView dateTv, elapsedTimeTv, slouchCountTv;
        RelativeLayout accordian_body;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            dateTv = (TextView) itemView.findViewById(R.id.task_date);
            elapsedTimeTv = (TextView) itemView.findViewById(R.id.task_elapsedTime);
            slouchCountTv = (TextView) itemView.findViewById(R.id.task_slouchCount);
            accordian_title = (CardView) itemView.findViewById(R.id.accordian_title);
            accordian_body = (RelativeLayout) itemView.findViewById(R.id.accordian_body);

        }
    }
}