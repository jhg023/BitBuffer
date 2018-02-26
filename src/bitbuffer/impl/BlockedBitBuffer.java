package bitbuffer.impl;

import bitbuffer.BitBuffer;

import java.nio.BufferUnderflowException;
import java.util.BitSet;

public final class BlockedBitBuffer implements BitBuffer {

    /**
     * Essentially this {@link BitBuffer}'s reader
     * index.  Whenever {@code n} bits are read from this
     * {@link BitBuffer}, this value is incremented by
     * {@code n}.
     */
    private int position;

    /**
     * Essentially this {@link BitBuffer}'s writer
     * index.  Whenever {@code n} bits are written to this
     * {@link BitBuffer}, this value is incremented by
     * {@code n}.
     */
    private int limit;

    /**
     * The datatype that will hold the bits as they
     * are written.  At any time, this {@link BitSet}
     * can be converted to a {@code byte[]} with
     * {@link BitSet#toByteArray()} for use in a
     * network.
     */
    private final BitSet bits;

    /**
     * Instantiates a new {@link BitBuffer} with a
     * default capacity of {@code 16}.
     */
    public BlockedBitBuffer() {
        this(16);
    }

    /**
     * Instantiates a new {@link HeapBitBuffer} with
     * a specified capacity.
     *
     * @param capacity
     *      The capacity passed to the backing {@link BitSet}.
     */
    public BlockedBitBuffer(int capacity) {
        bits = new BitSet(capacity);
    }

    @Override
    public BitBuffer putBits(long value, boolean compressed, int size, int maxBits) {
        if (!compressed) {
            for (int i = 0; i < size; i++) {
                bits.set(limit++, (value & (1L << i)) != 0);
            }

            return this;
        }

        boolean negated = value < 0;

        if (negated) {
            value = -value;
        }

        int numBits = Long.SIZE - Long.numberOfLeadingZeros(value);

        if (numBits >= size - maxBits) {
            limit++;

            bits.set(limit++, negated);

            for (int i = 0; i < size - 1; i++) {
                bits.set(limit++, (value & (1L << i)) != 0);
            }

            return this;
        }

        bits.set(limit++);

        int numBlocks = numBits >>> 2;

        // Write sign bit
        bits.set(limit++, negated);

        // Write number of 4-bit blocks.
        for (int i = 0; i < 3; i++) {
            bits.set(limit++, (numBlocks & (1L << i)) != 0);
        }

        // Write 4-bit blocks.
        for (int i = 0; i < numBlocks + 1; i++) {
            bits.set(limit++, (value & (1L << (i * 4))) != 0);
            bits.set(limit++, (value & (1L << (i * 4 + 1))) != 0);
            bits.set(limit++, (value & (1L << (i * 4 + 2))) != 0);
            bits.set(limit++, (value & (1L << (i * 4 + 3))) != 0);
        }

        return this;
    }

    @Override
    public Number readBits(boolean compressed, int size, int maxBits) {
        long value = 0;

        // If the number is too big to be compressed...
        if (!bits.get(position++)) {
            boolean shouldNegate = bits.get(position++);

            long number = readBits(31).longValue();

            if (shouldNegate) {
                number = -number;
            }

            return number;
        }

        boolean shouldNegate = bits.get(position++);

        int numBlocks = readBits(3).intValue();

        for (int index = 0; index < numBlocks + 1; index++) {
            if (bits.get(position++)) {
                value |= (1L << (index * 4));
            }

            if (bits.get(position++)) {
                value |= (1L << (index * 4 + 1));
            }

            if (bits.get(position++)) {
                value |= (1L << (index * 4 + 2));
            }

            if (bits.get(position++)) {
                value |= (1L << (index * 4 + 3));
            }
        }

        if (shouldNegate) {
            value = -value;
        }

        return value;
    }

    @Override
    public byte[] toByteArray() {
        return bits.toByteArray();
    }

    private Number readBits(int numBits) {
        if (limit - position < numBits) {
            throw new BufferUnderflowException();
        }

        long l = 0;

        for (int i = 0; i < numBits; i++) {
            if (bits.get(position++)) {
                l |= (1L << i);
            }
        }

        return l;
    }

}
