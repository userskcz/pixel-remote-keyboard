package dev.local.pixelkeyboard.watch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.inputmethod.*;
import android.widget.*;
import dev.local.pixelkeyboard.ui.Ui;

public final class SetupActivity extends Activity {
    private Button enable,select;
    private TextView heading,description,testLabel,help;
    private LinearLayout root;
    private EditText test;
    private RotaryScrollView scroll;
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        root=Ui.column(this);root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(26),dp(34),dp(26),dp(36));root.setBackgroundColor(0xff000000);
        ImageView mark=Ui.icon(this,"keyboard",Ui.ACCENT);root.addView(mark,lp(28,28));
        heading=Ui.text(this,getString(R.string.set_up_your_keyboard),20,Ui.TEXT,true);heading.setGravity(Gravity.CENTER);
        root.addView(heading,space(-1,-2,12));
        description=Ui.text(this,getString(R.string.two_steps_to_start_typing_from_your_phone),12,Ui.MUTED,false);description.setGravity(Gravity.CENTER);
        root.addView(description,space(-1,-2,8));
        enable=Ui.button(this,getString(R.string.text_1_enable),"keyboard",true,()->startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
        enable.setTextSize(12);root.addView(enable,space(-1,44,18));
        select=Ui.button(this,getString(R.string.text_2_select),"check",false,()->((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker());
        select.setTextSize(12);root.addView(select,space(-1,44,8));
        testLabel=Ui.text(this,getString(R.string.try_it_out),10,Ui.MUTED,true);testLabel.setLetterSpacing(.12f);testLabel.setGravity(Gravity.CENTER);
        root.addView(testLabel,space(-1,-2,22));
        test=new EditText(this);test.setTextColor(Ui.TEXT);test.setHintTextColor(Ui.MUTED);
        test.setTextSize(14);test.setSingleLine(false);test.setMinLines(2);test.setMaxLines(4);test.setGravity(Gravity.CENTER);
        test.setPadding(dp(14),dp(12),dp(14),dp(12));test.setBackground(Ui.outline(this,Ui.SURFACE,20,Ui.LINE));
        test.setHint(getString(R.string.tap_here));test.setSaveEnabled(false);test.setImportantForAutofill(android.view.View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        root.addView(test,space(-1,-2,8));
        help=Ui.text(this,getString(R.string.open_pixel_remote_on_your_phone_and_type),11,Ui.MUTED,false);help.setGravity(Gravity.CENTER);
        root.addView(help,space(-1,-2,14));
        Button licenses=Ui.button(this,getString(R.string.open_source_licenses),"info",false,
            ()->startActivity(new Intent(this,dev.local.pixelkeyboard.watch.WatchLicensesActivity.class)));
        licenses.setTextSize(11);root.addView(licenses,space(-1,44,18));
        scroll=new RotaryScrollView(this);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
        scroll.setFocusable(true);scroll.addView(root);setContentView(scroll);
    }
    @Override protected void onResume(){
        super.onResume();
        InputMethodManager manager=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        boolean enabled=false;
        for(InputMethodInfo info:manager.getEnabledInputMethodList())if(info.getPackageName().equals(getPackageName()))enabled=true;
        String selected=Settings.Secure.getString(getContentResolver(),Settings.Secure.DEFAULT_INPUT_METHOD);
        boolean current=selected!=null&&selected.startsWith(getPackageName()+"/");
        enable.setEnabled(!enabled);enable.setText(enabled?getString(R.string.enabled):getString(R.string.text_1_enable));
        Ui.setGlyph(this,enable,enabled?"check":"keyboard",enabled?Ui.ACCENT:Ui.INK,18);
        select.setEnabled(enabled&&!current);select.setText(current?getString(R.string.selected):getString(R.string.text_2_select));
        if(current){
            heading.setText(getString(R.string.youre_all_set));description.setText(getString(R.string.tap_the_field_below_to_try_it_out));
            enable.setVisibility(android.view.View.GONE);select.setVisibility(android.view.View.GONE);
            testLabel.setVisibility(android.view.View.GONE);help.setVisibility(android.view.View.GONE);
            root.setPadding(dp(26),dp(20),dp(26),dp(22));test.setMinLines(1);
        }
        else{
            enable.setVisibility(android.view.View.VISIBLE);select.setVisibility(android.view.View.VISIBLE);
            testLabel.setVisibility(android.view.View.VISIBLE);help.setVisibility(android.view.View.VISIBLE);
            root.setPadding(dp(26),dp(34),dp(26),dp(36));test.setMinLines(2);
            heading.setText(getString(R.string.set_up_your_keyboard));description.setText(getString(R.string.two_steps_to_start_typing_from_your_phone));}
    }
    @Override public boolean dispatchGenericMotionEvent(android.view.MotionEvent event){
        if(scroll!=null&&scroll.handleRotary(event))return true;
        return super.dispatchGenericMotionEvent(event);
    }
    private int dp(int n){return Ui.dp(this,n);}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    private LinearLayout.LayoutParams space(int w,int h,int top){LinearLayout.LayoutParams p=lp(w,h);p.topMargin=dp(top);return p;}
}
