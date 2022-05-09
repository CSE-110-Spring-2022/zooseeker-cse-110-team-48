package com.example.zooseeker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouteActivity extends AppCompatActivity {

    List<String> exhibitsInPlan;
    int directionsIndex;
    public RecyclerView recyclerView;
    public LocationsListViewModel viewModel;
    private TextView directionView;

    private ArrayList<String> directions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        Button exit_button = findViewById(R.id.exit_button);

        exit_button.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        onClosedClicked(v);
                    }
                }
        );

        Intent intent = getIntent();
        this.directions = (ArrayList<String>) intent.getSerializableExtra("directions_list");
        directionView = findViewById(R.id.directions);

        updateDirection(this.directions.get(0));
        directionsIndex = 1;



        // Next direction
        Button nextButton = findViewById(R.id.next_route_button);
        nextButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (directionsIndex < directions.size()) {
                            String result = directions.get(directionsIndex);
                            updateDirection(result);
                            directionsIndex++;
                        }
                        else {
                            directionsIndex = 0;
                            finish();
                        }
                    }
                }
        );
    }

    private void updateDirection(String result) {
        this.directionView.setText(result);
    }


    public void onClosedClicked(View view) {
        finish();
    }
}