package bitbuffer;

import java.nio.ByteBuffer;

/**
 * A data-type similar to {@link ByteBuffer}, but
 * reads/writes bits rather than {@code byte}s to
 * reduce bandwidth, increase throughput, and
 * allow for optional compression.
 *
 * @author Jacob G.
 * @since February 24, 2018
 */
public interface BitBuffer {

    /**
     * Appends an uncompressed {@code byte} to this {@link BitBuffer}.
     *
     * @param b
     *      The {@code byte} to append.
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    default BitBuffer putByte(int b) {
        return putBits(b, false, Byte.SIZE, Sign.EITHER);
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
    default BitBuffer putInt(int i) {
        return putBits(i, false, Integer.SIZE, Sign.EITHER);
    }

    /**
     * Appends a compressed {@code int} to this {@link BitBuffer}.
     *
     * @param i
     *      The {@code int} to append.
     * @param sign
     *      Whether {@code i} is explicitly positive, negative, or
     *      either of the two.  Explicitly making {@code parity} as
     *      positive or negative will yield a better compression ratio.
     *      Marking a positive {@code i} with a {@code NEGATIVE} parity
     *      (and vice versa) may result in the wrong value being added
     *      to the {@link BitBuffer}.
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    default BitBuffer putCompressedInt(int i, Sign sign) {
        return putBits(i, true, Integer.SIZE, sign);
    }

    /**
     * Appends an uncompressed {@code long} to this {@link BitBuffer}.
     *
     * @param l
     *      The {@code long} to append.
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    default BitBuffer putLong(long l) {
        return putBits(l, false, Long.SIZE, Sign.EITHER);
    }

    /**
     * Appends a compressed {@code long} to this {@link BitBuffer}.
     *
     * @param l
     *      The {@code long} to append.
     * @param sign
     *      Whether {@code l} is explicitly positive, negative, or
     *      either of the two.  Explicitly making {@code parity} as
     *      positive or negative will yield a better compression ratio.
     *      Marking a positive {@code l} with a {@code NEGATIVE} parity
     *      (and vice versa) may result in the wrong value being added
     *      to the {@link BitBuffer}.
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    default BitBuffer putCompressedLong(long l, Sign sign) {
        return putBits(l, true, Long.SIZE, sign);
    }

    /**
     * Appends an uncompressed {@code short} to this {@link BitBuffer}.
     *
     * @param s
     *      The {@code short} to append.
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    default BitBuffer putShort(int s) {
        return putBits(s, false, Short.SIZE, Sign.EITHER);
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
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    default BitBuffer putCompressedShort(int s, Sign sign) {
        return putBits(s, true, Short.SIZE, sign);
    }

    BitBuffer putBits(long value, boolean compressed, int size, Sign sign);

    Number readBits(boolean compressed, int size, Sign sign);

    /**
     * Converts this {@link BitBuffer}'s data to a byte array.
     *
     * @return
     *      A {@code byte[]}.
     */
    byte[] toByteArray();

    /**
     * Gets an uncompressed {@code byte} from this {@link BitBuffer}.
     *
     * @return
     *      A {@code byte}.
     */
    default byte getByte() {
        return readBits(false, Byte.SIZE, Sign.EITHER).byteValue();
    }

    /**
     * Gets an uncompressed {@code int} from this {@link BitBuffer}.
     *
     * @return
     *      An {@code int}.
     */
    default int getInt() {
        return readBits(false, Integer.SIZE, Sign.EITHER).intValue();
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
    default int getCompressedInt(Sign sign) {
        return readBits(true, Integer.SIZE, sign).intValue();
    }

    /**
     * Gets an uncompressed {@code long} from this {@link BitBuffer}.
     *
     * @return
     *      A {@code long}.
     */
    default long getLong() {
        return readBits(false, Long.SIZE, Sign.EITHER).intValue();
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
    default long getCompressedLong(Sign sign) {
        return readBits(true, Long.SIZE, sign).longValue();
    }

    /**
     * Gets an uncompressed {@code short} from this {@link BitBuffer}.
     *
     * @return
     *      A {@code short}.
     */
    default short getShort() {
        return readBits(false, Short.SIZE, Sign.EITHER).shortValue();
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
    default long getCompressedShort(Sign sign) {
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
    static int log2(long l) {
        return Long.SIZE - 1 - Long.numberOfLeadingZeros(l);
    }

}