package com.stewart.rap.botctrl;

import android.app.Application;

public class MyApplication extends Application {

    public static MyBus bus = new MyBus();
    public static RobotConnection robotConnection = new RobotConnection();

}
