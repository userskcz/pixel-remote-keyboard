package dev.local.pixelkeyboard.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.Gravity;
import android.widget.*;

public final class Ui {
    public static final int BG=0xff0c1110, SURFACE=0xff171e1b, RAISED=0xff202b25;
    public static final int TEXT=0xffedf4ed, MUTED=0xffa2b2a6, ACCENT=0xffc6efb1;
    public static final int INK=0xff173317, LINE=0xff34423a, ERROR=0xffffb4a6, DISABLED=0xff68766b;
    private Ui(){}
    public static int dp(Context c,float n){return Math.round(n*c.getResources().getDisplayMetrics().density);}
    public static GradientDrawable shape(Context c,int color,int radius) {
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(c,radius));return d;
    }
    public static GradientDrawable outline(Context c,int color,int radius,int stroke) {
        GradientDrawable d=shape(c,color,radius);d.setStroke(dp(c,1),stroke);return d;
    }
    public static TextView text(Context c,String value,int size,int color,boolean medium) {
        TextView v=new TextView(c);v.setText(value);v.setTextSize(size);v.setTextColor(color);
        v.setTypeface(Typeface.create(medium?"sans-serif-medium":"sans-serif",Typeface.NORMAL));
        v.setIncludeFontPadding(false);v.setLineSpacing(dp(c,3),1);return v;
    }
    public static LinearLayout column(Context c){LinearLayout v=new LinearLayout(c);v.setOrientation(LinearLayout.VERTICAL);return v;}
    public static LinearLayout row(Context c){LinearLayout v=new LinearLayout(c);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
    public static Drawable touch(Context c,int color,int radius) {
        StateListDrawable states=new StateListDrawable();
        states.addState(new int[]{-android.R.attr.state_enabled},shape(c,SURFACE,radius));
        states.addState(new int[]{},shape(c,color,radius));
        return new RippleDrawable(ColorStateList.valueOf(0x26ffffff),states,shape(c,Color.WHITE,radius));
    }
    public static Button button(Context c,String label,String glyph,boolean primary,Runnable action) {
        Button b=new Button(c);b.setText(label);b.setTextSize(15);b.setAllCaps(false);
        b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        b.setLetterSpacing(0.005f);b.setMinHeight(0);b.setMinimumHeight(0);b.setMinWidth(0);b.setMinimumWidth(0);
        b.setPadding(dp(c,16),0,dp(c,16),0);b.setGravity(Gravity.CENTER);b.setStateListAnimator(null);
        b.setBackground(touch(c,primary?ACCENT:RAISED,28));
        b.setTextColor(new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled},new int[]{}},
            new int[]{DISABLED,primary?INK:TEXT}));
        setGlyph(c,b,glyph,primary?INK:TEXT,20);
        b.setOnClickListener(v->action.run());return b;
    }
    public static void setGlyph(Context c,Button b,String glyph,int color,int size) {
        if(glyph==null){b.setCompoundDrawablesRelative(null,null,null,null);return;}
        Glyph d=new Glyph(glyph,color);int s=dp(c,size);d.setBounds(0,0,s,s);
        b.setCompoundDrawablesRelative(d,null,null,null);b.setCompoundDrawablePadding(dp(c,9));
    }
    public static ImageButton iconButton(Context c,String glyph,String description,Runnable action) {
        ImageButton b=new ImageButton(c);b.setImageDrawable(new Glyph(glyph,MUTED));
        b.setPadding(dp(c,12),dp(c,12),dp(c,12),dp(c,12));b.setScaleType(ImageView.ScaleType.FIT_CENTER);
        b.setBackground(touch(c,Color.TRANSPARENT,24));b.setContentDescription(description);
        b.setOnClickListener(v->action.run());return b;
    }
    public static ImageView icon(Context c,String name,int color) {
        ImageView v=new ImageView(c);v.setImageDrawable(new Glyph(name,color));v.setImportantForAccessibility(2);return v;
    }
    public static final class Glyph extends Drawable {
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final String name;private final int color;private int opacity=255;
        public Glyph(String name,int color){this.name=name;this.color=color;}
        @Override public void draw(Canvas original) {
            Canvas c=original;int save=c.save();Rect bounds=getBounds();
            c.translate(bounds.left,bounds.top);c.scale(bounds.width()/24f,bounds.height()/24f);
            paint.setColor(color);paint.setAlpha(isEnabled()?opacity:Math.min(opacity,90));paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.7f);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);
            switch(name){
                case "watch":
                    c.drawRoundRect(6,5,18,19,4,4,paint);line(c,9,1,15,1);line(c,9,23,15,23);
                    line(c,9,1,8,5);line(c,15,1,16,5);line(c,8,19,9,23);line(c,16,19,15,23);
                    line(c,12,9,12,12);line(c,12,12,14,13);break;
                case "phone":
                    c.drawRoundRect(6,2,18,22,3,3,paint);line(c,10,18,14,18);break;
                case "keyboard":
                    c.drawRoundRect(2,5,22,19,3,3,paint);
                    for(int x=6;x<=18;x+=4){line(c,x,9,x+.1f,9);line(c,x,12,x+.1f,12);}
                    line(c,8,16,16,16);break;
                case "arrow":line(c,4,12,20,12);line(c,14,6,20,12);line(c,20,12,14,18);break;
                case "check":line(c,5,12,10,17);line(c,10,17,20,7);break;
                case "close":line(c,7,7,17,17);line(c,17,7,7,17);break;
                case "paste":
                    c.drawRoundRect(5,5,19,22,2,2,paint);c.drawRoundRect(9,2,15,7,1,1,paint);
                    line(c,9,12,15,12);line(c,9,16,14,16);break;
                case "refresh":
                    c.drawArc(4,4,20,20,40,285,false,paint);line(c,19,3,20,9);line(c,20,9,14,8);break;
                case "more":
                    for(int y=5;y<=19;y+=7)c.drawCircle(12,y,1,paint);break;
                case "lock":
                    c.drawRoundRect(5,10,19,22,3,3,paint);c.drawArc(8,2,16,17,180,180,false,paint);
                    line(c,12,15,12,17);break;
                case "delete":
                    Path p=new Path();p.moveTo(9,5);p.lineTo(22,5);p.lineTo(22,19);p.lineTo(9,19);p.lineTo(2,12);p.close();c.drawPath(p,paint);
                    line(c,12,9,18,15);line(c,18,9,12,15);break;
                case "switch":
                    line(c,3,8,21,8);line(c,16,3,21,8);line(c,21,8,16,13);
                    line(c,21,17,3,17);line(c,8,12,3,17);line(c,3,17,8,22);break;
                case "info":
                    c.drawCircle(12,12,9,paint);line(c,12,11,12,17);line(c,12,7,12.1f,7);break;
            }
            c.restoreToCount(save);
        }
        private boolean isEnabled(){for(int s:getState())if(s==android.R.attr.state_enabled)return true;return getState().length==0;}
        private void line(Canvas c,float x,float y,float x2,float y2){c.drawLine(x,y,x2,y2,paint);}
        @Override public boolean isStateful(){return true;}
        @Override protected boolean onStateChange(int[] state){invalidateSelf();return true;}
        @Override public void setAlpha(int alpha){opacity=alpha;invalidateSelf();}
        @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
}
