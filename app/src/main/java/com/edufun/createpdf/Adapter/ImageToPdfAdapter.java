package com.edufun.createpdf.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edufun.createpdf.Model.ImageListModel;
import com.edufun.createpdf.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageToPdfAdapter extends RecyclerView.Adapter<ImageToPdfAdapter.imageViewHolder> {

    List<ImageListModel> list;
    Context context;

    public ImageToPdfAdapter(List<ImageListModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public imageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.show_images,parent,false);
        return new imageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull imageViewHolder holder, int position) {
        ImageListModel uriList = list.get(position);
        Uri imageUri = uriList.getUri();

        Picasso.get().load(imageUri).placeholder(R.drawable.home).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class imageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public imageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
