package cc.raynet.worldsharing.protocol.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VarInt {

    private final int value;

    public VarInt(int value) {
        this.value = value;
    }

    public static VarInt readFromStream(InputStream inputStream) throws IOException {
        int tmp;
        int result = 0;
        int shift = 0;
        int bytesRead = 0;
        do {
            tmp = inputStream.read();
            result |= (tmp & 0x7F) << shift;
            shift += 7;
            bytesRead++;
            if (bytesRead > 5) { // VarInt can have at most 5 bytes in this encoding
                throw new IOException("VarInt is too long");
            }
        } while ((tmp & 0x80) != 0);
        return new VarInt(result);
    }

    public static int size(int value) {
        int result = 0;
        do {
            result++;
            value >>>= 7;
        } while (value != 0);
        return result;
    }

    public static void writeToStream(OutputStream outputStream, int tmp) throws IOException {
        do {
            // Encode next 7 bits + terminator bit
            int bits = tmp & 0x7F;
            tmp >>>= 7;
            byte b = (byte) (bits + ((tmp != 0) ? 0x80 : 0));
            outputStream.write(b);
        } while (tmp != 0);
    }

    public int value() {
        return value;
    }


}