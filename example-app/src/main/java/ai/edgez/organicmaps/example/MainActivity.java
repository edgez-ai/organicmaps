package ai.edgez.organicmaps.example;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import app.organicmaps.sdk.Framework;
import app.organicmaps.sdk.MapController;
import app.organicmaps.sdk.MapRenderingListener;
import app.organicmaps.sdk.MapView;
import app.organicmaps.sdk.OrganicMaps;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public final class MainActivity extends FragmentActivity
{
  private static final double CENTER_LATITUDE = 59.3293;
  private static final double CENTER_LONGITUDE = 18.0686;
  private static final int DEFAULT_ZOOM = 12;
  private static final String SATELLITE_ASSET = "stockholm_satellite.mbtiles";

  private OrganicMaps mOrganicMaps;
  private MapController mMapController;
  private TextView mStatus;
  private Button mResetButton;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    showLoadingLayout();
    mOrganicMaps = ((ExampleApplication) getApplication()).getOrganicMaps();

    try
    {
      final boolean started = mOrganicMaps.init(() -> runOnUiThread(this::showMap));
      if (!started && mOrganicMaps.arePlatformAndCoreInitialized())
        showMap();
    }
    catch (IOException | RuntimeException error)
    {
      mStatus.setText("Organic Maps initialization failed: " + error.getMessage());
    }
  }

  private void showLoadingLayout()
  {
    final FrameLayout root = new FrameLayout(this);
    mStatus = statusView("Starting offline map…");
    root.addView(mStatus, topOverlayParams());
    setContentView(root);
  }

  private void showMap()
  {
    if (isFinishing() || mMapController != null)
      return;

    final FrameLayout root = new FrameLayout(this);
    final MapView mapView = new MapView(this);
    root.addView(mapView, new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT));

    mStatus = statusView("Creating renderer…");
    root.addView(mStatus, topOverlayParams());

    mResetButton = new Button(this);
    mResetButton.setText("Reset sample view");
    mResetButton.setEnabled(false);
    mResetButton.setOnClickListener(view -> renderSampleData());
    final FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
    buttonParams.setMargins(24, 24, 24, 48);
    root.addView(mResetButton, buttonParams);
    setContentView(root);

    mMapController = new MapController(
        mapView,
        mOrganicMaps.getLocationHelper(),
        new MapRenderingListener()
        {
          @Override
          public void onRenderingCreated()
          {
            runOnUiThread(() -> mStatus.setText("Renderer created…"));
          }

          @Override
          public void onRenderingRestored()
          {
            runOnUiThread(() -> renderSampleData());
          }

          @Override
          public void onRenderingInitializationFinished()
          {
            runOnUiThread(() -> renderSampleData());
          }
        },
        () -> runOnUiThread(() -> mStatus.setText("Map rendering is not supported on this device")),
        false);
    getLifecycle().addObserver(mMapController);
  }

  private void renderSampleData()
  {
    if (mMapController == null)
      return;

    Framework.nativeStopLocationFollow();
    Framework.nativeZoomToPoint(CENTER_LATITUDE, CENTER_LONGITUDE, DEFAULT_ZOOM, false);
    Framework.nativeClearApiPoints();
    Framework.nativeParseAndSetApiUrl(sampleMarkerUrl());
    Framework.nativeSetApiPointsFromUrl();
    Framework.nativeSetGpsCursorColor(0xFF1E88E5L);

    try
    {
      final File satelliteArchive = copyAssetToFiles(SATELLITE_ASSET);
      Framework.nativeSetBackgroundTileSources(
          true, "", new String[] {satelliteArchive.getAbsolutePath()}, 50, 20);
    }
    catch (IOException error)
    {
      mStatus.setText("Satellite archive failed: " + error.getMessage());
      return;
    }

    final double[] geofence = {
        59.3370, 18.0540,
        59.3375, 18.0860,
        59.3165, 18.0860,
        59.3160, 18.0540,
        59.3370, 18.0540
    };
    Framework.nativeSetEdgeZGeoFenceLines(
        geofence,
        new int[] {5},
        new int[] {(int) 0xFF43A047L},
        new String[] {"Stockholm test geofence"});

    mMapController.updateCompassOffset(0, 0);
    mMapController.getView().postInvalidate();
    mStatus.setText("Stockholm offline satellite · 3 nodes · 1 geofence");
    mResetButton.setEnabled(true);
  }

  private File copyAssetToFiles(String assetName) throws IOException
  {
    final File directory = new File(getFilesDir(), "mbtiles");
    if (!directory.isDirectory() && !directory.mkdirs())
      throw new IOException("Cannot create " + directory);

    final File destination = new File(directory, assetName);
    if (destination.isFile() && destination.length() > 0)
      return destination;

    final File temporary = new File(directory, assetName + ".tmp");
    try (InputStream input = getAssets().open(assetName);
         OutputStream output = new FileOutputStream(temporary))
    {
      final byte[] buffer = new byte[64 * 1024];
      int count;
      while ((count = input.read(buffer)) != -1)
        output.write(buffer, 0, count);
    }
    if (!temporary.renameTo(destination))
      throw new IOException("Cannot install " + assetName);
    return destination;
  }

  private static String sampleMarkerUrl()
  {
    final StringBuilder url = new StringBuilder("om://map?");
    appendMarker(url, 59.3293, 18.0686, "Gateway", "edgez-gateway", "placemark-blue");
    appendMarker(url, 59.3342, 18.0751, "Sensor node", "edgez-sensor", "placemark-green");
    appendMarker(url, 59.3221, 18.0614, "Relay node", "edgez-relay", "placemark-orange");
    return url.toString();
  }

  private static void appendMarker(StringBuilder url, double latitude, double longitude,
                                   String name, String id, String style)
  {
    if (url.charAt(url.length() - 1) != '?')
      url.append('&');
    url.append("ll=")
        .append(String.format(Locale.US, "%.7f,%.7f", latitude, longitude))
        .append("&n=").append(Uri.encode(name))
        .append("&id=").append(Uri.encode(id))
        .append("&s=").append(Uri.encode(style));
  }

  private TextView statusView(String text)
  {
    final TextView status = new TextView(this);
    status.setText(text);
    status.setTextColor(Color.WHITE);
    status.setTextSize(16);
    status.setGravity(Gravity.CENTER);
    status.setPadding(24, 18, 24, 18);
    status.setBackgroundColor(0xCC1B1B1B);
    return status;
  }

  private static FrameLayout.LayoutParams topOverlayParams()
  {
    return new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        Gravity.TOP);
  }
}
