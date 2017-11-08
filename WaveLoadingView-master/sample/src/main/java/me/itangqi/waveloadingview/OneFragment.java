package me.itangqi.waveloadingview;

/**
 * Created by Craig on 2017/11/1.
 */
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.larswerkman.lobsterpicker.OnColorListener;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;


public class OneFragment extends Fragment{
    public WaveLoadingView mWaveLoadingView;
    private int checkedItem = 0;

    private Handler handler = new Handler( );
    private static int changeTimes=0;


    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.wave_main, container, false);

        mWaveLoadingView = (WaveLoadingView) v.findViewById(R.id.waveLoadingView);
        mWaveLoadingView.setAnimDuration(3000);
        mWaveLoadingView.setCenterTitle("Temperature: "+Integer.toString(MainActivity.Temperature)+" C");
        mWaveLoadingView.setProgressValue(MainActivity.Temperature);

        handler.postDelayed(runHumidity,5000); // 开始Timer

//        mWaveLoadingView.setProgressValue(50);


//                // Animator
//        ((CheckBox) v.findViewById(R.id.cb_animator_cancel_and_start)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b) {
//                    mWaveLoadingView.cancelAnimation();
//                } else {
//                    mWaveLoadingView.startAnimation();
//                }
//            }
//        });
//
//        ((CheckBox) v.findViewById(R.id.cb_animator_pause_and_resume)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b) {
//                    mWaveLoadingView.pauseAnimation();
//                } else {
//                    mWaveLoadingView.resumeAnimation();
//                }
//            }
//        });
//
//        // Top Title
//        ((CheckBox) v.findViewById(R.id.cb_title_top)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b) {
//                    mWaveLoadingView.setTopTitle("Top Title");
//                } else {
//                    mWaveLoadingView.setTopTitle("");
//                }
//            }
//        });
//        // Center Title
//        ((CheckBox) v.findViewById(R.id.cb_title_center)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b) {
//                    mWaveLoadingView.setCenterTitle("Center Title");
//                } else {
//                    mWaveLoadingView.setCenterTitle("");
//                }
//            }
//        });
//        // Bottom Title
//        ((CheckBox) v.findViewById(R.id.cb_title_bottom)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b) {
//                    mWaveLoadingView.setBottomTitle("Bottom Title");
//                } else {
//                    mWaveLoadingView.setBottomTitle("");
//                }
//            }
//        });
//
//        // Progress
//        ((DiscreteSeekBar) v.findViewById(R.id.seekbar_progress)).setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
//            @Override
//            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
//                mWaveLoadingView.setProgressValue(value);
//            }
//
//            @Override
//            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
//
//            }
//        });
//
//        // Border
//        ((DiscreteSeekBar) v.findViewById(R.id.seekbar_border_width)).setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
//            @Override
//            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
//                mWaveLoadingView.setBorderWidth(value);
//            }
//
//            @Override
//            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
//            }
//        });
//
//        // Amplitude
//        ((DiscreteSeekBar) v.findViewById(R.id.seek_bar_amplitude)).setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
//            @Override
//            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
//                mWaveLoadingView.setAmplitudeRatio(value);
//            }
//
//            @Override
//            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
//            }
//        });
//
//        // Wave Color
//        ((LobsterShadeSlider) v.findViewById(R.id.shadeslider_wave_color)).addOnColorListener(new OnColorListener() {
//            @Override
//            public void onColorChanged(@ColorInt int color) {
//                mWaveLoadingView.setWaveColor(color);
//            }
//
//            @Override
//            public void onColorSelected(@ColorInt int color) {
//            }
//        });
//        //Wave Background Color
//        ((LobsterShadeSlider) v.findViewById(R.id.shadeslider_wave_background_color)).addOnColorListener(new OnColorListener() {
//            @Override
//            public void onColorChanged(@ColorInt int color) {
//                mWaveLoadingView.setWaveBgColor(color);
//            }
//
//            @Override
//            public void onColorSelected(@ColorInt int color) {
//            }
//        });
//
//        // Border Color
//        ((LobsterShadeSlider) v.findViewById(R.id.shadeslider_border_color)).addOnColorListener(new OnColorListener() {
//            @Override
//            public void onColorChanged(@ColorInt int color) {
//                mWaveLoadingView.setBorderColor(color);
//            }
//
//            @Override
//            public void onColorSelected(@ColorInt int color) {
//            }
//        });

        return v;
    }


    private Runnable runHumidity = new Runnable( ) {
        public void run ( ) {
            if(changeTimes%2==0){
                changeTimes++;
                mWaveLoadingView.setCenterTitle("Humidity: "+Integer.toString(MainActivity.Humidity)+" %");
                mWaveLoadingView.setProgressValue(MainActivity.Humidity);
                mWaveLoadingView.setWaveColor(Color.BLUE);
                handler.postDelayed(this,5000);
            }
            else if(changeTimes%2==1){
                changeTimes++;
                mWaveLoadingView.setCenterTitle("Temperature: "+Integer.toString(MainActivity.Temperature)+" C");
                mWaveLoadingView.setProgressValue(MainActivity.Temperature);
                mWaveLoadingView.setWaveColor(Color.RED);
                handler.postDelayed(this,5000);
            }

//postDelayed(this,1000)方法安排一个Runnable对象到主线程队列中
        }
    };

    @Override
    public void onPause() {
        handler.removeCallbacks(runHumidity);
        super.onPause();
    }

    @Override
    public void onResume(){
        if (handler == null)
            handler.postDelayed(runHumidity,5000);
        super.onResume();
    }
}
