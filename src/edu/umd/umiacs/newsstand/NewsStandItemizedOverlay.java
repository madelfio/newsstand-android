package edu.umd.umiacs.newsstand;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.widget.Toast;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class NewsStandItemizedOverlay extends ItemizedOverlay<OverlayItem> {

    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    private Context mContext;

    public NewsStandItemizedOverlay(Drawable defaultMarker) {
        super(boundCenterBottom(defaultMarker));
    }

    public NewsStandItemizedOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker));
        mContext = context;
    }
    
    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
        populate();
    }
    
    public void setNumShown(int num_to_show, Context context) {
        Toast.makeText(context, "num_to_show: " + num_to_show, Toast.LENGTH_LONG).show();
        for (int i=0; i < mOverlays.size(); i++) {
            OverlayItem overlay = mOverlays.get(i);
            Drawable marker = overlay.getMarker(0);
            if (i < num_to_show) {
                marker.setVisible(true,true);
            }
            else {
                marker.setVisible(false, true);
            }
        }
    }

    // method added to allow passing a drawable "marker" to use for the item
    // this must be done within the class because boundCenterBottom is protected
    public void addOverlay(OverlayItem overlay, Drawable marker) {
        overlay.setMarker(boundCenterBottom(marker));
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
      AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
      dialog.setTitle(item.getTitle());
      dialog.setMessage(Html.fromHtml(item.getSnippet()));
      dialog.show();
      return true;
    }
}