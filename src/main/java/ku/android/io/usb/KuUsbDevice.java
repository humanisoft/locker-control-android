package ku.android.io.usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.util.HashMap;

public class KuUsbDevice extends KuUsb {

    protected static final String TAG = "UsbDevice";
    protected int epNum = 0x01;

    UsbDevice usbDevice;
    UsbDeviceConnection usbConn;
    UsbInterface usbInterface;
    UsbEndpoint epRead, epWrite;

    public KuUsbDevice( Context ctx ) {
      super(ctx);
    }

    public HashMap<String, UsbDevice> list() {
      return usbmanager.getDeviceList();
    }

    public void setDevice(UsbDevice dev) {
        close();
        usbDevice = dev;
    }

    public void registerReceiver() {
        BroadcastReceiver usbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive( Context context, Intent intent) {
                String action=intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)){
                    synchronized (this) {
                        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            onPermissionGranted(device);
                        } else {
                            Log.e(TAG, "USB Permission Denied：" + device.getDeviceName());
                        }
                    }
                } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    onAttached(device);
                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    onDetached(device);
                }
            }
        };
        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);
    }

    @Override
    public void requestPermission() {
        if (usbmanager.hasPermission(usbDevice))
            return;
        synchronized (this) {
            usbmanager.requestPermission(usbDevice, permissionIntent);
        }
        while (!usbmanager.hasPermission(usbDevice)){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public boolean open(){
        if (isOpened()) return true;
        if (usbDevice == null) return false;
        if (permissionIntent == null) registerReceiver();
        requestPermission();
        usbConn= usbmanager.openDevice(usbDevice);
        if (usbConn == null) return false;
        usbInterface= usbDevice.getInterface(0);
        if (usbConn.claimInterface(usbInterface, true)) {
            Log.e(TAG, "USB Intergsvr Claimed：" + usbDevice.getDeviceName());
        } else {
            usbConn.close();
            Log.e(TAG, "USB Intergsvr Claimed failed：" + usbDevice.getDeviceName());
            return false;
        }
        setEndPoint();
        onOpened();
        return true;
    }

    @Override
    public void close(){
        if (!isOpened()) return;
        usbConn.releaseInterface(usbInterface);
        usbInterface = null;
        usbConn.close();
        usbConn= null;
        epWrite = epRead = null;
        onClosed();
    }
    @Override
    public boolean isOpened(){
        return usbInterface != null;
    }
    @Override
    public void write( byte[] datas ) {
        int offset = 0;
        int len = 0;
        while (offset < datas.length){
            len = usbConn.bulkTransfer(epWrite, datas, offset, datas.length, 500);
            if(len <= 0) return;
            offset += len;
        }
        onSent(datas);
    }

    @Override
    protected void read() {
        int len = 0;
        byte[] datas = new byte[1024];
        try {
            len = usbConn.bulkTransfer(epRead, datas, datas.length, 500);
            if(len <= 0) return;
            byte[] temp = new byte[len];
            System.arraycopy(datas,0, temp, 0, len);
            onReceived(temp);
        }
        catch (Exception ex){
            onError(ex);
        }
    }
    protected void onPermissionGranted( UsbDevice device ) { }
    protected void onAttached( UsbDevice device ) { }
    protected void onDetached( UsbDevice device ) { }

    private void setEndPoint() {
        int count = usbInterface.getEndpointCount();
        for(int i = 0; i < count; i++){
            UsbEndpoint ep = usbInterface.getEndpoint(i);
            if (ep.getEndpointNumber() == epNum) {
                if (ep.getDirection() == UsbConstants.USB_DIR_IN)
                    epRead = ep;
                else
                    epWrite = ep;
            }
            if (epWrite !=  null && epRead != null)
                return;
        }
    }
}
