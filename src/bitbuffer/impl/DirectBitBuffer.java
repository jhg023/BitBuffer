package bitbuffer.impl;

import bitbuffer.BitBuffer;
import bitbuffer.Sign;
import sun.misc.Unsafe;

public final class DirectBitBuffer implements BitBuffer {

    private static final Unsafe UNSAFE = Unsafe.getUnsafe();

    private final long address;

    private long readAddress;

    private int readBitIndex;

    private long writeAddress;

    private int writeBitIndex;

    public DirectBitBuffer() {
        this(16);
    }

    public DirectBitBuffer(int capacity) {
        address = readAddress = writeAddress = UNSAFE.allocateMemory(capacity);
    }

    @Override
    public BitBuffer putBits(long value, boolean compressed, int size, Sign sign) {
        if (!compressed) {
            /*
             * If the last byte has not been fully written to,
             * finish writing to it.
             */
            if (writeBitIndex != 0) {
                byte lastByte = UNSAFE.getByte(writeAddress - 1);

                lastByte |= (value & (Byte.SIZE - writeBitIndex) << writeBitIndex);

                UNSAFE.putByte(writeAddress - 1, lastByte);
            }

            UNSAFE.putLong(writeAddress, value >>> writeBitIndex);

            int numBits = Long.SIZE - Long.numberOfLeadingZeros(value) - writeBitIndex;

            writeAddress += numBits / Byte.SIZE;

            writeBitIndex = numBits % Byte.SIZE;
            return this;
        }

        return this;
    }

    @Override
    public Number readBits(boolean compressed, int size, Sign sign) {
        return null;
    }

    @Override
    public byte[] toByteArray() {
        return new byte[0];
    }

}
