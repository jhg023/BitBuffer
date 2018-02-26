package bitbuffer;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.BitSet;

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
     * The maximum number of bits required to encode the bit-length
     * of a {@code short}.
     */
    int MAX_SHORT_BITS = log2(Short.SIZE) - 1;

    /**
     * The maximum number of bits required to encode the bit-length
     * of an {@code int}.
     */
    int MAX_INTEGER_BITS = log2(Integer.SIZE) - 1;

    /**
     * The maximum number of bits required to encode the bit-length
     * of a {@code long}.
     */
    int MAX_LONG_BITS = log2(Long.SIZE) - 1;

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
     * @param parity
     *      Whether {@code i} is explicitly positive, negative, or
     *      either of the two.  Explicitly making {@code parity} as
     *      positive or negative will yield a better compression ratio.
     *      Marking a positive {@code i} with a {@code NEGATIVE} parity
     *      (and vice versa) may result in the wrong value being added
     *      to the {@link BitBuffer} (if {@code compressed} is set to
     *      {@code true}).
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    default BitBuffer putInt(int i, boolean compressed, Parity parity) {
        return putBits(i, compressed, parity, Integer.SIZE, MAX_INTEGER_BITS);
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
     * @param parity
     *      Whether {@code i} is explicitly positive, negative, or
     *      either of the two.  Explicitly making {@code parity} as
     *      positive or negative will yield a better compression ratio.
     *      Marking a positive {@code i} with a {@code NEGATIVE} parity
     *      (and vice versa) may result in the wrong value being added
     *      to the {@link BitBuffer} (if {@code compressed} is set to
     *      {@code true}).
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    default BitBuffer putLong(long l, boolean compressed, Parity parity) {
        return putBits(l, compressed, parity, Long.SIZE, MAX_LONG_BITS);
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
     * @param parity
     *      Whether {@code i} is explicitly positive, negative, or
     *      either of the two.  Explicitly making {@code parity} as
     *      positive or negative will yield a better compression ratio.
     *      Marking a positive {@code i} with a {@code NEGATIVE} parity
     *      (and vice versa) may result in the wrong value being added
     *      to the {@link BitBuffer} (if {@code compressed} is set to
     *      {@code true}).
     * @return
     *      This {@link BitBuffer} to allow for the
     *      convenience of method-chaining.
     */
    default BitBuffer putShort(int s, boolean compressed, Parity parity) {
        return putBits(s, compressed, parity, Short.SIZE, MAX_SHORT_BITS);
    }

    BitBuffer putBits(long value, boolean compressed, Parity parity, int size, int maxBits);

    Number readBits(boolean compressed, Parity parity, int size, int maxBits);

    byte[] toByteArray();

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
     * @param parity
     *      Whether the value returned is explicitly positive, negative, or
     *      either of the two.  If the {@link Parity} of the call to
     *      {@link BitBuffer#putInt(int, boolean, Parity)} for this respective
     *      value was marked as {@code POSITIVE} or {@code NEGATIVE}, then
     *      the {@link Parity} of this method call <strong>must</strong> be
     *      marked the same.  Marking the incorrect {@code parity} may result
     *      in the wrong value being read from the {@link BitBuffer} (if
     *      {@code compressed} is set to {@code true}).
     * @return
     *      An {@code int}.
     */
    default int getInt(boolean compressed, Parity parity) {
        return readBits(compressed, parity, Integer.SIZE, MAX_INTEGER_BITS).intValue();
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
     * @param parity
     *      Whether the value returned is explicitly positive, negative, or
     *      either of the two.  If the {@link Parity} of the call to
     *      {@link BitBuffer#putInt(int, boolean, Parity)} for this respective
     *      value was marked as {@code POSITIVE} or {@code NEGATIVE}, then
     *      the {@link Parity} of this method call <strong>must</strong> be
     *      marked the same.  Marking the incorrect {@code parity} may result
     *      in the wrong value being read from the {@link BitBuffer} (if
     *      {@code compressed} is set to {@code true}).
     * @return
     *      A {@code long}.
     */
    default long getLong(boolean compressed, Parity parity) {
        return readBits(compressed, parity, Long.SIZE, MAX_LONG_BITS).longValue();
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
     * @param parity
     *      Whether the value returned is explicitly positive, negative, or
     *      either of the two.  If the {@link Parity} of the call to
     *      {@link BitBuffer#putInt(int, boolean, Parity)} for this respective
     *      value was marked as {@code POSITIVE} or {@code NEGATIVE}, then
     *      the {@link Parity} of this method call <strong>must</strong> be
     *      marked the same.  Marking the incorrect {@code parity} may result
     *      in the wrong value being read from the {@link BitBuffer} (if
     *      {@code compressed} is set to {@code true}).
     * @return
     *      A {@code short}.
     */
    default short getShort(boolean compressed, Parity parity) {
        return readBits(compressed, parity, Short.SIZE, MAX_SHORT_BITS).shortValue();
    }

    /**
     * Returns the base 2 logarithm of a {@code long} value.
     *
     * @param l
     *      A value.
     * @return
     *      The base 2 logarithm of {@code l}.
     */
    private static int log2(long l) {
        return Long.SIZE - 1 - Long.numberOfLeadingZeros(l);
    }

}