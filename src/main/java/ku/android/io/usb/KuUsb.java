package ku.android.io.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;

import ku.base.KuThread;

public abstract class KuUsb {

    protected static final String TAG = "USB";
    protected static String ACTION_USB_PERMISSION = "com.hld.USB_PERMISSION";

    protected UsbManager usbmanager;
    protected PendingIntent permissionIntent;
    protected KuThread _rRead;
    protected Context ctx;

    protected KuUsb( Context ctx){
        this.ctx = ctx;
        usbmanager = (UsbManager) this.ctx.getSystemService(Context.USB_SERVICE);
    }
    public abstract void requestPermission();
    public abstract boolean isOpened();
    public abstract boolean open();
    public abstract void close();
    public abstract void write(byte[] datas);
    protected abstract void read();
    protected void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        filter.addAction(ACTION_USB_PERMISSION);
        ctx.registerReceiver(receiver, filter);
        permissionIntent = PendingIntent.getBroadcast(ctx, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE);
    }

    protected void onOpened(){
        if (_rRead == null)_rRead = new KuThread();
        _rRead.loop(() -> read(), 100);
    }
    protected void onClosed(){ _rRead.stopLoop(); }
    protected void onReceived( byte[] datas ) { }
    protected void onSent( byte[] datas ) { }
    protected void onError( Exception ex ) { }
}
