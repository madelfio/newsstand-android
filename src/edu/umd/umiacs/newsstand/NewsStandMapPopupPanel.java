package edu.umd.umiacs.newsstand;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class NewsStandMapPopupPanel {
    View popup;
    boolean isVisible=false;
    NewsStandMapView mMap = null;
    Context mCtx = null;
    
    NewsStandMapPopupPanel(Context ctx, int layout, NewsStandMapView map) {
        
        mCtx = ctx;
        mMap = map;
        ViewGroup parent=(ViewGroup)mMap.getParent();

        LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        popup=inflater.inflate(layout, parent, false);
                                
        popup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hide();
            }
        });
    }
    
    View getView() {
        return(popup);
    }
    
    public void show(boolean alignTop) {
        int x = 50;
        int y = 50;
        MapView.LayoutParams screenLayoutParams = new MapView.LayoutParams(
                MapView.LayoutParams.WRAP_CONTENT,
                MapView.LayoutParams.WRAP_CONTENT,
                x,y,MapView.LayoutParams.LEFT);
        
        final TextView tv = new TextView(mCtx);
        tv.setText("Adding View");
        tv.setTextColor(Color.BLUE);
        tv.setTextSize(20);
        mMap.addView(tv, screenLayoutParams);
                
        
        
        RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        
        if (alignTop) {
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            lp.setMargins(0, 20, 0, 0);
        }
        else {
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp.setMargins(0, 0, 0, 60);
        }
        
        hide();
        
        ViewGroup parent=(ViewGroup)mMap.getParent();
        Toast.makeText(mCtx, parent.toString(), Toast.LENGTH_SHORT).show();
        Toast.makeText(mCtx, popup.toString(), Toast.LENGTH_SHORT).show();
        parent.addView(popup, lp);
        isVisible=true;
    }
    
    public void display(GeoPoint marker_loc, String headline, String snippet) {
        
        Point pt=mMap.getProjection().toPixels(marker_loc, null);
        
        View view = getView();
        
        ((TextView)view.findViewById(R.id.headline)).setText(headline);
        ((TextView)view.findViewById(R.id.snippet)).setText(snippet);
        
        show(pt.y*2>mMap.getHeight());
    }
    
    public void hide() {
        if (isVisible) {
            isVisible=false;
            ((ViewGroup)popup.getParent()).removeView(popup);
        }
    }
}