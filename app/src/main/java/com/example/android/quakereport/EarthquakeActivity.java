package com.example.android.quakereport;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

         import android.app.LoaderManager.LoaderCallbacks;

    public class EarthquakeActivity extends AppCompatActivity implements LoaderCallbacks<List<Earthquake>> {
        /**
         * Constant value for the earthquake loader ID. We can choose any integer.
         * This really only comes into play if you're using multiple loaders.
         */
        private static final int EARTHQUAKE_LOADER_ID = 1;
        private static final String EARTHQUAKE_JSON_RESPONSE = "https://earthquake.usgs.gov/fdsnws/event/1/query";
        private EarthquakeAdapter mAdapter;

        /** TextView that is displayed when the list is empty */
        private TextView mEmptyStateTextView;
        public static final String LOG_TAG = EarthquakeActivity.class.getName();
        private ProgressBar progressBar;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.earthquake_activity);

            ListView earthquakeListView = (ListView) findViewById(R.id.list);

            mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
            earthquakeListView.setEmptyView(mEmptyStateTextView);
            progressBar = (ProgressBar) findViewById(R.id.loading_spinner);
            ConnectivityManager cm =
                    (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            if(isConnected) {

                // Get a reference to the LoaderManager, in order to interact with loaders.
                LoaderManager loaderManager = getLoaderManager();
                // Initialize the loader. Pass in the int ID constant defined above and pass in null for
                // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
                // because this activity implements the LoaderCallbacks interface).
                loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
                // Find a reference to the {@link ListView} in the layout
            }
            else {
                progressBar.setVisibility(View.GONE);
                mEmptyStateTextView.setText("No internet connection");
            }



            // Create a new adapter that takes an empty list of earthquakes as input
            mAdapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());
            // Set the adapter on the {@link ListView}
            // so the list can be populated in the user interface

            earthquakeListView.setAdapter(mAdapter);
            // Set an item click listener on the ListView, which sends an intent to a web browser
            // to open a website with more information about the selected earthquake.

            }




        @Override
        // onCreateLoader instantiates and returns a new Loader for the given ID
        public Loader<List<Earthquake>> onCreateLoader(int i, Bundle bundle) {

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            String orderBy  = sharedPrefs.getString(
                    getString(R.string.settings_order_by_key),
                    getString(R.string.settings_order_by_default)
            );
            // getString retrieves a String value from the preferences. The second parameter is the default value for this preference.
            String minMagnitude = sharedPrefs.getString(
                    getString(R.string.settings_min_magnitude_key),
                    getString(R.string.settings_min_magnitude_default));

            // parse breaks apart the URI string that's passed into its parameter
            Uri baseUri = Uri.parse(EARTHQUAKE_JSON_RESPONSE);

            // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
            Uri.Builder uriBuilder = baseUri.buildUpon();

            // Append query parameter and its value. For example, the `format=geojson`
            uriBuilder.appendQueryParameter("format", "geojson");
            uriBuilder.appendQueryParameter("limit", "10");
            uriBuilder.appendQueryParameter("minmag", minMagnitude);
            uriBuilder.appendQueryParameter("orderby", orderBy);

            // Return the completed uri `http://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&limit=10&minmag=minMagnitude&orderby=time
            return new EarthquakeLoader(this, uriBuilder.toString());

        }

        @Override
        public void onLoadFinished(android.content.Loader<List<Earthquake>> loader, List<Earthquake> data) {
            progressBar.setVisibility(View.GONE);
            mEmptyStateTextView.setText("No earthquakes found");
            // Clear the adapter of previous earthquake data
            mAdapter.clear();

            // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (data != null && !data.isEmpty()) {
                mAdapter.addAll(data);
            }

        }

        @Override
        public void onLoaderReset(android.content.Loader<List<Earthquake>> loader) {
            mAdapter.clear();
        }
        @Override
        // This method initialize the contents of the Activity's options menu.
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the Options Menu we specified in XML
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        @Override
        //This method passes the MenuItem that is selected
        public boolean onOptionsItemSelected(MenuItem item) {
            //To determine which item was selected and what action to take, we call getItemId, which returns the unique ID for the menu item
            int id = item.getItemId();
            if (id == R.id.action_settings) {
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

