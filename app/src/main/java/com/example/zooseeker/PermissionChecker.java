package com.example.zooseeker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

/**
 * Permission checker class imported from Lab 7 from class
 * Used to check if gps location requests are permitted
 */
public class PermissionChecker {
    private ComponentActivity activity;
    final ActivityResultLauncher<String[]> requestPermissionLauncher;

    /**
     * Ctor for permission checker
     * @param activity - activity which needs to use locations
     */
    public PermissionChecker(ComponentActivity activity) {
        this.activity = activity;
        requestPermissionLauncher = activity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), perms -> {
            perms.forEach((perm, isGranted) -> {
                Log.i("ZooSeeker", String.format("Permission %s granted: %s", perm, isGranted));
            });
        });
    }

    /**
     * Ensures that the app has permissions to use locations
     * @return true if permissions needed to be requested, false if permissions already obtained
     */
    boolean ensurePermissions() {
        String[] requiredPermissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        boolean hasNoLocationPerms = Arrays.stream(requiredPermissions)
                .map(perm -> ContextCompat.checkSelfPermission(activity, perm))
                .allMatch(status -> status == PackageManager.PERMISSION_DENIED);

        if (hasNoLocationPerms) {
            requestPermissionLauncher.launch(requiredPermissions);
            return true;
        }
        return false;
    }
}