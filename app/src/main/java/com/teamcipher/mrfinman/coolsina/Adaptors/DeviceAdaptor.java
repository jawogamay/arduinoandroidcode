package com.teamcipher.mrfinman.coolsina.Adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.teamcipher.mrfinman.coolsina.R;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class DeviceAdaptor extends BaseAdapter {

    ArrayList<String> arrayList;
    Context ctx;
    LayoutInflater inflate;
    ViewHolder holder;
    public DeviceAdaptor(Context ctx,ArrayList<String> arrayList) {
        this.arrayList = arrayList;
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        String name = arrayList.get(i);
        if (view == null)
        {
            LayoutInflater inflater = LayoutInflater.from(ctx);
            view = inflater.inflate(R.layout.item_devices, null, true);
            holder = new ViewHolder();
            holder.lblName = view.findViewById(R.id.lblDeviceName);

        }

        holder.lblName.setText(""+name);

        return view;
    }

    class ViewHolder{
        TextView lblName;
    }
}
