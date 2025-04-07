package com.edufun.createpdf.Activity;

import static com.edufun.createpdf.Activity.TextToPdfActivity.openPdf;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edufun.createpdf.R;
import com.edufun.createpdf.databinding.ActivityLockPdfBinding;
import com.edufun.createpdf.databinding.ActivityUnlockPdfBinding;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UnlockPdfActivity extends AppCompatActivity {
    ActivityUnlockPdfBinding binding;

    Uri uri;
    String pdfFileName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityUnlockPdfBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.imgBack.setOnClickListener(v -> {
            finish();
        });

        binding.btnSelectPdf.setOnClickListener(v -> {
            Intent in = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            in.setType("application/pdf");
            //startActivityForResult(in,1);
            selectPdfLauncher.launch(in);

//            File folder = new File(Environment.getExternalStorageDirectory(),"MyApp");
//            Uri folderUri = Uri.fromFile(folder);
//            Intent in = new Intent(Intent.ACTION_VIEW);
//            in.setDataAndType(folderUri,"*/*");
//            in.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            startActivity(in);

        });

        binding.btnProtect.setOnClickListener(v -> {
            String userPass = binding.etPassword.getText().toString();
            String ownerPass = binding.etConfirmPassword.getText().toString();
            if (!userPass.isEmpty() && !ownerPass.isEmpty() && userPass.equals(ownerPass)){
                Dialog dialog1 = new Dialog(this);

                dialog1.setCancelable(false);
                dialog1.setContentView(R.layout.ask_filename);
                dialog1.getWindow().setLayout( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog1.getWindow().setBackgroundDrawableResource(R.drawable.rounded_white_bg);
                Button btnCancel = dialog1.findViewById(R.id.btnCancel);
                Button btnSave = dialog1.findViewById(R.id.btnSave);
                EditText etFileName = dialog1.findViewById(R.id.etFileName);
                etFileName.setText("Unlock PDF");
                dialog1.show();
                btnCancel.setOnClickListener(v1 -> {
                    dialog1.dismiss();
                });
                btnSave.setOnClickListener(v1 -> {
                    pdfFileName = etFileName.getText().toString();
                    if (!pdfFileName.isBlank()){
                        lockPdf(uri,userPass,ownerPass);
                        dialog1.dismiss();
                    }else {
                        etFileName.setError("Enter File Name");
                    }
                });

            }else {
                Toast.makeText(this, "Enter Correct Password", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private final ActivityResultLauncher<Intent> selectPdfLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->{
        if (result.getResultCode() == RESULT_OK && result.getData()!=null){
            uri = result.getData().getData();
            pdfDetails(uri);
            binding.bottomLayout.setVisibility(View.VISIBLE);
            binding.btnProtect.setVisibility(View.VISIBLE);
        }
    });

    private void pdfDetails(Uri uri){
        Cursor cursor = getContentResolver().query(uri,null,null,null,null);
        if (cursor!=null && cursor.moveToFirst()){
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);

            if (nameIndex!= -1){
                String fileName = cursor.getString(nameIndex);
                binding.tvFileName.setText(fileName);
            }
            if (sizeIndex!= -1){
                long fileSize = cursor.getLong(sizeIndex);
            }
        }
    }
    private void lockPdf(Uri uri,String userPassword, String ownerPassword) {

        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, pdfFileName+".pdf");
        values.put(MediaStore.MediaColumns.MIME_TYPE,"application/pdf");
        values.put(MediaStore.MediaColumns.IS_PENDING,1);

        Uri unlockUri =null;
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS+"/MyApp/UnlockPdf");
            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            unlockUri = resolver.insert(collection,values);

        }else {
            Toast.makeText(this, "Older Device", Toast.LENGTH_SHORT).show();
            return;
        }
        if (unlockUri!=null){
            try {
                OutputStream out = resolver.openOutputStream(unlockUri);
                InputStream inputStream = resolver.openInputStream(uri);

                if (inputStream!=null && out!=null) {
                    PdfReader reader = new PdfReader(inputStream,ownerPassword.getBytes());

                    PdfReader.unethicalreading = true;

                    PdfStamper stamper = new PdfStamper(reader, out);
                    stamper.setEncryption(null,null,PdfWriter.ALLOW_COPY,PdfWriter.STRENGTH128BITS);

                    stamper.close();
                    reader.close();

                    Toast.makeText(this, "Pdf Unlocked Successful", Toast.LENGTH_SHORT).show();
                    values.put(MediaStore.MediaColumns.IS_PENDING,0);
                    resolver.update(unlockUri, values, null, null);
                    openPdf(unlockUri,UnlockPdfActivity.this);
                }

            } catch (IOException | DocumentException e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}