package es.uji.ei1057.ledparty;

/**
 * Created by oscar on 27/01/14.
 */
public class MeanAudioTransform implements AudioTransform {

    static double max_amplitude_square = 0;

    @Override
    public double[] transform(short[] nums) {
        double ms = 0;
        for (short num : nums) {
            ms += num * num;
        }
        ms /= nums.length;
        if (ms > max_amplitude_square * max_amplitude_square)
            max_amplitude_square = Math.sqrt(ms);
        return new double[]{Math.sqrt(ms) / max_amplitude_square};
    }
}
