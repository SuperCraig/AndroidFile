package me.itangqi.waveloadingview;

/**
 * Created by Craig on 2017/11/1.
 */
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import devlight.io.library.ArcProgressStackView;
import me.itangqi.waveloadingview.R;

public class TwoFragment extends Fragment{
    public final static int MODEL_COUNT = 4;

    // APSV
    private ArcProgressStackView mArcProgressStackView;

    // Parsed colors
    private int[] mStartColors = new int[MODEL_COUNT];
    private int[] mEndColors = new int[MODEL_COUNT];

    private Handler handler =new Handler();

    public TwoFragment() {
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
        View v=inflater.inflate(R.layout.new_main, container, false);

        mArcProgressStackView = (ArcProgressStackView) v.findViewById(R.id.newapsv);

        // Get colors
        final String[] startColors = getResources().getStringArray(R.array.devlight);
        final String[] endColors = getResources().getStringArray(R.array.default_preview);
        final String[] bgColors = getResources().getStringArray(R.array.medical_express);

        // Parse colors
        for (int i = 0; i < MODEL_COUNT; i++) {
            mStartColors[i] = Color.parseColor(startColors[i]);
            mEndColors[i] = Color.parseColor(endColors[i]);
        }

        // Set models
        final ArrayList<ArcProgressStackView.Model> models = new ArrayList<>();


        models.add(new ArcProgressStackView.Model("pm10.0: "+Integer.toString(MainActivity.Pm10_0), MainActivity.Pm10_0*2, Color.parseColor(bgColors[2]), mStartColors[2]));
        models.add(new ArcProgressStackView.Model("pm2.5: "+Integer.toString(MainActivity.Pm2_5), MainActivity.Pm2_5*2, Color.parseColor(bgColors[1]), mStartColors[1]));
        models.add(new ArcProgressStackView.Model("pm1.0: "+Integer.toString(MainActivity.Pm1_0), MainActivity.Pm1_0*2, Color.parseColor(bgColors[0]), mStartColors[0]));
        models.add(new ArcProgressStackView.Model("Particle Matters (units:ug/m3)", 100, Color.parseColor(bgColors[3]), Color.BLACK));
        mArcProgressStackView.setModels(models);
        mArcProgressStackView.setIsAnimated(true);
        mArcProgressStackView.setIsRounded(false);
        mArcProgressStackView.setShowProgress(false);
        mArcProgressStackView.setStartAngle(180);

        // Set animator listener
        mArcProgressStackView.setAnimatorUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                // Update goes here
                Log.d("onAnimationUpdate: ", String.valueOf(animation.getAnimatedValue()));
            }
        });

        handler.postDelayed(RunPM,3000); // 开始Timer
        return v;
    }

    private Runnable RunPM= new Runnable( ) {
        public void run ( ) {
            // Get colors
            final String[] bgColors = getResources().getStringArray(R.array.medical_express);

            final ArrayList<ArcProgressStackView.Model> models = new ArrayList<>();
            models.add(new ArcProgressStackView.Model("pm10.0: "+Integer.toString(MainActivity.Pm10_0), MainActivity.Pm10_0*2, Color.parseColor(bgColors[2]), mStartColors[2]));
            models.add(new ArcProgressStackView.Model("pm2.5: "+Integer.toString(MainActivity.Pm2_5), MainActivity.Pm2_5*2, Color.parseColor(bgColors[1]), mStartColors[1]));
            models.add(new ArcProgressStackView.Model("pm1.0: "+Integer.toString(MainActivity.Pm1_0), MainActivity.Pm1_0*2, Color.parseColor(bgColors[0]), mStartColors[0]));
            models.add(new ArcProgressStackView.Model("Particle Matters (units:ug/m3)", 100, Color.parseColor(bgColors[3]), Color.BLACK));
            mArcProgressStackView.setModels(models);
            mArcProgressStackView.animateProgress();
            mArcProgressStackView.setAnimationDuration(1000);
            handler.postDelayed(this,5000);
        }
    };

    @Override
    public void onPause() {
        handler.removeCallbacks(RunPM);
        super.onPause();
    }

    @Override
    public void onResume(){
        if (handler == null){
            handler.postDelayed(RunPM,5000);
        }
        super.onResume();
    }
}
