package dev.local.pixelkeyboard.phone;

import android.content.Context;
import android.content.SharedPreferences;
import dev.local.pixelkeyboard.protocol.Packet;

/** Metadata only: typed text and watch-field contents are never stored. */
final class PendingRequest {
    static final String CHANGED = "dev.local.pixelkeyboard.PENDING_CHANGED";
    static final long MAX_AGE = 10 * 60 * 1000L;
    final String node, session, title;
    final boolean active, secret;
    final long receivedAt;
    private PendingRequest(SharedPreferences p) {
        node=p.getString("node",""); session=p.getString("session","");
        title=p.getString("title",""); secret=p.getBoolean("secret",false);
        receivedAt=p.getLong("time",0);
        active=p.getBoolean("active",false) && System.currentTimeMillis()-receivedAt < MAX_AGE;
    }
    static SharedPreferences prefs(Context c) { return c.getSharedPreferences("request_metadata", Context.MODE_PRIVATE); }
    static PendingRequest get(Context c) { return new PendingRequest(prefs(c)); }
    static void put(Context c, String node, Packet p) {
        prefs(c).edit().putString("node",node).putString("session",p.session)
            .putString("title",p.data).putBoolean("secret","secret".equals(p.operation))
            .putLong("time",System.currentTimeMillis()).putBoolean("active",true).commit();
    }
    static boolean close(Context c, String node, String session) {
        PendingRequest p=get(c);
        if (!p.node.equals(node) || !p.session.equals(session)) return false;
        prefs(c).edit().putBoolean("active",false).commit(); return true;
    }
}
