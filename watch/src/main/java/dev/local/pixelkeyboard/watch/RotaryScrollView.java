package dev.local.pixelkeyboard.watch;

import android.content.Context;
import android.graphics.*;
import android.os.Build;
import android.view.*;
import android.widget.ScrollView;
import dev.local.pixelkeyboard.ui.Ui;

/** Crown input is intercepted before focused descendants, without stealing editor focus. */
public final class RotaryScrollView extends ScrollView {
    private float remainder;
    private Api35 feedback;
    private final Paint indicator=new Paint(Paint.ANTI_ALIAS_FLAG);
    public RotaryScrollView(Context context){
        super(context);
        setFillViewport(true);
        setFocusable(true);setFocusableInTouchMode(true);
        setVerticalScrollBarEnabled(false);
        setOverScrollMode(OVER_SCROLL_IF_CONTENT_SCROLLS);
    }
    public boolean handleRotary(MotionEvent event){
        if(event.getAction()!=MotionEvent.ACTION_SCROLL || !event.isFromSource(InputDevice.SOURCE_ROTARY_ENCODER))return false;
        float axis=event.getAxisValue(MotionEvent.AXIS_SCROLL);
        if(!Float.isFinite(axis))return true;
        float value=-axis*ViewConfiguration.get(getContext()).getScaledVerticalScrollFactor()+remainder;
        int maximum=Math.max(0,computeVerticalScrollRange()-computeVerticalScrollExtent());
        value=Math.max(-maximum,Math.min(maximum,value));
        int delta=(int)value;remainder=value-delta;
        int before=getScrollY();
        scrollBy(0,delta);
        int actual=getScrollY()-before;
        if(actual!=delta)remainder=0;
        if(Build.VERSION.SDK_INT>=35){
            if(feedback==null)feedback=new Api35(this);
            feedback.onEvent(event,actual,delta);
        }
        postInvalidateOnAnimation();
        return true;
    }
    @Override public boolean dispatchGenericMotionEvent(MotionEvent event){
        return handleRotary(event)||super.dispatchGenericMotionEvent(event);
    }
    @Override protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        post(()->{if(findFocus()==null)requestFocus();});
    }
    @Override public void onDrawForeground(Canvas c){
        super.onDrawForeground(c);
        int range=computeVerticalScrollRange(),extent=computeVerticalScrollExtent();
        if(range<=extent)return;
        float inset=Ui.dp(getContext(),3);
        RectF arc=new RectF(inset,inset,getWidth()-inset,getHeight()-inset);
        indicator.setStyle(Paint.Style.STROKE);indicator.setStrokeWidth(Ui.dp(getContext(),2));
        indicator.setStrokeCap(Paint.Cap.ROUND);indicator.setColor(Ui.LINE);
        c.drawArc(arc,-36,72,false,indicator);
        float length=Math.max(12,72f*extent/range);
        float offset=(72-length)*Math.min(1f,Math.max(0f,(float)getScrollY()/(range-extent)));
        indicator.setColor(Ui.ACCENT);c.drawArc(arc,-36+offset,length,false,indicator);
    }
    @android.annotation.TargetApi(35)
    private static final class Api35 {
        private final android.view.ScrollFeedbackProvider provider;
        Api35(RotaryScrollView view){provider=android.view.ScrollFeedbackProvider.createProvider(view);}
        void onEvent(MotionEvent event,int actual,int requested){
            if(actual!=0)provider.onScrollProgress(event.getDeviceId(),event.getSource(),MotionEvent.AXIS_SCROLL,actual);
            else if(requested!=0)provider.onScrollLimit(event.getDeviceId(),event.getSource(),MotionEvent.AXIS_SCROLL,requested<0);
        }
    }
}
