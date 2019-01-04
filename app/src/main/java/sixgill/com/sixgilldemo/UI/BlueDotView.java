package sixgill.com.sixgilldemo.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import sixgill.com.sixgilldemo.R;

public class BlueDotView extends SubsamplingScaleImageView {

    private float radius = 1.0f;
    private PointF dotCenter = null;

    Paint paint = new Paint();

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setDotCenter(PointF dotCenter) {
        this.dotCenter = dotCenter;
    }

    public BlueDotView(Context context) {
        this(context, null);
    }

    public BlueDotView(Context context, AttributeSet attr) {
        super(context, attr);
        initialise();
    }

    private void initialise() {
        setWillNotDraw(false);
        setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isReady()) {
            return;
        }

        if (dotCenter != null) {
            PointF vPoint = sourceToViewCoord(dotCenter);
            float scaledRadius = getScale() * radius;
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getResources().getColor(R.color.colorPrimary));
            if(vPoint != null) {
                canvas.drawCircle(vPoint.x, vPoint.y, scaledRadius, paint);
            }
        }
    }
}
