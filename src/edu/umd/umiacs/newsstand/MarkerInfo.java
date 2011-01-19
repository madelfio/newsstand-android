package edu.umd.umiacs.newsstand;

public class MarkerInfo {

    //title, latitude, longitude, name, description, gaz_id, topic, cluster_id, markup, snippet
    
    private String _title = null;
    private String _latitude = null;
    private String _longitude = null;
    private String _name = null;
    private String _description = null;
    private String _gaz_id = null;
    private String _topic = null;
    private String _cluster_id = null;
    private String _markup = null;
    private String _snippet = null;
    
    MarkerInfo()
    {
    }
    void setTitle(String title)
    {
        _title = title;
    }
    void setLatitude(String latitude)
    {
        _latitude = latitude;
    }
    void setLongitude(String longitude)
    {
        _longitude = longitude;
    }
    void setName(String name)
    {
        _name = name;
    }
    void setDescription(String description)
    {
        _description = description;
    }
    void setGazID(String gaz_id)
    {
        _gaz_id = gaz_id;
    }
    void setTopic(String topic) {
        _topic = topic;
    }
    void setClusterID(String cluster_id) {
        _cluster_id = cluster_id;
    }
    void setMarkup(String markup)
    {
        _markup = markup;
    }
    void setSnippet(String snippet)
    {
        _snippet = snippet;
    }
    String getTitle()
    {
        return _title;
    }
    String getLatitude()
    {
        return _latitude;
    }
    String getLongitude()
    {
        return _longitude;
    }
    String getName()
    {
        return _name;
    }
    String getDescription()
    {
        return _description;
    }
    String getGazID()
    {
        return _gaz_id;
    }
    String getTopic() {
        return _topic;
    }
    String getClusterID() {
        return _cluster_id;
    }
    String getMarkup() {
        return _markup;
    }
    String getSnippet() {
        return _snippet;
    }
    public String toString()
    {
        // limit how much text we display
        if (_title.length() > 42)
        {
            return _title.substring(0, 42) + "...";
        }
        return _title;
    }
}
