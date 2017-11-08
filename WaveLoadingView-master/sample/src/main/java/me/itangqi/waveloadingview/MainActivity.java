package me.itangqi.waveloadingview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import devlight.io.library.ArcProgressStackView;

public class MainActivity extends AppCompatActivity {

    public static int Temperature = 30;
    public static int Humidity = 50;
    public static int Pm1_0=12;
    public static int Pm2_5=27;
    public static int Pm10_0=18;
    public static int Co2=816;

    /////////////////////
    public static UDPUtils udpUtils;
    private int defaultServerPort = 25122;           //Craig static 171106
    private Handler handler=new Handler();

    //////////////////////////////////////////////////////
//    Craig
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.ic_tab_favourite,
            R.drawable.ic_tab_call,
            R.drawable.ic_tab_contacts
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabPageInit();
        UdpPageInit();
        handler.postDelayed(runMain,1000);
    }


    public void TabPageInit(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);

    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new ThreeFragment(), "THREE");
        adapter.addFrag(new OneFragment(), "ONE");
        adapter.addFrag(new Co2Fragment(),"Co2");
        adapter.addFrag(new TwoFragment(), "TWO");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public void UdpPageInit() {
        udpUtils = new UDPUtils(this);

        udpUtils.setServerPort(defaultServerPort);
        udpUtils.startReceiveUdp();

    }

    @Override
    public void onDestroy() {
        if ( MainActivity.udpUtils != null ) MainActivity.udpUtils.stopReceiveUdp();
        super.onDestroy();
    }

    private Runnable runMain=new Runnable() {
        @Override
        public void run() {
            if(ThreeFragment.UDPSetUpFlag){
                udpUtils.sendUDP(ThreeFragment.params);
                handler.postDelayed(this,3000);
            }
            else{
                handler.postDelayed(this,3000);
            }
        }
    };

}
