package com.edufun.createpdf.Activity;

import static com.edufun.createpdf.MainActivity.openPdf;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edufun.createpdf.Adapter.ImageToPdfAdapter;
import com.edufun.createpdf.Model.ImageListModel;
import com.edufun.createpdf.Model.PdfFileModel;
import com.edufun.createpdf.R;
import com.edufun.createpdf.databinding.ActivityImageToPdfBinding;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class ImageToPdfActivity extends AppCompatActivity {
    ActivityImageToPdfBinding binding;
    List<ImageListModel> imageList = new ArrayList<>();
    ImageToPdfAdapter adapter;
    ProgressDialog dialog;

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


        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ImageToPdfAdapter(imageList,this);
        binding.recyclerview.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerview);

        binding.btnSelectImage.setOnClickListener(v -> {
            Intent in = new Intent(Intent.ACTION_GET_CONTENT);
            in.setType("image/*");
            in.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
            startActivityForResult(Intent.createChooser(in,"Select Images"),1);
        });
        binding.btnCreatePdf.setOnClickListener(v -> {
            if (!imageList.isEmpty()){
                createPdf();
            }else {
                Toast.makeText(this, "Pick at least one Image", Toast.LENGTH_SHORT).show();
            }
            
        });
        binding.imgBack.setOnClickListener(v -> {
            finish();
        });


    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN |ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            Collections.swap(imageList,fromPosition,toPosition);
            adapter.notifyItemMoved(fromPosition,toPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };

    private void createPdf() {

        dialog = new ProgressDialog(ImageToPdfActivity.this);
        dialog.setMessage("Creating PDF");
        dialog.setCancelable(false);
        dialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
                        Toast.makeText(ImageToPdfActivity.this, "older device", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }
                    if (imagePdf!=null){
                        try(OutputStream out = resolver.openOutputStream(imagePdf)) {


                            Document document = new Document();
                            PdfWriter writer = PdfWriter.getInstance(document,out);
                            writer.open();
                            document.open();
                            for (ImageListModel list : imageList){
                                Uri imageUri =  list.getUri();
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
                                Image image = Image.getInstance(bitmapToBiteArray(bitmap));

                                int rotationAngle = (int) -list.getRotation();
                                image.setRotationDegrees(rotationAngle);

                                float docWidth = document.getPageSize().getWidth();
                                float docHeight = document.getPageSize().getHeight();

                                if (rotationAngle == 90 || rotationAngle == 270 || rotationAngle == -90 || rotationAngle == -270) {
                                    image.scaleToFit(docHeight, docWidth);
                                } else {
                                    image.scaleToFit(docWidth, docHeight);
                                }
                                //image.scaleToFit(document.getPageSize().getWidth(),document.getPageSize().getHeight());
                                image.setAlignment(Image.ALIGN_CENTER);


                                document.add(image);
                                document.newPage();
                            }
                            document.close();
                        }
                        values.put(MediaStore.MediaColumns.IS_PENDING,0);
                        resolver.update(imagePdf,values,null,null);
                        openPdf(imagePdf, ImageToPdfActivity.this);
                        Toast.makeText(ImageToPdfActivity.this, "PDF Created Successfully", Toast.LENGTH_SHORT).show();

                        dialog.dismiss();

                    }


                }catch (Exception e){
                    Toast.makeText(ImageToPdfActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    throw new RuntimeException(e);
                }
            }
        },500);

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

                    binding.btnCreatePdf.setVisibility(View.VISIBLE);
                    binding.tvOrderPdf.setVisibility(View.VISIBLE);
                }
            } else if (data.getData()!=null) {
                Uri uri =data.getData();
                imageList.add(getImageDetails(uri));

                binding.btnCreatePdf.setVisibility(View.VISIBLE);
                binding.tvOrderPdf.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        }
    }
    private ImageListModel getImageDetails(Uri uri){
        String fileName = "unknown";
        long fileSize =0;
        int rotation = 0;
        Rectangle pageSize = PageSize.A4;
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
            return new ImageListModel(uri,fileSize,fileName,rotation,pageSize);
        }
    }
    private byte[] bitmapToBiteArray (Bitmap bitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        return outputStream.toByteArray();
    }
}