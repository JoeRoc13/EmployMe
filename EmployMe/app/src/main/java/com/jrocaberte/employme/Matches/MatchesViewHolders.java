package com.jrocaberte.employme.Matches;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jrocaberte.employme.Chat.ChatActivity;
import com.jrocaberte.employme.R;

/**
 * Created by Joey on 4/2/18.
 */

public class MatchesViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView mMatchId, mMatchName;
    public ImageView mMatchImage;

    public MatchesViewHolders(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mMatchId = (TextView) itemView.findViewById(R.id.MatchId);
        mMatchName = (TextView) itemView.findViewById(R.id.MatchName);
        mMatchImage = (ImageView) itemView.findViewById(R.id.MatchImage);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), ChatActivity.class);
        Bundle b = new Bundle();
        b.putString("matchId", mMatchId.getText().toString());
        b.putString("matchName", mMatchName.getText().toString());
        intent.putExtras(b);
        view.getContext().startActivity(intent);
    }
}
