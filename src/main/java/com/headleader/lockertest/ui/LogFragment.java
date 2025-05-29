package com.headleader.lockertest.ui;


import android.os.Bundle;

import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.headleader.lockertest.global.Common;
import com.headleader.lockertest.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class LogFragment extends Fragment {

    private ArrayList<String> loglist;
    private TextView txtLog;

    public LogFragment() {
        loglist = new ArrayList<>();
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        txtLog = view.findViewById(R.id.txtLog);
        txtLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        try {
            txtLog.setText(TextUtils.join("\r\n",loglist));
        } catch (Exception ex){
        }
        return view;
    }

    public void Info(String text)
    {
        Common.toast(text);
        AddLog(text);
    }
    public void Error(String text)
    {
        Common.toast(text);
        AddLog(text);
    }
    public void Error(String pre, Exception e)
    {
        String err = e.getMessage();
        if (err == null) err = e.toString();
        err = String.format("%s : %s", pre, err);
        Error(err);
    }

    public void AddLog(String text)
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strTime = df.format(new Date(System.currentTimeMillis()));
        text = String.format("[%s]:\r\n%s\r\n", strTime, text);
        loglist.add(text);
        while(loglist.size() > 30)
            loglist.remove(0);

        if (txtLog != null){
            String t = TextUtils.join("\r\n",loglist);
            Common.runOnUiThread(() -> txtLog.setText(t));
        }
    }


}
