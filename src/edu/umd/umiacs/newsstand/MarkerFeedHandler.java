package edu.umd.umiacs.newsstand;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class MarkerFeedHandler extends DefaultHandler {
    
    MarkerFeed _feed;
    MarkerInfo _marker;
    String _lastElementName = "";
    boolean bFoundChannel = false;

    final int RSS_TITLE = 1;
    final int RSS_LATITUDE = 2;
    final int RSS_LONGITUDE = 3;
    final int RSS_NAME = 4;
    final int RSS_DESCRIPTION = 5;
    final int RSS_GAZ_ID = 6;
    final int RSS_TOPIC = 7;
    final int RSS_CLUSTER_ID = 8;
    final int RSS_MARKUP = 9;
    final int RSS_SNIPPET = 10;
    
    int depth = 0;
    int currentstate = 0;
    /*
     * Constructor 
     */
    MarkerFeedHandler()
    {
    }
    
    /*
     * getFeed - this returns our feed when all of the parsing is complete
     */
    MarkerFeed getFeed()
    {
        return _feed;
    }
    
    
    public void startDocument() throws SAXException
    {
        // initialize our MarkerFeed object - this will hold our parsed contents
        _feed = new MarkerFeed();
        // initialize the MarkerInfo object - we will use this as a crutch to grab the info from the channel
        // because the channel and items have very similar entries..
        _marker = new MarkerInfo();

    }
    public void endDocument() throws SAXException
    {
    }
    public void startElement(String namespaceURI, String localName,String qName, Attributes atts) throws SAXException
    {
        depth++;
        if (localName.equals("channel"))
        {
            currentstate = 0;
            return;
        }
        //if (localName.equals("image"))
        //{
        //    // record our feed data - we temporarily stored it in the item :)
        //    _feed.setTitle(_item.getTitle());
        //    _feed.setPubDate(_item.getPubDate());
        //}
        if (localName.equals("item"))
        {
            // create a new item
            _marker = new MarkerInfo();
            return;
        }
        if (localName.equals("title"))
        {
            currentstate = RSS_TITLE;
            return;
        }
        if (localName.equals("latitude"))
        {
            currentstate = RSS_LATITUDE;
            return;
        }
        if (localName.equals("longitude"))
        {
            currentstate = RSS_LONGITUDE;
            return;
        }
        if (localName.equals("name"))
        {
            currentstate = RSS_NAME;
            return;
        }
        if (localName.equals("description"))
        {
            currentstate = RSS_DESCRIPTION;
            return;
        }
        if (localName.equals("gaz_id"))
        {
            currentstate = RSS_GAZ_ID;
            return;
        }
        if (localName.equals("gaz_id"))
        {
            currentstate = RSS_GAZ_ID;
            return;
        }
        if (localName.equals("topic"))
        {
            currentstate = RSS_TOPIC;
            return;
        }
        if (localName.equals("cluster_id"))
        {
            currentstate = RSS_CLUSTER_ID;
            return;
        }
        if (localName.equals("marker"))
        {
            currentstate = RSS_MARKUP;
            return;
        }
        if (localName.equals("snippet"))
        {
            currentstate = RSS_SNIPPET;
            return;
        }

        // if we don't explicitly handle the element, make sure we don't wind up erroneously 
        // storing a newline or other bogus data into one of our existing elements
        currentstate = 0;
    }
    
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException
    {
        depth--;
        if (localName.equals("item"))
        {
            // add our item to the list!
            _feed.addItem(_marker);
            return;
        }
    }
     
    public void characters(char ch[], int start, int length)
    {
        String theString = new String(ch,start,length);
        Log.i("NewsStand","characters[" + theString + "]");
        
        switch (currentstate)
        {
            case RSS_TITLE:
                _marker.setTitle(theString);
                currentstate = 0;
                break;
            case RSS_LATITUDE:
                _marker.setLatitude(theString);
                currentstate = 0;
                break;
            case RSS_LONGITUDE:
                _marker.setLongitude(theString);
                currentstate = 0;
                break;
            case RSS_NAME:
                _marker.setName(theString);
                currentstate = 0;
                break;
            case RSS_DESCRIPTION:
                _marker.setDescription(theString);
                currentstate = 0;
                break;
            case RSS_GAZ_ID:
                _marker.setGazID(theString);
                currentstate = 0;
                break;
            case RSS_TOPIC:
                _marker.setTopic(theString);
                currentstate = 0;
                break;
            case RSS_CLUSTER_ID:
                _marker.setClusterID(theString);
                currentstate = 0;
                break;
            case RSS_MARKUP:
                _marker.setMarkup(theString);
                currentstate = 0;
                break;
            case RSS_SNIPPET:
                _marker.setSnippet(theString);
                currentstate = 0;
                break;
            default:
                return;
        }
        
    }

}
