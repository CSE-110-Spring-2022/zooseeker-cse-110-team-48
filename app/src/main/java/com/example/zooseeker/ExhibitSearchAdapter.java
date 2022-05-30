package com.example.zooseeker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Custom ArrayAdapter which takes Exhibit objects as generic search items
 */
public class ExhibitSearchAdapter extends ArrayAdapter<Exhibit> {
    private LayoutInflater layoutInflater;
    List<Exhibit> mExhibits;

    /**
     * Filter object for searching, adds appropriate exhibits to suggestions list
     */
    private Filter mFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            return ((Exhibit) resultValue).name;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null) {
                ArrayList<Exhibit> suggestions = new ArrayList<Exhibit>();

                // Scans each exhibit in Exhibit search domain list
                for (Exhibit exhibit : mExhibits) {
                    // Adds exhibit to suggestions if tags match
                    for (String tag : exhibit.tags) {
                        tag = tag.toLowerCase();
                        if (tag.contains(constraint.toString().toLowerCase())) {
                            suggestions.add(exhibit);
                            break;
                        }
                    }
                }

                results.values = suggestions;
                results.count = suggestions.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results != null && results.count > 0) {
                // we have filtered results
                addAll((ArrayList<Exhibit>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    /**
     * Ctor for adapter, used for the AutoCompleteTextView in Main (the search bar)
     * @param context - context of parent activity
     * @param textViewResourceId - Resource ID of TextView to inject search suggestions into
     * @param exhibits - List of exhibits to use as search domain
     */
    public ExhibitSearchAdapter(Context context, int textViewResourceId, List<Exhibit> exhibits) {
        super(context, textViewResourceId, exhibits);
        // copy all the customers into a master list
        mExhibits = new ArrayList<Exhibit>(exhibits.size());
        mExhibits.addAll(exhibits);
        layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Performs the injection of suggestions into the TextViews of the search results
     * @param position - position of TextView
     * @param convertView - View to inflate
     * @param parent - Parent group of view
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(android.R.layout.select_dialog_item, null);
        }

        Exhibit exhibit = getItem(position);

        TextView name = (TextView) view.findViewById(android.R.id.text1);
        name.setText(exhibit.name);

        return view;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }
}