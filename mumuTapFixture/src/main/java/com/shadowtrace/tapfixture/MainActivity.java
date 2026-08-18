package com.shadowtrace.tapfixture;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(new TapView());
    }

    private final class TapView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final String[] rows = {"QWERTYU", "ASDFGHJ", "ZXCVBNM"};
        private int events = 0;

        TapView() { super(MainActivity.this); paint.setTextAlign(Paint.Align.CENTER); }

        @Override protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.rgb(17, 24, 32));
            paint.setTextSize(34f);
            paint.setColor(Color.WHITE);
            canvas.drawText("安全触摸测试靶 · 事件 " + events, getWidth() / 2f, 55f, paint);
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 7; col++) {
                    float x = (0.11f + col * 0.78f / 6f) * getWidth();
                    float y = (0.25f + row * 0.25f) * getHeight();
                    paint.setColor(Color.rgb(121, 215, 255));
                    canvas.drawCircle(x, y, 34f, paint);
                    paint.setColor(Color.BLACK);
                    paint.setTextSize(24f);
                    canvas.drawText(String.valueOf(rows[row].charAt(col)), x, y + 8f, paint);
                }
            }
        }

        @Override public boolean onTouchEvent(MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN ||
                event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                events++;
                Log.i("PocketMusicTapFixture", "DOWN pointers=" + event.getPointerCount() +
                    " x=" + event.getX(event.getActionIndex()) + " y=" + event.getY(event.getActionIndex()));
                invalidate();
            }
            return true;
        }
    }
}
