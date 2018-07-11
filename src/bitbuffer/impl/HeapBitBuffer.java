package bitbuffer.impl;

import bitbuffer.BitBuffer;
import bitbuffer.Sign;
import java.io.Serializable;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * An implementation of {@link BitBuffer} that stores its
 * data within a {@link BitSet}.
 *
 * @author Jacob G.
 * @since January 4, 2018
 */
public class HeapBitBuffer implements BitBuffer, Serializable {

    /**
     * Essentially this {@link BitBuffer}'s reader
     * index.  Whenever {@code n} bits are read from this
     * {@link BitBuffer}, this value is incremented by
     * {@code n}.
     */
    protected int position;

    /**
     * Essentially this {@link BitBuffer}'s writer
     * index.  Whenever {@code n} bits are written to this
     * {@link BitBuffer}, this value is incremented by
     * {@code n}.
     */
    protected int limit;

    /**
     * The datatype that will hold the bits as they
     * are written.  At any time, this {@link BitSet}
     * can be converted to a {@code byte[]} with
     * {@link BitSet#toByteArray()} for use in a
     * network.
     */
    protected final BitSet bits;

    /**
     * Instantiates a new {@link BitBuffer} with a
     * default capacity of {@code 16}.
     */
    public HeapBitBuffer() {
        this(16);
    }

    /**
     * Instantiates a new {@link HeapBitBuffer} with
     * a specified capacity.
     *
     * @param capacity
     *      The capacity passed to the backing {@link BitSet}.
     */
    public HeapBitBuffer(int capacity) {
        bits = new BitSet(capacity);
    }

    /**
     * Instantiates a new {@link HeapBitBuffer} from
     * an existing {@link ByteBuffer}.
     *
     * @param buffer
     *      A {@link ByteBuffer} with order {@link java.nio.ByteOrder#LITTLE_ENDIAN}.
     */
    public HeapBitBuffer(ByteBuffer buffer) {
        bits = BitSet.valueOf(buffer);
        limit = buffer.limit() * Byte.SIZE;
    }

    @Override
    public BitBuffer putBits(long value, int numBits) {
        for (int i = 0; i < numBits; i++) {
            bits.set(limit++, (value & (1L << i)) != 0);
        }
        return this;
    }

    @Override
    public long getBits(int numBits) {
        if (limit - position < numBits) {
            throw new BufferUnderflowException();
        }

        long l = 0;

        for (int i = 0; i < numBits; i++) {
            if (bits.get(position++)) {
                l |= (1 << i);
            }
        }

        return l;
    }

    /**
     * Appends a {@code boolean} to this {@link BitBuffer}.
     *
     * @param b
     *      The {@code boolean} to append.
     * @param compressed
     *      Whether or not to compress this {@code boolean}.
     *      A single bit would be written to the backing
     *      {@link BitSet} rather than an entire {@code byte}.
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    public BitBuffer putBoolean(boolean b, boolean compressed) {
        bits.set(limit++, b);

        if (!compressed) {
            limit += (Byte.SIZE - 1);
        }

        return this;
    }

    @Override
    public BitBuffer putBits(long value, boolean compressed, int size, int maxSize, Sign sign) {
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

        if (numBits >= size - maxSize) {
            limit++;

            if (sign == Sign.EITHER) {
                bits.set(limit++, shouldNegate);
            }

            for (int i = 0; i < size - 1; i++) {
                bits.set(limit++, (value & (1L << i)) != 0);
            }
        } else {
            bits.set(limit++);

            if (sign == Sign.EITHER) {
                bits.set(limit++, shouldNegate);
            }

            int halfLength = numBits >> 1;

            for (int i = 0; i < maxSize; i++) {
                bits.set(limit++, (halfLength & (1 << i)) != 0);
            }

            for (int i = 0; i < numBits; i++) {
                bits.set(limit++, (value & (1L << i)) != 0);
            }

            if ((numBits & 1) == 0) {
                limit++;
            }
        }

        return this;
    }

    /**
     * A helper method to read {@code size} bits, either compressed
     * or uncompressed, from the backing {@link BitSet}.
     *
     * @param compressed
     *      Whether or not to read compressed bits.
     * @param size
     *      The amount of bits to read that holds the,
     *      possibly compressed, data.
     * @param sign
     *      TODO: Add documentation.
     * @return
     *      A {@link Number}.
     */
    @Override
    public Number readBits(boolean compressed, int size, Sign sign) {
        if (!compressed) {
            if (limit - position < size) {
                throw new BufferUnderflowException();
            }

            long value = 0;

            for (int index = 0; index < size; index++) {
                if (bits.get(position++)) {
                    value |= (1L << index);
                }
            }

            return value;
        }

        long value = 0;

        boolean shouldNegate;

        if (bits.get(position++)) {
            shouldNegate = sign == Sign.EITHER && bits.get(position++);

            int numBits = readBits(BitBuffer.log2(size) - 1).intValue();

            for (int index = 0; index < (numBits << 1) + 1; index++) {
                if (bits.get(position++)) {
                    value |= (1L << index);
                }
            }
        } else {
            shouldNegate = sign == Sign.EITHER && bits.get(position++);

            for (int index = 0; index < size - 1; index++) {
                if (bits.get(position++)) {
                    value |= (1L << index);
                }
            }
        }

        return shouldNegate || sign == Sign.NEGATIVE ? -value : value;
    }

    private Number readBits(int numBits) {
        if (limit - position < numBits) {
            throw new BufferUnderflowException();
        }

        long l = 0;

        for (int i = 0; i < numBits; i++) {
            if (bits.get(position++)) {
                l |= (1 << i);
            }
        }

        return l;
    }

    @Override
    public byte[] toByteArray() {
        return bits.toByteArray();
    }

    /**
     * Gets a {@code boolean} from this {@link BitBuffer}.
     *
     * @return
     *      A {@code boolean}.
     */
    public boolean getBoolean(boolean compressed) {
        if (position >= limit) {
            throw new BufferUnderflowException();
        }

        try {
            return bits.get(position++);
        } finally {
            if (!compressed) {
                position += (Byte.SIZE - 1);
            }
        }
    }

}
