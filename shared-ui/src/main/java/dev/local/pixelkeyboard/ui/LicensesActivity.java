package dev.local.pixelkeyboard.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** Reads project and dependency notices bundled at build time. */
public class LicensesActivity extends Activity {
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        boolean watch=getPackageManager().hasSystemFeature("android.hardware.type.watch");
        LinearLayout root=Ui.column(this);
        root.setPadding(Ui.dp(this,watch?30:22),Ui.dp(this,watch?38:24),Ui.dp(this,watch?30:22),Ui.dp(this,30));
        root.setBackgroundColor(Ui.BG);
        String title=string("open_source_licenses");
        root.addView(Ui.text(this,title,watch?16:20,Ui.TEXT,true));
        TextView body=Ui.text(this,read("project_license")+"\n\n"+title+"\n\n"+read("third_party_licenses"),watch?11:12,Ui.MUTED,false);
        body.setTextIsSelectable(true);body.setPadding(0,Ui.dp(this,18),0,Ui.dp(this,24));root.addView(body);
        ScrollView scroll=createScrollView();scroll.setFillViewport(true);scroll.addView(root);setContentView(scroll);
    }
    protected ScrollView createScrollView(){return new ScrollView(this);}
    private String string(String name){return getString(getResources().getIdentifier(name,"string",getPackageName()));}
    private String read(String name){
        int resource=getResources().getIdentifier(name,"raw",getPackageName());
        if(resource==0)return string("license_notices_missing");
        try(InputStream input=getResources().openRawResource(resource);ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buffer=new byte[8192];int count;
            while((count=input.read(buffer))!=-1)out.write(buffer,0,count);
            return out.toString(StandardCharsets.UTF_8.name());
        }catch(IOException e){return string("license_notices_missing");}
    }
}
