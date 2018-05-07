package com.stewart.rap.botctrl;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Switch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.otto.Subscribe;

import static com.stewart.rap.botctrl.MyApplication.bus;

public class MainActivity extends AppCompatActivity implements JoystickView.JoystickListener {

    private Switch mConnectionSwitch;

    @Subscribe public void getMessage(String s) { // otto bus callback
        if (s.equals("busy"))
            mConnectionSwitch.setEnabled(false); // disable switch while connecting/disconnecting
        else
            mConnectionSwitch.setEnabled(true);

        if (MyApplication.robotConnection.isConnected())
            mConnectionSwitch.setChecked(true);
        else
            mConnectionSwitch.setChecked(false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mConnectionSwitch = findViewById(R.id.connectSwitch);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mConnectionSwitch.setOnTouchListener((view, event) -> {

            if (event.getActionMasked() == MotionEvent.ACTION_DOWN)
                if (mConnectionSwitch.isChecked()) {
                    MyApplication.robotConnection.disconnect();
                    Snackbar.make(view, "Disconnecting...", Snackbar.LENGTH_SHORT).show();
                } else {
                    MyApplication.robotConnection.connect();
                    Snackbar.make(view, "Connecting...", Snackbar.LENGTH_SHORT).show();
                }
            return false;
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {

                Snackbar.make(view, "TODO: goto settings...", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
        });
    }

    @Override protected void onPause() { bus.unregister(this); super.onPause(); }
    @Override protected void onResume() { bus.register(this); super.onResume(); }

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

        try { json = (new ObjectMapper()).writeValueAsString(pos); }
        catch (JsonProcessingException e) { e.printStackTrace(); }

        if (id == R.id.joystick)
            MyApplication.robotConnection.sendString(json);
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