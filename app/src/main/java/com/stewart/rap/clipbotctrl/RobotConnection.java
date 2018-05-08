package com.stewart.rap.clipbotctrl;

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
    private boolean mConnected = false;
    private final ReentrantLock networkingLock = new ReentrantLock();

    public void connect() {

        new Thread(() -> {
            try {
                networkingLock.lockInterruptibly();
                MyApplication.bus.post("busy");
                mSession = new JSch().getSession("user", "192.168.1.100", 7005);
                mSession.setPassword("pass");
                Properties prop = new Properties();
                prop.put("StrictHostKeyChecking", "no");
                mSession.setConfig(prop);
                mSession.connect(5000); // 5 seconds
                mChannel = mSession.openChannel("shell");
                mChannel.setOutputStream(new ByteArrayOutputStream());
                mOutputStream = mChannel.getOutputStream();
                mChannel.connect();
                mConnected = true;
            }
            catch (JSchException|IOException|InterruptedException e) {
                if (mChannel != null && mChannel.isConnected())
                    mChannel.disconnect();
                if (mSession != null && mSession.isConnected())
                    mSession.disconnect();
                mConnected = false;
            }
            finally {
                networkingLock.unlock();
                MyApplication.bus.post("free");
            }
        }).start();

        return;
    }

    public void sendString(String string) {

        new Thread(() -> {
            try {
                networkingLock.lockInterruptibly();
                if (mConnected) {
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
                MyApplication.bus.post("busy");
                if (mChannel != null && mChannel.isConnected())
                    mChannel.disconnect();
                if (mSession != null && mSession.isConnected())
                    mSession.disconnect();
            }
            catch (InterruptedException e) { e.printStackTrace(); }
            finally {
                networkingLock.unlock();
                MyApplication.bus.post("free");
                mConnected = false;
            }
        }).start();
    }

    public boolean isConnected() {

        try { networkingLock.lockInterruptibly(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        finally { networkingLock.unlock(); }

        return mConnected;
    }

}