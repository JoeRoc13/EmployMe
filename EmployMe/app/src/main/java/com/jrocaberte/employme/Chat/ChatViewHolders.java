package com.jrocaberte.employme.Chat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jrocaberte.employme.R;

/**
 * Created by Joey on 4/2/18.
 */

public class ChatViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView mMessage;
    public LinearLayout mContainer;

    public ChatViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mMessage = itemView.findViewById(R.id.message);
        mContainer = itemView.findViewById(R.id.container);
    }

    @Override
    public void onClick(View view) {

    }
}
