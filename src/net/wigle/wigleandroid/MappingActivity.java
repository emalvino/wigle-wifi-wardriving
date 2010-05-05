package net.wigle.wigleandroid;

import java.util.concurrent.atomic.AtomicBoolean;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapViewController;

import android.app.Activity;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * show a map!
 */
public final class MappingActivity extends Activity {
  private OpenStreetMapViewController mapControl;
  private OpenStreetMapViewWrapper mapView;
  private Handler timer;
  private AtomicBoolean finishing;
  
  private static final int MENU_RETURN = 12;
  private static final int MENU_ZOOM_IN = 13;
  private static final int MENU_ZOOM_OUT = 14;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate( final Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );
    setContentView( R.layout.map );
    finishing = new AtomicBoolean( false );
    
    // media volume
    this.setVolumeControlStream( AudioManager.STREAM_MUSIC );  
    
    setupMapView();
    setupTimer();
  }
  
  private void setupMapView() {
    mapView = (OpenStreetMapViewWrapper) this.findViewById( R.id.mapview );
    mapControl = new OpenStreetMapViewController( mapView );
    mapControl.setCenter( new GeoPoint( 41950000, -87650000 ) );
    mapControl.setZoom( 15 );
    mapControl.setCenter( new GeoPoint( 41950000, -87650000 ) );
    
    WigleAndroid.info("done setupMapView");
  }
  
  private void setupTimer() {
    if ( timer == null ) {
      timer = new Handler();
      final Runnable mUpdateTimeTask = new Runnable() {
        public void run() {              
            // make sure the app isn't trying to finish
            if ( ! finishing.get() ) {
              final Location location = WigleAndroid.lameStatic.location;
              if ( location != null ) {
                // WigleAndroid.info( "mapping center location: " + location );
								final GeoPoint locGeoPoint = new GeoPoint( location );
                mapControl.animateTo( locGeoPoint );
              }
              final String savedStats = WigleAndroid.lameStatic.savedStats;
              if ( savedStats != null ) {
                final TextView tv = (TextView) findViewById( R.id.stats );
                tv.setText( savedStats );
              }
              
              final long period = 1000L;
              // info("wifitimer: " + period );
              timer.postDelayed( this, period );
            }
            else {
              WigleAndroid.info( "finishing mapping timer" );
            }
        }
      };
      timer.removeCallbacks( mUpdateTimeTask );
      timer.postDelayed( mUpdateTimeTask, 100 );
    }
  }
    
  @Override
  public void finish() {
    WigleAndroid.info( "finish mapping." );
    finishing.set( true );
    
    super.finish();
  }
  
  @Override
  public void onDestroy() {
    WigleAndroid.info( "destroy mapping." );
    finishing.set( true );
    
    super.onDestroy();
  }
  
  /* Creates the menu items */
  @Override
  public boolean onCreateOptionsMenu( final Menu menu ) {
      MenuItem item = menu.add(0, MENU_RETURN, 0, "Return");
      item.setIcon( android.R.drawable.ic_media_previous );
      
      item = menu.add(0, MENU_ZOOM_IN, 0, "Zoom in");
      item.setIcon( android.R.drawable.ic_menu_add );
      
      item = menu.add(0, MENU_ZOOM_OUT, 0, "Zoom out");
      item.setIcon( android.R.drawable.ic_menu_revert );
      
      return true;
  }

  /* Handles item selections */
  @Override
  public boolean onOptionsItemSelected( final MenuItem item ) {
      switch ( item.getItemId() ) {
        case MENU_RETURN:
          finish();
          return true;
        case MENU_ZOOM_IN: {
          int zoom = mapView.getZoomLevel();
          zoom++;
          mapControl.setZoom( zoom );
          return true;
        }
        case MENU_ZOOM_OUT: {
          int zoom = mapView.getZoomLevel();
          zoom--;
          mapControl.setZoom( zoom );
          return true;
        }
      }
      return false;
  }
  
}
