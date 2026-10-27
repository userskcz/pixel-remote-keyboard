package dev.local.pixelkeyboard.protocol;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.Objects;

/** Versioned, length-prefixed UTF-8 transport; contains no persistent state. */
public final class Packet {
    private static final int MAGIC = 0x50524b31; // PRK1
    public static final int MAX_BYTES = 60000;
    public final String data, session, operation;
    public Packet(String data, String session, String operation) {
        this.data=Objects.requireNonNull(data);
        this.session=Objects.requireNonNull(session);
        this.operation=Objects.requireNonNull(operation);
    }
    public byte[] encode() {
        byte[][] values={utf8(data),utf8(session),utf8(operation)};
        long size=16L;
        for(byte[] value:values) size+=value.length;
        if(size>MAX_BYTES)throw new IllegalArgumentException("Text is too large (maximum packet size: 60 KB).");
        ByteBuffer out=ByteBuffer.allocate((int)size).putInt(MAGIC);
        for(byte[] value:values)out.putInt(value.length).put(value);
        return out.array();
    }
    public static Packet decode(byte[] raw) {
        if(raw==null||raw.length<16||raw.length>MAX_BYTES)throw new IllegalArgumentException("Invalid packet size");
        ByteBuffer in=ByteBuffer.wrap(raw);
        if(in.getInt()!=MAGIC)throw new IllegalArgumentException("Unsupported protocol");
        String[] values=new String[3];
        for(int i=0;i<3;i++){
            if(in.remaining()<4)throw new IllegalArgumentException("Missing length");
            int size=in.getInt();
            if(size<0||size>in.remaining())throw new IllegalArgumentException("Invalid length");
            ByteBuffer part=in.slice();part.limit(size);
            try{values[i]=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(part).toString();}
            catch(CharacterCodingException e){throw new IllegalArgumentException("Invalid UTF-8",e);}
            in.position(in.position()+size);
        }
        if(in.hasRemaining())throw new IllegalArgumentException("Trailing bytes");
        return new Packet(values[0],values[1],values[2]);
    }
    private static byte[] utf8(String value){
        try{
            ByteBuffer b=StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).encode(java.nio.CharBuffer.wrap(value));
            byte[] bytes=new byte[b.remaining()];b.get(bytes);return bytes;
        }catch(CharacterCodingException e){throw new IllegalArgumentException("Text contains an incomplete Unicode character.",e);}
    }
}
