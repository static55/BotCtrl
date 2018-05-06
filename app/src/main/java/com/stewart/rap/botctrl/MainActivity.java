package com.stewart.rap.botctrl;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener {

    RobotConnection mRobotConnection = new RobotConnection();

    @Override protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Switch connectionSwitch = (Switch) findViewById(R.id.connectSwitch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        connectionSwitch.setChecked(false);

        connectionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mRobotConnection.isConnected())
                mRobotConnection.disconnect();
            else
                mRobotConnection.connect();
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {

                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
        });
    }


    @Override public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings)
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override public void onJoystickMoved(float xPercent, float yPercent, int id) {

        XYPos pos = new XYPos();
        pos.setX(xPercent);
        pos.setY(yPercent);
        String json = "";

        try { json = (new ObjectMapper()).writeValueAsString(pos);
        } catch (JsonProcessingException e) { e.printStackTrace(); }

        if (id == R.id.joystick) {
            mRobotConnection.sendString(json);
            Log.d("Joystick", json);
        } else {
            Log.d("derp", "WRONG ID");
        }
    }


}

class XYPos {

    private float x;
    private float y;

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

}