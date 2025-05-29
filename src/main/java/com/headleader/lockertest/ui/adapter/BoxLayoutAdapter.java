package com.headleader.lockertest.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.headleader.lockertest.R;
import com.headleader.lockertest.global.Common;
import com.headleader.lockertest.ui.LockerView;

public class BoxLayoutAdapter extends RecyclerView.Adapter<BoxLayoutAdapter.MyViewHolder>
{

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType ) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lockerview, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder( @NonNull MyViewHolder holder, int position ) {
        ((LockerView)holder.itemView).initBoxList(position, Common.boxMap.get(position));
    }

    @Override
    public int getItemCount() {
        return  Common.lockerCount;
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        public MyViewHolder( View itemView ) {
            super(itemView);
        }
    }
}

