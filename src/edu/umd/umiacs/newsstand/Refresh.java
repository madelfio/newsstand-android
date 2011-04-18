package edu.umd.umiacs.newsstand;

import java.net.URL;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class Refresh implements Runnable {

    private final NewsStand _ctx;
    private NewsStandMapView _mapView = null;
    private SeekBar _slider = null;
    private Resources _resources = null;
    private SharedPreferences _prefs = null;
    private int m_num_executing = 0;
    public int m_show_idx = 0;
    public int m_ajax_idx = 0;

    public int m_lat_l = 0;
    public int m_lat_h = 0;
    public int m_lon_l = 0;
    public int m_lon_h = 0;

    public Refresh(Context ctx, NewsStandMapView mapView, SeekBar slider, SharedPreferences prefs) {
        _ctx = (NewsStand)ctx;
        _mapView = mapView;
        _slider = slider;
        _resources = ctx.getResources();
        _prefs = prefs;
    }

    @Override
    public void run() {
        executeForce();
    }

    public void execute() {
        if (m_num_executing < 3) {
            if (curBoundsDiffer()) {
                updateBounds();
                new RefreshTask().execute("");
            }
        }
    }

    public void executeForce() {
        updateBounds();
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

        if (_ctx.mSearchQuery != null && _ctx.mSearchQuery != "") {
            marker_url += String.format("&search=%s", _ctx.mSearchQuery);
        }

        marker_url += topicQuery();

        return getFeed(marker_url);
    }

    private String topicQuery() {
        if (_prefs.getBoolean("all_topics", false)) {
            // add nothing to query string if showing all topics
        }
        else {
            String topics = "";
            if (_prefs.getBoolean("general_topics", false)) {
                topics += "'General',";
            }
            if (_prefs.getBoolean("business_topics", false)) {
                topics += "'Business',";
            }
            if (_prefs.getBoolean("scitech_topics", false)) {
                topics += "'SciTech',";
            }
            if (_prefs.getBoolean("entertainment_topics", false)) {
                topics += "'Entertainment',";
            }
            if (_prefs.getBoolean("health_topics", false)) {
                topics += "'Health',";
            }
            if (_prefs.getBoolean("sports_topics", false)) {
                topics += "'Sports',";
            }
            if (topics.length() > 0) {
                return String.format("&cat=(%s)", topics.substring(0, topics.length()-1));
            }
        }
        return "";
    }

    private void setMarkers(MarkerFeed feed) {
        List<Overlay> mapOverlays = _mapView.getOverlays();
        Drawable drawable = _resources.getDrawable(
                R.drawable.marker_general);
        MarkerOverlay itemizedoverlay = new MarkerOverlay(drawable, _ctx);
        for (int i = 0; i < feed.getMarkerCount(); i++) {
            MarkerInfo cur_marker = feed.getMarker(i);
            GeoPoint point = new GeoPoint(
                    (int) (Float.valueOf(cur_marker.getLatitude()).floatValue() * 1E6),
                    (int) (Float.valueOf(cur_marker.getLongitude()).floatValue() * 1E6));
            OverlayItem overlayitem = new OverlayItem(point,
                    cur_marker.getTitle(), cur_marker.getSnippet());

            String cur_topic = cur_marker.getTopic();

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
            else {
                my_marker = R.drawable.marker_general;
                Toast.makeText(_ctx, "Bad topic: " + cur_topic, Toast.LENGTH_SHORT).show();
            }
            itemizedoverlay.addOverlay(overlayitem, _resources.getDrawable(my_marker));
        }
        if (feed.getMarkerCount() > 0) {
            itemizedoverlay.setPctShown(_slider.getProgress(), _ctx);
            if (mapOverlays.size() > 0) {
                mapOverlays.set(0,itemizedoverlay);
            }
            else {
                mapOverlays.add(itemizedoverlay);
            }
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

        private int refresh_idx;

        @Override
        protected MarkerFeed doInBackground(String... string) {
            m_num_executing++;
            m_ajax_idx++;
            refresh_idx = m_ajax_idx;
            return getMarkers();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // setProgressPercent(progress[0]);
        }

        @Override
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