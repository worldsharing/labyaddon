package cc.raynet.worldsharing.protocol;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public record PacketBuffer(ByteBuf buffer) {

    public static void writeVarIntToBuffer(ByteBuf buf, int input) {
        while ((input & -128) != 0) {
            buf.writeByte(input & 127 | 128);
            input >>>= 7;
        }

        buf.writeByte(input);
    }

    public static int readVarIntFromStream(InputStream inputStream) throws IOException {
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
        return result;
    }

    public static int readVarIntFromByteBuf(ByteBuf buf) throws IOException {
        int tmp;
        int result = 0;
        int shift = 0;
        int bytesRead = 0;
        do {
            tmp = buf.readByte();
            result |= (tmp & 0x7F) << shift;
            shift += 7;
            bytesRead++;
            if (bytesRead > 5) { // VarInt can have at most 5 bytes in this encoding
                throw new IOException("VarInt is too long");
            }
        } while ((tmp & 0x80) != 0);
        return result;
    }

    public static int varIntSize(int value) {
        int result = 0;
        do {
            result++;
            value >>>= 7;
        } while (value != 0);
        return result;
    }

    public int size() {
        return buffer.readableBytes();
    }

    public int readVarIntFromBuffer() {
        int value = 0;
        int shift = 0;

        byte tmp;
        do {
            tmp = this.buffer.readByte();
            value |= (tmp & 127) << shift++ * 7;
            if (shift > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((tmp & 128) == 128);

        return value;
    }

    public void writeVarIntToBuffer(int input) {
        writeVarIntToBuffer(this.buffer, input);
    }

    public byte[] readByteArray() {
        byte[] b = new byte[this.buffer.readInt()];

        for (int i = 0; i < b.length; ++i) {
            b[i] = this.buffer.readByte();
        }

        return b;
    }

    public void writeByteArray(byte[] data) {
        this.buffer.writeInt(data.length);
        this.buffer.writeBytes(data);
    }

    public void writeByte(int value) {
        this.buffer.writeByte(value);
    }

    public byte readByte() {
        return this.buffer.readByte();
    }

    public void writeBytes(byte[] data) {
        this.buffer.writeBytes(data);
    }

    public int readInt() {
        return this.buffer.readInt();
    }

    public void writeInt(int value) {
        this.buffer.writeInt(value);
    }

    public String readString() {
        byte[] a = new byte[this.buffer.readInt()];

        for (int i = 0; i < a.length; ++i) {
            a[i] = this.buffer.readByte();
        }

        return new String(a, StandardCharsets.UTF_8);
    }

    public void writeString(String string) {
        buffer.writeInt(string.getBytes(StandardCharsets.UTF_8).length);
        buffer.writeBytes(string.getBytes(StandardCharsets.UTF_8));
    }

    public static String readSerializedString(ByteBuf buf) throws IOException {
        int length = readVarIntFromByteBuf(buf);
        if (buf.readableBytes() < length) {
            throw new IOException("Not enough bytes to read the string");
        }
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
