package com.example.asm2.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.asm2.R;

import java.io.InputStream;
import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> values;
    private final ArrayList<Uri> imageUris;

    public CustomAdapter(Context context, ArrayList<String> values, ArrayList<Uri> imageUris) {
        super(context, R.layout.list_item,values);
        this.context = context;
        this.values = values;
        this.imageUris = imageUris;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);

        TextView textView = rowView.findViewById(R.id.textViewItem);
        ImageView imageView = rowView.findViewById(R.id.imageViewItem);

        textView.setText(values.get(position));
        Uri imageUri = imageUris.get(position);

        if (imageUri != null) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                imageView.setImageResource(android.R.color.darker_gray); // Placeholder image
            }
        } else {
            imageView.setImageResource(android.R.color.darker_gray); // Placeholder image
        }

        return rowView;
    }
}
