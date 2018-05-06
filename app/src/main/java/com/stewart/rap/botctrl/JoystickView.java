package com.stewart.rap.botctrl;

// This code is adapted with minor modifications from
// https://github.com/efficientisoceles/JoystickView
// and is licensed under the GNU General Public License
// version 3

// also see:
// http://www.instructables.com/member/EfficentIsoceles/
// http://www.instructables.com/id/A-Simple-Android-UI-Joystick/

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class JoystickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private float mCenterX;
    private float mCenterY;
    private float mBaseRadius;
    private float mHatRadius;
    private JoystickListener joystickCallback;

    public JoystickView(Context context, AttributeSet attributes) {

        super(context, attributes);
        getHolder().addCallback(this);
        setOnTouchListener(this);

        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    @Override public void surfaceDestroyed(SurfaceHolder holder) {}

    private void setupDimensions() {

        mCenterX = getWidth() / 2;
        mCenterY = getHeight() / 2;
        mBaseRadius = Math.min(getWidth(), getHeight()) / 3;
        mHatRadius = Math.min(getWidth(), getHeight()) / 5;
    }

    private void drawJoystick(float newX, float newY) {

        if (getHolder().getSurface().isValid()) {

            Canvas myCanvas = this.getHolder().lockCanvas();
            Paint colors = new Paint();

            myCanvas.drawARGB(255, 255, 255, 255); //Background = white

            colors.setARGB(255, 100, 100, 100);
            myCanvas.drawCircle(mCenterX, mCenterY, mBaseRadius, colors); //Draw the joystick base

            colors.setARGB(255, 0, 0, 255);
            myCanvas.drawCircle(newX, newY, mHatRadius, colors); //Draw the joystick hat

            getHolder().unlockCanvasAndPost(myCanvas);
        }
    }

    @Override public void surfaceCreated(SurfaceHolder holder) {
        setupDimensions();
        drawJoystick(mCenterX, mCenterY);
    }


    @Override public boolean onTouchEvent(MotionEvent event) {
        Log.d("EVENT", "is " + event.getAction() + " uh " + android.view.MotionEvent.ACTION_UP);
        return super.onTouchEvent(event);
    }

    @Override public boolean onTouch(View view, MotionEvent event) {

        if (view.equals(this)) {

            if (event.getAction() != event.ACTION_UP) {

                float displacement = (float) Math.sqrt( Math.pow(event.getX() - mCenterX, 2)
                                                        + Math.pow(event.getY() - mCenterY, 2));
                if (displacement < mBaseRadius) {

                    drawJoystick(event.getX(), event.getY());
                    joystickCallback.onJoystickMoved(   (event.getX() - mCenterX)/ mBaseRadius,
                                                        (mCenterY - event.getY())/ mBaseRadius,
                                                        getId());
                } else {
                    float ratio = mBaseRadius / displacement;
                    float constrainedX = mCenterX + (event.getX() - mCenterX) * ratio;
                    float constrainedY = mCenterY + (event.getY() - mCenterY) * ratio;
                    drawJoystick(constrainedX, constrainedY);
                    joystickCallback.onJoystickMoved(   (constrainedX - mCenterX)/ mBaseRadius,
                                                        (mCenterY - constrainedY)/ mBaseRadius,
                                                        getId());
                }
            } else {
                drawJoystick(mCenterX, mCenterY);
                joystickCallback.onJoystickMoved(0, 0, getId());
            }
        }
        return true;
    }

    public interface JoystickListener {

        void onJoystickMoved(float xPercent, float yPercent, int id);
    }
}