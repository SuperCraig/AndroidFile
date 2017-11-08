package me.itangqi.waveloadingview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

import devlight.io.library.ArcProgressStackView;
import me.itangqi.waveloadingview.R;

/**
 * Created by mblock on 2017/11/3.
 */

public class Co2Fragment extends Fragment {
    private  Handler handler=new Handler();

    public WaveLoadingView co2Wave;

    public Co2Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.co2_main, container, false);

        co2Wave=(WaveLoadingView)v.findViewById(R.id.co2Wave);
        co2Wave = (WaveLoadingView) v.findViewById(R.id.co2Wave);
        co2Wave.setWaveColor(Color.LTGRAY);
        co2Wave.setAnimDuration(3000);
        co2Wave.setCenterTitle("Co2: "+Integer.toString(MainActivity.Co2)+" PPM");

        if((MainActivity.Co2*100/2500)<100){
            co2Wave.setProgressValue(MainActivity.Co2*100/2500);
        }else{
            co2Wave.setProgressValue(100);
        }


        handler.postDelayed(runCo2,5000); // 开始Timer
        return v;
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runCo2);
        super.onPause();
    }

    @Override
    public void onResume(){
        if(handler==null)
            handler.postDelayed(runCo2,1000);
        super.onResume();
    }

    private Runnable runCo2=new Runnable() {
        @Override
        public void run() {
            co2Wave.setCenterTitle("Co2: "+Integer.toString(MainActivity.Co2)+" PPM");

            if((MainActivity.Co2*100/2500)<100){
                co2Wave.setProgressValue(MainActivity.Co2*100/2500);
            }else{
                co2Wave.setProgressValue(100);
            }

            co2Wave.setWaveColor(Color.LTGRAY);
            handler.postDelayed(this,5000);
        }
    };
}
