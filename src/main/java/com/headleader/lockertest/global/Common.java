package com.headleader.lockertest.global;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.widget.Toast;

import com.headleader.LockBoard.Board;
import com.headleader.LockBoard.BoardSerial;
import com.headleader.LockBoard.ICallBack;
import com.headleader.lockertest.module.MySerial;
import com.headleader.lockertest.ui.MainActivity;
import com.headleader.lockertest.ui.adapter.BoxLayoutAdapter;
import com.headleader.lockertest.R;
import com.headleader.lockertest.model.Box;
import com.headleader.lockertest.ui.BoardFragment;
import com.headleader.lockertest.ui.LockFragment;
import com.headleader.lockertest.ui.LogFragment;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ku.util.KuConvert;

public final class Common implements ICallBack{

    public static int lockerCount = 8;
    public static int layerCount = 8;
    public static int lockKeepTIme = 500;              // 500毫秒
    public static String lang = "";//"en";
    public static Common com;
    public static int baudrate = 115200;
    public static int hasSensor = 1;
    public static String port = "";
    public static boolean portOpened = false;
    public static int[] allLockStatus;
    public static int[] allSensorStatus;

    public static BoardSerial boardSerial;
    public static Board mb;

    public static Map<Integer, Box[]> boxMap;
    public static BoardFragment boardFragment;
    public static LockFragment lockFragment;
    public static LogFragment logFragment;
    public static BoxLayoutAdapter boxLayoutAdapter;
    public static Monitor monitor;
    public static Activity activity;
    public static String flagtype;

    public Common(){
        Common.mb=Common.boardSerial=new BoardSerial(this);
        init();
        sp = activity.getSharedPreferences("Info", MainActivity.MODE_PRIVATE);
        flagtype=sp.getString("type", "");
    }
    public static SharedPreferences sp;
    public static void updatetype(String type) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("type", type);
        editor.apply();
    }

    public static void init()
    {
        setLocale();
        boardFragment = new BoardFragment();
        lockFragment = new LockFragment();
        logFragment = new LogFragment();
        setLayerCount(layerCount);
        monitor = new Monitor();
    }

    public static void initBoxMap() {
        boxMap = new HashMap<>();
        allLockStatus = new int[lockerCount * layerCount];
        allSensorStatus = new int[lockerCount * layerCount];
        int n = 0;
        for (int i = 0; i < lockerCount; i++)
        {
            Box[] boxlist = new Box[layerCount];
            for (int j = 0; j < layerCount; j++) {
                allLockStatus[n] = allSensorStatus[n] = 1;
                boxlist[j] = new Box();
                boxlist[j].ID = ++n;
            }
            boxMap.put(i,boxlist);
        }
        boxLayoutAdapter = new BoxLayoutAdapter();
    }

    public static void setLayerCount(int layer)
    {
        layerCount = layer;
        mb.layerCount(layer);
//        Common.lockBoard.layerCount(layer);
        initBoxMap();
        lockFragment.initCboRelay();
        Common.boxLayoutAdapter.notifyDataSetChanged();
    }

    public static void setLocale()
    {
        if (lang.isEmpty()) return;
        Configuration conf = activity.getResources().getConfiguration();
        conf.locale = new Locale(Common.lang);
        activity.getResources().updateConfiguration(conf, activity.getResources().getDisplayMetrics());
    }

    public static void checkPort() throws Exception
    {
        if (!Common.portOpened) throw new Exception(activity.getResources().getString(R.string.portNotOpened));
    }

    public static synchronized void unlock(int id) {
        int board = (id - 1) / layerCount + 1;
        int relay = (id - 1) % layerCount + 1;
        String msg = String.format("%s %d: ", activity.getResources().getString(R.string.unlock), relay);
        try {
            if(flagtype.equals("0"))
                mb.Unlock(board, relay);
            else
                mb.Unlock2(board, relay, lockKeepTIme);
                //mb.Unlock2(board, relay, lockKeepTIme / 10);毫秒为啥除以10？
//            mySerial.write(lockBoard.dataForUnlock2(board, relay, lockKeepTIme / 10));
            logFragment.Info(String.format("%s, %s",
                    msg, activity.getResources().getString(R.string.succeed)));
        } catch (Exception ex) {
            logFragment.Error(String.format("%s, %s, error:%s",
                    msg, activity.getResources().getString(R.string.failed), ex.getMessage()));
        }
    }
    public static synchronized void refreshBoardStatus() throws Exception
    {
        checkPort();
        mb.GetRelayStatus(true);
    }
    public static void runOnUiThread(Runnable r) { activity.runOnUiThread(r);}
    public static void toast(final String msg){ runOnUiThread(() -> Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show());}


    @Override
    public void OnRelayUpdated() {
                int temp1[],temp2[] = null;
        temp1=Common.mb.allLockStatus();
        temp2=Common.mb.allSensorStatus();
                boolean flagChanged;
                int i, j, n = 0;
                for (i = 0; i < Common.lockerCount; i++) {
                    flagChanged = false;
                    for (j = 0; j < Common.layerCount; j++) {
                        if (n >= temp1.length) break;
                        if (temp1[n] != Common.allLockStatus[n]) {
                            Common.allLockStatus[n] = temp1[n];
                            Common.boxMap.get(i)[j].FlagOpened = temp1[n] == 0;
                            flagChanged = true;
                        }
                        if (temp2[n] != Common.allSensorStatus[n]) {
                            Common.allSensorStatus[n] = temp2[n];
                            Common.boxMap.get(i)[j].FlagInfrared = temp2[n] == 0;
                            flagChanged = true;
                        }
                        n++;
                    }
                    if (flagChanged) {
                        final int index = i;
                        runOnUiThread(() -> Common.boxLayoutAdapter.notifyItemChanged(index));
                    }
                }
    }

    @Override
    public void OnError(Exception ex) {
                toast(ex.getMessage());
    }

    @Override
    public void OnSent(byte[] datas) {
                String strTemp = KuConvert.toHex(datas, " ");
                logFragment.AddLog(String.format("Serial Sent: %s", strTemp));
                System.out.println("Serial Sent:"+strTemp);
    }

    @Override
    public void OnReceived(byte[] datas) {
                String strTemp = KuConvert.toHex(datas, " ");
                logFragment.AddLog(String.format("Serial Received: %s", strTemp));
//                lockBoard.handle(datas);
                System.out.println("Serial Received:"+strTemp);
    }
}
