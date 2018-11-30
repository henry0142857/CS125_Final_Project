package crystal.snowandroid;

public class HexagonalSnow extends Snow {
    protected final float SQRT3 = 1.732050808f;
    protected final int PIXEL_GRID_SIZE_X = 800;
    protected final int PIXEL_GRID_SIZE_Y = (int) (PIXEL_GRID_SIZE_X / SQRT3);
    protected final float PIXEL_TO_FIGURE_RATIO = 4.0f;

    private final byte SIX = 6;
    private final int[][] HEXAGONAL_NEIGHBOR = { {-1, 1}, {-1, -1}, {1, -1}, {1, 1}, {0, 2}, {0, -2} };
    protected final short WATER_CONTENT_MAX = 108; //Multiple of 6, <32767.
    protected final short WATER_LOSS_RATIO = 3;

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
        snowPixel[PIXEL_GRID_SIZE_X / 2][PIXEL_GRID_SIZE_Y / 2 - 20] = -1;
        snowPixel[PIXEL_GRID_SIZE_X / 2 + 10][PIXEL_GRID_SIZE_Y / 2 - 20] = -1;
        time = 0;
        waterRecoverySpeed = 70;
        if (waterRecoverySpeed > WATER_CONTENT_MAX / 2) {
            waterRecoverySpeed = WATER_CONTENT_MAX / 2;
        }
        checkHexagonalGrid();
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
    public void timePass() {
        expendSnow();
        waterRecovery();
        time++;
    }

    @Override
    public void timePass(int time) {
        for (int i = 0; i < time; i++) {
            timePass();
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
    public int[] project(final int width, final int height) {
        final float widthConvertRatio = 1.0f  / PIXEL_TO_FIGURE_RATIO / SQRT3 / width * PIXEL_GRID_SIZE_X;
        final float heightConvertRatio = 1.0f  / PIXEL_TO_FIGURE_RATIO / height * PIXEL_GRID_SIZE_Y;

        int[] snowFigureColor = new int[width * height];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) { //Flip x - y.
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
}