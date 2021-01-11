package com.simibubi.create.foundation.utility.render;

import net.minecraft.client.renderer.GLAllocation;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

import java.nio.*;

public class SafeDirectBuffer implements AutoCloseable {

    private ByteBuffer wrapped;

    public SafeDirectBuffer(int capacity) {
        this.wrapped = GLAllocation.createDirectByteBuffer(capacity);
    }

    public void close() throws Exception {
        if (wrapped instanceof DirectBuffer) {
            Cleaner cleaner = ((DirectBuffer) wrapped).cleaner();
            if (!cleaner.isEnqueued()) {
                cleaner.clean();
                cleaner.enqueue();
            }
        }
    }

    /**
     * Only use this function to pass information to OpenGL.
     */
    @Deprecated
    public ByteBuffer getBacking() {
        return wrapped;
    }

    public void order(ByteOrder bo) {
        wrapped.order(bo);
    }

    public void limit(int limit) {
        wrapped.limit(limit);
    }

    public void rewind() {
        wrapped.rewind();
    }

    public byte get() {
        return wrapped.get();
    }

    public ByteBuffer put(byte b) {
        return wrapped.put(b);
    }

    public byte get(int index) {
        return wrapped.get();
    }

    public ByteBuffer put(int index, byte b) {
        return wrapped.put(index, b);
    }

    public ByteBuffer compact() {
        return wrapped.compact();
    }

    public boolean isReadOnly() {
        return wrapped.isReadOnly();
    }

    public boolean isDirect() {
        return wrapped.isDirect();
    }

    public char getChar() {
        return wrapped.getChar();
    }

    public ByteBuffer putChar(char value) {
        return wrapped.putChar(value);
    }

    public char getChar(int index) {
        return wrapped.getChar(index);
    }

    public ByteBuffer putChar(int index, char value) {
        return wrapped.putChar(index, value);
    }

    public short getShort() {
        return wrapped.getShort();
    }

    public ByteBuffer putShort(short value) {
        return wrapped.putShort(value);
    }

    public short getShort(int index) {
        return wrapped.getShort(index);
    }

    public ByteBuffer putShort(int index, short value) {
        return wrapped.putShort(index, value);
    }

    public int getInt() {
        return wrapped.getInt();
    }

    public ByteBuffer putInt(int value) {
        return wrapped.putInt(value);
    }

    public int getInt(int index) {
        return wrapped.getInt(index);
    }

    public ByteBuffer putInt(int index, int value) {
        return wrapped.putInt(index, value);
    }

    public long getLong() {
        return wrapped.getLong();
    }

    public ByteBuffer putLong(long value) {
        return wrapped.putLong(value);
    }

    public long getLong(int index) {
        return wrapped.getLong(index);
    }

    public ByteBuffer putLong(int index, long value) {
        return wrapped.putLong(index, value);
    }

    public float getFloat() {
        return wrapped.getFloat();
    }

    public ByteBuffer putFloat(float value) {
        return wrapped.putFloat(value);
    }

    public float getFloat(int index) {
        return wrapped.getFloat(index);
    }

    public ByteBuffer putFloat(int index, float value) {
        return wrapped.putFloat(index, value);
    }

    public double getDouble() {
        return wrapped.getDouble();
    }

    public ByteBuffer putDouble(double value) {
        return wrapped.putDouble(value);
    }

    public double getDouble(int index) {
        return wrapped.getDouble(index);
    }

    public ByteBuffer putDouble(int index, double value) {
        return wrapped.putDouble(index, value);
    }
}
