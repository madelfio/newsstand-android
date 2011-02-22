package edu.umd.umiacs.newsstand;

import java.net.URL;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class NewsStandRefresh {
    
    private Context _ctx;
    private NewsStandMapView _mapView = null;
    private SeekBar _slider = null;
    private NewsStandMapPopupPanel _popup_panel = null;
    private Resources _resources = null;
    private int m_num_executing = 0;
    private Lock l = new ReentrantLock();
    public int m_show_idx = 0;
    public int m_ajax_idx = 0;

    public int m_lat_l = 0;
    public int m_lat_h = 0;
    public int m_lon_l = 0;
    public int m_lon_h = 0;

    
    public NewsStandRefresh(Context ctx, NewsStandMapView mapView, SeekBar slider, NewsStandMapPopupPanel popup_panel) {
        _ctx = ctx;
        _mapView = mapView; 
        _slider = slider;
        _popup_panel = popup_panel;
        _resources = ctx.getResources();
    }
    
    public void execute() {
        try {
            if (m_num_executing < 3) {
                if (curBoundsDiffer()) {
                    updateBounds();
                    m_num_executing++;
                    new RefreshTask().execute("");
                }
            } 
        } catch (Exception e) {
            Log.e(">>>>>>>>>>>> Error executing MyAsyncTask: ", e.getMessage(), e);
        }
    }
    
    public void executeForce() {
        updateBounds();
        m_num_executing++;
        new RefreshTask().execute("");
    }
    
    public void clearSavedLocation() {
        m_lat_l = 0;
        m_lat_h = 0;
        m_lon_l = 0;
        m_lon_h = 0;
    }
    
    private boolean curBoundsDiffer() {
        GeoPoint centerpoint = _mapView.getMapCenter();
        int lat_span = _mapView.getLatitudeSpan();
        int lon_span = _mapView.getLongitudeSpan();

        int lat_l = centerpoint.getLatitudeE6() - (lat_span / 2);
        int lat_h = lat_l + lat_span;
        int lon_l = centerpoint.getLongitudeE6() - (lon_span / 2);
        int lon_h = lon_l + lon_span;
        
        return (lat_l != m_lat_l || lat_h != m_lat_h || lon_l != m_lon_l || lon_h != m_lon_h);
    }

    private void updateBounds() {
        GeoPoint centerpoint = _mapView.getMapCenter();
        int lat_span = _mapView.getLatitudeSpan();
        int lon_span = _mapView.getLongitudeSpan();

        m_lat_l = centerpoint.getLatitudeE6() - (lat_span / 2);
        m_lat_h = m_lat_l + lat_span;
        m_lon_l = centerpoint.getLongitudeE6() - (lon_span / 2);
        m_lon_h = m_lon_l + lon_span;
    }
    
    private MarkerFeed getMarkers() {
        // get map coordinates
        String marker_url = "http://newsstand.umiacs.umd.edu/news/xml_map?lat_low=%f&lat_high=%f&lon_low=%f&lon_high=%f";
        marker_url = String
                .format(marker_url, 
                        m_lat_l / 1E6, m_lat_h / 1E6,
                        m_lon_l / 1E6, m_lon_h / 1E6);
        
        if (_mapView.current_search != null && _mapView.current_search != "") {
            marker_url += String.format("&search=%s", _mapView.current_search);
        }
        
        Log.i("NewsStand", "marker_url[" + marker_url + "]");

        return getFeed(marker_url);
    }
    
    private void setMarkers(MarkerFeed feed) {
        l.lock();
        try {
            List<Overlay> mapOverlays = _mapView.getOverlays();
            Drawable drawable = _resources.getDrawable(
                    R.drawable.marker_general);
            NewsStandItemizedOverlay itemizedoverlay = new NewsStandItemizedOverlay(
                    drawable, _ctx, _popup_panel);
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
                itemizedoverlay.setPctShown(_slider.getProgress(), _ctx);
                mapOverlays.clear();
                mapOverlays.add(itemizedoverlay);
                _mapView.invalidate();
            }
        }
        finally {
            l.unlock();
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

        private int refresh_idx;

        protected MarkerFeed doInBackground(String... string) {
            try {
                m_ajax_idx++;
                refresh_idx = m_ajax_idx;
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
            if (feed != null) {
                if (refresh_idx > m_show_idx) {
                    m_show_idx = refresh_idx;
                    setMarkers(feed);
                }
                m_num_executing--;
            } else {
                Toast.makeText(_ctx, "Null marker feed...", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
