package com.edufun.createpdf.Activity;

import static com.edufun.createpdf.Activity.TextToPdfActivity.openPdf;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.format.Formatter;
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

import com.edufun.createpdf.Adapter.ImageToPdfAdapter;
import com.edufun.createpdf.Model.ImageListModel;
import com.edufun.createpdf.R;
import com.edufun.createpdf.databinding.ActivityImageToPdfBinding;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import id.zelory.compressor.Compressor;

public class ImageToPdfActivity extends AppCompatActivity {
    ActivityImageToPdfBinding binding;
    List<ImageListModel> imageList = new ArrayList<>();
    ImageToPdfAdapter adapter;
    ProgressDialog dialog;
    String pdfFileName;

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
        binding.selectMediaLayout.setOnClickListener(v -> {
            binding.btnSelectImage.performClick();
        });

        binding.btnCreatePdf.setOnClickListener(v -> {
            if (!imageList.isEmpty()){

                Dialog dialog1 = new Dialog(this);

                dialog1.setCancelable(false);
                dialog1.setContentView(R.layout.ask_filename);
                dialog1.getWindow().setLayout( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog1.getWindow().setBackgroundDrawableResource(R.drawable.rounded_white_bg);
                Button btnCancel = dialog1.findViewById(R.id.btnCancel);
                Button btnSave = dialog1.findViewById(R.id.btnSave);
                EditText etFileName = dialog1.findViewById(R.id.etFileName);
                etFileName.setText("ImageToPdf");
                dialog1.show();
                btnCancel.setOnClickListener(v1 -> {
                    dialog1.dismiss();
                });
                btnSave.setOnClickListener(v1 -> {
                    pdfFileName = etFileName.getText().toString();
                    if (!pdfFileName.isBlank()){
                        createPdf();
                        dialog1.dismiss();
                    }else {
                        etFileName.setError("Enter File Name");
                    }
                });
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
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME,pdfFileName+".pdf");
                    values.put(MediaStore.MediaColumns.MIME_TYPE,"application/pdf");
                    values.put(MediaStore.MediaColumns.IS_PENDING,1);

                    ContentResolver resolver = getContentResolver();
                    Uri imagePdf = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MyApp /ImageToPdf");
                        Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                        imagePdf = resolver.insert(collection, values);
                    }else {
                        Toast.makeText(ImageToPdfActivity.this, "older device", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }
                    if (imagePdf!=null){
                        try(OutputStream out = resolver.openOutputStream(imagePdf)) {

                            int widthInPixels = 512;
                            int heightInPixels = 1024;

                            // Convert to points (1 pixel = 72/96 points)
                            float widthInPoints = widthInPixels * (72f / 96f);
                            float heightInPoints = heightInPixels * (72f / 96f);

                            Document document = new Document();
                            document.setMargins(0,0,0,0);
                            PdfWriter writer = PdfWriter.getInstance(document,out);
                            writer.setCompressionLevel(9);
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

                                float imageWidth = image.getScaledWidth();
                                float imageHeight = image.getScaledHeight();

//                                if (rotationAngle == 90 || rotationAngle == 270 || rotationAngle == -90 || rotationAngle == -270) {
//                                    image.scaleToFit(new Rectangle(docHeight,docWidth));
//                                   // document.setPageSize(new Rectangle(docHeight,docWidth));
//                                } else {
//                                    image.scaleToFit(docWidth, docHeight);
//                                    //document.setPageSize(new Rectangle(docWidth,docHeight));
//                                }

                              //image.scaleToFit(document.getPageSize().getWidth(),document.getPageSize().getHeight());
                              image.scaleToFit(PageSize.A4.getWidth(),PageSize.A4.getHeight());
                              image.setAbsolutePosition((PageSize.A4.getWidth()-image.getScaledWidth())/2,(PageSize.A4.getHeight()-image.getScaledHeight())/2);
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
                    binding.selectMediaLayout.setVisibility(View.GONE);
                }
            } else if (data.getData()!=null) {
                Uri uri =data.getData();
                imageList.add(getImageDetails(uri));

                binding.btnCreatePdf.setVisibility(View.VISIBLE);
                binding.tvOrderPdf.setVisibility(View.VISIBLE);
                binding.selectMediaLayout.setVisibility(View.GONE);
            }
            adapter.notifyDataSetChanged();
        }
    }
    private ImageListModel getImageDetails(Uri uri){
        String fileName = "unknown";
        long fileSize =0;
        int rotation = 0;
        Uri compressUri = null;
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
                compressUri = compress(uri,ImageToPdfActivity.this);
            }
            return new ImageListModel(compressUri,fileSize,fileName,rotation,pageSize);
        }
    }
    private byte[] bitmapToBiteArray (Bitmap bitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,80,outputStream);
        return outputStream.toByteArray();
    }

    private Uri compress(Uri originalUri, Context context) {
        File originalImageFile = new File(getRealPathFromURI(originalUri, context));
        File compressedImageFile = null;
        Uri compressedImageUri = null;

        try {
            // Use the Compressor library to compress the image
            compressedImageFile = new Compressor(context)
                    .setMaxWidth(512)    // Set the maximum width
                    .setMaxHeight(512)  // Set the maximum height
                    .setQuality(80)      // Set the quality (0 to 100)
                    .setCompressFormat(Bitmap.CompressFormat.JPEG) // Set the compression format
                    .compressToFile(originalImageFile); // Compress and save to a file

            // After compression, get the Uri of the compressed file
            compressedImageUri = Uri.fromFile(compressedImageFile);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error during image compression: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }

        // Return the Uri of the compressed image
        return compressedImageUri;
    }

    private String getRealPathFromURI(Uri uri, Context context) {
        String[] projection = { MediaStore.Images.Media.DATA };
        android.database.Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        } else {
            return null;
        }
    }

}