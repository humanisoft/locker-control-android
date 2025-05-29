package ku.android.io.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;


import com.headleader.lockertest.global.Common;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ku.base.KuThread;

//TODO 需要设备测试及调整
public class KuUsbAccessory extends KuUsb {

    UsbAccessory usbAccessory;

    ParcelFileDescriptor filedescriptor = null;
    FileInputStream inputstream = null;
    FileOutputStream outputstream = null;

    public KuUsbAccessory( Context ctx ) { super(ctx); }

    public UsbAccessory[] list() { return usbmanager.getAccessoryList(); }

    public void registerReceiver() {
        BroadcastReceiver usbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive( Context context, Intent intent) {
                String action = intent.getAction();
                if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
                    synchronized (this) {
                        UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            Common.toast("Allow USB Permission");
                            OpenAccessory(accessory);
                        }
                        else {
                            Common.toast("Deny USB Permission");
                        }
                    }
                }
                else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                    DestroyAccessory();
                }
            }
        };
        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(usbReceiver, filter);
    }
    @Override
    public void requestPermission() {
        if (usbmanager.hasPermission(usbAccessory))
            return;
        synchronized (this) {
            usbmanager.requestPermission(usbAccessory, permissionIntent);
        }
        while (!usbmanager.hasPermission(usbAccessory)){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean open() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isOpened() {
        return false;
    }

    @Override
    public void write( byte[] datas ) {

    }

    @Override
    protected void read() {

    }

    private void OpenAccessory(UsbAccessory accessory) {
        filedescriptor = usbmanager.openAccessory(accessory);
        if(filedescriptor != null){
            usbAccessory= accessory;
            FileDescriptor fd = filedescriptor.getFileDescriptor();
            inputstream = new FileInputStream(fd);
            outputstream = new FileOutputStream(fd);
            if (_rRead == null) _rRead = new KuThread();
            if (_rRead.isLoop()) return;
//            SetConfig();
            _rRead.loop(new Runnable() {
                @Override
                public void run() {
                    readData();
                }
            }, 100);
        }
    }
    private void CloseAccessory() {
        try{
            if(filedescriptor != null)
                filedescriptor.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            if(inputstream != null)
                inputstream.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        try {
            if(outputstream != null)
                outputstream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        filedescriptor = null;
        inputstream = null;
        outputstream = null;
    }
    /*destroy accessory*/
    public void DestroyAccessory(){
//        SendToUsb(new byte[] {0});
        try {
            Thread.sleep(10);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        CloseAccessory();
    }

    private void readData( ) {
        byte[] usbdata = new byte[1024];
        try {
            int len = inputstream.read(usbdata,0,usbdata.length);
            if(len <= 0) return;
            byte[] temp = new byte[len];
            System.arraycopy(usbdata, 0, temp, 0, len);
            onReceived(temp);
        }
        catch (IOException ex){
            onError(ex);
        }
    }
    /*method to send on USB*/
    private void SendData(byte[] datas) {
        try {
            if(outputstream != null) {
                outputstream.write(datas, 0, datas.length);
                onSent(datas);
            }
        } catch (IOException ex) {
            onError(ex);
        }
    }

}
