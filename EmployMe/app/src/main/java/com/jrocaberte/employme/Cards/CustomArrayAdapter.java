package com.jrocaberte.employme.Cards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jrocaberte.employme.R;

import java.util.List;

/**
 * Created by Joey on 2/18/18.
 */

public class CustomArrayAdapter extends android.widget.ArrayAdapter<Cards> {

    Context context;

    public CustomArrayAdapter(Context context, int resourceId, List<Cards> items) {
        super(context, resourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Cards card_item = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.name);
        ImageView image = (ImageView) convertView.findViewById(R.id.image);

        name.setText(card_item.getName());
        switch(card_item.getProfileImageUrl()){
            case "default":
                image.setImageResource(R.drawable.default_profile);
                break;
            default:
                Glide.clear(image);
                Glide.with(convertView.getContext()).load(card_item.getProfileImageUrl()).centerCrop().into(image);
                break;
        }


        return convertView;
    }

}
