package dev.local.pixelkeyboard.protocol;

import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ProtocolTest {
    @Test public void unicodeAndSpecialCharactersSurviveTransport() {
        String text="Привіт, ґанок! Їжак 🦔 👨‍👩‍👧‍👦\nhttps://example.test/?a=1&b=%s \"quotes\" $ ` \\";
        Packet p=Packet.decode(new Packet(text,"session","operation").encode());
        assertEquals(text,p.data);assertEquals("session",p.session);assertEquals("operation",p.operation);
    }
    @Test public void rejectsDifferentProtocolAndTrailingBytes() {
        byte[] valid=new Packet("hello","s","o").encode();
        byte[] wrong=valid.clone();wrong[0]=0;
        rejects(wrong);rejects(Arrays.copyOf(valid,valid.length+1));
    }
    @Test public void emptyAndBoundarySizedMessagesRoundTrip() {
        assertEquals("",Packet.decode(new Packet("","","").encode()).data);
        String boundary="a".repeat(Packet.MAX_BYTES-16);
        assertEquals(boundary,Packet.decode(new Packet(boundary,"","").encode()).data);
        try{new Packet(boundary+"a","","").encode();fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void rejectsTruncationNegativeLengthsAndInvalidUnicode() {
        byte[] valid=new Packet("hello","s","o").encode();
        for(int n=0;n<valid.length;n++)rejects(Arrays.copyOf(valid,n));
        byte[] negative=valid.clone();ByteBuffer.wrap(negative).putInt(4,-1);rejects(negative);
        byte[] bad=valid.clone();bad[8]=(byte)255;rejects(bad);
        rejects(new byte[60001]);rejects(null);
        try{new Packet("\uD800","s","o").encode();fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void staleReplyCannotTypeIntoNextFieldEvenFromSamePhone() {
        InputSession s=new InputSession();String old=s.start();s.bind(old,"phone");
        Packet p=new Packet("secret",old,"op");assertTrue(s.accepts("phone",p));
        s.bind(s.start(),"phone");assertFalse(s.accepts("phone",p));
    }
    @Test public void closedEditorAndDifferentDeviceCannotReceiveText() {
        InputSession s=new InputSession();String id=s.start();s.bind(id,"phone");
        Packet p=new Packet("text",id,"op");assertFalse(s.accepts("other",p));
        s.end();assertFalse(s.accepts("phone",p));
    }
    @Test public void duplicateDeliveryIsRecognizedWithinSession() {
        InputSession s=new InputSession();s.start();assertFalse(s.applied("op"));
        s.markApplied("op");assertTrue(s.applied("op"));s.start();assertFalse(s.applied("op"));
    }
    @Test public void asynchronousDiscoveryCannotBindOldSession() {
        InputSession s=new InputSession();String old=s.start();s.start();assertFalse(s.bind(old,"phone"));
    }
    private void rejects(byte[] packet){try{Packet.decode(packet);fail("Invalid packet accepted");}catch(IllegalArgumentException expected){}}
}
