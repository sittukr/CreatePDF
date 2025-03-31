package com.edufun.createpdf;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edufun.createpdf.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    String fName,textInput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PackageManager.PERMISSION_GRANTED);

        binding.imgBack.setOnClickListener(v -> {
            finish();
        });
        binding.btnCreate.setOnClickListener(v -> {
            fName = binding.etFileName.getText().toString();
            String withoutSpaceName = fName.replaceAll("\\s","");
            textInput = binding.etText.getText().toString();
            String withoutSpaceText =textInput.replaceAll("\\s","");
            if (fName != null && !withoutSpaceName.isEmpty()){
                if (textInput != null && !withoutSpaceText.isEmpty()){
                    try {
                        createPdf();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else binding.etText.setError("Enter Data");
            }else binding.etFileName.setError("Enter File Name");

        });
       


    }

    private void createPdf() throws IOException {
        PdfDocument pdfDocument = new PdfDocument();
        //Paint paint = new Paint();
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(14);
        int currentPageNumber = 1;
        int pageWidth = 595;
        int pageHeight = 842;
        int margin = 40;
        int textWidth = pageWidth-(margin*2);
        int textHeight = pageHeight-(margin*2);

        Paint pageNumberPaint = new Paint();
        pageNumberPaint.setTextSize(12);
        pageNumberPaint.setTextAlign(Paint.Align.CENTER);

       // PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(595,842,1).create();
       // PdfDocument.Page page = pdfDocument.startPage(info);

//        Canvas canvas = page.getCanvas();

        //canvas.drawText(textInput,40,50,textPaint);



        StaticLayout staticLayout = StaticLayout.Builder.obtain(textInput,0,textInput.length(),textPaint,textWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1.0f,1.2f)
                .setIncludePad(false)
                .build();

        int lineHeight = staticLayout.getLineBottom(0)-staticLayout.getLineTop(0);
        int maxLinePerPage = (pageHeight-2*margin)/lineHeight;
        int totalLines = staticLayout.getLineCount();
        int startLine =0;

        while (startLine<totalLines){
            int endLine = Math.min(startLine+maxLinePerPage,totalLines);
            int startOffSet = staticLayout.getLineStart(startLine);
            int endOffSet = staticLayout.getLineEnd(endLine-1);

            if (endOffSet>textInput.length()){
                endOffSet=textInput.length();
            }
            String pageText = textInput.substring(startOffSet,endOffSet);

            //Create sub-layout for this page
            StaticLayout pageLayout = StaticLayout.Builder.obtain(pageText,0,pageText.length(),textPaint,textWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1f,1.2f)
                    .setIncludePad(false)
                    .build();

            //Start new page
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth,pageHeight,currentPageNumber).create();
            PdfDocument.Page newPage = pdfDocument.startPage(pageInfo);

            Canvas canvas = newPage.getCanvas();
            canvas.save();
            canvas.translate(margin,margin);
            pageLayout.draw(canvas);
            canvas.restore();

            String pageNumberText = "Page " + currentPageNumber;
            canvas.drawText(pageNumberText, pageWidth / 2, pageHeight - 20, pageNumberPaint);

            pdfDocument.finishPage(newPage);

            //Prepare for next
            startLine = endLine;
            currentPageNumber++;
        }


       // To Save in File
       ContentValues values = new ContentValues();
       values.put(MediaStore.MediaColumns.DISPLAY_NAME,fName+".pdf");
       values.put(MediaStore.MediaColumns.MIME_TYPE,"application/pdf");
       values.put(MediaStore.MediaColumns.IS_PENDING,1);

       ContentResolver resolver = getContentResolver();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS +"/MyApp");
            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri pdfUri = resolver.insert(collection,values);

            if (pdfUri!= null){
                OutputStream out = resolver.openOutputStream(pdfUri);
                pdfDocument.writeTo(out);
                out.close();
                values.clear();
                Toast.makeText(this, "Pdf Created Successfully", Toast.LENGTH_SHORT).show();

                openPdf(pdfUri,MainActivity.this);

                values.put(MediaStore.MediaColumns.IS_PENDING,0);
                resolver.update(pdfUri,values,null,null);
            }
        }

        pdfDocument.close();




    }
    public static void openPdf(Uri pdfUri, Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No PDF Viewer Installed", Toast.LENGTH_SHORT).show();
        }
    }

}