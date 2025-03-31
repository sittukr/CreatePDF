package com.edufun.createpdf.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.edufun.createpdf.MainActivity;
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

        binding.mergePdf.setOnClickListener(v -> {
            Intent in = new Intent(this,MergePdfActivity.class);
            startActivity(in);
        });
        binding.textToPdf.setOnClickListener(v -> {
            Intent in = new Intent(this, MainActivity.class);
            startActivity(in);
        });
        binding.imageToPdf.setOnClickListener(v -> {
            Intent in = new Intent(this, ImageToPdfActivity.class);
            startActivity(in);
        });
    }
}