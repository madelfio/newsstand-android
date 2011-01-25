package edu.umd.umiacs.newsstand;

import java.net.URL;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class NewsStandRefresh {
    
    private Context _ctx;
    private NewsStandMapView _mapView = null;
    private Resources _resources = null;
    
    public NewsStandRefresh(Context ctx, NewsStandMapView mapView) {
        _ctx = ctx;
        _mapView = mapView; 
        _resources = ctx.getResources();
    }
    
    public void execute() {
        try {
            new RefreshTask().execute("");
        } catch (Exception e) {
            Log.e(">>>>>>>>>>>> Error executing MyAsyncTask: ", e.getMessage(), e);
        }        
    }
    
    private MarkerFeed getMarkers() {
        // get map coordinates
        _mapView.updateMapWindow();
        
        String marker_url = "http://newsstand.umiacs.umd.edu/news/xml_map?lat_low=%f&lat_high=%f&lon_low=%f&lon_high=%f";
        marker_url = String
                .format(marker_url, 
                        _mapView.lat_low / 1E6, _mapView.lat_high / 1E6,
                        _mapView.lon_low / 1E6, _mapView.lon_high / 1E6);

        return getFeed(marker_url);
    }
    
    private void setMarkers(MarkerFeed feed) {
        List<Overlay> mapOverlays = _mapView.getOverlays();
        mapOverlays.clear();
        Drawable drawable = _resources.getDrawable(
                R.drawable.marker_general);
        NewsStandItemizedOverlay itemizedoverlay = new NewsStandItemizedOverlay(
                drawable, _ctx);
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
            itemizedoverlay.addOverlay(overlayitem, _resources.getDrawable(my_marker));
        }
        if (feed.getMarkerCount() > 0) {
            mapOverlays.add(itemizedoverlay);
            _mapView.invalidate();
        }

    }
    
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
            Toast.makeText(_ctx, "Error fetching markers",
                    Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public class RefreshTask extends AsyncTask<String, Integer, MarkerFeed> {

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
            //Toast.makeText(_ctx,
            //        "Markers Downloaded.", Toast.LENGTH_SHORT)
            //        .show();
            if (feed != null) {
                setMarkers(feed);
            }
        }
    }
}
