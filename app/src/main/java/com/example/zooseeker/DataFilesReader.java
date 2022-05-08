package com.example.zooseeker;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Auxiliary class to read an input stream, from context.getAssets().open(file), into a JSONObject
 */
public class DataFilesReader {
    public static JSONObject inputStreamToJSONObject(InputStream is) {
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
}
