package bitbuffer;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.IntFunction;

/**
 * A data type similar to {@link ByteBuffer}, but can read/write bits as well as {@code byte}s to reduce bandwidth,
 * increase throughput, and allow for optional compression.
 *
 * @author Jacob G.
 * @version February 24, 2018
 */
public final class BitBuffer {

    /**
     * The bitIndex-mask used when writing/reading bits.
     */
    private static final long[] MASKS = new long[Long.SIZE + 1];

    /*
     * Initialize the mask to its respective values.
     */
    static {
        for (int i = 0; i < MASKS.length; i++) {
            MASKS[i] = BigInteger.TWO.pow(i).subtract(BigInteger.ONE).longValue();
        }
    }

    /**
     * The backing {@link ByteBuffer}.
     */
    public final ByteBuffer buffer;

    /**
     * The bitIndex-index within {@code cache}.
     */
    public int bitIndex;

    /**
     * The <i>cache</i> used when writing and reading bits.
     */
    public long cache;

    /**
     * A private constructor.
     *
     * @param buffer the backing {@link ByteBuffer}.
     */
    private BitBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }
    
    /**
     * A helper method to eliminate duplicate code when allocating a {@link BitBuffer} with a specified capacity.
     *
     * @param capacity the capacity of the {@link BitBuffer} in {@code byte}s.
     * @param function a function that accepts the specified capacity and returns an allocated {@link ByteBuffer}.
     * @return a {@link BitBuffer} allocated with the specified capacity.
     */
    private static BitBuffer allocate(int capacity, IntFunction<ByteBuffer> function) {
        return new BitBuffer(function.apply((capacity + 7) / 8 * 8));
    }
    
    /**
     * Allocates a new {@link BitBuffer} backed by a {@link ByteBuffer}.
     *
     * @param capacity the capacity of the {@link BitBuffer} in {@code byte}s.
     * @return this {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public static BitBuffer allocate(int capacity) {
        return allocate(capacity, ByteBuffer::allocate);
    }

    /**
     * Allocates a new {@link BitBuffer} backed by a <strong>direct</strong> {@link ByteBuffer}.
     * <br><br>
     * This should be used over {@link #allocate(int)} when the {@link BitBuffer} will be used for I/O (files,
     * networking, etc.).
     *
     * @param capacity the capacity of the {@link BitBuffer} in {@code byte}s.
     * @return this {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public static BitBuffer allocateDirect(int capacity) {
        return allocate(capacity, ByteBuffer::allocateDirect);
    }

    /**
     * Writes {@code value} to this {@link BitBuffer} using {@code numBits} bits.
     *
     * @param value   the value to write.
     * @param numBits the amount of bits to use when writing {@code value}.
     * @return this {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    private BitBuffer putBits(long value, int numBits) {
        int totalBits = bitIndex + numBits;
    
        // If the value that we're writing is too large to be placed entirely in the cache, then we need to place as
        // much as we can in the cache (the most significant bits), flush the cache to the backing ByteBuffer, and
        // place the rest in the cache.
        if (totalBits > Long.SIZE) {
            int difference = Long.SIZE - bitIndex;
            cache |= (value & (MASKS[difference] << bitIndex));
            buffer.putLong(cache);
            cache = value & MASKS[bitIndex = (Long.SIZE - difference)];
        } else {
            cache |= value << bitIndex;
            bitIndex += numBits;
        }
        
        return this;
    }

    /**
     * Writes either {@link Byte#BYTES} or {@link Byte#SIZE} bits to this {@link BitBuffer}, depending on the value of
     * {@code compressed}.
     *
     * @param b          the {@code boolean} to write.
     * @param compressed whether or not the {@code boolean} should be compressed.
     * @return this {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer putBoolean(boolean b, boolean compressed) {
        return compressed ? putBits(b ? 1 : 0, 1) : putByte(b ? 1 : 0);
    }

    /**
     * Writes a value to this {@link BitBuffer} using {@link Byte#SIZE} bits.
     *
     * @param b the {@code byte} to write as an {@code int} for ease-of-use, but internally down-casted to a {@code byte}.
     * @return this {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer putByte(int b) {
        return putBits((byte) b, Byte.SIZE);
    }
    
    /**
     * Writes a value with {@link ByteOrder#BIG_ENDIAN} order to this {@link BitBuffer} using {@link Character#SIZE} bits.
     *
     * @param c the {@code char} to write.
     * @return this {@link BitBuffer} to allow for the convenience of method-chaining.
     * @see #putChar(char, ByteOrder)
     */
    public BitBuffer putChar(char c) {
        return putChar(c, ByteOrder.BIG_ENDIAN);
    }
    
    /**
     * Writes a value with the specified {@link ByteOrder} to this {@link BitBuffer} using {@link Character#SIZE} bits.
     *
     * @param c the {@code char} to write.
     * @return this {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer putChar(char c, ByteOrder order) {
        return putBits(order == ByteOrder.LITTLE_ENDIAN ? Character.reverseBytes(c) : c, Character.SIZE);
    }
    
    /**
     * Writes a value with {@link ByteOrder#BIG_ENDIAN} order to this {@link BitBuffer} using {@link Integer#SIZE} bits.
     *
     * @param i the {@code int} to write.
     * @return this {@link BitBuffer} to allow for the convenience of method-chaining.
     * @see #putInt(int, ByteOrder)
     */
    public BitBuffer putInt(int i) {
        return putInt(i, ByteOrder.BIG_ENDIAN);
    }
    
    /**
     * Writes a value with the specified {@link ByteOrder} to this {@link BitBuffer} using {@link Integer#SIZE} bits.
     *
     * @param i the {@code int} to write.
     * @return this {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer putInt(int i, ByteOrder order) {
        return putBits(order == ByteOrder.LITTLE_ENDIAN ? Integer.reverseBytes(i) : i, Integer.SIZE);
    }
    
    /**
     * Writes a value with {@link ByteOrder#BIG_ENDIAN} order to this {@link BitBuffer} using {@link Long#SIZE} bits.
     *
     * @param l the {@code int} to write.
     * @return this {@link BitBuffer} to allow for the convenience of method-chaining.
     * @see #putLong(long, ByteOrder)
     */
    public BitBuffer putLong(long l) {
        return putLong(l, ByteOrder.BIG_ENDIAN);
    }
    
    /**
     * Writes a value with the specified {@link ByteOrder} to this {@link BitBuffer} using {@link Long#SIZE} bits.
     *
     * @param l the {@code long} to write.
     * @return this {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer putLong(long l, ByteOrder order) {
        return putBits(order == ByteOrder.LITTLE_ENDIAN ? Long.reverseBytes(l) : l, Long.SIZE);
    }
    
    /**
     * Writes a value with {@link ByteOrder#BIG_ENDIAN} order to this {@link BitBuffer} using {@link Short#SIZE} bits.
     *
     * @param s the {@code short} to write as an {@code int} for ease-of-use, but internally down-casted to a {@code short}.
     * @return this {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer putShort(int s) {
        return putShort(s, ByteOrder.BIG_ENDIAN);
    }
    
    /**
     * Writes a value with the specified {@link ByteOrder} to this {@link BitBuffer} using {@link Short#SIZE} bits.
     *
     * @param s the {@code short} to write as an {@code int} for ease-of-use, but internally down-casted to a {@code short}.
     * @return this {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer putShort(int s, ByteOrder order) {
        var value = (short) s;
        return putBits(order == ByteOrder.LITTLE_ENDIAN ? Short.reverseBytes(value) : value, Short.SIZE);
    }

    /**
     * After a series of relative {@code put} operations, flip the <i>cache</i> to prepare for a series of relative
     * {@code get} operations.
     *
     * @return this {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer flip() {
        
        return this;
    }

    /**
     * Reads the next {@code numBits} bits and composes a {@code long} that can be down-casted to other primitive types.
     *
     * @param numBits the amount of bits to read.
     * @return a {@code long} value at the {@link BitBuffer}'s current position.
     */
    private long getBits(int numBits) {
        return 0;
    }

    /**
     * Reads {@link Byte#BYTES} or {@link Byte#SIZE} bits (depending on the value of {@code compressed}) from this
     * {@link BitBuffer} and composes a {@code boolean}.
     *
     * @param compressed whether or not the {@code boolean} to read is compressed.
     * @return {@code true} if the value read is not equal to {@code 0}, otherwise {@code false}.
     */
    public boolean getBoolean(boolean compressed) {
        return (compressed ? getBits(1) : getByte()) != 0;
    }

    /**
     * Reads {@link Byte#SIZE} bits from this {@link BitBuffer} and composes a {@code byte}.
     *
     * @return A {@code byte}.
     */
    public byte getByte() {
        return (byte) getBits(Byte.SIZE);
    }
    
    /**
     * Reads {@link Character#SIZE} bits from this {@link BitBuffer} and composes a {@code char} with
     * {@link ByteOrder#BIG_ENDIAN} order.
     *
     * @return A {@code char}.
     * @see #getChar(ByteOrder)
     */
    public char getChar() {
        return getChar(ByteOrder.BIG_ENDIAN);
    }
    
    /**
     * Reads {@link Character#SIZE} bits from this {@link BitBuffer} and composes a {@code char} with the specified
     * {@link ByteOrder}.
     *
     * @return A {@code char}.
     */
    public char getChar(ByteOrder order) {
        var value = (char) getBits(Integer.SIZE);
        return order == ByteOrder.LITTLE_ENDIAN ? Character.reverseBytes(value) : value;
    }
    
    /**
     * Reads {@link Integer#SIZE} bits from this {@link BitBuffer} and composes an {@code int} with
     * {@link ByteOrder#BIG_ENDIAN} order.
     *
     * @return An {@code int}.
     * @see #getInt(ByteOrder)
     */
    public int getInt() {
        return getInt(ByteOrder.BIG_ENDIAN);
    }
    
    /**
     * Reads {@link Integer#SIZE} bits from this {@link BitBuffer} and composes an {@code int} with the specified
     * {@link ByteOrder}.
     *
     * @return An {@code int}.
     */
    public int getInt(ByteOrder order) {
        var value = (int) getBits(Integer.SIZE);
        return order == ByteOrder.LITTLE_ENDIAN ? Integer.reverseBytes(value) : value;
    }
    
    /**
     * Reads {@link Long#SIZE} bits from this {@link BitBuffer} and composes an {@code int} with
     * {@link ByteOrder#BIG_ENDIAN} order.
     *
     * @return A {@code long}.
     * @see #getLong(ByteOrder)
     */
    public long getLong() {
        return getLong(ByteOrder.BIG_ENDIAN);
    }
    
    /**
     * Reads {@link Long#SIZE} bits from this {@link BitBuffer} and composes a {@code long} with the specified
     * {@link ByteOrder}.
     *
     * @return A {@code long}.
     */
    public long getLong(ByteOrder order) {
        var value = getBits(Long.SIZE);
        return order == ByteOrder.LITTLE_ENDIAN ? Long.reverseBytes(value) : value;
    }
    
    /**
     * Reads {@link Short#SIZE} bits from this {@link BitBuffer} and composes a {@code short} with
     * {@link ByteOrder#BIG_ENDIAN} order.
     *
     * @return A {@code short}.
     * @see #getShort(ByteOrder)
     */
    public short getShort() {
        return getShort(ByteOrder.BIG_ENDIAN);
    }
    
    /**
     * Reads {@link Short#SIZE} bits from this {@link BitBuffer} and composes a {@code short} with the specified
     * {@link ByteOrder}.
     *
     * @return A {@code short}.
     */
    public short getShort(ByteOrder order) {
        var value = (short) getBits(Short.SIZE);
        return order == ByteOrder.LITTLE_ENDIAN ? Short.reverseBytes(value) : value;
    }
    
    /**
     * Gets the capacity of the backing {@link ByteBuffer}.
     *
     * @return the capacity of the backing buffer in {@code byte}s.
     */
    public int capacity() {
        return buffer.capacity();
    }
    
    /**
     * Compacts the backing {@link ByteBuffer}.
     */
    public void compact() {
        buffer.compact();
    }
    
    /**
     * Gets the backing {@link ByteBuffer} of this {@link BitBuffer}.
     * <br><br>
     * Modifying this {@link ByteBuffer} in any way <strong>will</strong> de-synchronize it from the {@link BitBuffer}
     * that encompasses it.
     *
     * @return A {@link ByteBuffer}.
     */
    public ByteBuffer toByteBuffer() {
        if (bitIndex != 0) {
            buffer.putLong(cache);
        }
        
        return buffer.flip();
    }

}