package com.headleader.lockertest.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.headleader.LockBoard.BoardSerial;
import com.headleader.LockBoard.ICallBack;
import com.headleader.lockertest.R;
import com.headleader.lockertest.global.Common;
import com.headleader.lockertest.util.PermissionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity  {

    final List<Fragment> fragmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TabLayout tabBar = findViewById(R.id.tabbar);
        ViewPager viewPager = findViewById(R.id.viewPager);

        PermissionUtil.AcquirePermission(this);
        Common.activity = this;
        Common.com=new Common();




        fragmentList.add(Common.boardFragment);
        fragmentList.add(Common.lockFragment);
        fragmentList.add(Common.logFragment);

        final List<String> titleList = Arrays.asList(
            getResources().getString(R.string.commonSet),
            getResources().getString(R.string.boardTest),
            getResources().getString(R.string.logs));

//        setSupportActionBar(toolbar);
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem( int position) {
                return fragmentList.get(position);
            }

            @Override   //返回Tab名字
            public CharSequence getPageTitle(int position) { return titleList.get(position); }

            @Override
            public int getCount() {
                return titleList.size();
            }
        });

        tabBar.setupWithViewPager(viewPager);
        //注册广播
        Common.logFragment.AddLog(getResources().getString(R.string.app_start));
        Common.monitor.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

}
