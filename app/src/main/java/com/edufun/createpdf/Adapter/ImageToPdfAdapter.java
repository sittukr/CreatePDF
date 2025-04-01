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

        holder.imgRemove.setOnClickListener(v -> {
            list.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,list.size());
        });
        holder.imgRotate.setOnClickListener(v -> {
            int rot = 0;
            if (list.get(position).getRotation()==0) {
                rot = 90;
            } else if ((list.get(position).getRotation()==90)) {
                rot = 180;
            } else if ((list.get(position).getRotation()==180)) {
                rot = 270;
            } else if ((list.get(position).getRotation()==270)) {
                rot = 0;
            }
            list.get(position).setRotation(rot);
            holder.imageView.setRotation(rot);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class imageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView,imgRemove,imgRotate;
        public imageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            imgRemove = itemView.findViewById(R.id.imgRemove);
            imgRotate = itemView.findViewById(R.id.imgRotate);
        }
    }
}
