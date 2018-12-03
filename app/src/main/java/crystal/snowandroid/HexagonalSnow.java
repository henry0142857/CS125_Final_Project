 package crystal.snowandroid;

 public class HexagonalSnow extends Snow {
    protected final float SQRT3 = 1.732050808f;
    protected final int PIXEL_GRID_SIZE_X = 1000;
    protected final int PIXEL_GRID_SIZE_Y = (int) (PIXEL_GRID_SIZE_X / SQRT3);
    protected final float PIXEL_TO_FIGURE_RATIO = 3.0f;

    private final byte SIX = 6;
    private final int[][] HEXAGONAL_NEIGHBOR = { {-1, 1}, {-1, -1}, {1, -1}, {1, 1}, {0, 2}, {0, -2} };
    protected final short WATER_CONTENT_MAX = 108; //Multiple of 6, <32767.
    protected final short WATER_LOSS_RATIO = 4;

    public HexagonalSnow() {
        snowPixel = new short[PIXEL_GRID_SIZE_X][PIXEL_GRID_SIZE_Y];
        for (int i = 0; i < PIXEL_GRID_SIZE_X; i++) {
            for (int j = 0; j < PIXEL_GRID_SIZE_Y; j++) {
                if ((i + j) % 2 == 0) {
                    snowPixel[i][j] = WATER_CONTENT_MAX;
                } else {
                    snowPixel[i][j] = 0;
                }
            }
        }
        time = 0;
        initializeWaterRecoverySpeed(MAX_HUMIDITY);
        waterAverageTime = 0;
        if (waterAverageTime > MAX_AVERAGE_TIMES) {
            waterAverageTime = MAX_AVERAGE_TIMES;
        }
        checkHexagonalGrid();
    }

    @Override
    public void initializeWaterRecoverySpeed(double humidity) {
        if (humidity < 0) {
            return;
        }
        waterRecoverySpeed = (int) Math.round(humidity / MAX_HUMIDITY * WATER_CONTENT_MAX / 2);
        if (waterRecoverySpeed > WATER_CONTENT_MAX / 2) {
            waterRecoverySpeed = WATER_CONTENT_MAX / 2;
        }
    }

    private void checkHexagonalGrid() {
        if (snowPixel == null) {
            throw new NullPointerException();
        }
        for (int i = 0; i < PIXEL_GRID_SIZE_X; i++) {
            for (int j = 0; j < PIXEL_GRID_SIZE_Y; j++) {
                if ((i + j) % 2 != 0 && snowPixel[i][j] != 0) {
                    throw new Error();
                }
            }
        }
    }

    @Override
    public void expendSnow() {
        short[][] tempSnowPixel = arrayDeepCopy(snowPixel);
        for (int i = 0; i < PIXEL_GRID_SIZE_X; i++) {
            for (int j = 0; j < PIXEL_GRID_SIZE_Y; j++) {
                if ((i + j) % 2 == 0 && snowPixel[i][j] < 0) {
                    expendSnowHexagonal(i, j, tempSnowPixel);
                }
            }
        }
        snowPixel = tempSnowPixel;
    }

    private void expendSnowHexagonal(int ix, int iy, short[][] newSnowPixel) {
        if (ix <= 0 || ix >= PIXEL_GRID_SIZE_X - 1 || iy <= 1 || iy >= PIXEL_GRID_SIZE_Y - 2) {
            return;
        }
        if (snowPixel[ix][iy] >= 0) {
            return;
        }
        for (int i = 0; i < SIX; i++) {
            int ixNeighbor = ix + HEXAGONAL_NEIGHBOR[i][0], iyNeighbor = iy + HEXAGONAL_NEIGHBOR[i][1];
            try {
                if (pixelNeighborWaterContent(ixNeighbor, iyNeighbor) >= WATER_CONTENT_MAX * WATER_LOSS_RATIO) {
                    newSnowPixel[ixNeighbor][iyNeighbor] = -1;
                    pixelAbsorbWater(ixNeighbor, iyNeighbor, pixelNeighborWaterContent(ixNeighbor, iyNeighbor), newSnowPixel);
                }
            } catch (ArrayIndexOutOfBoundsException outOfBound) {}
        }
    }

    private short[] pixelFindNeighbor(int ix, int iy) {
        short[] neighborList = new short[SIX];
        for (int i = 0; i < SIX; i++) {
            try {
                neighborList[i] = snowPixel[ix + HEXAGONAL_NEIGHBOR[i][0]][iy + HEXAGONAL_NEIGHBOR[i][1]];
            } catch (ArrayIndexOutOfBoundsException outOfBound) {
                neighborList[i] = 0;
            }
        }
        return neighborList;
    }

    private short pixelNeighborWaterContent(int ix, int iy) {
        short sum = 0;
        boolean ifAllIce = true;
        for (short neighbor : pixelFindNeighbor(ix, iy)) {
            if (neighbor >= 0) {
                sum += neighbor;
                ifAllIce = false;
            }
        }
        if (ifAllIce) {
            return -1;
        }
        return sum;
    }

    private void pixelAbsorbWater(int ix, int iy, int totalWater, short[][] newSnowPixel) {
        byte numberOfNeighborNotIce = 0;
        for (int i = 0; i < SIX; i++) {
            try {
                if (snowPixel[ix + HEXAGONAL_NEIGHBOR[i][0]][iy + HEXAGONAL_NEIGHBOR[i][1]] >= 0) {
                    numberOfNeighborNotIce++;
                }
            } catch (ArrayIndexOutOfBoundsException outOfBound) {}
        }
        if (numberOfNeighborNotIce < 1) {
            throw new Error();
        }
        for (int i = 0; i < SIX; i++) {
            try {
                if (newSnowPixel[ix + HEXAGONAL_NEIGHBOR[i][0]][iy + HEXAGONAL_NEIGHBOR[i][1]] >= 0) {
                    newSnowPixel[ix + HEXAGONAL_NEIGHBOR[i][0]][iy + HEXAGONAL_NEIGHBOR[i][1]] -= totalWater / numberOfNeighborNotIce;
                    if (newSnowPixel[ix + HEXAGONAL_NEIGHBOR[i][0]][iy + HEXAGONAL_NEIGHBOR[i][1]] < 0) {
                        newSnowPixel[ix + HEXAGONAL_NEIGHBOR[i][0]][iy + HEXAGONAL_NEIGHBOR[i][1]] = 0;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException outOfBound) {}
        }
    }

    @Override
    public void waterRecovery() {
        for (int i = 0; i < PIXEL_GRID_SIZE_X; i++) {
            for (int j = 0; j < PIXEL_GRID_SIZE_Y; j++) {
                if ((i + j) % 2 == 0 && snowPixel[i][j] >= 0 && snowPixel[i][j] < WATER_CONTENT_MAX) {
                    snowPixel[i][j] += waterRecoverySpeed;
                    if (snowPixel[i][j] > WATER_CONTENT_MAX) {
                        snowPixel[i][j] = WATER_CONTENT_MAX;
                    }
                }
            }
        }
    }

    @Override
    public void waterAverage() {
        short[][] tempSnowPixel = arrayDeepCopy(snowPixel);
        for (int i = 0; i < PIXEL_GRID_SIZE_X; i++) {
            for (int j = 0; j < PIXEL_GRID_SIZE_Y; j++) {
                if ((i + j) % 2 == 0 && snowPixel[i][j] >= 0) {
                    waterAverageHexagonal(i, j, tempSnowPixel);
                }
            }
        }
        snowPixel = tempSnowPixel;
    }

    private void waterAverageHexagonal(int ix, int iy, short[][] newSnowPixel) {
        if (ix <= 0 || ix >= PIXEL_GRID_SIZE_X - 1 || iy <= 1 || iy >= PIXEL_GRID_SIZE_Y - 2) {
            return;
        }
        if (snowPixel[ix][iy] < 0) {
            return;
        }
        short waterNeighborCount = 1;
        short neighborWaterTotal = snowPixel[ix][iy];
        for (int i = 0; i < SIX; i++) {
            int ixNeighbor = ix + HEXAGONAL_NEIGHBOR[i][0], iyNeighbor = iy + HEXAGONAL_NEIGHBOR[i][1];
            try {
                if (snowPixel[ixNeighbor][iyNeighbor] >= 0) {
                    waterNeighborCount++;
                    neighborWaterTotal += snowPixel[ixNeighbor][iyNeighbor];
                }
            } catch (ArrayIndexOutOfBoundsException outOfBound) {}
        }
        newSnowPixel[ix][iy] = (short) (neighborWaterTotal / waterNeighborCount);
    }

    @Override
    public int[] project(final int width, final int height) {
        final float widthConvertRatio = 1.0f / PIXEL_TO_FIGURE_RATIO / SQRT3 / width * PIXEL_GRID_SIZE_X;
        final float heightConvertRatio = 1.0f / PIXEL_TO_FIGURE_RATIO / height * PIXEL_GRID_SIZE_Y;

        int[] snowFigureColor = new int[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) { //Flip x,y
                int i_projected = Math.round((j - width / 2) * widthConvertRatio + PIXEL_GRID_SIZE_X / 2);
                int j_projected = Math.round((i - height / 2) * heightConvertRatio + PIXEL_GRID_SIZE_Y / 2);

                if ((i_projected + j_projected) % 2 != 0) {
                    snowFigureColor[j * height + i] = backgroundColor;
                    continue;
                }
                if (i_projected < 0 || i_projected >= PIXEL_GRID_SIZE_X
                        || j_projected < 0 || j_projected >= PIXEL_GRID_SIZE_Y) {
                    snowFigureColor[j * height + i] = errorColor; //Projection out of bound.
                    continue;
                }
                if (this.snowPixel[i_projected][j_projected] < 0) {
                    snowFigureColor[j * height + i] = iceColor;
                    continue;
                }
                snowFigureColor[j * height + i] = backgroundColor;
            }
        }
        return snowFigureColor;
    }

    @Override
    public void generateSnow(float fractionX, float fractionY) {
        int ix = Math.round(fractionX * PIXEL_GRID_SIZE_X / PIXEL_TO_FIGURE_RATIO / SQRT3 + PIXEL_GRID_SIZE_X / 2);
        int iy = Math.round(fractionY * PIXEL_GRID_SIZE_Y / PIXEL_TO_FIGURE_RATIO + PIXEL_GRID_SIZE_Y / 2);
        if ((ix + iy) % 2 != 0) {
            ix++;
        }
        if (ix < 0 || ix >= PIXEL_GRID_SIZE_X || iy < 0 || iy >= PIXEL_GRID_SIZE_Y) {
            snowPixel[PIXEL_GRID_SIZE_X / 2][PIXEL_GRID_SIZE_Y / 2 - 40] = -1;
            return;
        }
        this.snowPixel[ix][iy] = -1;
    }

    @Override
    public void waterSeekBar(float waterSeekBarRatio) {
        this.waterRecoverySpeed = Math.round(waterSeekBarRatio * WATER_CONTENT_MAX / 2);
        if (this.waterRecoverySpeed > WATER_CONTENT_MAX / 2) {
            this.waterRecoverySpeed = WATER_CONTENT_MAX / 2;
        }
    }

    @Override
    public float getInitialWaterRatio() {
        float toReturn = 1.0f * this.waterRecoverySpeed / WATER_CONTENT_MAX * 2;
        if (toReturn > 1) {
            return 1.0f;
        }
        return toReturn;
    }

    @Override
    public void clear() {
        for (int i = 0; i < PIXEL_GRID_SIZE_X; i++) {
            for (int j = 0; j < PIXEL_GRID_SIZE_Y; j++) {
                if ((i + j) % 2 == 0) {
                    snowPixel[i][j] = WATER_CONTENT_MAX;
                }
            }
        }
    }

    /* For test in Java only, plot in a new window.
    public static void main(String[] unused) {
        Snow snow = new HexagonalSnow();
        //Graphic part.
        JFrame jFrame = new JFrame();
        for (int t = 0; t < 150; t++) {
            snow.timePass();
            JPanel jpanel = new JPanel() {
                @Override
                public void paint(Graphics graphics) {
                    super.paint(graphics);
                    for (int i = 0; i < PIXEL_GRID_SIZE_X; i++) {
                        for (int j = 0; j < PIXEL_GRID_SIZE_Y; j++) {
                            if (snow.getSnowPixel()[i][j] < 0) {
                                graphics.drawOval((int) (i * SQRT3), j, 2, 2);
                            }
                        }
                    }
                }
            };
            jFrame.add(jpanel);
            jFrame.setSize((int) (PIXEL_GRID_SIZE_X * SQRT3), PIXEL_GRID_SIZE_Y);
            jFrame.setVisible(true);
            try {
                Thread.sleep(200);
            } catch (Exception e) {}
        }
    }
    */
}