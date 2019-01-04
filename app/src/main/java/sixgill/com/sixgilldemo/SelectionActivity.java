package sixgill.com.sixgilldemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static sixgill.com.sixgilldemo.MainActivity.storeName;

public class SelectionActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        Button simpleDemo = findViewById(R.id.simple);
        simpleDemo.setOnClickListener(this);

        Button indoorDemo = findViewById(R.id.provider);
        indoorDemo.setOnClickListener(this);

        //save the status that SDK was enabled, so on next app start we skip the login screen
        SharedPreferences.Editor editor = getSharedPreferences(storeName, MODE_PRIVATE).edit();
        editor.putInt("running", 1);
        editor.apply();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.simple:
                simpleDemonstration();
                break;
            case R.id.provider:
                providerDemonstration();
                break;
        }
    }

    private void providerDemonstration() {
        Intent intent = new Intent(SelectionActivity.this, ProviderDemoActivity.class);
        startActivity(intent);
    }

    private void simpleDemonstration() {
        Intent intent = new Intent(SelectionActivity.this, SimpleDemoActivity.class);
        startActivity(intent);
    }
}
