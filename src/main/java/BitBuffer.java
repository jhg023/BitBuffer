package main.java;

import java.io.Serializable;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * A datatype similar to {@link ByteBuffer}, but
 * reads/writes bits rather than {@code byte}s to
 * reduce bandwidth and increase throughput.
 *
 * @author Jacob G.
 * @since January 4, 2018
 */
public final class BitBuffer implements Serializable {

    private static final int MAX_SHORT_BITS = log2(Short.SIZE) - 1;

    private static final int MAX_INTEGER_BITS = log2(Integer.SIZE) - 1;

    private static final int MAX_LONG_BITS = log2(Long.SIZE) - 1;

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
     * Instantiates a new {@link BitBuffer}.
     */
    public BitBuffer() {
        bits = new BitSet();
    }

    /**
     * Instantiates a new {@link BitBuffer} from
     * an existing {@link ByteBuffer}.
     *
     * @param buffer
     *      A {@link ByteBuffer}.
     */
    public BitBuffer(ByteBuffer buffer) {
        bits = BitSet.valueOf(buffer);
        limit = buffer.limit(); // TODO: Verify that this is correct.
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
            limit += 7;
        }

        return this;
    }

    /**
     * Appends a {@code byte} to this {@link BitBuffer}.
     * <p>
     * No option to compress the {@code byte} is provided,
     * as attempting to compress it will most likely lead
     * to more than 8 bits being written to the backing
     * {@link BitSet}.
     *
     * @param b
     *      The {@code byte} to append.
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    public BitBuffer putByte(int b) {
        b = (byte) b;

        for (int index = 0; index < Byte.SIZE; index++) {
            bits.set(limit++, (b & (1 << index)) != 0);
        }

        return this;
    }

    /**
     * Appends a {@code double} to this {@link BitBuffer}.
     *
     * @param d
     *      The {@code double} to append.
     * @param compressed
     *      Whether or not to compress this {@code double}.
     *      A currently unnamed algorithm will write the
     *      minimum amount of bits to the backing
     *      {@link BitSet} rather than an entire {@code double}.
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    public BitBuffer putDouble(double d, boolean compressed) {
        return putLong(Double.doubleToLongBits(d), compressed);
    }

    /**
     * Appends an {@code int} to this {@link BitBuffer}.
     *
     * @param i
     *      The {@code int} to append.
     * @param compressed
     *      Whether or not to compress this {@code int}.
     *      A currently unnamed algorithm will write the
     *      minimum amount of bits to the backing
     *      {@link BitSet} rather than an entire {@code int}.
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    public BitBuffer putInt(int i, boolean compressed) {
        return putBits(compressed, i, Integer.SIZE, MAX_INTEGER_BITS);
    }

    /**
     * Appends a {@code long} to this {@link BitBuffer}.
     *
     * @param l
     *      The {@code long} to append.
     * @param compressed
     *      Whether or not to compress this {@code long}.
     *      A currently unnamed algorithm will write the
     *      minimum amount of bits to the backing
     *      {@link BitSet} rather than an entire {@code long}.
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    public BitBuffer putLong(long l, boolean compressed) {
        return putBits(compressed, l, Long.SIZE, MAX_LONG_BITS);
    }

    /**
     * Appends a {@code short} to this {@link BitBuffer}.
     *
     * @param s
     *      The {@code short} to append.
     * @param compressed
     *      Whether or not to compress this {@code short}.
     *      A currently unnamed algorithm will write the
     *      minimum amount of bits to the backing
     *      {@link BitSet} rather than an entire {@code short}.
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    public BitBuffer putShort(int s, boolean compressed) {
        return putBits(compressed, s, Short.SIZE, MAX_SHORT_BITS);
    }

    private BitBuffer putBits(final boolean compressed, final long value, final int size, final int maxBits) {
        if (!compressed) {
            for (int i = 0; i < Short.SIZE; i++) {
                bits.set(limit++, (value & (1L << i)) != 0);
            }

            return this;
        }

        int numBits = Long.SIZE - Long.numberOfLeadingZeros(value);

        if (numBits >= size) {
            limit++;

            for (int i = 0; i < size; i++) {
                bits.set(limit++, (value & (1L << i)) != 0);
            }
        } else {
            bits.set(limit++);

            int halfLength = (int) Math.ceil((numBits - 1) / 2.0);

            for (int i = 0; i < maxBits; i++) {
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

    private Number readBits(final boolean compressed, final int size, final int maxBits) {
        if (!compressed) {
            if (limit - position < size) {
                throw new BufferUnderflowException();
            }

            long l = 0;

            for (int index = 0; index < size; index++) {
                if (bits.get(position++)) {
                    l |= (1L << index);
                }
            }

            return l;
        }

        long l = 0;

        if (bits.get(position++)) {
            int numBits = readBits(maxBits).intValue();

            for (int index = 0; index < numBits * 2 + 1; index++) {
                if (bits.get(position++)) {
                    l |= (1L << index);
                }
            }
        } else {
            for (int index = 0; index < size; index++) {
                if (bits.get(position++)) {
                    l |= (1L << index);
                }
            }
        }

        return l;
    }

    public Number readBits(int numBits) {
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

    /**
     * Gets a {@code byte} from this {@link BitBuffer}.
     *
     * @return
     *      A {@code byte}.
     */
    public byte getByte() {
        if (limit - position < Byte.SIZE) {
            throw new BufferUnderflowException();
        }

        byte b = 0;

        for (int i = 0; i < Byte.SIZE; i++) {
            if (bits.get(position++)) {
                b |= (1 << i);
            }
        }

        return b;
    }

    /**
     * Gets a {@code double} from this {@link BitBuffer}.
     *
     * @param compressed
     *      Whether or not the {@code double} being read
     *      was compressed when it was written. Note that
     *      no {@link BufferUnderflowException} checks
     *      are made when attempting to read compressed
     *      data, so calling this method at the wrong time
     *      may return an incorrect value.
     * @return
     *      A {@code double}.
     */
    public double getDouble(boolean compressed) {
        return Double.longBitsToDouble(getLong(compressed));
    }

    /**
     * Gets an {@code int} from this {@link BitBuffer}.
     *
     * @param compressed
     *      Whether or not the {@code int} being read
     *      was compressed when it was written. Note that
     *      no {@link BufferUnderflowException} checks
     *      are made when attempting to read compressed
     *      data, so calling this method at the wrong time
     *      may return an incorrect value.
     * @return
     *      An {@code int}.
     */
    public int getInt(boolean compressed) {
        return readBits(compressed, Integer.SIZE, MAX_INTEGER_BITS).intValue();
    }

    /**
     * Gets a {@code long} from this {@link BitBuffer}.
     *
     * @param compressed
     *      Whether or not the {@code long} being read
     *      was compressed when it was written. Note that
     *      no {@link BufferUnderflowException} checks
     *      are made when attempting to read compressed
     *      data, so calling this method at the wrong time
     *      may return an incorrect value.
     * @return
     *      A {@code long}.
     */
    public long getLong(boolean compressed) {
        return readBits(compressed, Long.SIZE, MAX_LONG_BITS).longValue();
    }

    /**
     * Gets a {@code short} from this {@link BitBuffer}.
     *
     * @param compressed
     *      Whether or not the {@code short} being read
     *      was compressed when it was written. Note that
     *      no {@link BufferUnderflowException} checks
     *      are made when attempting to read compressed
     *      data, so calling this method at the wrong time
     *      may return an incorrect value.
     * @return
     *      A {@code short}.
     */
    public short getShort(boolean compressed) {
        return readBits(compressed, Short.SIZE, MAX_SHORT_BITS).shortValue();
    }

    /**
     * Returns the base 2 logarithm of a {@code int} value.
     *
     * @param a
     *      A value.
     * @return
     *      The base 2 logarithm of {@code a}.
     */
    private static int log2(int a) {
        return 31 - Integer.numberOfLeadingZeros(a);
    }

}
