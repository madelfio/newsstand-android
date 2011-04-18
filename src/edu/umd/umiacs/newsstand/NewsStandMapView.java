package edu.umd.umiacs.newsstand;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class NewsStandMapView extends MapView {
    private final NewsStand _ctx;
    private Refresh _refresh;

    private long lastTouchTime = -1;
    private LocationManager locationManager;
    private MapController mapController;

    public NewsStandMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        _ctx = (NewsStand)context;

        initLocate();

        Resources resources = _ctx.getResources();
        Drawable drawable = resources.getDrawable(
                R.drawable.marker_general);
        getOverlays().add(new MarkerOverlay(drawable, context));
    }

    public void setRefresh(Refresh refresh_instance) {
        _refresh = refresh_instance;
    }

    private void initLocate() {
        locationManager = (LocationManager) _ctx.getSystemService(Context.LOCATION_SERVICE);
        mapController = this.getController();
    }

    // based on example on p262 in Android Developer's Cookbook
    public void goToPlace(String location_query) {

        List<Address> addresses;
        Geocoder gc = new Geocoder(_ctx);

        try {
            addresses = gc.getFromLocationName(location_query, 1);
            if (addresses != null) {
                Address x = addresses.get(0);
                int lat = (int) (x.getLatitude() * 1E6);
                int lng = (int) (x.getLongitude() * 1E6);
                //Toast.makeText(_ctx, x.getExtras().toString(), Toast.LENGTH_SHORT).show();
                goToLocation(new GeoPoint(lat, lng));
            }
        } catch(IOException e) {
            Toast.makeText(_ctx, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void goToCurrentLocation() {
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        // Or use LocationManager.GPS_PROVIDER

        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        goToLocation(lastKnownLocation);
    }

    public void goToLocation(Location loc) {
        int lat = (int) (loc.getLatitude() * 1E6);
        int lng = (int) (loc.getLongitude() * 1E6);
        goToLocation(new GeoPoint(lat, lng));
    }

    public void goToLocation(GeoPoint loc) {
        mapController.animateTo(loc);
        mapController.setZoom(14);
        _ctx.mapUpdateForce(1000);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        _ctx.getPanel().hide();

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
        if (_refresh == null) {
            Toast.makeText(_ctx, "Refresh object is null.  Can't refresh", Toast.LENGTH_SHORT).show();
        } else {
            _refresh.execute();
        }
    }

    public void updateMapWindowForce() {
        if (_refresh == null) {
            Toast.makeText(_ctx, "Refresh object is null.  Can't refresh", Toast.LENGTH_SHORT).show();
        } else {
            _refresh.executeForce();
        }
    }
}
