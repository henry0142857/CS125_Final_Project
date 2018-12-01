package crystal.snowandroid;

import android.drm.DrmStore;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBar;

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
        final Button snowGrowButton = findViewById(R.id.snowGrowButton);
        final Handler handler = new Handler();

        actionbarHeight = 300; //getSupportActionBar().getHeight(); //Not working.

        ifTimePass = true;
        mainSnowGrowth(snow, snowImage, handler);

        snowGrowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ifTimePass = !ifTimePass;
            }
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
