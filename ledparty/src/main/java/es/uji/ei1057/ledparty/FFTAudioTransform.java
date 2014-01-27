package es.uji.ei1057.ledparty;

/**
 * Created by oscar on 27/01/14.
 * http://blog.datasingularity.com/?p=53
 */
public class FFTAudioTransform implements AudioTransform {

    @Override
    public double[] transform(short[] data) {
        // Defaults
        int nn = data.length / 2;
        int isign = 1;

        double[] transformed = new double[data.length];
        for (int j = 0; j < data.length; j++) {
            transformed[j] = (double) data[j];
        }
        int i, j, n, mmax, m, istep;
        double wtemp, wr, wpr, wpi, wi, theta, tempr, tempi;

        n = nn << 1;
        j = 1;
        for (i = 1; i < n; i += 2) {
            if (j > i) {
                double temp;
                temp = transformed[j];
                transformed[j] = transformed[i];
                transformed[i] = temp;
                temp = transformed[j + 1];
                transformed[j + 1] = transformed[i + 1];
                transformed[i + 1] = temp;
            }
            m = n >> 1;
            while (m >= 2 && j > m) {
                j -= m;
                m >>= 1;
            }
            j += m;
        }
        mmax = 2;
        while (n > mmax) {
            istep = (mmax << 1);
            theta = isign * (6.28318530717959 / mmax);
            wtemp = Math.sin(0.5 * theta);
            wpr = -2.0 * wtemp * wtemp;
            wpi = Math.sin(theta);
            wr = 1.0;
            wi = 0.0;
            for (m = 1; m < mmax; m += 2) {
                for (i = m; i <= n; i += istep) {
                    j = i + mmax - 1;
                    tempr = wr * transformed[j] - wi * transformed[j + 1];
                    tempi = wr * transformed[j + 1] + wi * transformed[j];
                    transformed[j] = transformed[i] - tempr;
                    transformed[j + 1] = transformed[i + 1] - tempi;
                    transformed[i] += tempr;
                    transformed[i + 1] += tempi;
                }
                wr = (wtemp = wr) * wpr - wi * wpi + wr;
                wi = wi * wpr + wtemp * wpi + wi;
            }
            mmax = istep;
        }
        return transformed;
    }
}
