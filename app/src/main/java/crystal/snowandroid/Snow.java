package crystal.snowandroid;
import android.graphics.Bitmap;

public abstract class Snow {
    private final static int FIGURE_GRID_SIZE_X = 900;
    private final static int FIGURE_GRID_SIZE_Y = 1200;
    protected final double MAX_HUMIDITY = 100.0; //%
    protected final int MAX_AVERAGE_TIMES = 5;
    protected final double MAX_WIND_SPEED = 20; //m/s, https://en.wikipedia.org/wiki/Beaufort_scale
    //(A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff)
    final int iceColor = (0xff & 0xff) << 24 | (0xff & 0xff) << 16 | (0xff & 0xff) << 8 | (0xff & 0xff); //White
    final int backgroundColor = (0x00 & 0xff) << 24 | (0 & 0xff) << 16 | (0 & 0xff) << 8 | (0xff & 0xff); //Blue
    final int errorColor = (0xff & 0xff) << 24 | (0xff & 0xff) << 16 | (0 & 0xff) << 8 | (0 & 0xff); //Red

    protected short[][] snowPixel;
    protected int time;
    protected int waterRecoverySpeed;
    protected int waterAverageTime;

    public Snow() {
        snowPixel = new short[0][0];
        time = 0;
        waterRecoverySpeed = 0;
        waterAverageTime = 0;
    }

    public abstract void initializeWaterRecoverySpeed(double humidity);

    public void initializeWaterAverageTimes(double windSpeed) {
        if (windSpeed < 0) {
            return;
        }
        this.waterAverageTime = (int) Math.round(windSpeed / MAX_WIND_SPEED * MAX_AVERAGE_TIMES);
        if (waterAverageTime > MAX_AVERAGE_TIMES) {
            waterAverageTime = MAX_AVERAGE_TIMES;
        }
    }

    public short[][] getSnowPixel() {
        if (snowPixel == null) {
            throw new NullPointerException();
        }
        return snowPixel;
    }

    public abstract void expendSnow();

    public abstract void waterRecovery();

    public void timePass() {
        expendSnow();
        waterRecovery();
        waterAverage(waterAverageTime);
        time++;
    }

    public void timePass(int time) {
        for (int i = 0; i < time; i++) {
            timePass();
        }
    }

    public void waterAverage(int times) {
        for (int i = 0; i < times; i++) {
            waterAverage();
        }
    }

    public abstract void waterAverage();

    public abstract int[] project(int width, int height);

    public Bitmap toBitmap() {
        int[] snowFigureColor = this.project(FIGURE_GRID_SIZE_Y, FIGURE_GRID_SIZE_X); //Flip x,y
        return Bitmap.createBitmap(snowFigureColor, FIGURE_GRID_SIZE_X,
                FIGURE_GRID_SIZE_Y, Bitmap.Config.ARGB_8888);
    }

    public abstract void generateSnow(float fractionX, float fractionY);

    public void onTouch(float x, float y, float ImageWidth, float ImageHeight, float ImageX, float ImageY) {
        float fractionX = (x - ImageX - ImageWidth / 2) / ImageWidth;
        float fractionY = (y - ImageY - ImageHeight / 2) / ImageHeight;
        generateSnow(fractionY, fractionX); //Flip x,y
    }

    public abstract void waterSeekBar(float waterSeekBarRatio);

    public abstract float getInitialWaterRatio();

    public abstract void clear();

    public int getMaxAverageTimes() {
        return MAX_AVERAGE_TIMES;
    }

    public int getInitialAverageTimes() {
        return this.waterAverageTime;
    }

    public void setAverageTimes(int setAverage) {
        this.waterAverageTime = setAverage;
        if (this.waterAverageTime > MAX_AVERAGE_TIMES) {
            this.waterAverageTime = MAX_AVERAGE_TIMES;
        }
    }

    protected static short[][] arrayDeepCopy(short[][] array) {
        if (array == null || array[0] == null) {
            throw new NullPointerException();
        }
        short[][] copy = new short[array.length][array[0].length];
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                copy[i][j] = array[i][j];
            }
        }
        return copy;
    }
}