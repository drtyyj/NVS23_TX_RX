package model;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PacketProcessing {

    public short getBytesAsShort(byte[] packet, int offset, int length) {
        return ByteBuffer.wrap(packet, offset, length).getShort();
    }

    public int getBytesAsInt(byte[] packet, int offset, int length) {
        return ByteBuffer.wrap(packet, offset, length).getInt();
    }

    public String getBytesAsString(byte[] packet, int offset, int length) {
        return new String(Arrays.copyOfRange(packet, offset, offset + length), StandardCharsets.US_ASCII);
    }
}
