package bitbuffer.impl;

import bitbuffer.BitBuffer;
import bitbuffer.Sign;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class DirectBitBuffer implements BitBuffer {

    private static final long[] MASKS = new long[Long.SIZE];

    static {
        for (int i = 0; i < MASKS.length; i++) {
            MASKS[i] = (long) (Math.pow(2, i) - 1);
        }
    }

    private final ByteBuffer bytes;

    /**
     * The index of the next bit to begin writing to, which is located in the last byte that
     * was previously written to, or the byte located at {@code address} if nothing has been
     * written yet.
     */
    private int bit;

    private long buffer;

    public static void main(String[] args) {
        int n = 2;

        DirectBitBuffer buffer = new DirectBitBuffer(ByteBuffer.allocate(n * 8));

        for (int i = 0; i < n; i++) {
            long number = ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
            System.out.println(number);
            buffer.putBits(number, 64);
            System.out.println(Arrays.toString(buffer.toByteArray()));
        }

        buffer.bytes.flip();

        for (int i = 0; i < n; i++) {
            System.out.println("Bits: " + buffer.getBits(64));
        }
    }

    public DirectBitBuffer(ByteBuffer bytes) {
        Objects.requireNonNull(bytes);
        this.bytes = bytes;
    }

    @Override
    public void putBits(long value, int numBits) {
        if (bit == Long.SIZE) {
            bytes.putLong(buffer);
            buffer = value;
            bit = numBits;
        } else {
            int bitsToWrite = Math.min(numBits, Long.SIZE - bit);
            buffer |= value << bit;
            if ((bit += bitsToWrite) > Long.SIZE) {
                bytes.putLong(buffer);
                buffer = value >>> bitsToWrite;
                bit -= Long.SIZE - numBits - bitsToWrite;
            }
        }
    }

    @Override
    public long getBits(int numBits) {
        int bitsToRead = Math.min(numBits, bit);
        int oldNumBits = numBits;
        numBits -= bitsToRead;
        long value = buffer << numBits & MASKS[oldNumBits - 1];
        if ((bit -= bitsToRead) < 0) {
            buffer = bytes.getLong();
            value |= buffer & MASKS[numBits];
            bit += Long.SIZE;
        }
        return value;
    }

    @Override
    public BitBuffer putBits(long value, boolean compressed, int size, int maxBits, Sign sign) {
        /*int numBits;

        if (compressed) {
            numBits = size;
        } else {
            numBits = size;
        }

        // If the current byte to write to isn't empty and isn't full...
        while (numBits > 0) {
            int remainingBits = Byte.SIZE - writeBitIndex;

            bytes[writeIndex++] |= ((value & MASKS[remainingBits - 1]) << writeBitIndex);

            numBits -= remainingBits;
            value >>>= remainingBits;

            writeBitIndex = (writeBitIndex + remainingBits) % Byte.SIZE;
        }*/

        return this;
    }

    @Override
    public Number readBits(boolean compressed, int size, Sign sign) {
        return null;
    }

    @Override
    public byte[] toByteArray() {
        return Arrays.copyOf(bytes.array(), bytes.capacity());
    }

}
