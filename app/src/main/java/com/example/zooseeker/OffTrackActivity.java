package com.example.zooseeker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class OffTrackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offtrack);

        Intent intent = getIntent();

        Button yesButton = findViewById(R.id.yes_btn);
        yesButton.setOnClickListener(v -> {
            RouteSingleton.getRoute().reroute(intent.getExtras().getString("new_start_id"));
            finish();
        });

        Button noButton = findViewById(R.id.no_btn);
        noButton.setOnClickListener(v -> {
            finish();
        });
    }
}
