package edu.umd.umiacs.newsstand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class PopupPanel extends Overlay {
    View popup;
    boolean isVisible=false;
    NewsStandMapView mMap = null;
    Context mCtx = null;

    private final int POPUP_OFFSET = 15;
    private final int MARKER_HEIGHT = 40;
    private final int RECT_MARGIN = 10;
    private final int TEXT_MARGIN = 20;
    //private final int LEFT_TEXT_MARGIN = 20;
    //private final int RIGHT_TEXT_MARGIN = 15;

    private Paint innerPaint, borderPaint, textPaint;

    PopupPanel(Context ctx, int layout, NewsStandMapView map) {
        mCtx = ctx;
        mMap = map;
        ViewGroup parent=(ViewGroup)mMap.getParent();

        LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        popup=inflater.inflate(layout, parent, false);

        popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }

    View getView() {
        return(popup);
    }

    public void show(boolean alignTop, Point marker_pos ) {
        // Show text view
        RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        if (alignTop) {
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            lp.setMargins(TEXT_MARGIN, 0, TEXT_MARGIN, (mMap.getHeight() - marker_pos.y) + MARKER_HEIGHT + POPUP_OFFSET);
        }
        else {
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            lp.setMargins(TEXT_MARGIN, marker_pos.y + POPUP_OFFSET, TEXT_MARGIN, 0);
        }

        hide();

        ViewGroup parent=(ViewGroup)mMap.getParent();
        parent.addView(popup, lp);
        isVisible=true;

        if (mMap.getOverlays().size() >= 2) {
            mMap.getOverlays().set(1, this);
        }
        else {
            mMap.getOverlays().add(this);
        }
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (isVisible) {
            View v = getView();

            //  Setup the info window with the right size & location
            int width = v.getWidth() + 2 * RECT_MARGIN;
            int height = v.getHeight() + 2 * RECT_MARGIN;
            int left = v.getLeft() - RECT_MARGIN;
            int top = v.getTop() - RECT_MARGIN;

            RectF infoWindowRect = new RectF(0, 0, width, height);
            infoWindowRect.offset(left, top);

            //  Draw inner info window
            canvas.drawRoundRect(infoWindowRect, 5, 5, getInnerPaint());

            //  Draw border for info window
            canvas.drawRoundRect(infoWindowRect, 5, 5, getBorderPaint());
        }
    }


    public void display(GeoPoint marker_loc, String headline, String snippet) {

        Point pt=mMap.getProjection().toPixels(marker_loc, null);

        View view = getView();

        snippet = str_replace(snippet, "<span class='georef'>", "<font color=\"red\">");
        snippet = str_replace(snippet, "</span>", "</font>");
        snippet = str_replace(snippet, "&mdash;", "-");
        snippet = str_replace(snippet, "&ldquo;", "\"");
        snippet = str_replace(snippet, "&rdquo;", "\"");
        snippet = str_replace(snippet, "&quot;", "\"");
        snippet = str_replace(snippet, "&#39;", "'");
        snippet = str_replace(snippet, "&#x2029;", "");

        ((TextView)view.findViewById(R.id.headline)).setText(headline);
        //((TextView)view.findViewById(R.id.snippet)).setText(snippet);
        ((TextView)view.findViewById(R.id.snippet)).setText(Html.fromHtml(snippet));

        show(pt.y*2>mMap.getHeight(), pt);
    }

    // Naive unescaping of HTML...
    // Modeled after:
    // http://java.sun.com/developer/technicalArticles/releases/1.4regex/
    public String str_replace(String the_string, String from_what, String to_what) {
        Pattern p = Pattern.compile(from_what);
        Matcher m = p.matcher(the_string);

        StringBuffer sb = new StringBuffer();
        boolean result = m.find();
        while (result) {
            m.appendReplacement(sb, to_what);
            result = m.find();
        }

        m.appendTail(sb);

        return sb.toString();
    }

    public void hide() {
        if (isVisible) {
            isVisible=false;
            ((ViewGroup)popup.getParent()).removeView(popup);
        }
    }

    public Paint getInnerPaint() {
        if ( innerPaint == null) {
            innerPaint = new Paint();
            innerPaint.setARGB(220, 75, 75, 75); //gray
            innerPaint.setAntiAlias(true);
        }
        return innerPaint;
    }

    public Paint getBorderPaint() {
        if ( borderPaint == null) {
            borderPaint = new Paint();
            borderPaint.setARGB(255, 255, 255, 255);
            borderPaint.setAntiAlias(true);
            borderPaint.setStyle(Style.STROKE);
            borderPaint.setStrokeWidth(2);
        }
        return borderPaint;
    }

    public Paint getTextPaint() {
        if ( textPaint == null) {
            textPaint = new Paint();
            textPaint.setARGB(255, 255, 255, 255);
            textPaint.setAntiAlias(true);
        }
        return textPaint;
    }

}