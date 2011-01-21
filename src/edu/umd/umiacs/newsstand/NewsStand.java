package edu.umd.umiacs.newsstand;

import java.net.URL;
import java.util.List;
import java.util.TimerTask;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class NewsStand extends MapActivity {
    private MapView mapView = null;
    private SeekBar slider = null;

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.displayZoomControls(true);

        slider = (SeekBar) findViewById(R.id.slider);

        Button refreshButton = (Button) findViewById(R.id.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                try {
                    new MyAsyncTask().execute("");
                } catch (Exception e) {
                    Log.e(">>>>>>>>>>>> Error executing MyAsyncTask: ", e.getMessage(), e);
                }
            }
        });

        slider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                List<Overlay> mapOverlays = mapView.getOverlays();
                NewsStandItemizedOverlay o = (NewsStandItemizedOverlay) mapOverlays.get(0);
                o.setNumShown(progress, getApplicationContext());
                mapView.invalidate();
                //Toast.makeText(getApplicationContext(), 
                //        "My SeekBar = " + seekBar.getId() + ", Progress = " + progress, Toast.LENGTH_LONG)
                //        .show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                
            }
        });
        
        try {
            new MyAsyncTask().execute("");
        } catch (Exception e) {
            Log.e(">>>>>>>>>>>> Error executing MyAsyncTask: ", e.getMessage(), e);
        }
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

    private MarkerFeed getMarkers() {
        // get map coordinates
        GeoPoint centerpoint = mapView.getMapCenter();
        int lat_span = mapView.getLatitudeSpan();
        int lon_span = mapView.getLongitudeSpan();

        int lat_low = centerpoint.getLatitudeE6() - (lat_span / 2);
        int lat_high = lat_low + lat_span;
        int lon_low = centerpoint.getLongitudeE6() - (lon_span / 2);
        int lon_high = lon_low + lon_span;

        String marker_url = "http://newsstand.umiacs.umd.edu/news/xml_map?lat_low=%f&lat_high=%f&lon_low=%f&lon_high=%f";
        marker_url = String
                .format(marker_url, lat_low / 1.0E6, lat_high / 1.0E6,
                        lon_low / 1.0E6, lon_high / 1.0E6);
        // Toast.makeText(getApplicationContext(), marker_url,
        // Toast.LENGTH_LONG).show();

        return getFeed(marker_url);
    }
    
    private void setMarkers(MarkerFeed feed) {
        List<Overlay> mapOverlays = mapView.getOverlays();
        mapOverlays.clear();
        Drawable drawable = this.getResources().getDrawable(
                R.drawable.marker_general);
        NewsStandItemizedOverlay itemizedoverlay = new NewsStandItemizedOverlay(
                drawable, this);
        for (int i = 0; i < feed.getMarkerCount(); i++) {
            MarkerInfo cur_marker = feed.getMarker(i);
            GeoPoint point = new GeoPoint(
                    (int) (Float.valueOf(cur_marker.getLatitude()).floatValue() * 1E6),
                    (int) (Float.valueOf(cur_marker.getLongitude()).floatValue() * 1E6));
            OverlayItem overlayitem = new OverlayItem(point,
                    cur_marker.getTitle(), cur_marker.getSnippet());

            String cur_topic = cur_marker.getTopic();
            Log.i("NewsStand", "cur_topic[" + cur_topic + "]");

            int my_marker = 0;

            if (cur_topic.equals("General"))
                my_marker = R.drawable.marker_general;
            else if (cur_topic.equals("Business"))
                my_marker = R.drawable.marker_business;
            else if (cur_topic.equals("Entertainment"))
                my_marker = R.drawable.marker_entertainment;
            else if (cur_topic.equals("Health"))
                my_marker = R.drawable.marker_health;
            else if (cur_topic.equals("SciTech"))
                my_marker = R.drawable.marker_scitech;
            else if (cur_topic.equals("Sports"))
                my_marker = R.drawable.marker_sports;
            itemizedoverlay.addOverlay(overlayitem, this.getResources()
                    .getDrawable(my_marker));
        }
        if (feed.getMarkerCount() > 0) {
        	mapOverlays.add(itemizedoverlay);
        	mapView.invalidate();
        }

        slider.setMax(feed.getMarkerCount());
    
    }
    
    /*private void refreshMarkers() {

    	MarkerFeed feed;
    	
        // Toast.makeText(getApplicationContext(), "Refreshing...",
        // Toast.LENGTH_SHORT).show();
        feed = getMarkers();
        setMarkers(feed);
        
    }*/

    private MarkerFeed getFeed(String urlToRssFeed) {
        try {
            // set up the url
            URL url = new URL(urlToRssFeed);

            // create the factory
            SAXParserFactory factory = SAXParserFactory.newInstance();
            // create a parser
            SAXParser parser = factory.newSAXParser();

            // create the reader (scanner)
            XMLReader xmlreader = parser.getXMLReader();
            // instantiate our handler
            MarkerFeedHandler theMarkerFeedHandler = new MarkerFeedHandler();
            // assign our handler
            xmlreader.setContentHandler(theMarkerFeedHandler);
            // get our data via the url class
            InputSource is = new InputSource(url.openStream());
            // perform the synchronous parse
            xmlreader.parse(is);
            // get the results - should be a fully populated RSSFeed instance,
            // or null on error
            return theMarkerFeedHandler.getFeed();
        } catch (Exception ee) {
            // if we have a problem, simply return null
            Toast.makeText(getApplicationContext(), "Error fetching markers",
                    Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    
    public class MyAsyncTask extends AsyncTask<String, Integer, MarkerFeed> {

    	protected MarkerFeed doInBackground(String... string) {
            try {
            	return getMarkers();
            } catch (Exception e) {
                Log.e(">>>>>>>>>>>> Error getting myData: ", e.getMessage(), e);
                return null;
            }

        }

        protected void onProgressUpdate(Integer... progress) {
            // setProgressPercent(progress[0]);
        }

        protected void onPostExecute(MarkerFeed feed) {
            //Toast.makeText(getApplicationContext(),
            //        "Markers Downloaded.", Toast.LENGTH_SHORT)
            //        .show();
        	setMarkers(feed);
        }
    }

    // Create runnable for posting
    //final Runnable mUpdateResults = new Runnable() {
    //    public void run() {
    //        setMarkers();
    //    }
    //};

    
    public class MyTimerTask extends TimerTask {
        public void run() {
            try {
                new MyAsyncTask().execute("");
            } catch (Exception e) {
                Log.e(">>>>>>>>>>>> Error executing MyAsyncTask: ", e.getMessage(), e);
            }
        }
    }
    
}