package com.example.zooseeker;

//IMPORTANT: make sure to add your package name here (example -> package com.example.jgraphsample;)

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.ArrayAdapter;

import com.google.gson.Gson;

import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.*;
import org.jgrapht.nio.json.JSONImporter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Paths to files
    public String graph_file;
    public String node_info_file;
    public String edge_info_file;

    // ViewModel for database
    private LocationsDatabase db;
    private LocationsListViewModel viewModel;
    private LocationsListItemDao locationsListItemDao;

    // Search domain
    protected ArrayList<String> searchList;
    private ArrayAdapter<String> arrayAdapter;
    private ListView suggestions;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load zoo data files
        try {
            InputStream is = this.getAssets().open("zoo_data_files.json");
            JSONObject zooDataFiles = DataFilesReader.inputStreamToJSONObject(is);
            this.graph_file = zooDataFiles.getString("graph_file");
            this.node_info_file = zooDataFiles.getString("vertex_file");
            this.edge_info_file = zooDataFiles.getString("edge_file");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        // Load graph into app
        Graph<String, IdentifiedWeightedEdge> zooGraph = ZooData.loadZooGraphJSON(this, graph_file);
        // Load graph info
        Map<String, ZooData.VertexInfo> vertexInfo = ZooData.loadVertexInfoJSON(this, node_info_file);
        Map<String, ZooData.EdgeInfo> edgeInfo = ZooData.loadEdgeInfoJSON(this, edge_info_file);

        searchList = new ArrayList<>();
        for (String location : vertexInfo.keySet()) {
            searchList.add(vertexInfo.get(location).name);
        }

        this.viewModel = new ViewModelProvider(this).get(LocationsListViewModel.class);
        Context context = getApplication().getApplicationContext();
        db = LocationsDatabase.getSingleton(context);
        locationsListItemDao = db.locationsListItemDao();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, searchList);
        AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.search_field);
        actv.setThreshold(1);
        actv.setAdapter(adapter);

        Button planningListButton = findViewById(R.id.view_list_btn);
        updateListCount();

        actv.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        List<LocationsListItem> curr_db = locationsListItemDao.getAll();
                        String query = adapter.getItem(position).toString();
                        for (LocationsListItem location : curr_db) {
                            if (location.text.equals(query)) {
                                updateListCount();
                                return;
                            }
                        }
                        // Find vertex id of query
                        String vertexId = "";
                        for (String key : vertexInfo.keySet()) {
                            if (vertexInfo.get(key).name == query) {
                                vertexId = key;
                            }
                        }
                        viewModel.createLocation(query, vertexId, 0);
                        updateListCount();
                    }
                }
        );

        planningListButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        launchPlanningList(v);
                    }
                }
        );

    }

    /**
     * Launches activity for viewing / editing planning list
     * @param view - current view
     */
    public void launchPlanningList(View view) {
        Intent intent = new Intent(this, LocationsListActivity.class);
        intent.putExtra("graph_file", this.graph_file);
        intent.putExtra("node_info_file", this.node_info_file);
        intent.putExtra("edge_info_file", this.edge_info_file);
        startActivity(intent);
    }

    public void onResume() {
        super.onResume();
        updateListCount();
    }
    public void updateListCount(){
        Context context = getApplication().getApplicationContext();
        db = LocationsDatabase.getSingleton(context);
        locationsListItemDao = db.locationsListItemDao();

        Button planningListButton = findViewById(R.id.view_list_btn);
        planningListButton.setText("Plan (" + locationsListItemDao.getDataCount() + ")");
    }
}