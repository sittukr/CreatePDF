package com.edufun.createpdf.Activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edufun.createpdf.R;
import com.edufun.createpdf.databinding.ActivityPdfToImageBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PdfToImageActivity extends AppCompatActivity {
    ActivityPdfToImageBinding binding;
    Uri uri;
    Uri imgUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityPdfToImageBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        binding.btnSelectPdf.setOnClickListener(v -> {
            Intent in = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            in.setType("application/pdf");
            pdfLauncher.launch(in);
        });
    }
    private final ActivityResultLauncher<Intent> pdfLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result ->{
        if (result.getResultCode() == RESULT_OK && result.getData() != null){
            uri = result.getData().getData();
            pdfToImage(uri,PdfToImageActivity.this);
            Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();
        }
    });
    private void pdfToImage( Uri uri,Context context) {
        File pdfFile = getFileFromUri(uri,context);
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        //values.put(MediaStore.MediaColumns.DISPLAY_NAME);
        values.put(MediaStore.MediaColumns.MIME_TYPE,"image/png");
        values.put(MediaStore.MediaColumns.IS_PENDING,1);
        try {
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer renderer = new PdfRenderer(fileDescriptor);

          //  PdfDocument document= zz
            for (int i = 0; i < renderer.getPageCount(); i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                int width = page.getWidth();
                int height = page.getHeight();

                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                if (Build.VERSION.SDK_INT>Build.VERSION_CODES.Q){
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME,"Pdf to Image "+i+".png");
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS+"/MyApp/PdfToImage");
                    Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                    imgUri = resolver.insert(collection,values);
                }
                if (imgUri!=null) {
                    OutputStream outputStream = resolver.openOutputStream(imgUri);


//                File outputFile = new File(outputDir, "page.jpg");
//                FileOutputStream out = new FileOutputStream(outputFile);

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                   // out.close();
                    outputStream.close();
                    page.close();
                    values.put(MediaStore.MediaColumns.IS_PENDING,0);
                    resolver.update(imgUri,values,null,null);
                }
            }
            renderer.close();
            fileDescriptor.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }
    private File getFileFromUri(Uri uri, Context context){
        File file = null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);

            file = new File(context.getCacheDir(),"temp_file");
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer))>0){
                outputStream.write(buffer,0,length);
            }
            outputStream.close();
            inputStream.close();
            return file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;

    }


}