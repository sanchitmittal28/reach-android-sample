
# reach-android-sample

## Overview
This repository contains the sample app and source code to demonstrate the basic use of Sixgill Reach SDK. 
You can find the complete SDK documentation [here](https://github.com/sixgill/sense-docs/blob/master/guides/002-sdks/002-android-sdk.md)

*Reach SDK version: 1.2.8*
##### [Download sample app](https://github.com/sixgill/reach-android-sample/raw/master/android-sample-build.apk)

## Implementation details
Reach SDK requires some permissions in order to work properly. Skipping some of the permissions will disable the related feature, for example if you skip the location permission then SDK won't be able to collect the location of the user.
The permissions are added in the [AndroidManifest](https://github.com/sixgill/reach-android-sample/blob/initial-setup/app/src/main/AndroidManifest.xml)

Once the permission are added in the SDK, it's time to ask the user to grant those required permissions. See [MainActivity.java](https://github.com/sixgill/reach-android-sample/blob/initial-setup/app/src/main/java/sixgill/com/sixgilldemo/MainActivity.java#L87)
```java
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
}

private void RequestPermission(String[] permissions){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        boolean granted = true;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
        }

        if (!granted) {
            requestPermissions(permissions, 1);
        }
    }
}

//Request permissions
RequestPermission(new String[]{
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION
});
```

To start the SDK we need to pass some of the basic information:
- API Key
- Aliases (can be unique phone number)

You can turn off the functionality of the SDK to stop sending the events to Sixgill server (by default this is ON).

See [MainActivity.java](https://github.com/sixgill/reach-android-sample/blob/initial-setup/app/src/main/java/sixgill/com/sixgilldemo/MainActivity.java#L114)
```java
ReachConfig config = new ReachConfig();
config.setSendEvents(false);
```

Optionally you can also provide Notification builder to customize the notifications.
```java
//custom notification builder for sticky notification
Notification.Builder stickyBuilder = new Notification.Builder(getApplicationContext())
        .setContentTitle("Reach SDK") // custom sticky notification title
        .setContentText("This shows demo of sticky notification") // custom sticky notification body
        .setSmallIcon(R.drawable.sticky_notification_icon) // custom icon for sticky notifications
        .setAutoCancel(true);
config.setStickyNotificationBuilder(stickyBuilder);

// custom notification builder for any notification
Notification.Builder notification = new Notification.Builder(getApplicationContext())
        .setSmallIcon(R.drawable.notification_icon) // custom icon for normal notifications
        .setAutoCancel(true);
config.setNotificationBuilder(notification);
```

Finally to start the SDK you can call the `initWithAPIKey` method of the SDK. 
```java
Map<String, String> aliases = new HashMap<>();
aliases.put("phone", phoneNumber);
// some additional information can be added to aliases as well
aliases.put("organization", "sixgill");

ReachConfig config = new ReachConfig();
if (useDevelopmentEndpoint) {
    config.setIngressURL("https://edge-ingress.staging.sixgill.io");
} else {
    config.setIngressURL("https://sense-ingress-api.sixgill.com");
}
config.setSendEvents(false);
config.setAliases(aliases);

Reach.initWithAPIKey(this, apiKey, config, new ReachCallback() {
    @Override
    public void onReachSuccess() {
        // successfully registered the SDK
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onReachFailure(String s) {
        // failed to register the SDK
        Toast.makeText(MainActivity.this, "Failed to register the SDK", Toast.LENGTH_LONG).show();
    }
});
```

Once the SDK is initialized, you need to enable the SDK to start collecting the events.

Once the SDK is enabled you can collect the events data by setting up the local broadcast. Do remember to unregister the broadcast listner to prevent memory leaks

See [DetailsActivity.java](https://github.com/sixgill/reach-android-sample/blob/initial-setup/app/src/main/java/sixgill/com/sixgilldemo/DetailsActivity.java)
```java
BroadcastReceiver mEventReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String encodedEvent = bundle.getString(Reach.EVENT_DATA);
            if(encodedEvent != null) {
                byte[] b = Base64.decode(encodedEvent, Base64.DEFAULT);
                Ingress.Event event = Ingress.Event.parseFrom(b);
            }
        }
    }
};

Reach.enable(this, true, new ReachCallback() {
    @Override
    public void onReachSuccess() {
        // setup a local broadcast receiver to get events from SDK
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DetailsActivity.this);
        manager.registerReceiver(mEventReceiver, new IntentFilter(Reach.EVENT_BROADCAST));
    }

    @Override
    public void onReachFailure(String s) {
        Toast.makeText(DetailsActivity.this, "Failed to enable the SDK", Toast.LENGTH_LONG).show();
    }
});

@Override
protected void onDestroy() {
    super.onDestroy();
    // unregister the local broadcast receiver to prevent memory leak
    LocalBroadcastManager.getInstance(this).unregisterReceiver(mEventReceiver);
}
```

Finally to stop the SDK, you need to call `disable` method of SDK
```java
Reach.disable(DetailsActivity.this);
```

### How to use Indoor providers
Reach SDK provides support for Indoor Atlas as a provider. To use this, you need to optain Indoor Atlas API Key and API Secret. Once the key is optained, you can put the values in your Android manifest
```xml
<meta-data
    android:name="com.indooratlas.android.sdk.API_KEY"
    android:value="your-api-key-will-go-here" />
<meta-data
    android:name="com.indooratlas.android.sdk.API_SECRET"
    android:value="your-api-secret-will-go-here" />
```
*Note: To check the usages of Indoor Atlas in this sample app, you need to provide these values in `secrets.xml` file which can be found under res>values folder*

Running Reach SDK with indoor providers require you to add `WAKE_LOCK` permission
```xml
<uses-permission android:name="android.permission.WAKE_LOCK" />
```
Add the Indoor Atlas Maven repository in your root level gradle file
```gradle
maven {
    // maven repository for indoor atlas
    url "http://indooratlas-ltd.bintray.com/mvn-public"
}
```
Add Indoor atlas as a dependency in your app's gradle
```gradle
/* Indoor Atlas */
implementation 'com.indooratlas.android:indooratlas-android-sdk:2.8.3@aar'
```

In order to properly use the Indoor Atlas in the background, you need to create an instance of Indoor Atlas provider in application class
```java
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
            Get instance of Indoor Atlas provider. This call will throw an error if failed to
            retrieve API Key and API secret from manifest.
        */
        provider = IndoorAtlasProvider.getInstance(this);

        /* Set the location provider for Reach SDK */
        Reach.setLocationProvider(provider, this);
    }
}
```

Set the provider instance as a location provider to the Reach SDK
```java
Reach.setLocationProvider(provider, this);
```
Implement `IndoorAtlasCallback` in your class where you want to get the data from Indoor atlas, for example
```java
public class ProviderDemoActivity extends AppCompatActivity implements IndoorAtlasCallback {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);
        // enable the Reach SDK
        Reach.enable(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        IndoorAtlasProvider.getInstance(this).setProviderEventCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        IndoorAtlasProvider.getInstance(this).removeProviderEventCallback(this);
    }

    @Override
    public void onIndoorLocationChanged(PointF pointF) {

    }

    @Override
    public void onEnterFloorPlan(IAFloorPlan iaFloorPlan, String s) {

    }

    @Override
    public void onExitFloorPlan(IAFloorPlan iaFloorPlan) {

    }
}
```

The `IndoorAtlasCallback` will provide you 3 methods
- onIndoorLocationChanged : `onIndoorLocationChanged` will be fired when the location is changed. It will provide you the X and Y co-ordinate relative to the floor plan. Using these details you can mark where a user is on the given floor
- onEnterFloorPlan : `onEnterFloorPlan` will be fired when the user enters any floor. It will provide you the floor plan details and a URL for the floor plan image
- onExitFloorPlan : `onExitFloorPlan` will be fired when a user exits a floor

Checkout the `ProviderDemoActivity.java` class to checkout how the floor plan image, floor data and Points are used.

#### Plotting the floor map provided from Indoor Atlas
To plot the floor image, we are using the [Subsampling Scale Image View](https://github.com/davemorrissey/subsampling-scale-image-view). A custom image view has been created on top of this library to handle the floor map plotting.

We know that `onIndoorLocationChanged` provides us the Point with X and Y co-ordinate, using these values, we can draw a dot on the image
```java
PointF dotCenter;
if (dotCenter != null) {
    PointF vPoint = sourceToViewCoord(dotCenter);
    float scaledRadius = getScale() * radius;
    paint.setAntiAlias(true);
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(getResources().getColor(R.color.colorPrimary));
    if(vPoint != null) {
        canvas.drawCircle(vPoint.x, vPoint.y, scaledRadius, paint);
    }
}
```
Now when we set the floor map image to the view, we can handle the drawing of the dot on the floor map.
```java
public class BlueDotView extends SubsamplingScaleImageView {

    private float radius = 1.0f;
    private PointF dotCenter = null;

    Paint paint = new Paint();

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setDotCenter(PointF dotCenter) {
        this.dotCenter = dotCenter;
    }

    public BlueDotView(Context context) {
        this(context, null);
    }

    public BlueDotView(Context context, AttributeSet attr) {
        super(context, attr);
        initialise();
    }

    private void initialise() {
        setWillNotDraw(false);
        setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isReady()) {
            return;
        }

        if (dotCenter != null) {
            PointF vPoint = sourceToViewCoord(dotCenter);
            float scaledRadius = getScale() * radius;
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getResources().getColor(R.color.colorPrimary));
            if(vPoint != null) {
                canvas.drawCircle(vPoint.x, vPoint.y, scaledRadius, paint);
            }
        }
    }
}
```
