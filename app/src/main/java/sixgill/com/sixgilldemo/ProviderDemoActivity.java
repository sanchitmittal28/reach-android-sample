package sixgill.com.sixgilldemo;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sixgill.sync.sdk.Reach;
import com.sixgill.sync.sdk.providers.indooratlas.IndoorAtlasCallback;
import com.sixgill.sync.sdk.providers.indooratlas.IndoorAtlasProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import sixgill.com.sixgilldemo.UI.BlueDotView;

import static sixgill.com.sixgilldemo.MainActivity.storeName;

public class ProviderDemoActivity extends AppCompatActivity implements IndoorAtlasCallback {
    private BlueDotView imageView;
    private ProgressBar loader;
    private TextView floor;
    private TextView time;
    private TextView location;

    private IAFloorPlan mFloorPlan;
    private ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);
        imageView = findViewById(R.id.imageView);
        loader = findViewById(R.id.loader);
        floor = findViewById(R.id.floor);
        time = findViewById(R.id.time);
        location = findViewById(R.id.location);
        Button stopSDK = findViewById(R.id.stopSdk);
        stopSDK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // stop the SDK
                Reach.disable(ProviderDemoActivity.this);
                //save the status that SDK was stopped by user
                SharedPreferences.Editor editor = getSharedPreferences(storeName, MODE_PRIVATE).edit();
                editor.putInt("running", 0);
                editor.apply();

                finish();
            }
        });

        Reach.enable(this);
        imageLoader = ImageLoader.getInstance();
        ImageLoaderConfiguration config = ImageLoaderConfiguration.createDefault(this);
        imageLoader.init(config);
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
        imageView.setDotCenter(pointF);
        imageView.postInvalidate();
    }

    @Override
    public void onEnterFloorPlan(IAFloorPlan iaFloorPlan, String s) {
        mFloorPlan = iaFloorPlan;
        imageLoader.loadImage(s, new SimpleImageLoadingListener(){
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                //display floor plan
                imageView.setRadius(mFloorPlan.getMetersToPixels() * 0.5f);
                imageView.setImage(ImageSource.bitmap(loadedImage));
                loader.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);

                double latitude = mFloorPlan.getCenter().latitude;
                double longitude = mFloorPlan.getCenter().longitude;
                location.setText(String.format(Locale.ENGLISH, "Location: %s, %s", String.valueOf(latitude), String.valueOf(longitude)));
                floor.setText(String.format(Locale.ENGLISH, "Floor Level: %d", mFloorPlan.getFloorLevel()));
                time.setText(String.format("Time: %s", FormatTimestamp(System.currentTimeMillis())));
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
                Toast.makeText(ProviderDemoActivity.this, "Failed to load floor plan", Toast.LENGTH_LONG).show();
                loader.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                location.setText(R.string.failed_location_provider);
                floor.setText(R.string.failed_floor_provider);
                time.setText(String.format("Time: %s", FormatTimestamp(System.currentTimeMillis())));
            }
        });
    }

    @Override
    public void onExitFloorPlan(IAFloorPlan iaFloorPlan) {

    }

    private String FormatTimestamp(long timestamp){
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a, MMM dd, yyyy", Locale.US);
        Date netDate = (new Date(timestamp));
        return sdf.format(netDate);
    }
}
