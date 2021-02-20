package io.marioslab.shakyboi.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteArrayOutput extends OutputStream {
    public byte[] buffer;
    public int index;
    public OutputStream stream;

    public ByteArrayOutput(int initialSize) {
        this(new byte[initialSize]);
    }

    public ByteArrayOutput(byte[] buffer) {
        this.buffer = buffer;
    }

    public void write(int value) {
        if (index + 1 >= buffer.length) ensureCapacity(1);
        buffer[index++] = (byte) value;
    }

    public void write(byte[] b) {
        write(b, 0, b.length);
    }

    public void write(byte[] data, int offset, int length) {
        if (index + length >= buffer.length) ensureCapacity(length);
        System.arraycopy(data, offset, buffer, index, length);
        index += length;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(buffer, index);
    }

    public void writeTo(OutputStream out) throws IOException {
        out.write(buffer, 0, index);
    }

    public void writeTo(ByteBuffer out) {
        out.put(buffer, 0, index);
    }

    public void readFrom(InputStream input) throws IOException {
        while (true) {
            int count = input.read(buffer, index, buffer.length - index);
            if (count == -1) break;
            if (index + count >= buffer.length) ensureCapacity(count);
            index += count;
        }
    }

    public void ensureCapacity(int additionalCapacity) {
        int oldLength = buffer.length;
        int needed = (index + additionalCapacity) - oldLength;
        if (needed <= 0) return;

        if (stream != null) {
            try {
                flush();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            if (additionalCapacity <= oldLength) return;
        }

        int increase;
        if (oldLength > 1 << 24)
            increase = oldLength >> (oldLength > 1 << 29 ? 2 : 1);
        else
            increase = oldLength;
        int newLength = oldLength + Math.max(increase, needed);
        if (newLength <= 0) newLength = Integer.MAX_VALUE;
        buffer = Arrays.copyOf(buffer, newLength);
    }

    public void flush() throws IOException {
        if (stream == null) return;
        try {
            stream.write(buffer, 0, index);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        index = 0;
    }

    public void close() throws IOException {
        if (stream == null) return;
        stream.write(buffer, 0, index);
        stream.close();
    }

    public void reset() {
        index = 0;
    }

    public int size() {
        return index;
    }

    public String toString() {
        return toString(StandardCharsets.UTF_8);
    }

    public String toString(Charset charset) {
        return new String(buffer, 0, index, charset);
    }

    public void writeBoolean(boolean value) {
        write(value ? (byte) 1 : 0);
    }

    public void writeShort(int value) {
        if (index + 2 >= buffer.length) ensureCapacity(2);
        buffer[index] = (byte) (value >> 8);
        buffer[index + 1] = (byte) value;
        index += 2;
    }

    public void writeShortLE(int value) {
        if (index + 2 >= buffer.length) ensureCapacity(2);
        buffer[index] = (byte) value;
        buffer[index + 1] = (byte) (value >> 8);
        index += 2;
    }

    public void writeChar(char value) {
        if (index + 2 >= buffer.length) ensureCapacity(2);
        buffer[index] = (byte) (value >> 8);
        buffer[index + 1] = (byte) value;
        index += 2;
    }

    public void writeCharLE(char value) {
        if (index + 2 >= buffer.length) ensureCapacity(2);
        buffer[index] = (byte) value;
        buffer[index + 1] = (byte) (value >> 8);
        index += 2;
    }

    public void writeInt(int value) {
        if (index + 4 >= buffer.length) ensureCapacity(4);
        buffer[index] = (byte) (value >> 24);
        buffer[index + 1] = (byte) (value >> 16);
        buffer[index + 2] = (byte) (value >> 8);
        buffer[index + 3] = (byte) value;
        index += 4;
    }

    public void writeIntLE(int value) {
        if (index + 4 >= buffer.length) ensureCapacity(4);
        buffer[index] = (byte) value;
        buffer[index + 1] = (byte) (value >> 8);
        buffer[index + 2] = (byte) (value >> 16);
        buffer[index + 3] = (byte) (value >> 24);
        index += 4;
    }

    public void writeLong(long value) {
        if (index + 8 >= buffer.length) ensureCapacity(8);
        buffer[index] = (byte) (value >> 56);
        buffer[index + 1] = (byte) (value >> 48);
        buffer[index + 2] = (byte) (value >> 40);
        buffer[index + 3] = (byte) (value >> 32);
        buffer[index + 4] = (byte) (value >> 24);
        buffer[index + 5] = (byte) (value >> 16);
        buffer[index + 6] = (byte) (value >> 8);
        buffer[index + 7] = (byte) value;
        index += 8;
    }

    public void writeLongLE(long value) {
        if (index + 8 >= buffer.length) ensureCapacity(8);
        buffer[index] = (byte) value;
        buffer[index + 1] = (byte) (value >> 8);
        buffer[index + 2] = (byte) (value >> 16);
        buffer[index + 3] = (byte) (value >> 24);
        buffer[index + 4] = (byte) (value >> 32);
        buffer[index + 5] = (byte) (value >> 40);
        buffer[index + 6] = (byte) (value >> 48);
        buffer[index + 7] = (byte) (value >> 56);
        index += 8;
    }

    public void writeFloat(float value) {
        writeInt(Float.floatToIntBits(value));
    }

    public void writeFloatLE(float value) {
        writeIntLE(Float.floatToIntBits(value));
    }

    public void writeDouble(double value) {
        writeLong(Double.doubleToLongBits(value));
    }

    public void writeDoubleLE(double value) {
        writeLongLE(Double.doubleToLongBits(value));
    }

    static public void writeInt(byte[] buffer, int i, int value) {
        buffer[i] = (byte) (value >> 24);
        buffer[i + 1] = (byte) (value >> 16);
        buffer[i + 2] = (byte) (value >> 8);
        buffer[i + 3] = (byte) value;
    }

    static public void writeInt(byte[] buffer, int value) {
        buffer[0] = (byte) (value >> 24);
        buffer[1] = (byte) (value >> 16);
        buffer[2] = (byte) (value >> 8);
        buffer[3] = (byte) value;
    }

    static public void writeIntLE(byte[] buffer, int i, int value) {
        buffer[i] = (byte) value;
        buffer[i + 1] = (byte) (value >> 8);
        buffer[i + 2] = (byte) (value >> 16);
        buffer[i + 3] = (byte) (value >> 24);
    }

    static public void writeIntLE(byte[] buffer, int value) {
        buffer[0] = (byte) value;
        buffer[1] = (byte) (value >> 8);
        buffer[2] = (byte) (value >> 16);
        buffer[3] = (byte) (value >> 24);
    }
}
