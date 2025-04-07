package com.edufun.createpdf.Activity;

import static com.edufun.createpdf.Activity.TextToPdfActivity.openPdf;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edufun.createpdf.Adapter.PdfListAdapter;
import com.edufun.createpdf.Model.PdfFileModel;
import com.edufun.createpdf.R;
import com.edufun.createpdf.databinding.ActivityMergePdfBinding;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MergePdfActivity extends AppCompatActivity {
    ActivityMergePdfBinding binding ;
    List<PdfFileModel> pdfUri = new ArrayList<>();
    PdfListAdapter adapter;
    String pdfFileName;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityMergePdfBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });




        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PdfListAdapter(pdfUri,this, binding);
        binding.recyclerview.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerview);


        binding.imgBack.setOnClickListener(v -> {
            finish();
        });

        binding.btnSelectPdf.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent,1);
        });

        binding.selectMediaLayout.setOnClickListener(v -> {
            binding.btnSelectPdf.performClick();
        });

        binding.btnMergePdf.setOnClickListener(v -> {
            if (pdfUri.isEmpty()){
                Toast.makeText(this, "Please select at least one PDF File", Toast.LENGTH_SHORT).show();
                return;
            }
            Dialog dialog1 = new Dialog(this);

            dialog1.setCancelable(false);
            dialog1.setContentView(R.layout.ask_filename);
            dialog1.getWindow().setLayout( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog1.getWindow().setBackgroundDrawableResource(R.drawable.rounded_white_bg);
            Button btnCancel = dialog1.findViewById(R.id.btnCancel);
            Button btnSave = dialog1.findViewById(R.id.btnSave);
            EditText etFileName = dialog1.findViewById(R.id.etFileName);
            etFileName.setText("Merged PDF");
            dialog1.show();
            btnCancel.setOnClickListener(v1 -> {
                dialog1.dismiss();
            });
            btnSave.setOnClickListener(v1 -> {
                pdfFileName = etFileName.getText().toString();
                if (!pdfFileName.isBlank()){
                    mergePDF();
                    dialog1.dismiss();
                }else {
                    etFileName.setError("Enter File Name");
                }
            });
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK){

            if (data.getData() != null){
                Uri uri = data.getData();
                pdfUri.add(getPdfDetails(uri));
                binding.btnMergePdf.setVisibility(View.VISIBLE);
                binding.tvOrderPdf.setVisibility(View.VISIBLE);
                binding.selectMediaLayout.setVisibility(View.GONE);

            } else if (data.getClipData() != null) {
                for (int i=0; i<data.getClipData().getItemCount(); i++){
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    pdfUri.add(getPdfDetails(uri));
                    binding.btnMergePdf.setVisibility(View.VISIBLE);
                    binding.tvOrderPdf.setVisibility(View.VISIBLE);
                    binding.selectMediaLayout.setVisibility(View.GONE);
                }
            }
            adapter.notifyDataSetChanged();
            //Toast.makeText(this, "Selected "+pdfUri.size()+" PDF Files", Toast.LENGTH_SHORT).show();
        }
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            Collections.swap(pdfUri,fromPosition,toPosition);
            adapter.notifyItemMoved(fromPosition,toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };
    private void mergePDF(){
        dialog = new ProgressDialog(MergePdfActivity.this);
        dialog.setMessage("Merging PDF...");
        dialog.setCancelable(false);
        dialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME,pdfFileName+".pdf");
                    values.put(MediaStore.MediaColumns.MIME_TYPE,"application/pdf");
                    values.put(MediaStore.MediaColumns.IS_PENDING,1);

                    ContentResolver resolver = getContentResolver();
                    Uri mergedPdfUri = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MyApp/Merged_Pdf");
                        Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                        mergedPdfUri = resolver.insert(collection, values);
                    }else {
                        Toast.makeText(MergePdfActivity.this, "older device", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }

                    if (mergedPdfUri != null) {
                        try (OutputStream out = resolver.openOutputStream(mergedPdfUri)) {

                            Document document = new Document();
                            PdfCopy copy = new PdfCopy(document, out);
                            document.open();

                            for (PdfFileModel uri : pdfUri) {
                                PdfReader reader = new PdfReader(getContentResolver().openInputStream(uri.getUri()));
                                copy.addDocument(reader);
                                reader.close();
                            }

                            document.close();
                            values.clear();
                            values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                            resolver.update(mergedPdfUri, values, null, null);

                            Toast.makeText(MergePdfActivity.this, "PDF File Merged Successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            openPdf(mergedPdfUri,MergePdfActivity.this);
                        }
                    }dialog.dismiss();
                } catch (DocumentException | IOException e) {
                    Toast.makeText(MergePdfActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    throw new RuntimeException(e);
                }
            }
        },500);

    }
    private PdfFileModel getPdfDetails(Uri uri){
        String fileName = "unknown";
        long fileSize =0;
        try ( Cursor cursor = getContentResolver().query(uri,null,null,null,null)) {
            if (cursor!=null && cursor.moveToFirst()){
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

                if (nameIndex != -1){
                    fileName = cursor.getString(nameIndex);
                }
                if (sizeIndex!= -1){
                    fileSize = cursor.getLong(sizeIndex);
                }
            }
            return new PdfFileModel(uri,fileName,fileSize);
        }
    }
}