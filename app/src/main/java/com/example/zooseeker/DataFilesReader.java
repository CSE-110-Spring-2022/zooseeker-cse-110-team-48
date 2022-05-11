package com.example.zooseeker;

import android.content.Context;

import org.jgrapht.Graph;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * This class implements a DataFilesReader object which handles reading a json file,
 *  containing a list of json files for graph attributes.
 *  Contains methods to return objects representing Graphs, and edge/vertex mappings to info
 */
public class DataFilesReader {
    private Context parentContext;
    private JSONObject zooGraphFiles;
    private String graph_file_path;
    private String node_info_path;
    private String edge_info_path;

    /**
     * Ctor for a reader object which handles reading graph data
     * @param context - context of parent of this object
     * @param jsonListFile - filename of json path list in Assets folder
     */
    public DataFilesReader (Context context, String jsonListFile) {
        try {
            // Creates input stream from file with json asset paths, using args
            parentContext = context;
            InputStream is = parentContext.getAssets().open(jsonListFile);

            // Create JSONObject containing file paths of graph assets
            this.zooGraphFiles = inputStreamToJSONObject(is);

            // Set individual file paths
            this.graph_file_path = zooGraphFiles.getString("graph_file");
            this.node_info_path = zooGraphFiles.getString("vertex_file");
            this.edge_info_path = zooGraphFiles.getString("edge_file");

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a JSONObject given an input stream of a json file
     * @param is - input stream representing json file
     * @return JSONObject of file
     */
    private static JSONObject inputStreamToJSONObject(InputStream is) {
        // Read stream by bytes into a string, and return a JSONObject read from that string
        try {
            byte[] bytes = new byte[is.available()];
            is.read(bytes, 0, bytes.length);
            String json = new String(bytes);
            JSONObject jObject = new JSONObject(json);
            return jObject;
        } catch (IOException | JSONException e) {
            return null;
        }
    }

    /**
     * Returns the zoo's graph
     * @return Graph containing vertex and edge information
     */
    public Graph<String, IdentifiedWeightedEdge> getGraph() {
        return ZooData.loadZooGraphJSON(parentContext, graph_file_path);
    }

    /**
     * Returns vertex info
     * @return Mapping from String vertices to info about those vertices
     */
    public Map<String, ZooData.VertexInfo> getVertexInfo () {
        return ZooData.loadVertexInfoJSON(parentContext, node_info_path);
    }

    /**
     * Returns edge info
     * @return Mapping from String edges to info about those edges
     */
    public Map<String, ZooData.EdgeInfo> getEdgeInfo () {
        return ZooData.loadEdgeInfoJSON(parentContext, edge_info_path);
    }

    /**
     * Returns the id of the first gate found in the json files
     *  If no gate is present, return null
     * @return id of zoo gate
     */
    public String getGateId() {
        Map<String, ZooData.VertexInfo> vertexInfo = this.getVertexInfo();
        for (String location : vertexInfo.keySet()) {
            if (vertexInfo.get(location).kind.equals(ZooData.VertexInfo.Kind.GATE)) {
                return vertexInfo.get(location).id;
            }
        }
        return null;
    }
}
