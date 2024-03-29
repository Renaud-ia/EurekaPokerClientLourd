package analyzor.modele.poker.evaluation;

import java.util.Arrays;

public class Percentiles {
    public static float[] calculerPercentiles(float[] valeurs, int nPercentiles) {
        Arrays.sort(valeurs);
        float[] percentiles = new float[nPercentiles - 1];
        float stepPercentile = (float) 100 / nPercentiles;

        int indexPercentile = 0;
        for(int percentile = (int) stepPercentile; percentile < 100; percentile += stepPercentile) {
            percentiles[indexPercentile++] = getPercentile(valeurs, percentile);
        }

        return percentiles;
    }

    private static float getPercentile(float[] data, float percentile) {
        float index = (percentile / 100) * (data.length - 1);


        int intPart = (int) index;
        float fracPart = index - intPart;

        if (fracPart == 0 && intPart < data.length - 1) {

            return (data[intPart] + data[intPart + 1]) / 2;
        } else {

            return data[(int) Math.round(index)];
        }
    }
}
