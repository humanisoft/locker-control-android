package com.headleader.lockertest.ui;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.headleader.lockertest.global.Common;
import com.headleader.lockertest.R;

import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class BoardFragment extends Fragment implements RadioGroup.OnCheckedChangeListener{

    private Spinner cboPort, cboLayer,cboKeepTime;
    private Button btnPort, btnGetLayer, btnSetLayer;
    private LinearLayout layout2;
    private RadioGroup radioGroup;
    private RadioButton radio_option1,radio_option2;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_board, container, false);
        cboPort = view.findViewById(R.id.board_cboPort);
        btnPort = view.findViewById(R.id.board_btnPort);
        cboLayer = view.findViewById(R.id.board_cboLayer);
        btnGetLayer = view.findViewById(R.id.board_btnGetLayer);
        btnSetLayer = view.findViewById(R.id.board_btnSetLayer);
        layout2 = view.findViewById(R.id.board_layout2);
        radioGroup = view.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(this);
        radio_option1= view.findViewById(R.id.radio_option1);
        radio_option2= view.findViewById(R.id.radio_option2);
        if(Common.flagtype.equals("0"))
            radio_option1.setChecked(true);
        else
            radio_option2.setChecked(true);

        cboKeepTime = view.findViewById(R.id.board_cboKeepTime);

        final Context context = container.getContext();
        btnPort.setOnClickListener(BtnPort_OnClickListener);
        btnSetLayer.setOnClickListener(BtnSetLayer_OnClickListener);

        String[] portlist = new String[0];
        try {
            portlist=Common.boardSerial.getPortNames();
//            portlist = Common.mySerial.list();
        } catch (Exception ex) {
            Log.e("mySerial", ex.getMessage());
        }
        List<String> listPort = Arrays.asList(portlist);
        ArrayAdapter<String> adapterPort = new ArrayAdapter<String>(container.getContext()
                , android.R.layout.simple_spinner_dropdown_item, listPort);
        cboPort.setAdapter(adapterPort);
        cboPort.setSelection(0);
        initLayoutLayer();
        initCboKeepTIme();

        return view;
    }

    private void initCboKeepTIme() {
        Integer[] arr = new Integer[20];
        for (Integer i = 1; i <= 20; i++)
            arr[i - 1] = 500 * i;
        List<Integer> list = Arrays.asList(arr);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this.getActivity()
                , android.R.layout.simple_spinner_dropdown_item, list);
        cboKeepTime.setAdapter(adapter);
        for  (int i = 0; i < arr.length; i++) {
            if (Common.lockKeepTIme == arr[i]){
                cboKeepTime.setSelection(i);
                break;
            }
        }
        cboKeepTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Common.lockKeepTIme = arr[i];
                System.out.println("time:"+Common.lockKeepTIme);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void initLayoutLayer() {
        layout2.setVisibility(View.VISIBLE);
        Integer[] arr = { 8, 9, 10, 12};
        List<Integer> list = Arrays.asList(arr);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this.getActivity()
                , android.R.layout.simple_spinner_dropdown_item, list);
        cboLayer.setAdapter(adapter);
        for  (int i = 0; i < arr.length; i++) {
            if (Common.layerCount == arr[i]){
                cboLayer.setSelection(i);
                break;
            }
        }
    }

    private View.OnClickListener BtnPort_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            String msg = "";
            try {
                if (btnPort.getText().equals(getResources().getString(R.string.open)))
                {
                    msg = getResources().getString(R.string.openPort);
                    Common.port = cboPort.getSelectedItem().toString();
                    Common.boardSerial.Port=Common.port;
                    Common.boardSerial.Baudrate=Common.baudrate;
                    Common.boardSerial.Open();
//                    Common.mySerial.open(Common.port, Common.baudrate);
                    Common.logFragment.AddLog(getResources().getString(R.string.portOpened));
                    btnPort.setText(getResources().getString(R.string.close));
                    Common.portOpened = true;
                }
                else
                {
                    msg = getResources().getString(R.string.closePort);
                    Common.boardSerial.Close();
//                    Common.mySerial.close();
                    Common.logFragment.Info(getResources().getString(R.string.portClosed));
                    btnPort.setText(getResources().getString(R.string.open));
                    Common.portOpened = false;
                }
            } catch (Exception e) {
                Common.logFragment.Error(msg, e);
            }
        }
    };
    private View.OnClickListener BtnSetLayer_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
            String msg = getResources().getString(R.string.setLockboardTiers);
            try {
                int row = (int)(cboLayer.getSelectedItem());
                Common.runOnUiThread(()->{
                    Common.setLayerCount(row);
                });
                Common.logFragment.Info(String.format("%s : %s", msg, getResources().getString(R.string.succeed)));
            } catch (Exception e) {
                Common.logFragment.Error(msg, e);
            }
        }
    };

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.radio_option1:
                Common.flagtype="0";
                Common.updatetype(Common.flagtype);
                break;
            case R.id.radio_option2:
                Common.flagtype="1";
                Common.updatetype(Common.flagtype);
                break;
        }
    }
}
