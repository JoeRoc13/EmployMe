package com.jrocaberte.employme.Chat;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jrocaberte.employme.R;

import java.util.List;

/**
 * Created by Joey on 4/2/18.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatViewHolders> {
    private List<ChatObject> chatList;
    private Context context;

    public ChatAdapter(List<ChatObject> chatList, Context context) {
        this.chatList = chatList;
        this.context = context;
    }

    @Override
    public ChatViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        ChatViewHolders rcv = new ChatViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(ChatViewHolders holder, int position) {
        holder.mMessage.setText(chatList.get(position).getMessage());
        holder.mMessage.setTextSize(15);
        if(chatList.get(position).getCurrentUser()) {
            holder.mContainer.setGravity(Gravity.END);
            holder.mMessage.setTextColor(Color.parseColor("#FFFFFF"));
            holder.mMessage.setBackgroundResource(R.drawable.rounded_rectangle_sent);
//            holder.mMessage.setBackgroundColor(Color.parseColor("#0084ff"));
        } else {
            holder.mContainer.setGravity(Gravity.START);
            holder.mMessage.setTextColor(Color.parseColor("#000000"));
            holder.mMessage.setBackgroundResource(R.drawable.rounded_rectangle_received);
//            holder.mMessage.setBackgroundColor(Color.parseColor("#E6E5EB"));
        }
    }

    @Override
    public int getItemCount() {
        return this.chatList.size();
    }
}
