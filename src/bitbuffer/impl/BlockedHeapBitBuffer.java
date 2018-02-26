package bitbuffer.impl;

import bitbuffer.BitBuffer;
import bitbuffer.Sign;

import java.nio.BufferUnderflowException;
import java.util.BitSet;

/**
 * An implementation of {@link BitBuffer} that stores its
 * data within a {@link BitSet} and separates its data into
 * blocks of {@code 4} bits.
 *
 * @author Jacob G.
 * @since February 25, 2018
 */
public class BlockedHeapBitBuffer extends HeapBitBuffer {

    /**
     * Instantiates a new {@link BitBuffer} with a
     * default capacity of {@code 16}.
     */
    public BlockedHeapBitBuffer() {
        super(16);
    }

    /**
     * Instantiates a new {@link HeapBitBuffer} with
     * a specified capacity.
     *
     * @param capacity
     *      The capacity passed to the backing {@link BitSet}.
     */
    public BlockedHeapBitBuffer(int capacity) {
        super(capacity);
    }

    @Override
    public BitBuffer putBits(long value, boolean compressed, int size, Sign sign) {
        if (!compressed) {
            for (int i = 0; i < size; i++) {
                bits.set(limit++, (value & (1L << i)) != 0);
            }

            return this;
        }

        boolean shouldNegate = value < 0;

        // If the value is negative, negate it.
        if (shouldNegate) {
            value = -value;
        }

        int numBits = Long.SIZE - Long.numberOfLeadingZeros(value);

        if (numBits >= size - BitBuffer.log2(size) - 1) {
            limit++;

            if (sign == Sign.EITHER) {
                bits.set(limit++, shouldNegate);
            }

            for (int i = 0; i < size - 1; i++) {
                bits.set(limit++, (value & (1L << i)) != 0);
            }

            return this;
        }

        bits.set(limit++);

        int numBlocks = numBits >>> 2;

        // Write sign bit
        if (sign == Sign.EITHER) {
            bits.set(limit++, shouldNegate);
        }

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
    public Number readBits(boolean compressed, int size, Sign sign) {
        long value = 0;

        // If the number is too big to be compressed...
        if (!bits.get(position++)) {
            boolean shouldNegate = sign == Sign.EITHER && bits.get(position++);

            long number = readBits(size - 1).longValue();

            return shouldNegate || sign == Sign.NEGATIVE ? -number : number;
        }

        boolean shouldNegate = sign == Sign.EITHER && bits.get(position++);

        int numBlocks = readBits(BitBuffer.log2(size) - 2).intValue();

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

        return shouldNegate || sign == Sign.NEGATIVE ? -value : value;
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
