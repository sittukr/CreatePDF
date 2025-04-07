package com.edufun.createpdf.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edufun.createpdf.Activity.MergePdfActivity;
import com.edufun.createpdf.Model.PdfFileModel;
import com.edufun.createpdf.R;
import com.edufun.createpdf.databinding.ActivityMergePdfBinding;

import java.util.List;

public class PdfListAdapter extends RecyclerView.Adapter<PdfListAdapter.pdfViewHolder> {

    List<PdfFileModel> pdfListUri ;
    Context context;
    ActivityMergePdfBinding binding;



    public PdfListAdapter(List<PdfFileModel> pdfListUri, Context context,ActivityMergePdfBinding binding) {
        this.pdfListUri = pdfListUri;
        this.context = context;
        this.binding = binding;
    }

    @NonNull
    @Override
    public pdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.show_pdflist,parent,false);
        return new pdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull pdfViewHolder holder, int position) {
        PdfFileModel pdfUri = pdfListUri.get(position);
        holder.textView.setText(pdfUri.getFileName());
        holder.tvSize.setText(pdfUri.getReadableFileSize());

        holder.imgRemove.setOnClickListener(v -> {
            pdfListUri.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position,pdfListUri.size());
           // Toast.makeText(context, "PDF Removed", Toast.LENGTH_SHORT).show();

            if (pdfListUri.size() == 0){
               binding.selectMediaLayout.setVisibility(View.VISIBLE);
            }
            else {
                binding.selectMediaLayout.setVisibility(View.GONE);
            }

        });
    }

    @Override
    public int getItemCount() {
        return pdfListUri.size();
    }

    public static class pdfViewHolder extends RecyclerView.ViewHolder {
        TextView textView,tvSize;
        ImageView imgRemove;
        public pdfViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tvPdfName);
            tvSize = itemView.findViewById(R.id.tvSize);
            imgRemove = itemView.findViewById(R.id.imgRemove);
        }
    }
}
