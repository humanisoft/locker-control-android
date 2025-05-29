package com.headleader.lockertest.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.headleader.lockertest.R;
import com.headleader.lockertest.global.Common;

import java.util.ArrayList;

public class LockFragment extends Fragment {

    private Button btnUnlock, btnPause, btnRefresh;
    private Spinner cboRelay1, cboRelay2;
    private RecyclerView boxLayout;

    @Override
    public View onCreateView( LayoutInflater inflater, final ViewGroup container,
                              Bundle savedInstanceState ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lock, container, false);
        btnUnlock = view.findViewById(R.id.lock_btnUnlock);
        btnPause = view.findViewById(R.id.lock_btnPause);
        btnRefresh = view.findViewById(R.id.lock_btnRefresh);
        cboRelay1 = view.findViewById(R.id.lock_cboRelay1);
        cboRelay2 = view.findViewById(R.id.lock_cboRelay2);
        boxLayout = view.findViewById(R.id.lock_boxLayout);

        btnUnlock.setOnClickListener(BtnUnlock_OnClickListener);
        btnRefresh.setOnClickListener(BtnRefresh_OnClickListener);
        btnPause.setOnClickListener(BtnPause_OnClickListener);

        init();
        return view;
    }

    public void init( ) {

        initCboRelay();

        //设置RecyclerView管理器
        boxLayout.setLayoutManager(new LinearLayoutManager(this.getActivity(), LinearLayoutManager.HORIZONTAL, false));
        //设置添加或删除item时的动画，这里使用默认动画
        boxLayout.setItemAnimator(null);
//        boxLayout.setItemAnimator(new DefaultItemAnimator());
        //设置适配器
        boxLayout.setAdapter(Common.boxLayoutAdapter);
    }

    public void initCboRelay() {
        if (cboRelay1 == null) return;
        ArrayList<String> listRelay = new ArrayList<>();
        int n = 0;
        for (int i = 0; i < Common.lockerCount; i++)
            for (int j = 0; j < Common.layerCount; j++)
                listRelay.add(String.valueOf(++n));
        ArrayAdapter<String> adapterRelay1 = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, listRelay);
        cboRelay1.setAdapter(adapterRelay1);
        cboRelay1.setSelection(0);
        ArrayAdapter<String> adapterRelay2 = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, listRelay);
        cboRelay2.setAdapter(adapterRelay2);
        cboRelay2.setSelection(0);
    }

    private final View.OnClickListener BtnUnlock_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            if (btnUnlock.getText().equals(getResources().getString(R.string.unlock))){
                int relay1 = cboRelay1.getSelectedItemPosition() + 1;
                int relay2 = cboRelay2.getSelectedItemPosition() + 1;
                Unlock(relay1, relay2);
            } else {
                flagUnlock = 0;
            }
        }
    };

    private final View.OnClickListener BtnPause_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            if (flagUnlock == 1){
                flagUnlock = 2;
                btnPause.setText(getResources().getString(R.string.Continue));
            } else if (flagUnlock == 2){
                flagUnlock = 1;
                btnPause.setText(getResources().getString(R.string.pause));
            }
        }
    };

    private final View.OnClickListener BtnRefresh_OnClickListener = v -> {
        String msg = getResources().getString(R.string.updateRelayStatus);
        try {
            Common.refreshBoardStatus();
            Common.logFragment.Info(String.format("%s : %s",
                    msg, getResources().getString(R.string.succeed)));
        } catch (Exception e) {
            Common.logFragment.Error(msg, e);
        }
    };

    private int flagUnlock = 0;
    private void Unlock( final int relay1, final int relay2 ) {
        new Thread(){
            @Override
            public void run(){
                int relayB = relay1;
                int relayE = relay2;
                if (relay1 > relay2) {
                    relayB = relay2;
                    relayE = relay1;
                }
                String msg = String.format("%s %d -> %d : ",
                        getResources().getString(R.string.unlock), relayB, relayE);
                try {
                    Common.checkPort();
                    flagUnlock = 1;
                    Common.runOnUiThread(() -> btnUnlock.setText(getResources().getString(R.string.stop)));
                    while(true){
                        Common.unlock(relayB);
                        if (relayB >= relayE) break;
                        if (flagUnlock == 0) break;
                        while(true){
                            Thread.sleep(1000);
                            if (flagUnlock != 2) break;
                        }
                        relayB++;
                    }
                    Common.runOnUiThread(() -> {
                        btnUnlock.setText(getResources().getString(R.string.unlock));
                        btnPause.setText(getResources().getString(R.string.pause));
                    });
                } catch (Exception e) {
                    Common.logFragment.Error(msg, e);
                }
            }
        }.start();
    }
}
