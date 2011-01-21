package edu.umd.umiacs.newsstand;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class NewsStandMapView extends MapView {
    private long lastTouchTime = -1;
    private NewsStandRefresh refresh;
    Context ctx = null;
    public int lat_low = 0;
    public int lat_high = 0;
    public int lon_low = 0;
    public int lon_high = 0;

    public NewsStandMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
        updateMapWindow();
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
        GeoPoint centerpoint = getMapCenter();
        int lat_span = getLatitudeSpan();
        int lon_span = getLongitudeSpan();

        int lat_l = centerpoint.getLatitudeE6() - (lat_span / 2);
        int lat_h = lat_l + lat_span;
        int lon_l = centerpoint.getLongitudeE6() - (lon_span / 2);
        int lon_h = lon_l + lon_span;
        
        if (lat_l != lat_low || lat_h != lat_high || lon_l != lon_low || lon_h != lon_high) {
            lat_low = lat_l;
            lat_high = lat_h;
            lon_low = lon_l;
            lon_high = lon_h;
            //Toast.makeText(ctx,
            //        "lat: " + lat_low/1E6 + " - " + lat_high/1E6 + ", lon: " + lon_low/1E6 + " - " + lon_high/1E6, 
            //        Toast.LENGTH_SHORT)
            //        .show();
            //Toast.makeText(ctx, "Should refresh now...", Toast.LENGTH_SHORT).show();
            if (refresh == null) {
                Toast.makeText(ctx, "'refresh' object is null!  Can't refresh", Toast.LENGTH_SHORT).show();
            } else {
                refresh.execute();
            }
        }
    }
}
