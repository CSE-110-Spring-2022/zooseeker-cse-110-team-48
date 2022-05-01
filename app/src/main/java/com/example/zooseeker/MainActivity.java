package com.example.zooseeker;

//IMPORTANT: make sure to add your package name here (example -> package com.example.jgraphsample;)

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.ArrayAdapter;

import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.*;
import org.jgrapht.nio.json.JSONImporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Paths to files
    public final String GRAPH_FILE = "sample_zoo_graph.json";
    public final String NODE_INFO = "sample_node_info.json";
    public final String EDGE_INFO = "sample_edge_info.json";

    // ViewModel for database
    private LocationsListViewModel viewModel;

    // Search domain
    protected ArrayList<String> searchList;
    private ArrayAdapter<String> searchAdapter;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load graph into app
        Graph<String, IdentifiedWeightedEdge> zooGraph = ZooData.loadZooGraphJSON(this, GRAPH_FILE);
        // Load graph info
        Map<String, ZooData.VertexInfo> vertexInfo = ZooData.loadVertexInfoJSON(this, NODE_INFO);
        Map<String, ZooData.EdgeInfo> edgeInfo = ZooData.loadEdgeInfoJSON(this, EDGE_INFO);

        searchList = new ArrayList<String>();
        for (String location : vertexInfo.keySet()) {
            searchList.add(vertexInfo.get(location).name);
        }

        searchAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, searchList);

        this.viewModel = new ViewModelProvider(this).get(LocationsListViewModel.class);

        SearchView searchBar = findViewById(R.id.search_bar);
        Button planningListButton = findViewById(R.id.view_list_btn);

        searchBar.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if (searchList.contains(query)) {
                            viewModel.createLocation(query);
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
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

        //Intent locationIntent = new Intent(this, LocationsListActivity.class);
        //startActivity(locationIntent);

        /*
        //Create graph
        Graph<String, DefaultWeightedEdge> g = null;
        try {
            //Reads in graph from JSON file
            g = createGraphFromJSON(this,"sample_graph.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Tests the generated graph against a shortest algorithm function from jgrpaht
        calculateShortestPath(g);
        */
    }

    /**
     * Launches activity for viewing / editing planning list
     * @param view - current view
     */
    public void launchPlanningList(View view) {
        Intent intent = new Intent(this, LocationsListActivity.class);
        startActivity(intent);
    }

    public void calculateShortestPath(Graph<String, DefaultWeightedEdge> g) {
        GraphPath<String, DefaultWeightedEdge> shortest_path = DijkstraShortestPath.findPathBetween(g, "entranceExitGate1", "arcticFoxViewpoint");
        System.out.println("Shortest path from entranceExitGate1 to arcticFoxViewpoint: \n" + shortest_path.toString());
    }

    public static Graph<String, DefaultWeightedEdge> createGraphFromJSON(Context context, String path) throws IOException {
        Graph<String, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        JSONImporter<String, DefaultWeightedEdge> jsonImporter = new JSONImporter<>();
        jsonImporter.setVertexFactory(label -> label);

        InputStream input = context.getAssets().open(path);
        Reader reader = new InputStreamReader(input);
        jsonImporter.importGraph(g, reader);

        return g;
    }
}