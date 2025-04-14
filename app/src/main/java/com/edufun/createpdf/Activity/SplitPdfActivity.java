package com.edufun.createpdf.Activity;

import static com.edufun.createpdf.Activity.TextToPdfActivity.openPdf;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edufun.createpdf.Model.PdfFileModel;
import com.edufun.createpdf.R;
import com.edufun.createpdf.databinding.ActivitySplitPdfBinding;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SplitPdfActivity extends AppCompatActivity {
    ActivitySplitPdfBinding binding;

    ProgressDialog dialog;
    int NumberOfPage;
    Uri uri;
    String pdfFileName;
    String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivitySplitPdfBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        dialog = new ProgressDialog(SplitPdfActivity.this);
        dialog.setMessage("Split PDF...");
        dialog.setCancelable(false);

        binding.btnSelectPdf.setOnClickListener(v -> {
            Intent in = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            in.setType("application/pdf");
            //  in.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
            startActivityForResult(in, 1);
        });
        binding.secondLayout.setOnClickListener(v -> {
            binding.secondLayout.setBackground(getDrawable(R.drawable.rounded_bg));
            binding.thirdLayout.setBackground(getDrawable(R.drawable.rounded_white_bg));
            binding.cbExtractAll.setChecked(true);
            binding.cbSplitRange.setChecked(false);
            binding.btnSelectPdf.setVisibility(View.GONE);
            binding.btnExtractAll.setVisibility(View.VISIBLE);
            binding.btnSplitRange.setVisibility(View.GONE);
            binding.fourthLayout.setVisibility(View.GONE);
        });
        binding.thirdLayout.setOnClickListener(v -> {
            binding.secondLayout.setBackground(getDrawable(R.drawable.rounded_white_bg));
            binding.thirdLayout.setBackground(getDrawable(R.drawable.rounded_bg));
            binding.cbExtractAll.setChecked(false);
            binding.cbSplitRange.setChecked(true);
            binding.btnSelectPdf.setVisibility(View.GONE);
            binding.btnExtractAll.setVisibility(View.GONE);
            binding.btnSplitRange.setVisibility(View.VISIBLE);
            binding.fourthLayout.setVisibility(View.VISIBLE);
        });
        binding.cbSplitRange.setOnClickListener(v -> {
            binding.thirdLayout.performClick();
        });
        binding.cbExtractAll.setOnClickListener(v -> {
            binding.secondLayout.performClick();
        });
        binding.btnSplitRange.setOnClickListener(v -> {
            String sPage = binding.etStartPage.getText().toString();
            String ePage = binding.etEndPage.getText().toString();
            if (!sPage.isEmpty() && !ePage.isEmpty()) {
                if (Integer.parseInt(sPage) > 0 && Integer.parseInt(sPage) <= NumberOfPage ) {
                    if (Integer.parseInt(ePage) <= +NumberOfPage && Integer.parseInt(sPage)<=Integer.parseInt(ePage) ) {
                        Dialog dialog1 = new Dialog(this);

                        dialog1.setCancelable(false);
                        dialog1.setContentView(R.layout.ask_filename);
                        dialog1.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        dialog1.getWindow().setBackgroundDrawableResource(R.drawable.rounded_white_bg);
                        Button btnCancel = dialog1.findViewById(R.id.btnCancel);
                        Button btnSave = dialog1.findViewById(R.id.btnSave);
                        EditText etFileName = dialog1.findViewById(R.id.etFileName);

                        etFileName.setText(fileName + " Split Page " + sPage + "to " + ePage);
                        dialog1.show();
                        btnCancel.setOnClickListener(v1 -> {
                            dialog1.dismiss();
                        });
                        btnSave.setOnClickListener(v1 -> {
                            pdfFileName = etFileName.getText().toString();
                            if (!pdfFileName.isBlank()) {
                                SplitPdfRange(uri, Integer.parseInt(sPage), Integer.parseInt(ePage));
                                dialog1.dismiss();
                            } else {
                                etFileName.setError("Enter File Name");
                            }
                        });
                    } else {
                        binding.etEndPage.setError("Invalid Input");
                    }
                } else {
                    binding.etStartPage.setError("Invalid Input");
                }
            }else {
                binding.etStartPage.setError("Invalid Input");
                binding.etEndPage.setError("Invalid Input");
            }
        });
        binding.btnExtractAll.setOnClickListener(v -> {
            SplitPdfPerPage(uri);
        });
        binding.imgBack.setOnClickListener(v -> {
            finish();
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            uri = data.getData();
           // Toast.makeText(this, uri.toString(), Toast.LENGTH_SHORT).show();
            try {
                getPdfDetail(uri);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void getPdfDetail(Uri uri) throws FileNotFoundException {

        long fileSize;
        try (Cursor cursor = getContentResolver().query(uri,null,null,null,null)){
            if (cursor!=null && cursor.moveToFirst()){
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

                if (nameIndex!= -1){
                    fileName = cursor.getString(nameIndex);
                    binding.tvFileName.setVisibility(View.VISIBLE);
                    binding.tvFileName.setText(fileName);
                };
                PdfReader reader = new PdfReader(getContentResolver().openInputStream(uri));
                NumberOfPage = reader.getNumberOfPages();
                binding.tvPageNo.setVisibility(View.VISIBLE);
                binding.tvPageNo.setText("There is "+NumberOfPage+" Page in PDF File");
                binding.secondLayout.setVisibility(View.VISIBLE);
                binding.thirdLayout.setVisibility(View.VISIBLE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void SplitPdfPerPage(Uri uri){
        dialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    ContentValues values = new ContentValues();

                    values.put(MediaStore.MediaColumns.MIME_TYPE,"application/pdf");
                    values.put(MediaStore.MediaColumns.IS_PENDING,1);

                    ContentResolver resolver = getContentResolver();
                    PdfReader reader = new PdfReader(getContentResolver().openInputStream(uri));
                    int NumberOfPage = reader.getNumberOfPages();

                    for (int i =1; i<=NumberOfPage; i++){
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME,fileName+" split_Page "+i+".pdf");

                        Uri pageUri = null;
                        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                            values.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/MyApp/Split Pdf");
                            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                            pageUri = resolver.insert(collection,values);
                        }else {
                            Toast.makeText(SplitPdfActivity.this, "This is not Supported in old Device", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }
                        if (pageUri!=null){
                            try (OutputStream out = resolver.openOutputStream(pageUri)){
                                Document document = new Document();
                                PdfCopy copy = new PdfCopy(document, out);
                                document.open();

                                copy.addPage(copy.getImportedPage(reader,i));
                                document.close();

                                values.put(MediaStore.MediaColumns.IS_PENDING,0);
                                resolver.update(pageUri,values,null,null);

                                Toast.makeText(SplitPdfActivity.this, "successfully created", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } catch (DocumentException e) {
                                dialog.dismiss();
                                throw new RuntimeException(e);
                            }
                        }
                    }


                } catch (IOException e) {
                    Toast.makeText(SplitPdfActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    throw new RuntimeException(e);
                }
            }
        },500);

    }
    private void SplitPdfRange(Uri uri, int startPage, int endPage) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {

                    if (startPage < 1) {
                        Toast.makeText(SplitPdfActivity.this, "Start page must be greater than 0", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }
                    if (endPage < startPage) {
                        Toast.makeText(SplitPdfActivity.this, "End page must be greater than or equal to start page", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }

                    ContentResolver resolver = getContentResolver();
                    PdfReader reader = new PdfReader(getContentResolver().openInputStream(uri));
                    int numberOfPages = reader.getNumberOfPages();

                    if (startPage > numberOfPages || endPage > numberOfPages) {
                        Toast.makeText(SplitPdfActivity.this, "Page range exceeds the total number of pages", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                    values.put(MediaStore.MediaColumns.IS_PENDING, 1);

                   // for (int i = startPage; i <= endPage; i++) {
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, pdfFileName+".pdf");

                        Uri pageUri = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/MyApp/Split Pdf");
                            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                            pageUri = resolver.insert(collection, values);
                        } else {
                            Toast.makeText(SplitPdfActivity.this, "This device is not supported for older versions", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }

                        if (pageUri != null) {
                            try (OutputStream out = resolver.openOutputStream(pageUri)) {
                                // Create a document for the specified page range
                                Document document = new Document();
                                PdfCopy copy = new PdfCopy(document, out);
                                document.open();

                                // Add the pages in the specified range
                                for (int j = startPage; j <= endPage; j++) {
                                    copy.addPage(copy.getImportedPage(reader, j));
                                }
                                document.close();

                                values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                                resolver.update(pageUri, values, null, null);

                                Toast.makeText(SplitPdfActivity.this, pdfFileName+" saved successfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                   // }

                    Toast.makeText(SplitPdfActivity.this, "PDF Split Successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    openPdf(pageUri, SplitPdfActivity.this);

                } catch (DocumentException | IOException e) {
                    dialog.dismiss();
                    Toast.makeText(SplitPdfActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, 500);
    }


}