package edu.umd.umiacs.newsstand;

import java.util.List;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.google.android.maps.Overlay;

public class NewsStand extends MapActivity {
    private NewsStandMapView mapView = null;
    private SeekBar slider = null;
    private NewsStandRefresh refresh = null;

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initMapView();
        initSlider();
        initRefresh();
        mapView.setRefresh(refresh);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.clearSavedLocation();
        mapView.updateMapWindow();
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
                NewsStandItemizedOverlay o = (NewsStandItemizedOverlay) mapOverlays.get(0);
                o.setPctShown(progress, getApplicationContext());
                mapView.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void initRefresh() {
        refresh = new NewsStandRefresh(this, mapView, slider);
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
        case R.id.locate:
        case R.id.settings:
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