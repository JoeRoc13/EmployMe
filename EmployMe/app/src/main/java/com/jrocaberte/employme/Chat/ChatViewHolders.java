package com.jrocaberte.employme.Chat;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Joey on 4/2/18.
 */

public class ChatViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public ChatViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

    }
}
