package top.ninng.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import top.ninng.demo.activity.demo.P2PActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "MainActivity";
    Button wifiP2pBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    public void initView() {
        wifiP2pBtn = (Button) findViewById(R.id.wifiP2pBtn);

        wifiP2pBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wifiP2pBtn:
                goActivity(P2PActivity.class);
                break;
            default:
                break;
        }
    }

    private void goActivity(Class<?> cls) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, cls);
        startActivity(intent);
    }
}