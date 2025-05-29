package com.headleader.lockertest.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatTextView;

import com.headleader.lockertest.global.Common;
import com.headleader.lockertest.model.Box;

import java.util.LinkedList;

/**
 *  锁板布局
 */
public class LockerView extends LinearLayout {

    public View.OnClickListener onBoxClicked;

    private int _lockerNo = -1;
    private LinkedList<BoxView> boxviewList = new LinkedList<>();

    public LockerView( Context context ) {
        this(context, null);
    }

    public LockerView( Context context, @Nullable AttributeSet attrs ) {
        this(context, attrs, 0);
    }

    public LockerView( final Context context, @Nullable AttributeSet attrs, int defStyleAttr ) {
        super(context, attrs, defStyleAttr);
        this.setOrientation(VERTICAL);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LockerView( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes ) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void initBoxList(int lockerNo, Box[] boxlist)
    {
        if (_lockerNo != lockerNo){
            _lockerNo = lockerNo;
            initLayout(boxlist);
        } else {
            updateLayout(boxlist);
        }
    }

    /**
     * 初始哈布局
     * @param boxlist
     */
    private void initLayout( Box[] boxlist )
    {
        this.removeAllViews();
        TextView txt = new TextView(this.getContext());
        this.addView(txt);
        LayoutParams layoutParams = (LayoutParams)txt.getLayoutParams();
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.setMargins(0,0,0,0);
        txt.setLayoutParams(layoutParams);
        txt.setTextColor(Color.WHITE);
        txt.setText("柜 " + String.valueOf(_lockerNo + 1));
        int i = 0;
        while(i < boxlist.length) {
            BoxView boxView = new BoxView(this.getContext());
            this.addView(boxView);
            boxviewList.add(boxView);
            layoutParams = (LayoutParams)boxView.getLayoutParams();
            layoutParams.height = LayoutParams.WRAP_CONTENT;
            layoutParams.width = LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.setMargins(0,10,0,0);
            boxView.setLayoutParams(layoutParams);
            boxView.setPadding(40,15,40,15);
            boxView.setBackgroundColor(Color.CYAN);
            boxView.setGravity(Gravity.CENTER);
            boxView.setBox(boxlist[i]);
            i++;
        }
    }

    /**
     * 更新布局
     * @param boxlist
     */
    private void updateLayout( Box[] boxlist )
    {
        int i = 0;
        while(i < boxlist.length) {
            BoxView bv = boxviewList.get(i);
            Box box = bv.getBox();
            if ((box.FlagInfrared != boxlist[i].FlagInfrared)
                || (box.FlagOpened != boxlist[i].FlagOpened))
            {
                boxviewList.get(i).setBox(boxlist[i]);
            }
            i++;
        }
    }

    class BoxView extends AppCompatTextView
    {
        private Box _box;

        public BoxView( Context context ) {
            this(context, null, 0);
        }

        public BoxView( Context context, AttributeSet attrs ) {
            this(context, attrs, 0);
        }

        public BoxView( Context context, AttributeSet attrs, int defStyleAttr ) {
            super(context, attrs, defStyleAttr);
            this.setTypeface(Typeface.SERIF);
            this.setOnClickListener(BoxView_OnClickListener);
        }

        public Box getBox() {
            return _box;
        }

        public void setBox( Box box ) {
            this._box = (Box) box.clone();
            String text = String.format("<font color=\"#0\">%d</font><br/>", box.ID);
            text += String.format("<font color=\"%s\">%s</font><br/>", (box.FlagOpened) ? "#FE00000" : "#0001000" ,(box.FlagOpened) ? "开启" : "关闭"); //
            if (Common.hasSensor == 1){
                text += String.format("<font color=\"%s\">%s</font>", (box.FlagInfrared) ? "#00FE000": "#0001000", (box.FlagInfrared) ? "有物" : "无物");
            }
            this.setText(Html.fromHtml(text));
        }

        private View.OnClickListener BoxView_OnClickListener = new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                try {
                    Common.checkPort();
                    Common.unlock(_box.ID);
                } catch (Exception e) {
                    Common.logFragment.Error("", e);
                }
            }
        };
    }
}


