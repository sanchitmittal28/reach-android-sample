package sixgill.com.sixgilldemo;

import android.app.Application;

import com.sixgill.sync.sdk.Reach;
import com.sixgill.sync.sdk.providers.IReachProvider;
import com.sixgill.sync.sdk.providers.indooratlas.IndoorAtlasProvider;

public class App extends Application {
    private static App instance = null;
    private IReachProvider provider = null;

    public static App getInstance(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        /*
            If you are planning to use Indoor Atlas as a provider in your app, you should create an instance
            of Indoor Atlas in Application class, it helps Indoor Atlas to run in background
         */

        /*
            Get instance of Indoor Atlas provider. This call will throw an error if failed to
            retrieve API Key and API secret from manifest.
            In your Android manifest you should put the value of Indoor Atlas API Key and API Secret,
            <meta-data
                android:name="com.indooratlas.android.sdk.API_KEY"
                android:value="@string/atlas_api_key" />
            <meta-data
                android:name="com.indooratlas.android.sdk.API_SECRET"
                android:value="@string/atlas_api_secret" />

            To configure this app, you should provide these values in secrets.xml file under res>values folder
        */
        provider = IndoorAtlasProvider.getInstance(this);

        /* Set the location provider for Reach SDK */
        Reach.setLocationProvider(provider, this);
    }
}
