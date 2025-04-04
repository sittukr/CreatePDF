package com.edufun.createpdf.Activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edufun.createpdf.R;
import com.edufun.createpdf.databinding.ActivityTextToPdfBinding;

import java.io.IOException;
import java.io.OutputStream;

public class TextToPdfActivity extends AppCompatActivity {

    ActivityTextToPdfBinding binding;
    String fName,textInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityTextToPdfBinding.inflate(getLayoutInflater());
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
            textInput = binding.etText.getText().toString();
            String withoutSpaceText =textInput.replaceAll("\\s","");
                if (textInput != null && !withoutSpaceText.isEmpty()){
                    Dialog dialog1 = new Dialog(this);

                    dialog1.setCancelable(false);
                    dialog1.setContentView(R.layout.ask_filename);
                    dialog1.getWindow().setLayout( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog1.getWindow().setBackgroundDrawableResource(R.drawable.rounded_white_bg);
                    Button btnCancel = dialog1.findViewById(R.id.btnCancel);
                    Button btnSave = dialog1.findViewById(R.id.btnSave);
                    EditText etFileName = dialog1.findViewById(R.id.etFileName);
                    etFileName.setText("TextToPdf");
                    dialog1.show();
                    btnCancel.setOnClickListener(v1 -> {
                        dialog1.dismiss();
                    });
                    btnSave.setOnClickListener(v1 -> {
                        fName = etFileName.getText().toString();
                        if (!fName.isBlank()){
                            try {
                                createPdf();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            dialog1.dismiss();
                        }else {
                            etFileName.setError("Enter File Name");
                        }
                    });
                }else Toast.makeText(this, "Write Something...", Toast.LENGTH_SHORT).show();

        });
        binding.etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count==0){
                    binding.btnCreate.setVisibility(View.GONE);
                }else binding.btnCreate.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
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

                openPdf(pdfUri, TextToPdfActivity.this);

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