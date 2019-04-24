package com.tamaq.courier.utils;

import android.graphics.Canvas;
import android.graphics.Color;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class ColoredLabelXAxisRenderer extends XAxisRenderer {

    private String mLastDateForPayment;

    public ColoredLabelXAxisRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans) {
        super(viewPortHandler, xAxis, trans);
    }

    public ColoredLabelXAxisRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer transformer, String lastDateForPayment) {
        super(viewPortHandler, xAxis, transformer);
        mLastDateForPayment = lastDateForPayment;
    }

    @Override
    protected void drawLabels(Canvas c, float pos, MPPointF anchor) {
        final float labelRotationAngleDegrees = mXAxis.getLabelRotationAngle();
        boolean centeringEnabled = mXAxis.isCenterAxisLabelsEnabled();

        float[] positions = new float[mXAxis.mEntryCount * 2];

        for (int i = 0; i < positions.length; i += 2) {
            positions[i] = centeringEnabled ? mXAxis.mCenteredEntries[i / 2] : mXAxis.mEntries[i / 2];
        }

        mTrans.pointValuesToPixel(positions);

        for (int i = 0; i < positions.length; i += 2) {
            float x = positions[i];

            if (mViewPortHandler.isInBoundsX(x)) {

                String label = mXAxis.getValueFormatter().getFormattedValue(mXAxis.mEntries[i / 2], mXAxis);
                int color = label.equals(mLastDateForPayment) ? Color.parseColor("#4aaee2") : mXAxis.getTextColor(); //added

                mAxisLabelPaint.setColor(color);

                if (mXAxis.isAvoidFirstLastClippingEnabled()) {

                    // avoid clipping of the last
                    if (i == mXAxis.mEntryCount - 1 && mXAxis.mEntryCount > 1) {
                        float width = Utils.calcTextWidth(mAxisLabelPaint, label);

                        if (width > mViewPortHandler.offsetRight() * 2
                                && x + width > mViewPortHandler.getChartWidth())
                            x -= width / 2;

                        // avoid clipping of the first
                    } else if (i == 0) {

                        float width = Utils.calcTextWidth(mAxisLabelPaint, label);
                        x += width / 2;
                    }
                }

                drawLabel(c, label, x, pos, anchor, labelRotationAngleDegrees);
            }
        }
    }
}
