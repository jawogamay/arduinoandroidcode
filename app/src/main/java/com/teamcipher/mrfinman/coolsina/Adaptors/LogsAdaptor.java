package com.teamcipher.mrfinman.coolsina.Adaptors;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.teamcipher.mrfinman.coolsina.Model.Logs;
import com.teamcipher.mrfinman.coolsina.R;

import java.util.ArrayList;

public class LogsAdaptor extends RecyclerView.Adapter<LogsAdaptor.ItemViewHolder> {
    private ArrayList<Logs> logsArrayList;
    private Context ctx;
    View.OnClickListener mClickListener;

    public LogsAdaptor(ArrayList<Logs> logsArrayList, Context ctx) {
        this.logsArrayList = logsArrayList;
        this.ctx = ctx;
    }

    public void setClickListener(View.OnClickListener callback) {
         mClickListener = callback;
    }
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_logs,viewGroup,false);
        RecyclerView.ViewHolder holder = new ItemViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onClick(view);
            }
        });
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int i) {
        Logs log = logsArrayList.get(i);
        if (log.getMessage().contains("Gas"))
        {
            holder.imgIcon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.gas_leak));
        }
        else
        {
            holder.imgIcon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.on_fire));
        }
        holder.lbldevice.setText(""+log.getDeviceId());
        holder.lbldate.setText(""+log.getDateTime());
        holder.lblmessage.setText("Message: "+log.getMessage().toString().substring(0,25)+"...");
    }

    @Override
    public int getItemCount() {
        return logsArrayList.size();
    }

    public void filter(ArrayList<Logs> logs)
    {
        this.logsArrayList = logs;
        notifyDataSetChanged();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imgIcon;
        protected TextView lblmessage;
        protected TextView lbldevice;
        protected TextView lbldate;
        public ItemViewHolder(View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.img);
            lbldevice = itemView.findViewById(R.id.lblDeviceName);
            lblmessage = itemView.findViewById(R.id.lblMessage);
            lbldate = itemView.findViewById(R.id.lblDate);

        }
    }


}
