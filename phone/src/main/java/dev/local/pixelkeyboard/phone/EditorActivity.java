package dev.local.pixelkeyboard.phone;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.android.gms.wearable.*;
import dev.local.pixelkeyboard.protocol.*;
import dev.local.pixelkeyboard.ui.Ui;
import java.util.UUID;

public final class EditorActivity extends Activity {
    private TextView stateTitle,stateDetail,counter,notice,footer;
    private LinearLayout stateCard,hero;
    private ImageView stateIcon;
    private EditText editor;
    private Button send,paste;
    private ImageButton clear;
    private String session="",sendingOperation="";
    private boolean registered=false,busy=false,active=false,completed=false;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable timeout=()->{
        busy=false;updateActions();
        state(getString(R.string.no_confirmation),getString(R.string.check_your_watch_for_the_text_then_reopen_th),"info",Ui.ERROR);
    };
    private final BroadcastReceiver receiver=new BroadcastReceiver(){
        @Override public void onReceive(Context c,Intent intent){
            String eventSession=intent.getStringExtra("session");
            if(Protocol.ACK.equals(intent.getStringExtra("path")) && session.equals(eventSession)
                && sendingOperation.equals(intent.getStringExtra("operation"))){
                handler.removeCallbacks(timeout);busy=false;
                if("ok".equals(intent.getStringExtra("result"))){
                    completed=true;active=false;editor.setText("");editor.setEnabled(false);updateActions();
                    state(getString(R.string.inserted_on_watch),getString(R.string.you_can_finish_typing_on_your_watch),"check",Ui.ACCENT);
                    ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editor.getWindowToken(),0);
                    send.announceForAccessibility(getString(R.string.text_inserted_on_your_watch));
                }else{
                    active=false;updateActions();
                    state(getString(R.string.text_field_closed),getString(R.string.reopen_the_text_field_on_your_watch),"info",Ui.ERROR);
                }
                return;
            }
            if(Protocol.CLOSE.equals(intent.getStringExtra("path")) && session.equals(eventSession) && busy)return;
            refresh();
        }
    };
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        getWindow().setDecorFitsSystemWindows(false);
        PhoneListenerService.channel(this);
        LinearLayout root=Ui.column(this);root.setBackgroundColor(Ui.BG);

        LinearLayout toolbar=Ui.row(this);
        ImageView mark=Ui.icon(this,"keyboard",Ui.ACCENT);
        mark.setPadding(dp(10),dp(10),dp(10),dp(10));mark.setBackground(Ui.shape(this,Ui.RAISED,14));
        toolbar.addView(mark,new LinearLayout.LayoutParams(dp(40),dp(40)));
        LinearLayout brand=Ui.column(this);
        brand.addView(Ui.text(this,"Pixel Remote",17,Ui.TEXT,true));
        TextView subtitle=Ui.text(this,getString(R.string.keyboard_for_your_watch),12,Ui.MUTED,false);
        LinearLayout.LayoutParams sub=lp(-1,-2);sub.topMargin=dp(3);brand.addView(subtitle,sub);
        LinearLayout.LayoutParams brandSize=new LinearLayout.LayoutParams(0,-2,1);brandSize.leftMargin=dp(12);
        toolbar.addView(brand,brandSize);
        toolbar.addView(Ui.iconButton(this,"more",getString(R.string.settings),()->settings()),lp(44,44));
        root.addView(toolbar,lp(-1,56));

        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);scroll.setVerticalScrollBarEnabled(false);
        LinearLayout body=Ui.column(this);body.setPadding(0,dp(20),0,dp(16));
        hero=Ui.column(this);hero.setPadding(0,dp(8),0,dp(26));
        TextView headline=Ui.text(this,getString(R.string.from_your_phone_to_your_watch),36,Ui.TEXT,true);
        headline.setLineSpacing(dp(2),1);headline.setLetterSpacing(-0.035f);hero.addView(headline);
        TextView intro=Ui.text(this,getString(R.string.more_room_for_your_words),15,Ui.MUTED,false);
        LinearLayout.LayoutParams introSize=lp(-1,-2);introSize.topMargin=dp(12);hero.addView(intro,introSize);
        body.addView(hero);

        stateCard=Ui.row(this);stateCard.setPadding(dp(16),dp(16),dp(8),dp(16));
        stateCard.setBackground(Ui.outline(this,Ui.SURFACE,24,Ui.LINE));stateCard.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        stateIcon=Ui.icon(this,"watch",Ui.MUTED);
        stateCard.addView(stateIcon,lp(28,28));
        LinearLayout stateText=Ui.column(this);
        stateTitle=Ui.text(this,getString(R.string.waiting_for_your_watch),16,Ui.TEXT,true);stateText.addView(stateTitle);
        stateDetail=Ui.text(this,getString(R.string.open_a_text_field_on_your_watch),13,Ui.MUTED,false);
        LinearLayout.LayoutParams detailSize=lp(-1,-2);detailSize.topMargin=dp(5);stateText.addView(stateDetail,detailSize);
        LinearLayout.LayoutParams stateTextSize=new LinearLayout.LayoutParams(0,-2,1);stateTextSize.leftMargin=dp(14);
        stateCard.addView(stateText,stateTextSize);
        stateCard.addView(Ui.iconButton(this,"refresh",getString(R.string.check_connection),()->checkConnection(true)),lp(44,44));
        body.addView(stateCard,lp(-1,-2));

        LinearLayout editorCard=Ui.column(this);editorCard.setPadding(dp(18),dp(12),dp(18),dp(12));
        editorCard.setBackground(Ui.outline(this,Ui.SURFACE,28,Ui.LINE));
        LinearLayout editorBar=Ui.row(this);
        TextView caption=Ui.text(this,getString(R.string.your_text),11,Ui.MUTED,true);caption.setLetterSpacing(.12f);
        editorBar.addView(caption,new LinearLayout.LayoutParams(0,-2,1));
        clear=Ui.iconButton(this,"close",getString(R.string.clear_text),()->editor.setText(""));
        editorBar.addView(clear,lp(40,40));editorCard.addView(editorBar);
        editor=new EditText(this);
        editor.setId(View.generateViewId());editor.setTextColor(Ui.TEXT);editor.setHintTextColor(Ui.DISABLED);
        editor.setTextSize(21);editor.setTypeface(android.graphics.Typeface.create("sans-serif",android.graphics.Typeface.NORMAL));
        editor.setLineSpacing(dp(6),1);editor.setGravity(Gravity.TOP|Gravity.START);
        editor.setPadding(0,dp(8),0,dp(14));editor.setBackgroundColor(Color.TRANSPARENT);
        editor.setMinLines(5);editor.setMaxLines(16);editor.setHorizontallyScrolling(false);
        editor.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editor.setSaveEnabled(false);editor.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        editor.setHint(getString(R.string.your_text_goes_here));editorCard.addView(editor,lp(-1,-2));
        View divider=new View(this);divider.setBackgroundColor(Ui.LINE);editorCard.addView(divider,lp(-1,1));
        LinearLayout editorTools=Ui.row(this);editorTools.setPadding(0,dp(8),0,0);
        paste=Ui.button(this,getString(R.string.paste),"paste",false,()->pasteClipboard());paste.setTextSize(13);paste.setPadding(dp(12),0,dp(12),0);
        editorTools.addView(paste,lp(-2,40));
        counter=Ui.text(this,getString(R.string.text_0_characters),12,Ui.MUTED,false);counter.setGravity(Gravity.END);
        editorTools.addView(counter,new LinearLayout.LayoutParams(0,-2,1));editorCard.addView(editorTools);
        LinearLayout.LayoutParams cardSize=lp(-1,-2);cardSize.topMargin=dp(16);body.addView(editorCard,cardSize);

        notice=Ui.text(this,getString(R.string.notifications_are_off_enable),13,Ui.ACCENT,true);notice.setGravity(Gravity.CENTER);
        notice.setPadding(dp(8),dp(18),dp(8),dp(8));notice.setOnClickListener(v->requestNotifications());
        body.addView(notice,lp(-1,-2));
        scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout dock=Ui.column(this);dock.setPadding(0,dp(10),0,dp(4));
        send=Ui.button(this,getString(R.string.insert_on_watch),"arrow",true,()->sendText());
        dock.addView(send,lp(-1,56));
        footer=Ui.text(this,getString(R.string.no_text_history),12,Ui.MUTED,false);footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerSize=lp(-1,-2);footerSize.topMargin=dp(12);footerSize.bottomMargin=dp(6);dock.addView(footer,footerSize);
        root.addView(dock);
        root.setOnApplyWindowInsetsListener((v,insets)->{
            android.graphics.Insets bars=insets.getInsets(WindowInsets.Type.systemBars());
            int bottom=Math.max(bars.bottom,insets.getInsets(WindowInsets.Type.ime()).bottom);
            boolean typing=insets.isVisible(WindowInsets.Type.ime());
            root.setPadding(dp(20)+bars.left,bars.top,dp(20)+bars.right,bottom+dp(8));
            hero.setVisibility(typing?View.GONE:View.VISIBLE);footer.setVisibility(typing?View.GONE:View.VISIBLE);
            return insets;
        });
        setContentView(root);
        editor.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){updateActions();}
            public void afterTextChanged(Editable s){}
        });
    }
    @Override protected void onResume(){
        super.onResume();registerReceiver(receiver,new IntentFilter(PendingRequest.CHANGED),Context.RECEIVER_NOT_EXPORTED);
        registered=true;refresh();checkConnection(false);
    }
    @Override protected void onPause(){if(registered){unregisterReceiver(receiver);registered=false;}super.onPause();}
    @Override protected void onDestroy(){handler.removeCallbacks(timeout);super.onDestroy();}
    private void refresh(){
        PendingRequest p=PendingRequest.get(this);
        boolean changed=!session.equals(p.session);
        if(changed){handler.removeCallbacks(timeout);busy=false;completed=false;sendingOperation="";editor.setText("");session=p.session;}
        active=p.active&&!completed;
        int type=p.secret?(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
            :(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        if(editor.getInputType()!=type)editor.setInputType(type);
        if(p.secret)getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        editor.setEnabled(active&&!busy&&sendingOperation.isEmpty());
        editor.setHint(active?(p.secret?getString(R.string.enter_your_password):getString(R.string.write_or_paste_your_text)):getString(R.string.first_open_a_text_field_on_your_watch));
        notice.setVisibility(checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED?View.GONE:View.VISIBLE);
        updateActions();
        if(completed)state(getString(R.string.inserted_on_watch),getString(R.string.you_can_finish_typing_on_your_watch),"check",Ui.ACCENT);
        else if(!busy&&sendingOperation.isEmpty()){
            if(active)state(p.secret?getString(R.string.password_field):getString(R.string.ready_to_type),p.secret?getString(R.string.your_password_is_hidden_and_is_not_saved):p.title,"watch",Ui.ACCENT);
            else state(getString(R.string.waiting_for_your_watch),getString(R.string.open_a_text_field_on_your_watch),"watch",Ui.MUTED);
        }
    }
    private void updateActions(){
        if(send==null)return;
        boolean editable=active&&!busy&&sendingOperation.isEmpty();
        boolean hasText=editor.length()>0;
        send.setEnabled(editable&&hasText);paste.setEnabled(editable);clear.setEnabled(editable&&hasText);
        clear.setAlpha(hasText?1f:.35f);
        send.setText(busy?getString(R.string.inserting):completed?getString(R.string.text_inserted):getString(R.string.insert_on_watch));
        Ui.setGlyph(this,send,completed?"check":"arrow",completed?Ui.ACCENT:Ui.INK,20);
        String value=editor.getText().toString();
        int n=value.codePointCount(0,value.length());
        counter.setText(getResources().getQuantityString(R.plurals.character_count,n,n));
    }
    private void sendText(){
        PendingRequest p=PendingRequest.get(this);
        if(!p.active||!session.equals(p.session)||busy||!sendingOperation.isEmpty()){refresh();return;}
        if(editor.length()==0)return;
        sendingOperation=UUID.randomUUID().toString();byte[] bytes;
        try{bytes=new Packet(editor.getText().toString(),p.session,sendingOperation).encode();}
        catch(IllegalArgumentException e){sendingOperation="";state(getString(R.string.text_too_long),e.getMessage(),"info",Ui.ERROR);updateActions();return;}
        busy=true;editor.setEnabled(false);updateActions();
        state(getString(R.string.inserting_text),getString(R.string.waiting_for_confirmation_from_your_watch),"refresh",Ui.ACCENT);
        handler.postDelayed(timeout,12000);
        Wearable.getMessageClient(this).sendMessage(p.node,Protocol.INPUT,bytes).addOnFailureListener(e->{
            handler.removeCallbacks(timeout);busy=false;updateActions();
            state(getString(R.string.connection_lost),getString(R.string.reopen_the_text_field_on_your_watch),"info",Ui.ERROR);
        });
    }
    private void pasteClipboard(){
        if(!editor.isEnabled())return;
        ClipboardManager clipboard=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData clip=clipboard.getPrimaryClip();
        if(clip==null||clip.getItemCount()==0){Toast.makeText(this,getString(R.string.your_clipboard_is_empty),Toast.LENGTH_SHORT).show();return;}
        CharSequence text=clip.getItemAt(0).coerceToText(this);
        if(text==null)return;
        int start=Math.max(0,editor.getSelectionStart()),end=Math.max(0,editor.getSelectionEnd());
        editor.getText().replace(Math.min(start,end),Math.max(start,end),text);
        editor.requestFocus();
    }
    private void checkConnection(boolean explicit){
        if(explicit&&!active&&!busy&&!completed)state(getString(R.string.checking_connection),getString(R.string.looking_for_your_watch),"refresh",Ui.MUTED);
        Wearable.getCapabilityClient(this).getCapability(Protocol.WATCH,CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener(c->{
                if(active||busy||completed||!sendingOperation.isEmpty())return;
                if(c.getNodes().isEmpty())state(getString(R.string.watch_disconnected),getString(R.string.check_bluetooth_and_the_app_on_your_watch),"watch",Ui.ERROR);
                else state(getString(R.string.watch_connected),getString(R.string.open_a_text_field_on_your_watch),"watch",Ui.ACCENT);
            }).addOnFailureListener(e->{
                if(!active&&!busy&&!completed)state(getString(R.string.could_not_connect),getString(R.string.check_the_connection_between_your_phone_and),"info",Ui.ERROR);
            });
    }
    private void state(String title,String detail,String glyph,int color){
        stateTitle.setText(title);stateDetail.setText(detail);stateIcon.setImageDrawable(new Ui.Glyph(glyph,color));
        stateCard.setBackground(Ui.outline(this,Ui.SURFACE,24,color==Ui.ACCENT?0xff465a41:Ui.LINE));
    }
    private void settings(){
        String notification=checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED
            ?getString(R.string.notification_settings):getString(R.string.enable_notifications);
        new AlertDialog.Builder(this).setTitle("Pixel Remote")
            .setItems(new String[]{notification,getString(R.string.check_connection),getString(R.string.how_to_use),getString(R.string.open_source_licenses)},(dialog,which)->{
                if(which==0)requestNotifications();
                if(which==1)checkConnection(true);
                if(which==3)startActivity(new Intent(this,dev.local.pixelkeyboard.ui.LicensesActivity.class));
                if(which==2)new AlertDialog.Builder(this).setTitle(getString(R.string.three_simple_steps))
                    .setMessage(getString(R.string.text_1_open_a_text_field_on_your_watch_2_type_her))
                    .setPositiveButton(getString(R.string.got_it),null).show();
            }).setNegativeButton(getString(R.string.close),null).show();
    }
    private void requestNotifications(){
        if(checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED){
            startActivity(new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,getPackageName()));
        }else requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},8);
    }
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grants){
        super.onRequestPermissionsResult(requestCode,permissions,grants);
        if(requestCode==8)notice.setVisibility(checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED?View.GONE:View.VISIBLE);
    }
    private int dp(int n){return Ui.dp(this,n);}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
}
