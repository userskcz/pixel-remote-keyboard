package dev.local.pixelkeyboard.protocol;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Only the current focused editor and selected peer can receive input. */
public final class InputSession {
    private String id = "", node = "";
    private final Set<String> applied = new HashSet<>();
    public String start() { id = UUID.randomUUID().toString(); node = ""; applied.clear(); return id; }
    public String id() { return id; }
    public String node() { return node; }
    public boolean bind(String session, String peer) {
        if (id.isEmpty() || !id.equals(session) || peer.isEmpty()) return false;
        node = peer; return true;
    }
    public boolean accepts(String peer, Packet payload) {
        return !id.isEmpty() && !node.isEmpty() && node.equals(peer)
            && id.equals(payload.session) && !payload.operation.isEmpty();
    }
    public boolean applied(String operation) { return applied.contains(operation); }
    public void markApplied(String operation) { applied.add(operation); }
    public void end() { id = ""; node = ""; applied.clear(); }
}
