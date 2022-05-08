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

import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouteActivity extends AppCompatActivity {

    List<String> exhibitsInPlan;
    int directionsIndex;
    public RecyclerView recyclerView;
    public LocationsListViewModel viewModel;
    private EditText newLocationText;

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

        directionsIndex = 0;

        Button nextButton = findViewById(R.id.next_route_button);
        nextButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (directionsIndex < directions.size()) {
                            String result = directions.get(directionsIndex);
                            directionsIndex++;
                        }
                        else {
                            finish();
                        }
                    }
                }
        );
    }

    public void nextLocation(GraphRoute route) {
        ArrayList<String> directionEdges = route.advanceToNextExhibit();
    }

    public void onClosedClicked(View view) {
        finish();
    }
}