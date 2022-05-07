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
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
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
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Paths to files
    public final String GRAPH_FILE = "sample_zoo_graph.json";
    public final String NODE_INFO = "sample_node_info.json";
    public final String EDGE_INFO = "sample_edge_info.json";

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

        // Load graph into app
        Graph<String, IdentifiedWeightedEdge> zooGraph = ZooData.loadZooGraphJSON(this, GRAPH_FILE);
        // Load graph info
        Map<String, ZooData.VertexInfo> vertexInfo = ZooData.loadVertexInfoJSON(this, NODE_INFO);
        Map<String, ZooData.EdgeInfo> edgeInfo = ZooData.loadEdgeInfoJSON(this, EDGE_INFO);

        searchList = new ArrayList<String>();
        for (String location : vertexInfo.keySet()) {
            searchList.add(vertexInfo.get(location).name);
        }

        this.viewModel = new ViewModelProvider(this).get(LocationsListViewModel.class);
        Context context = getApplication().getApplicationContext();
        db = LocationsDatabase.getSingleton(context);
        locationsListItemDao = db.locationsListItemDao();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, searchList);
        AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.search_field);
        actv.setThreshold(1);
        actv.setAdapter(adapter);

        Button planningListButton = findViewById(R.id.view_list_btn);
        Button planRouteButton = findViewById(R.id.plan_route_btn);

        actv.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        List<LocationsListItem> curr_db = locationsListItemDao.getAll();
                        String query = adapter.getItem(position).toString();
                        for (LocationsListItem location : curr_db) {
                            if (location.text.equals(query)) {
                                return;
                            }
                        }
                        viewModel.createLocation(query);
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

        planRouteButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        launchRoutePlan(v);
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
        startActivity(intent);
    }

    public void launchRoutePlan(View view) {
        Intent intent = new Intent(this, RouteActivity.class);
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