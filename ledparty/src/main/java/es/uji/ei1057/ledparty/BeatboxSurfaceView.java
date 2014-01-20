package es.uji.ei1057.ledparty;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceView;

/**
 * Created by oscar on 17/01/14.
 */
public class BeatboxSurfaceView extends SurfaceView {

    private float level;

    public BeatboxSurfaceView(Context context) {
        super(context);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //int c = Math.round();
        //canvas.drawRGB(level, level, level);
    }

    public void setLevel(float l) {
        this.level = l;
    }
}
