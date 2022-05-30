package com.example.zooseeker;

import android.app.Activity;
import android.app.AlertDialog;

import java.util.Optional;

public class WarningUtilities {
    /**
     * This method shows an alert pop-up for an activity, when an error occurs
     * @param activity - parent activity of pop-up
     * @param message - message to display
     */
    public static void showError(Activity activity, String message) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);

        alertBuilder
                .setTitle("Error!")
                .setMessage(message)
                .setPositiveButton("Ok", (dialog, id) -> {
                    dialog.cancel();
                })
                .setCancelable(true);

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }
}
