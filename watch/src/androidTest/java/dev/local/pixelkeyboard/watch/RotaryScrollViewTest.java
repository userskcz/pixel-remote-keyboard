package dev.local.pixelkeyboard.watch;

import android.content.Context;
import android.os.SystemClock;
import android.view.*;
import android.widget.*;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class RotaryScrollViewTest {
    @Test public void crownScrollsBothWaysAndKeepsEditorFocus(){
        InstrumentationRegistry.getInstrumentation().runOnMainSync(()->{
            Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
            RotaryScrollView scroll=new RotaryScrollView(c);
            LinearLayout content=new LinearLayout(c);content.setOrientation(LinearLayout.VERTICAL);
            EditText editor=new EditText(c);content.addView(editor,new LinearLayout.LayoutParams(300,120));
            View spacer=new View(c);content.addView(spacer,new LinearLayout.LayoutParams(300,3000));
            scroll.addView(content);
            scroll.measure(View.MeasureSpec.makeMeasureSpec(400,View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(400,View.MeasureSpec.EXACTLY));
            scroll.layout(0,0,400,400);editor.requestFocus();assertTrue(editor.hasFocus());
            MotionEvent down=event(-3,InputDevice.SOURCE_ROTARY_ENCODER);
            assertTrue(scroll.dispatchGenericMotionEvent(down));down.recycle();
            int lower=scroll.getScrollY();assertTrue(lower>0);assertTrue(editor.hasFocus());
            MotionEvent up=event(1,InputDevice.SOURCE_ROTARY_ENCODER);
            assertTrue(scroll.dispatchGenericMotionEvent(up));up.recycle();
            assertTrue(scroll.getScrollY()<lower);assertTrue(editor.hasFocus());
            scroll.scrollTo(0,0);
            for(int i=0;i<100;i++){MotionEvent tiny=event(-.01f,InputDevice.SOURCE_ROTARY_ENCODER);scroll.handleRotary(tiny);tiny.recycle();}
            assertTrue(scroll.getScrollY()>0);
            MotionEvent top=event(100000,InputDevice.SOURCE_ROTARY_ENCODER);scroll.handleRotary(top);top.recycle();
            assertEquals(0,scroll.getScrollY());
            MotionEvent mouse=event(-1,InputDevice.SOURCE_MOUSE);
            assertFalse(scroll.handleRotary(mouse));mouse.recycle();
        });
    }
    private MotionEvent event(float value,int source){
        MotionEvent.PointerProperties property=new MotionEvent.PointerProperties();property.id=0;
        MotionEvent.PointerCoords coords=new MotionEvent.PointerCoords();coords.setAxisValue(MotionEvent.AXIS_SCROLL,value);
        long time=SystemClock.uptimeMillis();
        return MotionEvent.obtain(time,time,MotionEvent.ACTION_SCROLL,1,new MotionEvent.PointerProperties[]{property},
            new MotionEvent.PointerCoords[]{coords},0,0,1,1,0,0,source,0);
    }
}
