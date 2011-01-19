package edu.umd.umiacs.newsstand;

import java.util.List;
import java.util.Vector;

public class MarkerFeed {

    private String _title = null;
    private String _pubdate = null;
    private int _markercount = 0;
    private List<MarkerInfo> _markerlist;
    
    
    MarkerFeed()
    {
        _markerlist = new Vector<MarkerInfo>(0); 
    }
    int addItem(MarkerInfo marker)
    {
        _markerlist.add(marker);
        _markercount++;
        return _markercount;
    }
    MarkerInfo getMarker(int location)
    {
        return _markerlist.get(location);
    }
    List<MarkerInfo> getAllMarkers()
    {
        return _markerlist;
    }
    int getMarkerCount()
    {
        return _markercount;
    }
    void setTitle(String title)
    {
        _title = title;
    }
    void setPubDate(String pubdate)
    {
        _pubdate = pubdate;
    }
    String getTitle()
    {
        return _title;
    }
    String getPubDate()
    {
        return _pubdate;
    }
        
}
