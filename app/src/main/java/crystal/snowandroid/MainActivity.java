package crystal.snowandroid;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.Bitmap;

public class MainActivity extends AppCompatActivity {

    private final static int TIMEDELAY = 0; //millisecond

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView snowImage = findViewById(R.id.snowImage);
        final Button snowGrowButton = findViewById(R.id.snowGrowButton);
        final Snow snow = new HexagonalSnow();
        final Handler handler = new Handler();

        snowGrowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mainSnowGrowth(snow, snowImage, handler);
            }
        });
    }

    private void mainSnowGrowth(final Snow snow, final ImageView snowImage, final Handler handler) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                snow.timePass();
                Bitmap snowBitmap = snow.toBitmap();
                snowImage.setImageBitmap(snowBitmap);
                handler.postDelayed(this, TIMEDELAY);
            }
        };
        handler.postDelayed(runnable, TIMEDELAY);
    }
}
