package com.stewart.rap.botctrl;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

public class RobotConnection {

    private static Channel mChannel;
    private static Session mSession;
    private static OutputStream mOutputStream;
    private boolean mIsConnected = false;
    private final ReentrantLock networkingLock = new ReentrantLock();

    public void connect() {

        new Thread(() -> {
            try {
                networkingLock.lockInterruptibly();
                mSession = new JSch().getSession("user", "192.168.1.100", 7005);
                mSession.setPassword("pass");
                Properties prop = new Properties();
                prop.put("StrictHostKeyChecking", "no");
                mSession.setConfig(prop);
                mSession.connect();
                mChannel = mSession.openChannel("shell");
                mChannel.setOutputStream(new ByteArrayOutputStream());
                mOutputStream = mChannel.getOutputStream();
                mChannel.connect();
                mIsConnected = true;
            }
            catch (JSchException|IOException|InterruptedException e) { e.printStackTrace(); }
            finally { networkingLock.unlock(); }
        }).start();

        return;
    }

    public void sendString(String string) {

        new Thread(() -> {
            try {
                networkingLock.lockInterruptibly();
                if (mIsConnected) {
                    mOutputStream.write(string.getBytes());
                    mOutputStream.flush();
                }
            }
            catch (IOException|InterruptedException e) { e.printStackTrace(); }
            finally { networkingLock.unlock(); }
        }).start();
    }

    public void disconnect() {

        new Thread(() -> {
            try {
                networkingLock.lockInterruptibly();
                mChannel.disconnect();
                mSession.disconnect();
                mIsConnected = false;
            }
            catch (InterruptedException e) { e.printStackTrace(); }
            finally { networkingLock.unlock(); }
        }).start();
    }

    public boolean isConnected() {

        try { networkingLock.lockInterruptibly(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        finally { networkingLock.unlock(); }

        return mIsConnected;
    }

}