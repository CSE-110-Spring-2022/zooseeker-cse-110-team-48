package com.example.zooseeker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class PlanningListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning_list);
        /*ExhibitDao ExhibitDao = ExhibitDatabase.getSingleton(this).ExhibitDao();
        List<Exhibit> Exhibits = ExhibitDao.getAll();
        PlanningListAdapter adapter = new PlanningListAdapter();
        adapter.setHasStableIds(true);
        adapter.setExhibits(Exhibits);
        recyclerView = findViewById(R.id.exhibits);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.setExhibits(Exhibit.loadJSON(this, "demo_exhibits.json"));*/
    }
}