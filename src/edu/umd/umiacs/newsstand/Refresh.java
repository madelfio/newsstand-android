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

import edu.umd.umiacs.newsstand.MarkerOverlay.MarkerOverlayItem;

public class Refresh implements Runnable {

    // references to other class member variables
    private final NewsStand _ctx;
    private final NewsStandMapView _mapView;
    private final SeekBar _slider;
    private final Resources _resources;
    private final SharedPreferences _prefs;

    // member variables
    private int mNumExecuting = 0;
    private int mShowIdx = 0;
    private int mAjaxIdx = 0;

    private int mLatL = 0;
    private int mLatH = 0;
    private int mLonL = 0;
    private int mLonH = 0;

    public Refresh(Context ctx) {
        _ctx = (NewsStand)ctx;
        _mapView = _ctx.getMapView();
        _slider = _ctx.getSlider();
        _resources = _ctx.getResources();
        _prefs = _ctx.getPrefs();
    }

    @Override
    public void run() {
        executeForce();
    }

    public void execute() {
        if (mNumExecuting < 3) {
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
        mLatL = 0;
        mLatH = 0;
        mLonL = 0;
        mLonH = 0;
    }

    private boolean curBoundsDiffer() {
        GeoPoint centerpoint = _mapView.getMapCenter();
        int lat_span = _mapView.getLatitudeSpan();
        int lon_span = _mapView.getLongitudeSpan();

        int lat_l = centerpoint.getLatitudeE6() - (lat_span / 2);
        int lat_h = lat_l + lat_span;
        int lon_l = centerpoint.getLongitudeE6() - (lon_span / 2);
        int lon_h = lon_l + lon_span;

        return (lat_l != mLatL || lat_h != mLatH || lon_l != mLonL || lon_h != mLonH);
    }

    private void updateBounds() {
        GeoPoint centerpoint = _mapView.getMapCenter();
        int lat_span = _mapView.getLatitudeSpan();
        int lon_span = _mapView.getLongitudeSpan();

        mLatL = centerpoint.getLatitudeE6() - (lat_span / 2);
        mLatH = mLatL + lat_span;
        mLonL = centerpoint.getLongitudeE6() - (lon_span / 2);
        mLonH = mLonL + lon_span;
    }

    private MarkerFeed getMarkers() {
        // get map coordinates
        String marker_url = "http://newsstand.umiacs.umd.edu/news/xml_map?lat_low=%f&lat_high=%f&lon_low=%f&lon_high=%f";
        marker_url = String
                .format(marker_url,
                        mLatL / 1E6, mLatH / 1E6,
                        mLonL / 1E6, mLonH / 1E6);

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
            MarkerOverlayItem overlayitem = new MarkerOverlayItem(point,
                    cur_marker.getTitle(), cur_marker.getSnippet(), cur_marker.getGazID());

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
            mNumExecuting++;
            mAjaxIdx++;
            refresh_idx = mAjaxIdx;
            return getMarkers();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // setProgressPercent(progress[0]);
        }

        @Override
        protected void onPostExecute(MarkerFeed feed) {
            if (feed != null) {
                if (refresh_idx > mShowIdx) {
                    mShowIdx = refresh_idx;
                    setMarkers(feed);
                }
                mNumExecuting--;
            } else {
                Toast.makeText(_ctx, "Null marker feed...", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
