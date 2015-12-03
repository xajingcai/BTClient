package com.test.BTClient;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.Button;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Li on 2015/9/14.
 */
public class MyButton extends Button {

    BluetoothSocket _socket = null;
    public MyButton(Context context, AttributeSet set)
    {
        super(context, set);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {

        super.onKeyDown(keyCode, event);
        try {
            OutputStream osUp = _socket.getOutputStream();
            osUp.write(0xAA);
            osUp.write(0x33);
            osUp.write(0x44);
        }catch(IOException e){
        }
        return true;
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        super.onKeyUp(keyCode, event);
        try {
            OutputStream osUp = _socket.getOutputStream();
            osUp.write(0xAA);
            osUp.write(0x00);
            osUp.write(0x00);
        }catch(IOException e){
        }
        return true;
    }
}


