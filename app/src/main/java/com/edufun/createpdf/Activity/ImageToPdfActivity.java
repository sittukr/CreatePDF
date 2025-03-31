package com.edufun.createpdf.Activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edufun.createpdf.Model.ImageListModel;
import com.edufun.createpdf.Model.PdfFileModel;
import com.edufun.createpdf.R;
import com.edufun.createpdf.databinding.ActivityImageToPdfBinding;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ImageToPdfActivity extends AppCompatActivity {
    ActivityImageToPdfBinding binding;
    List<ImageListModel> imageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityImageToPdfBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        binding.btnSelectImage.setOnClickListener(v -> {
            Intent in = new Intent(Intent.ACTION_GET_CONTENT);
            in.setType("image/*");
            in.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
            startActivityForResult(Intent.createChooser(in,"Select Images"),1);
        });
        binding.btnCreatePdf.setOnClickListener(v -> {
            createPdf();
        });


    }

    private void createPdf() {
        try {

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME,"Image_To_Pdf.pdf");
            values.put(MediaStore.MediaColumns.MIME_TYPE,"application/pdf");
            values.put(MediaStore.MediaColumns.IS_PENDING,1);

            ContentResolver resolver = getContentResolver();
            Uri imagePdf = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MyApp");
                Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                imagePdf = resolver.insert(collection, values);
            }else {
                Toast.makeText(this, "older device", Toast.LENGTH_SHORT).show();
                return;
            }
            if (imagePdf!=null){
                try(OutputStream out = resolver.openOutputStream(imagePdf)) {


                }
            }


        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data!=null){
            if (data.getClipData()!= null){
                int count = data.getClipData().getItemCount();
                for (int i =0; i<count; i++){
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    imageList.add(getImageDetails(uri));
                }
            } else if (data.getData()!=null) {
                Uri uri =data.getData();
                imageList.add(getImageDetails(uri));
            }
        }
    }
    private ImageListModel getImageDetails(Uri uri){
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
            return new ImageListModel(uri,fileSize,fileName);
        }
    }
}