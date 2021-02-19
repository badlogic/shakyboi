package io.marioslab.shakyboi.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ByteArrayInput extends InputStream {
    public final byte[] buffer;
    public int index, end;

    private char[] chars;

    public ByteArrayInput(byte[] buffer) {
        this(buffer, 0, buffer.length);
    }

    public ByteArrayInput(byte[] buffer, int offset, int length) {
        this.buffer = buffer;
        index = offset;
        end = offset + length;
    }

    public int read() {
        if (index >= end) return -1;
        return buffer[index++] & 0xff;
    }

    public int read(byte b[]) {
        return read(b, 0, b.length);
    }

    public int read(byte[] bytes, int offset, int length) {
        if (length == 0) return 0;
        int count = Math.min(length, end - index);
        if (count == 0) return -1;
        System.arraycopy(buffer, index, bytes, offset, count);
        index += count;
        return count;
    }

    public int available() {
        return end - index;
    }

    public boolean end() {
        return index >= end;
    }

    public long skip(long n) {
        if (n < 0 || n > Integer.MAX_VALUE) throw new IllegalArgumentException();
        int count = Math.min((int) n, end - index);
        index += count;
        return count;
    }

    public void skipNBytes(long n) {
        skip(n);
    }

    public long transferTo(OutputStream out) throws IOException {
        return transferTo(out, end - index);
    }

    public long transferTo(OutputStream out, int count) throws IOException {
        out.write(buffer, index, count);
        index += count;
        return count;
    }

    public long transferTo(ByteArrayOutput out) {
        return transferTo(out, end - index);
    }

    public long transferTo(ByteArrayOutput out, int count) {
        out.ensureCapacity(count);
        System.arraycopy(buffer, index, out.buffer, out.index, count);
        out.index += count;
        index += count;
        return count;
    }

    public byte readByte() {
        if (index >= end) return -1;
        return buffer[index++];
    }

    public int readUnsignedByte() {
        return buffer[index++] & 0xff;
    }

    public boolean readBoolean() {
        return buffer[index++] != 0;
    }

    public short readShort() {
        int i = index;
        index += 2;
        return (short) ((buffer[i] & 0xff) << 8 //
                | buffer[i + 1] & 0xff);
    }

    public short readShortLE() {
        int i = index;
        index += 2;
        return (short) (buffer[i + 1] & 0xff //
                | (buffer[i] & 0xff) << 8);
    }

    public char readChar() {
        int i = index;
        index += 2;
        return (char) ((buffer[i] & 0xff) << 8 //
                | buffer[i + 1] & 0xff);
    }

    public char readCharLE() {
        int i = index;
        index += 2;
        return (char) (buffer[i + 1] & 0xff //
                | (buffer[i] & 0xff) << 8 //
        );
    }

    public int readUnsignedShort() {
        int i = index;
        index += 2;
        return (buffer[i] & 0xff) << 8 //
                | buffer[i + 1] & 0xff;
    }

    public int readUnsignedShortLE() {
        int i = index;
        index += 2;
        return buffer[i + 1] & 0xff //
                | (buffer[i] & 0xff) << 8;
    }

    public int readInt() {
        int i = index;
        index += 4;
        return buffer[i] << 24 //
                | (buffer[i + 1] & 0xff) << 16 //
                | (buffer[i + 2] & 0xff) << 8 //
                | buffer[i + 3] & 0xff;
    }

    public int readIntLE() {
        int i = index;
        index += 4;
        return buffer[i] & 0xff //
                | (buffer[i + 1] & 0xff) << 8 //
                | (buffer[i + 2] & 0xff) << 16 //
                | buffer[i + 3] << 24;
    }

    public long readLong() {
        int i = index;
        index += 8;
        return (long) buffer[i] << 56 //
                | (buffer[i + 1] & 0xffL) << 48 //
                | (buffer[i + 2] & 0xffL) << 40 //
                | (buffer[i + 3] & 0xffL) << 32 //
                | (buffer[i + 4] & 0xffL) << 24 //
                | (buffer[i + 5] & 0xff) << 16 //
                | (buffer[i + 6] & 0xff) << 8 //
                | buffer[i + 7] & 0xff;
    }

    public long readLongLE() {
        int i = index;
        index += 8;
        return buffer[i] & 0xff //
                | (buffer[i + 1] & 0xff) << 8 //
                | (buffer[i + 2] & 0xff) << 16 //
                | (buffer[i + 3] & 0xffL) << 24 //
                | (buffer[i + 4] & 0xffL) << 32 //
                | (buffer[i + 5] & 0xffL) << 40 //
                | (buffer[i + 6] & 0xffL) << 48 //
                | (long) buffer[7] << 56;
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public float readFloatLE() {
        return Float.intBitsToFloat(readIntLE());
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public double readDoubleLE() {
        return Double.longBitsToDouble(readLongLE());
    }

    public String readString(int length) {
        String value = new String(buffer, index, length, UTF_8);
        index += length;
        return value;
    }

    static public int readInt(byte[] buffer, int i) {
        return buffer[i] << 24 //
                | (buffer[i + 1] & 0xff) << 16 //
                | (buffer[i + 2] & 0xff) << 8 //
                | buffer[i + 3] & 0xff;
    }

    static public int readInt(byte[] buffer) {
        return buffer[0] << 24 //
                | (buffer[1] & 0xff) << 16 //
                | (buffer[2] & 0xff) << 8 //
                | buffer[3] & 0xff;
    }

    static public int readIntLE(byte[] buffer, int i) {
        return buffer[i] & 0xff //
                | (buffer[i + 1] & 0xff) << 8 //
                | (buffer[i + 2] & 0xff) << 16 //
                | buffer[i + 3] << 24;
    }

    static public int readIntLE(byte[] buffer) {
        return buffer[0] & 0xff //
                | (buffer[1] & 0xff) << 8 //
                | (buffer[2] & 0xff) << 16 //
                | buffer[3] << 24;
    }

    static public long readLong(byte[] buffer, int i) {
        return (long) buffer[i] << 56 //
                | (buffer[i + 1] & 0xffL) << 48 //
                | (buffer[i + 2] & 0xffL) << 40 //
                | (buffer[i + 3] & 0xffL) << 32 //
                | (buffer[i + 4] & 0xffL) << 24 //
                | (buffer[i + 5] & 0xff) << 16 //
                | (buffer[i + 6] & 0xff) << 8 //
                | buffer[i + 7] & 0xff;
    }

    static public long readLong(byte[] buffer) {
        return (long) buffer[0] << 56 //
                | (buffer[1] & 0xffL) << 48 //
                | (buffer[2] & 0xffL) << 40 //
                | (buffer[3] & 0xffL) << 32 //
                | (buffer[4] & 0xffL) << 24 //
                | (buffer[5] & 0xff) << 16 //
                | (buffer[6] & 0xff) << 8 //
                | buffer[7] & 0xff;
    }

    static public long readLongLE(byte[] buffer, int i) {
        return buffer[i] & 0xff //
                | (buffer[i + 1] & 0xff) << 8 //
                | (buffer[i + 2] & 0xff) << 16 //
                | (buffer[i + 3] & 0xffL) << 24 //
                | (buffer[i + 4] & 0xffL) << 32 //
                | (buffer[i + 5] & 0xffL) << 40 //
                | (buffer[i + 6] & 0xffL) << 48 //
                | (long) buffer[i + 7] << 56;
    }

    static public long readLongLE(byte[] buffer) {
        return buffer[0] & 0xff //
                | (buffer[1] & 0xff) << 8 //
                | (buffer[2] & 0xff) << 16 //
                | (buffer[3] & 0xffL) << 24 //
                | (buffer[4] & 0xffL) << 32 //
                | (buffer[5] & 0xffL) << 40 //
                | (buffer[6] & 0xffL) << 48 //
                | (long) buffer[7] << 56;
    }
}
