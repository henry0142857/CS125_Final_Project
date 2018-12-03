package crystal.snowandroid;

import android.drm.DrmStore;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBar;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final static int TIMEDELAY = 0; //millisecond

    private boolean ifTimePass;
    private ImageView snowImageCopy;
    private final Snow snow = new HexagonalSnow();
    private float actionbarHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView snowImage = findViewById(R.id.snowImage);
        snowImageCopy = snowImage;
        final ImageButton pauseImageButton = findViewById(R.id.pauseImageButton);
        final ImageButton refreshImageButton = findViewById(R.id.refreshImageButton);
        final SeekBar waterSeekBar = findViewById(R.id.waterSeekBar);
        final SeekBar windSeekBar = findViewById(R.id.windSeekBar);
        final Handler handler = new Handler();

        actionbarHeight = 300; //getSupportActionBar().getHeight(); //Not working.

        ifTimePass = true;
        mainSnowGrowth(snow, snowImage, handler);

        pauseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ifTimePass) {
                    pauseImageButton.setImageResource(R.drawable.run);
                } else {
                    pauseImageButton.setImageResource(R.drawable.pause);
                }
                ifTimePass = !ifTimePass;
            }
        });

        refreshImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snow.clear();
                ifTimePass = true;
            }
        });

        waterSeekBar.setProgress(Math.round(snow.getInitialWaterRatio() * waterSeekBar.getMax()));
        waterSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            final int maxSeekBar = waterSeekBar.getMax();

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                snow.waterSeekBar(1.0f * progress / maxSeekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
        });

        windSeekBar.setMax(snow.getMaxAverageTimes());
        windSeekBar.setProgress(Math.round(snow.getInitialAverageTimes()));
        windSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                snow.setAverageTimes(progress);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void mainSnowGrowth(final Snow snow, final ImageView snowImage, final Handler handler) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (ifTimePass) {
                    snow.timePass();
                    Bitmap snowBitmap = snow.toBitmap();
                    snowImage.setImageBitmap(snowBitmap);
                }
                handler.postDelayed(this, TIMEDELAY);
            }
        };
        handler.postDelayed(runnable, TIMEDELAY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        snow.onTouch(event.getX(), event.getY(),
                snowImageCopy.getWidth(), snowImageCopy.getHeight(), snowImageCopy.getX(), snowImageCopy.getY() + actionbarHeight);
        return true;
    }
}
