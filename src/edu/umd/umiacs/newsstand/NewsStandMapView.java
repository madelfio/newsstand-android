package edu.umd.umiacs.newsstand;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.MapView;

public class NewsStandMapView extends MapView {
    private long lastTouchTime = -1;
    private NewsStandRefresh refresh;
    Context ctx = null;
    public String current_search = null;
    private TextView mSearchDisplay = null;

    public NewsStandMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
        initSearch();
    }

    public void setRefresh(NewsStandRefresh refresh_instance) {
        refresh = refresh_instance;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

      if (ev.getAction() == MotionEvent.ACTION_DOWN) {

        long thisTime = System.currentTimeMillis();
        if (thisTime - lastTouchTime < 250) {

          // Double tap
          this.getController().zoomInFixing((int) ev.getX(), (int) ev.getY());
          lastTouchTime = -1;

        } else {

          // Too slow :)
          lastTouchTime = thisTime;
        }
      }

      boolean t = super.onInterceptTouchEvent(ev);
      updateMapWindow();
      return t;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
     if (ev.getAction()==MotionEvent.ACTION_UP) {
       updateMapWindow();
     }
     return super.onTouchEvent(ev);
    }

    public void updateMapWindow() {
        if (refresh == null) {
            Toast.makeText(ctx, "Refresh object is null.  Can't refresh", Toast.LENGTH_SHORT).show();
        } else {
            refresh.execute();
        }
    }
    
    public void updateMapWindowForce() {
        if (refresh == null) {
            Toast.makeText(ctx, "Refresh object is null.  Can't refresh", Toast.LENGTH_SHORT).show();
        } else {
            refresh.executeForce();
        }        
    }
    
    public void addSearch(String query) {
        current_search = query;
        Toast.makeText(ctx,"Searching for: " + query, Toast.LENGTH_SHORT).show();
        mSearchDisplay.setText("Search: " + current_search);
        mSearchDisplay.setVisibility(View.VISIBLE);
        updateMapWindowForce();
    }
    
    public void clearSearch() {
        current_search = null;
        Toast.makeText(ctx, "Search has been cleared", Toast.LENGTH_SHORT).show();
        mSearchDisplay.setText("");
        mSearchDisplay.setVisibility(View.INVISIBLE);
    }

    public void initSearch() {
        int x = 10;
        int y = 10;
        MapView.LayoutParams screenLayoutParams = new MapView.LayoutParams(
                MapView.LayoutParams.WRAP_CONTENT,
                MapView.LayoutParams.WRAP_CONTENT,
                x,y,MapView.LayoutParams.LEFT);

        mSearchDisplay = new TextView(ctx);
        mSearchDisplay.setText("Search Text");
        mSearchDisplay.setTextColor(Color.BLUE);
        mSearchDisplay.setTextSize(16);
        addView(mSearchDisplay, screenLayoutParams);
        mSearchDisplay.setVisibility(View.INVISIBLE);
    }
}