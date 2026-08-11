package ai.edgez.organicmaps.example;

import android.app.Application;
import androidx.annotation.NonNull;
import app.organicmaps.sdk.OrganicMaps;

public final class ExampleApplication extends Application
{
  private OrganicMaps mOrganicMaps;

  @Override
  public void onCreate()
  {
    super.onCreate();
    mOrganicMaps = new OrganicMaps(
        getApplicationContext(),
        "edgez-example",
        BuildConfig.APPLICATION_ID,
        BuildConfig.VERSION_CODE,
        BuildConfig.VERSION_NAME);
  }

  @NonNull
  public OrganicMaps getOrganicMaps()
  {
    return mOrganicMaps;
  }
}
