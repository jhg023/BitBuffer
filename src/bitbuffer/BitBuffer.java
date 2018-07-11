package bitbuffer;

import java.nio.ByteBuffer;

/**
 * A data-type similar to {@link ByteBuffer}, but reads/writes bits rather than {@code byte}s to
 * reduce bandwidth, increase throughput, and allow for optional compression.
 *
 * @author Jacob G.
 * @since February 24, 2018
 */
public abstract class BitBuffer {
	
	public enum Sign {
		POSITIVE, NEGATIVE, EITHER
	}
	
    /**
     * The maximum number of bits required to encode the bit-length
     * of a {@code short}.
     */
    private int MAX_SHORT_BITS = log2(Short.SIZE) - 1;

    /**
     * The maximum number of bits required to encode the bit-length
     * of an {@code int}.
     */
    private int MAX_INT_BITS = log2(Integer.SIZE) - 1;

    /**
     * The maximum number of bits required to encode the bit-length
     * of a {@code long}.
     */
    private int MAX_LONG_BITS = log2(Long.SIZE) - 1;

    public abstract BitBuffer putBits(long value, int numBits);

    public abstract long getBits(int numBits);
	
	/**
	 * Appends an uncompressed {@code boolean} to this {@link BitBuffer}.
	 *
	 * @param b
	 *      The {@code boolean} to append.
	 * @return
	 *      This {@link BitBuffer} to allow for the convenience of method-chaining.
	 */
    public final BitBuffer putBoolean(boolean b) {
    	return putByte(b ? 1 : 0);
	}
	
	/**
	 * Appends a compressed {@code boolean} to this {@link BitBuffer}.
	 *
	 * @param b
	 *      The {@code boolean} to append.
	 * @return
	 *      This {@link BitBuffer} to allow for the convenience of method-chaining.
	 */
	public final BitBuffer putCompressedBoolean(boolean b) {
		return putBits(b ? 1 : 0, 1);
	}
    
    /**
     * Appends an uncompressed {@code byte} to this {@link BitBuffer}.
     *
     * @param b
     *      The {@code byte} to append.
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    public final BitBuffer putByte(int b) {
        return putBits(b, Byte.SIZE);
    }

    /**
     * Appends an uncompressed {@code int} to this {@link BitBuffer}.
     *
     * @param i
     *      The {@code int} to append.
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    public final BitBuffer putInt(int i) {
        return putBits(i, Integer.SIZE);
    }

    /**
     * Appends a compressed {@code int} to this {@link BitBuffer}.
     *
     * @param i
     *      The {@code int} to append.
     * @param sign
     *      Whether {@code i} is explicitly positive, negative, or
     *      either of the two.  Explicitly marking {@code sign} as
     *      positive or negative will yield a better compression ratio.
     *      Marking a positive {@code i} with a {@code negative} parity
     *      (and vice versa) may result in the wrong value being added
     *      to the {@link BitBuffer}.
     * @return
     *      This {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer putCompressedInt(int i, Sign sign) {
        return putBits(i, true, Integer.SIZE, MAX_INT_BITS, sign);
    }

    /**
     * Appends an uncompressed {@code long} to this {@link BitBuffer}.
     *
     * @param l
     *      The {@code long} to append.
     * @return
     *      This {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public final BitBuffer putLong(long l) {
        return putBits(l, Long.SIZE);
    }

    /**
     * Appends a compressed {@code long} to this {@link BitBuffer}.
     *
     * @param l
     *      The {@code long} to append.
     * @param sign
     *      Whether {@code l} is explicitly positive, negative, or
     *      either of the two.  Explicitly marking {@code sign} as
     *      positive or negative will yield a better compression ratio.
     *      Marking a positive {@code l} with a {@code negative} parity
     *      (and vice versa) may result in the wrong value being added
     *      to the {@link BitBuffer}.
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    public BitBuffer putCompressedLong(long l, Sign sign) {
        return putBits(l, true, Long.SIZE, MAX_LONG_BITS, sign);
    }

    /**
     * Appends an uncompressed {@code short} to this {@link BitBuffer}.
     *
     * @param s
     *      The {@code short} to append.
     * @return
     *      This {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public final BitBuffer putShort(int s) {
        return putBits(s, Short.SIZE);
    }

    /**
     * Appends a compressed {@code short} to this {@link BitBuffer}.
     *
     * @param s
     *      The {@code short} to append.
     * @param sign
     *      Whether {@code s} is explicitly positive, negative, or
     *      either of the two.  Explicitly making {@code parity} as
     *      positive or negative will yield a better compression ratio.
     *      Marking a positive {@code s} with a {@code NEGATIVE} parity
     *      (and vice versa) may result in the wrong value being added
     *      to the {@link BitBuffer}.
     * @return
     *      This {@link BitBuffer} to allow for the convenience of method-chaining.
     */
    public BitBuffer putCompressedShort(int s, Sign sign) {
        return putBits(s, true, Short.SIZE, MAX_SHORT_BITS, sign);
    }

    public abstract BitBuffer putBits(long value, boolean compressed, int size, int maxSize, Sign sign);

    public abstract Number readBits(boolean compressed, int size, Sign sign);

    /**
     * Converts this {@link BitBuffer}'s data to a byte array.
     *
     * @return
     *      A {@code byte[]}.
     */
    public abstract byte[] toByteArray();
	
	/**
	 * Gets an uncompressed {@code boolean} from this {@link BitBuffer}.
	 *
	 * @param compressed
	 * 		Whether or not the {@code boolean} to read is compressed.
	 * @return
	 *      A {@code boolean}.
	 */
   	public boolean getBoolean(boolean compressed) {
		return (compressed ? getBits(1) : getByte()) == 1;
	}
    
    /**
     * Gets an uncompressed {@code byte} from this {@link BitBuffer}.
     *
     * @return
     *      A {@code byte}.
     */
    public final byte getByte() {
        return (byte) getBits(Byte.SIZE);
    }

    /**
     * Gets an uncompressed {@code int} from this {@link BitBuffer}.
     *
     * @return
     *      An {@code int}.
     */
    public final int getInt() {
        return (int) getBits(Integer.SIZE);
    }

    /**
     * Gets a compressed {@code int} from this {@link BitBuffer}.
     *
     * @param sign
     *      Whether the value returned is explicitly positive, negative, or
     *      either of the two.  If the {@link Sign} of the call to
     *      {@link BitBuffer#putCompressedInt(int, Sign)} for this respective
     *      value was marked as {@code POSITIVE} or {@code NEGATIVE}, then
     *      the {@link Sign} of this method call <strong>must</strong> be
     *      marked the same.  Marking the incorrect {@code parity} may result
     *      in the wrong value being read from the {@link BitBuffer} (if
     *      {@code compressed} is set to {@code true}).
     * @return
     *      An {@code int}.
     */
    public int getCompressedInt(Sign sign) {
        return readBits(true, Integer.SIZE, sign).intValue();
    }

    /**
     * Gets an uncompressed {@code long} from this {@link BitBuffer}.
     *
     * @return
     *      A {@code long}.
     */
    public final long getLong() {
        return getBits(Long.SIZE);
    }

    /**
     * Gets a compressed {@code long} from this {@link BitBuffer}.
     *
     * @param sign
     *      Whether the value returned is explicitly positive, negative, or
     *      either of the two.  If the {@link Sign} of the call to
     *      {@link BitBuffer#putCompressedLong(long, Sign)} for this respective
     *      value was marked as {@code POSITIVE} or {@code NEGATIVE}, then
     *      the {@link Sign} of this method call <strong>must</strong> be
     *      marked the same.  Marking the incorrect {@code parity} may result
     *      in the wrong value being read from the {@link BitBuffer} (if
     *      {@code compressed} is set to {@code true}).
     * @return
     *      A {@code long}.
     */
    public long getCompressedLong(Sign sign) {
        return readBits(true, Long.SIZE, sign).longValue();
    }

    /**
     * Gets an uncompressed {@code short} from this {@link BitBuffer}.
     *
     * @return
     *      A {@code short}.
     */
    public final short getShort() {
        return (short) getBits(Short.SIZE);
    }

    /**
     * Gets a compressed {@code short} from this {@link BitBuffer}.
     *
     * @param sign
     *      Whether the value returned is explicitly positive, negative, or
     *      either of the two.  If the {@link Sign} of the call to
     *      {@link BitBuffer#putCompressedShort(int, Sign)} for this respective
     *      value was marked as {@code POSITIVE} or {@code NEGATIVE}, then
     *      the {@link Sign} of this method call <strong>must</strong> be
     *      marked the same.  Marking the incorrect {@code parity} may result
     *      in the wrong value being read from the {@link BitBuffer} (if
     *      {@code compressed} is set to {@code true}).
     * @return
     *      A {@code long}.
     */
    public short getCompressedShort(Sign sign) {
        return readBits(true, Short.SIZE, sign).shortValue();
    }

    /**
     * Returns the base 2 logarithm of a {@code long} value.
     *
     * @param l
     *      A value.
     * @return
     *      The base 2 logarithm of {@code l}.
     */
    public static int log2(long l) {
        return Long.SIZE - Long.numberOfLeadingZeros(l) - 1;
    }

}