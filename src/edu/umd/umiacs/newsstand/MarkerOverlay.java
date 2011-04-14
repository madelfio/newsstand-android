package edu.umd.umiacs.newsstand;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class MarkerOverlay extends ItemizedOverlay<OverlayItem> {
    private final ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    private final NewsStand mContext;

    public MarkerOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker));
        mContext = (NewsStand)context;
    }

    // append new overlay object to mOverlays array
    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
        populate();
    }

    public void setPctShown(int pct_to_show, Context context) {
        int num_to_show = mOverlays.size() * pct_to_show / 100;
        for (int i=0; i < mOverlays.size(); i++) {
            OverlayItem overlay = mOverlays.get(i);
            Drawable marker = overlay.getMarker(0);
            if (i < num_to_show + 1) {
                marker.mutate().setAlpha(255);
                marker.setVisible(true,true);
            }
            else {
                marker.mutate().setAlpha(0);
                marker.setVisible(false, true);
            }
        }
    }

    // method added to allow passing a drawable "marker" to use for the item
    // this must be done within the class because boundCenterBottom is protected
    public void addOverlay(OverlayItem overlay, Drawable marker) {
        marker.setBounds(-marker.getIntrinsicWidth() / 3,
               -marker.getIntrinsicHeight() * 2 / 3,
               marker.getIntrinsicWidth() / 3,
                0);
        overlay.setMarker(marker);
        addOverlay(overlay);
    }

    @Override
    protected OverlayItem createItem(int i) {
        return mOverlays.get(i);
    }

    @Override
    public int size() {
        return mOverlays.size();
    }

    @Override
    protected boolean onTap(int index) {
        OverlayItem item = mOverlays.get(index);
        Drawable marker = item.getMarker(0);

        if (!marker.isVisible()) {
            return true;
        }

        mContext.panel.display(item.getPoint(), item.getTitle(), item.getSnippet());

        return true;
    }

}
