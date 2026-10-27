package dev.local.pixelkeyboard.watch;

import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import com.google.android.gms.wearable.*;
import dev.local.pixelkeyboard.protocol.*;
import dev.local.pixelkeyboard.ui.Ui;

/**
 * System IME for phone-to-watch text entry using the Wearable Data Layer.
 */
public final class RemoteKeyboardService extends InputMethodService implements MessageClient.OnMessageReceivedListener {
    private final Handler main=new Handler(Looper.getMainLooper());
    private final InputSession session=new InputSession();
    private TextView status, heading;
    private ImageView stateIcon;
    private Button request;
    private boolean visible=false, secret=false;
    private final Runnable requestTimeout=()->setStatus(Status.WAITING);
    @Override public void onCreate() {
        super.onCreate();
        Wearable.getMessageClient(this).addListener(this);
    }
    @Override public void onDestroy() {
        endSession(); Wearable.getMessageClient(this).removeListener(this);
        super.onDestroy();
    }
    @Override public boolean onEvaluateFullscreenMode(){return true;}
    @Override public View onCreateExtractTextView(){return null;}
    @Override public View onCreateInputView() {
        LinearLayout root=Ui.column(this);root.setGravity(Gravity.CENTER);
        root.setPadding(dp(26),dp(16),dp(26),dp(16));root.setBackgroundColor(Color.BLACK);
        stateIcon=Ui.icon(this,"phone",Ui.ACCENT);root.addView(stateIcon,lp(22,22));
        heading=Ui.text(this,getString(R.string.type_on_phone),16,Ui.TEXT,true);heading.setGravity(Gravity.CENTER);
        root.addView(heading,space(-1,-2,7));
        status=Ui.text(this,getString(R.string.open_pixel_remote_on_your_phone),11,Ui.MUTED,false);
        status.setGravity(Gravity.CENTER);status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        root.addView(status,space(-1,-2,4));
        request=Ui.button(this,getString(R.string.try_again),"refresh",false,()->requestPhone());
        request.setTextSize(12);request.setPadding(dp(12),0,dp(12),0);Ui.setGlyph(this,request,"refresh",Ui.TEXT,16);
        root.addView(request,space(148,34,9));
        LinearLayout controls=Ui.row(this);controls.setGravity(Gravity.CENTER);
        ImageButton delete=Ui.iconButton(this,"delete",getString(R.string.delete_character),()->{
            if(getCurrentInputConnection()!=null)sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
        });
        delete.setPadding(dp(9),dp(9),dp(9),dp(9));delete.setBackground(Ui.touch(this,Ui.SURFACE,18));
        controls.addView(delete,lp(36,36));
        Button done=Ui.button(this,getString(R.string.done),null,true,()->{
            sendDefaultEditorAction(true);requestHideSelf(0);
        });
        done.setTextSize(12);done.setPadding(dp(6),0,dp(6),0);
        LinearLayout.LayoutParams doneSize=lp(68,36);doneSize.leftMargin=dp(4);doneSize.rightMargin=dp(4);
        controls.addView(done,doneSize);
        ImageButton other=Ui.iconButton(this,"keyboard",getString(R.string.switch_keyboard),()->{
            endSession();((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker();
        });
        other.setPadding(dp(9),dp(9),dp(9),dp(9));other.setBackground(Ui.touch(this,Ui.SURFACE,18));
        controls.addView(other,lp(36,36));root.addView(controls,space(-1,-2,6));
        RotaryScrollView scroll=new RotaryScrollView(this);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);scroll.addView(root);
        int height=Math.min(dp(220),getResources().getDisplayMetrics().heightPixels);
        scroll.setLayoutParams(new FrameLayout.LayoutParams(-1,height));
        return scroll;
    }
    @Override public void onStartInput(EditorInfo info,boolean restarting){
        super.onStartInput(info,restarting);endSession();
    }
    @Override public void onStartInputView(EditorInfo info,boolean restarting){
        super.onStartInputView(info,restarting);
        visible=true;secret=isSecret(info.inputType);
        request.setText(secret?getString(R.string.allow):getString(R.string.try_again));
        if(secret) setStatus(Status.PASSWORD);
        else requestPhone();
    }
    @Override public void onFinishInputView(boolean finishingInput){
        visible=false;endSession();super.onFinishInputView(finishingInput);
    }
    @Override public void onFinishInput(){visible=false;endSession();super.onFinishInput();}
    // Detects password fields belonging to other apps; this IME does not request credentials.
    @android.annotation.SuppressLint("WearPasswordInput")
    private boolean isSecret(int type){
        int cls=type & InputType.TYPE_MASK_CLASS, variation=type & InputType.TYPE_MASK_VARIATION;
        return (cls==InputType.TYPE_CLASS_TEXT && (variation==InputType.TYPE_TEXT_VARIATION_PASSWORD
            || variation==InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD || variation==InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD))
            || (cls==InputType.TYPE_CLASS_NUMBER && variation==InputType.TYPE_NUMBER_VARIATION_PASSWORD);
    }
    private void requestPhone(){
        if(!visible || getCurrentInputConnection()==null)return;
        endSession();
        final String id=session.start();
        setStatus(Status.CONNECTING);
        Wearable.getCapabilityClient(this).getCapability(Protocol.PHONE,CapabilityClient.FILTER_REACHABLE)
            .addOnSuccessListener(capability->{
                if(!visible || !session.id().equals(id))return;
                Node selected=null;
                for(Node node:capability.getNodes()){if(selected==null || node.isNearby())selected=node;}
                if(selected==null){setStatus(Status.DISCONNECTED);return;}
                if(!session.bind(id,selected.getId()))return;
                String title=getString(R.string.text_field);
                EditorInfo info=getCurrentInputEditorInfo();
                if(info!=null && !secret) {
                    try {title=getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(info.packageName,0)).toString();}
                    catch(PackageManager.NameNotFoundException ignored){title=info.packageName;}
                }
                byte[] payload=new Packet(secret?getString(R.string.password):title,id,secret?"secret":"text").encode();
                Wearable.getMessageClient(this).sendMessage(selected.getId(),Protocol.REQUEST,payload)
                    .addOnSuccessListener(ignored->{
                        if(!session.id().equals(id))return;
                        setStatus(Status.WAITING);
                        main.removeCallbacks(requestTimeout);main.postDelayed(requestTimeout,60000);
                    }).addOnFailureListener(e->{
                        if(session.id().equals(id))setStatus(Status.DISCONNECTED);
                    });
            }).addOnFailureListener(e->{
                if(session.id().equals(id))setStatus(Status.DISCONNECTED);
            });
    }
    @Override public void onMessageReceived(MessageEvent event){
        if(!Protocol.INPUT.equals(event.getPath()))return;
        byte[] bytes=event.getData();
        String node=event.getSourceNodeId();
        main.post(()->{
            Packet p;
            try {p=Packet.decode(bytes);}catch(IllegalArgumentException e){return;}
            if(!visible || !session.accepts(node,p)) {
                ack(node,p,"stale");return;
            }
            if(session.applied(p.operation)){ack(node,p,"ok");return;}
            InputConnection ic=getCurrentInputConnection();
            if(ic==null){ack(node,p,"stale");return;}
            boolean committed=ic.commitText(p.data,1);
            if(committed){
                session.markApplied(p.operation);
                main.removeCallbacks(requestTimeout);
                setStatus(Status.INSERTED);
            } else setStatus(Status.FAILED);
            ack(node,p,committed?"ok":"failed");
        });
    }
    private void ack(String node,Packet p,String result){
        Wearable.getMessageClient(this).sendMessage(node,Protocol.ACK,
            new Packet(result,p.session,p.operation).encode());
    }
    private void endSession(){
        main.removeCallbacks(requestTimeout);
        String node=session.node(),id=session.id();session.end();
        if(!node.isEmpty()&&!id.isEmpty())
            Wearable.getMessageClient(this).sendMessage(node,Protocol.CLOSE,new Packet("",id,"").encode());
    }
    private enum Status { CONNECTING, WAITING, INSERTED, PASSWORD, FAILED, DISCONNECTED }
    private void setStatus(Status value){
        if(status==null)return;
        String title=getString(R.string.check_your_phone),detail=getString(R.string.open_pixel_remote_on_your_phone),glyph="info";
        int color=Ui.ERROR;
        switch(value){
            case CONNECTING:title=getString(R.string.connecting);detail=getString(R.string.looking_for_your_phone);glyph="refresh";color=Ui.ACCENT;break;
            case WAITING:title=getString(R.string.type_on_phone);detail=getString(R.string.open_pixel_remote_on_your_phone);glyph="phone";color=Ui.ACCENT;break;
            case INSERTED:title=getString(R.string.text_inserted);detail=getString(R.string.you_can_finish_typing);glyph="check";color=Ui.ACCENT;request.setText(getString(R.string.more_text));break;
            case PASSWORD:title=getString(R.string.password_field);detail=getString(R.string.allow_typing_from_your_phone);glyph="lock";color=Ui.ACCENT;break;
            case FAILED:title=getString(R.string.could_not_insert);detail=getString(R.string.reopen_the_text_field);break;
            case DISCONNECTED:title=getString(R.string.not_connected);detail=getString(R.string.open_pixel_remote_on_your_phone);break;
        }
        heading.setText(title);status.setText(detail);stateIcon.setImageDrawable(new Ui.Glyph(glyph,color));
    }
    private int dp(int n){return Ui.dp(this,n);}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    private LinearLayout.LayoutParams space(int w,int h,int top){LinearLayout.LayoutParams p=lp(w,h);p.topMargin=dp(top);return p;}
}