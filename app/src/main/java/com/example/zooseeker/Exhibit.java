package com.example.zooseeker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simple class representing an instantiable exhibit object
 */
public class Exhibit {
    public String id;
    public String name;
    public List<String> tags;

    /**
     * Ctor for simple exhibit object
     * @param id - raw id of exhibit (e.g. "arctic_foxes")
     * @param name - proper name (e.g. "Arctic Foxes")
     * @param tags - List of tags to search for
     */
    public Exhibit(String id, String name, List<String> tags) {
        this.id = id;
        this.name = name;
        this.tags = tags;
    }

    public static ArrayList<Exhibit> returnExhibits(Map<String, ZooData.VertexInfo> vertexInfo) {
        ArrayList<Exhibit> exhibitList = new ArrayList<>();

        for (String location : vertexInfo.keySet()) {
            ZooData.VertexInfo v = vertexInfo.get(location);
            if (v != null && v.kind.equals(ZooData.VertexInfo.Kind.EXHIBIT)) {
                List<String> tags = v.tags;
                // Add parent name to tags if exhibit is part of an exhibit group
                if (v.group_id != null) {
                    tags.add(vertexInfo.get(v.group_id).name);
                }
                Exhibit e = new Exhibit(v.id, v.name, tags);
                exhibitList.add(e);
            }
        }

        return exhibitList;
    }
}
