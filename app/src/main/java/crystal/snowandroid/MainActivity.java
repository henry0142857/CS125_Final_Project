package crystal.snowandroid;

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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

        ifTimePass = true;
        actionbarHeight = 300; //getSupportActionBar().getHeight(); //Not working.

        new Thread(networkTask).start();

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

        mainSnowGrowth(snow, snowImage, handler);
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

    Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            try {
                String stringURL = "https://api.openweathermap.org/data/2.5/weather?zip=61820,us&APPID=309c73c90ffc17cbcd34e6523e2be0d3";
                URL weatherURL = new URL(stringURL);
                HttpURLConnection connection = (HttpURLConnection) weatherURL.openConnection();
                connection.setConnectTimeout(30*1000);
                connection.setRequestMethod("GET");
                Log.d("SnowTag","Connection started.");
                connection.connect();
                Log.d("SnowTag","Connected.");
                int requestCode = connection.getResponseCode();
                String requestMessage = connection.getResponseMessage();
                Log.d("SnowTag","Request state: " + requestMessage);
                if (requestCode != HttpURLConnection.HTTP_OK){
                    return;
                }
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String result = "";
                String templine;
                while ((templine = bufferedReader.readLine()) != null) {
                    result += templine;
                }
                Log.d("SnowTag", result);

                JSONObject jsonObject = new JSONObject(result);
                JSONObject mainObject = jsonObject.getJSONObject("main");
                double humidity = mainObject.getDouble("humidity");
                Log.d("SnowTag","Humidity: " + Double.toString(humidity));
                snow.initializeWaterRecoverySpeed(humidity);
                SeekBar waterSeekBar = findViewById(R.id.waterSeekBar);
                waterSeekBar.setProgress(Math.round(snow.getInitialWaterRatio() * waterSeekBar.getMax()));

                JSONObject windObject = jsonObject.getJSONObject("wind");
                double windSpeed = windObject.getDouble("speed");
                Log.d("SnowTag","Wind Speed: " + Double.toString(windSpeed));
            } catch (Exception allException) {
                Log.d("SnowTag",allException.toString());
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        snow.onTouch(event.getX(), event.getY(),
                snowImageCopy.getWidth(), snowImageCopy.getHeight(), snowImageCopy.getX(), snowImageCopy.getY() + actionbarHeight);
        return true;
    }
}
