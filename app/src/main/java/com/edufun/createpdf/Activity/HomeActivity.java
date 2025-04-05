package com.edufun.createpdf.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edufun.createpdf.R;
import com.edufun.createpdf.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {
    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().setStatusBarColor(getResources().getColor(R.color.blue));

        binding.mergePdf.setOnClickListener(v -> {
            Intent in = new Intent(this,MergePdfActivity.class);
            startActivity(in);
        });
        binding.textToPdf.setOnClickListener(v -> {
            Intent in = new Intent(this, TextToPdfActivity.class);
            startActivity(in);
        });
        binding.imageToPdf.setOnClickListener(v -> {
            Intent in = new Intent(this, ImageToPdfActivity.class);
            startActivity(in);
        });
        binding.SplitPdf.setOnClickListener(v -> {
            Intent in = new Intent(this,SplitPdfActivity.class);
            startActivity(in);
        });
        binding.protectPdf.setOnClickListener(v -> {
            Intent in = new Intent(this,LockPdfActivity.class);
            startActivity(in);
        });
        binding.unlockPdf.setOnClickListener(v -> {
            Intent in = new Intent(this,UnlockPdfActivity.class);
            startActivity(in);
        });
    }
}