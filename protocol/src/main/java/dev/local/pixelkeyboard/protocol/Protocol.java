package dev.local.pixelkeyboard.protocol;
public final class Protocol {
    private Protocol() {}
    public static final String ROOT = "/pixel-keyboard/v1";
    public static final String REQUEST = ROOT + "/request";
    public static final String INPUT = ROOT + "/input";
    public static final String ACK = ROOT + "/ack";
    public static final String CLOSE = ROOT + "/close";
    public static final String PHONE = "pixel_keyboard_phone_v1";
    public static final String WATCH = "pixel_keyboard_watch_v1";
}
