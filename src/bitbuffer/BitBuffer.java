package bitbuffer;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * A data-type similar to {@link ByteBuffer}, but reads/writes bits rather than {@code byte}s to
 * reduce bandwidth, increase throughput, and allow for optional compression.
 *
 * @author Jacob G.
 * @since February 24, 2018
 */
public final class BitBuffer {

    /**
     * The bit-mask used when writing/reading bits.
     */
    private static final long[] MASKS = new long[Long.SIZE];

    // Initialize the mask to its respective values.
    static {
        for (int i = 0; i < MASKS.length; i++) {
            MASKS[i] = BigInteger.TWO.pow(i).subtract(BigInteger.ONE).longValue();
        }
        MASKS[MASKS.length - 1] = -1L;
    }

    /**
     * The backing {@link ByteBuffer}.
     */
    private final ByteBuffer bytes;

    /**
     * The bit-index within {@code buffer}.
     */
    private int bit;

    /**
     * The "cache" used when writing and reading bits.
     */
    private long buffer;

    /**
     * A private constructor.
     *
     * @param bytes The backing {@link ByteBuffer}.
     */
    private BitBuffer(ByteBuffer bytes) {
        this.bytes = bytes;
    }

    /**
     * Allocates a new {@link BitBuffer} backed by a big-endian {@link ByteBuffer}.
     *
     * @param capacity The capacity of the {@link BitBuffer} in bytes.
     * @return This {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public static BitBuffer allocate(int capacity) {
        return new BitBuffer(ByteBuffer.allocate((capacity + 7) / 8 * 8));
    }

    /**
     * Allocates a new {@link BitBuffer} backed by a direct, big-endian {@link ByteBuffer}.
     * <p>
     * This should be used over {@link #allocate(int)} when the {@link BitBuffer} will be
     * used for I/O (files, networking, etc.).
     *
     * @param capacity The capacity of the {@link BitBuffer} in bytes.
     * @return This {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public static BitBuffer allocateDirect(int capacity) {
        return new BitBuffer(ByteBuffer.allocateDirect((capacity + 7) / 8 * 8));
    }

    /**
     * Appends {@code numBits} bits containing {@code value} to this {@link BitBuffer}.
     *
     * @param value   The value to append.
     * @param numBits The number of bits to contain {@code value}.
     * @return This {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer putBits(long value, int numBits) {
        int bitsWritten = Math.min(Long.SIZE - bit, numBits);
        buffer |= ((value & MASKS[bitsWritten]) << bit);
        if ((bit += bitsWritten) == Long.SIZE) {
            bytes.putLong(buffer);
            buffer = (value >> bitsWritten) & MASKS[bit = numBits - bitsWritten];
        }
        return this;
    }

    /**
     * Equivalent to {@link #putBits(long, int)}, but allows the user to enter
     * the maximum value that {@code value} can have instead of the amount of bits.
     *
     * @param value        The value to append.
     * @param maximumValue The maximum possible value of {@code value} (the lower the maximum value,
     *                     the fewer amount of bits needed to represent it, resulting in a better
     *                     compression ratio).
     * @return This {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer putValue(long value, int maximumValue) {
        return putBits(value, Long.SIZE - Long.numberOfLeadingZeros(maximumValue));
    }

    /**
     * Appends either {@code 1} bit or {@code 8} bits to this {@link BitBuffer}, depending
     * on the value of {@code compressed}.
     *
     * @param b          The {@code boolean} to append.
     * @param compressed Whether or not the {@code boolean} should be compressed.
     * @return This {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer putBoolean(boolean b, boolean compressed) {
        return compressed ? putBits(b ? 1 : 0, 1) : putByte(b ? 1 : 0);
    }

    /**
     * Appends {@code 8} bits to this {@link BitBuffer}.
     *
     * @param b The {@code byte} to append.
     * @return This {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer putByte(int b) {
        return putBits(b, Byte.SIZE);
    }

    /**
     * Appends {@code 32} bits to this {@link BitBuffer}.
     *
     * @param i The {@code int} to append.
     * @return This {@link BitBuffer} to allow for the
     * convenience of method-chaining.
     */
    public BitBuffer putInt(int i) {
        return putBits(i, Integer.SIZE);
    }

    /**
     * Appends {@code 64} bits to this {@link BitBuffer}.
     *
     * @param l The {@code long} to append.
     * @return This {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer putLong(long l) {
        return putBits(l, Long.SIZE);
    }

    /**
     * Appends {@code 16} bits to this {@link BitBuffer}.
     *
     * @param s The {@code short} to append.
     * @return This {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer putShort(int s) {
        return putBits(s, Short.SIZE);
    }

    /**
     * Flips this {@link BitBuffer}.  After a series of relative {@code put} operations,
     * flip the buffer to prepare for a series of relative {@code get} operations.
     *
     * @return This {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer flip() {
        if (bit != 0) {
            bytes.putLong(buffer);
            bit = 0;
        }
        buffer = bytes.flip().getLong();
        return this;
    }

    /**
     * Reads the next {@code numBits} bits and composes a {@code long}.
     *
     * @param numBits The number of bits to read.
     * @return A {@code long} value at the {@link BitBuffer}'s current position.
     */
    public long getBits(int numBits) {
        int bitsRead = Math.min(Long.SIZE - bit, numBits);
        long value = (buffer >> bit) & MASKS[bitsRead];
        if ((bit += bitsRead) == Long.SIZE) {
            if (!bytes.hasRemaining()) {
                return value;
            }
            buffer = bytes.getLong();
            if ((bit = numBits - bitsRead) != 0) {
                value |= (buffer & MASKS[bit]) << bitsRead;
            }
        }
        return value;
    }

    /**
     * Equivalent to {@link #getBits(int)}, but allows the user to enter
     * the maximum possible, expected value instead of the amount of bits.
     *
     * @param maximumValue The same value entered when calling {@link #putValue(long, int)}.
     * @return This {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public long getValue(long maximumValue) {
        return getBits(Long.SIZE - Long.numberOfLeadingZeros(maximumValue));
    }

    /**
     * Gets a (possibly compressed) {@code boolean} from this {@link BitBuffer}.
     *
     * @param compressed Whether or not the {@code boolean} to read is compressed.
     * @return A {@code boolean}.
     */
    public boolean getBoolean(boolean compressed) {
        return (compressed ? getBits(1) : getByte()) == 1;
    }

    /**
     * Reads the next {@code 8} bits and composes a {@code byte} from this {@link BitBuffer}.
     *
     * @return A {@code byte}.
     */
    public byte getByte() {
        return (byte) getBits(Byte.SIZE);
    }

    /**
     * Reads the next {@code 32} bits and composes an {@code int} from this {@link BitBuffer}.
     *
     * @return An {@code int}.
     */
    public int getInt() {
        return (int) getBits(Integer.SIZE);
    }

    /**
     * Reads the next {@code 64} bits and composes a {@code long} from this {@link BitBuffer}.
     *
     * @return A {@code long}.
     */
    public long getLong() {
        return getBits(Long.SIZE);
    }

    /**
     * Reads the next {@code 16} bits and composes a {@code short} from this {@link BitBuffer}.
     *
     * @return A {@code short}.
     */
    public short getShort() {
        return (short) getBits(Short.SIZE);
    }

    /**
     * Gets the backing {@link ByteBuffer} of this {@link BitBuffer}.
     *
     * @return A {@link ByteBuffer}.
     */
    public ByteBuffer toByteBuffer() {
        return bytes;
    }

}