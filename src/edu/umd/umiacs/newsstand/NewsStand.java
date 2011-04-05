package edu.umd.umiacs.newsstand;

import java.util.List;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.google.android.maps.Overlay;

public class NewsStand extends MapActivity implements View.OnClickListener {
    private SharedPreferences prefs;
    private NewsStandMapView mapView = null;
    private SeekBar slider = null;
    private Refresh refresh = null;

    public String mSearchQuery;
    private LinearLayout mSearchLayout;
    private TextView mSearchView;

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // initialize main MapActivity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // initialize user preferences
        initPrefs();

        // initialize UI
        initMapView();
        initSlider();

        // handle search requests
        handleIntent(getIntent());

        // initialize Refresh processing object
        initRefresh();
        mapView.setRefresh(refresh);
    }

    /** load default or saved prefs into prefs object **/
    public void initPrefs() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    /** Handle incoming intents **/
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            addSearch(query);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh.clearSavedLocation();
        mapUpdateForce();
    }

    /** Delayed call to map refresh function.
     *
     *  Without delay, the refresh does not always happen. **/
    public void mapUpdateForce() {
        Handler mHandler = new Handler();
        mHandler.postDelayed(refresh, 250);
    }

    private void initMapView() {
        mapView = (NewsStandMapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(false);
    }

    private void initSlider() {
        slider = (SeekBar) findViewById(R.id.slider);
        slider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                List<Overlay> mapOverlays = mapView.getOverlays();
                if (mapOverlays.size() > 0) {
                    MarkerOverlay o = (MarkerOverlay) mapOverlays.get(0);
                    o.setPctShown(progress, getApplicationContext());
                    mapView.invalidate();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void initRefresh() {
        refresh = new Refresh(this, mapView, slider, prefs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.search:
            onSearchRequested();
            break;
        //case R.id.locate:
        case R.id.settings:
            Intent i = new Intent(this, Settings.class);
            startActivity(i);
            break;
        case R.id.locate:
        case R.id.sources:
        case R.id.home:
        case R.id.top_stories:
        default:
            Toast.makeText(getApplicationContext(),
                    "Functionality not yet implemented.", Toast.LENGTH_SHORT)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void addSearch(String query) {
        mSearchQuery = query;
        LinearLayout searchOptions = (LinearLayout)findViewById(R.id.search_options);

        mSearchLayout = new LinearLayout(this);
        mSearchLayout.setOrientation(LinearLayout.HORIZONTAL);
        mSearchLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        TextView search_view = new TextView(this);
        search_view.setText("Search: " + query);
        search_view.setTextColor(Color.BLUE);
        search_view.setTextSize(16);
        mSearchLayout.addView(search_view, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                                         LinearLayout.LayoutParams.WRAP_CONTENT));

        // Instantiate an ImageView and define its properties
        ImageView mSearchView = new ImageView(this);
        mSearchView.setImageResource(R.drawable.ic_delete);
        mSearchView.setAdjustViewBounds(true); // set the ImageView bounds to match the Drawable's dimensions
        MarginLayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(3,6,0,0);
        mSearchView.setLayoutParams(lp);
        mSearchView.setOnClickListener(this);
        mSearchLayout.addView(mSearchView);

        searchOptions.addView(mSearchLayout);
        Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
        mapView.updateMapWindowForce();
    }

    public void clearSearch () {
        mSearchQuery = "";
        LinearLayout searchOptions = (LinearLayout)findViewById(R.id.search_options);
        searchOptions.removeView(mSearchLayout);
        Toast.makeText(this, "Search cleared.", Toast.LENGTH_SHORT).show();
        mapView.updateMapWindowForce();
    }

    @Override
    public void onClick(View v) {
        if (v == mSearchView) {
            clearSearch();
        }
    }
}
