package dev.local.pixelkeyboard.phone;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import com.google.android.gms.wearable.*;
import dev.local.pixelkeyboard.protocol.*;

public final class PhoneListenerService extends WearableListenerService {
    static final String CHANNEL="watch_input";
    static final int NOTIFICATION=17;
    static void channel(Context c) {
        c.getSystemService(NotificationManager.class).createNotificationChannel(
            new NotificationChannel(CHANNEL,c.getString(R.string.watch_input),NotificationManager.IMPORTANCE_DEFAULT));
    }
    @Override public void onMessageReceived(MessageEvent event) {
        try {
            String path=event.getPath();
            if (!path.startsWith(Protocol.ROOT+"/")) return;
            Packet p=Packet.decode(event.getData());
            if (p.session.isEmpty()) return;
            if (Protocol.REQUEST.equals(path)) {
                PendingRequest.put(this,event.getSourceNodeId(),p);
                channel(this);
                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED) {
                    Intent editor=new Intent(this,EditorActivity.class).putExtra("session",p.session);
                    PendingIntent open=PendingIntent.getActivity(this,0,editor,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    Notification n=new Notification.Builder(this,CHANNEL).setSmallIcon(dev.local.pixelkeyboard.phone.R.drawable.ic_keyboard)
                        .setContentTitle(getString(R.string.pixel_remote_keyboard))
                        .setContentText(getString(R.string.tap_to_type_for_your_watch))
                        .setContentIntent(open).setAutoCancel(true).setVisibility(Notification.VISIBILITY_PRIVATE).build();
                    getSystemService(NotificationManager.class).notify(NOTIFICATION,n);
                }
            } else if (Protocol.CLOSE.equals(path) || Protocol.ACK.equals(path)) {
                PendingRequest current=PendingRequest.get(this);
                if (!current.node.equals(event.getSourceNodeId()) || !current.session.equals(p.session)) return;
                if (Protocol.CLOSE.equals(path) || "ok".equals(p.data)) {
                    PendingRequest.close(this,event.getSourceNodeId(),p.session);
                    getSystemService(NotificationManager.class).cancel(NOTIFICATION);
                }
            } else return;
            sendBroadcast(new Intent(PendingRequest.CHANGED).setPackage(getPackageName())
                .putExtra("path",path).putExtra("session",p.session)
                .putExtra("operation",p.operation).putExtra("result",p.data));
        } catch (IllegalArgumentException ignored) { /* Reject invalid packets without logging input. */ }
    }
}
