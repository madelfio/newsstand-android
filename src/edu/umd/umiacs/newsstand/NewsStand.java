package edu.umd.umiacs.newsstand;

import java.util.List;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.google.android.maps.Overlay;

public class NewsStand extends MapActivity {
    private SharedPreferences prefs;
    private NewsStandMapView mapView = null;
    private SeekBar slider = null;
    private NewsStandMapPopupPanel popup_panel = null;
    private NewsStandRefresh refresh = null;

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
        initPopupPanel();
        
        // handle search requests
        handleIntent(getIntent());
        
        // initialize Refresh processing and call it
        initRefresh();
        mapView.setRefresh(refresh);
        
        Handler mHandler = new Handler();
        mHandler.postDelayed(refresh, 1000);
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
    
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            mapView.addSearch(query);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        refresh.clearSavedLocation();
        mapView.updateMapWindowForce();
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
                    NewsStandItemizedOverlay o = (NewsStandItemizedOverlay) mapOverlays.get(0);
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
    
    private void initPopupPanel() {
        popup_panel = new NewsStandMapPopupPanel(this, R.layout.map_popup, mapView);
    }

    private void initRefresh() {
        refresh = new NewsStandRefresh(this, mapView, slider, popup_panel, prefs);
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
            Intent i = new Intent(this, NewsStandPreferences.class);
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
}