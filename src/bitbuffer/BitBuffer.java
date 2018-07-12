package bitbuffer;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A data-type similar to {@link ByteBuffer}, but reads/writes bits rather than {@code byte}s to
 * reduce bandwidth, increase throughput, and allow for optional compression.
 *
 * @author Jacob G.
 * @since February 24, 2018
 */
public final class BitBuffer {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        int n = 2;

        int[] numbers = new int[n];

        //BitBuffer buffer = new HeapBitBuffer(n * 4 * 8);
        BitBuffer buffer = BitBuffer.allocate(n * 8);

        for (int i = 0; i < n; i++) {
            int num = ThreadLocalRandom.current().nextInt();

            numbers[i] = num;

            //System.out.println((i + 1) + ". " + num + " " + (Long.SIZE - Long.numberOfLeadingZeros(num)));

            buffer.putCompressedInt(num, Sign.EITHER);
            //buffer.putInt(num);
        }

        byte[] array = buffer.bytes.array();

        System.out.println("Compressed: " + NumberFormat.getInstance().format(array.length) + " Bytes");
        System.out.println("Uncompressed: " + NumberFormat.getInstance().format(n * 4) + " Bytes");
        System.out.println("Compressed " + String.format("%.8f", 100 - (array.length / (n * 4D) * 100)) + "% (Higher is better)");
        System.out.println("Bits/Integer: " + array.length * 8D / n);
        //System.out.println(Arrays.toString(array));

        buffer.flip();

        for (int i = 0; i < n; i++) {
            int num = buffer.getCompressedInt(Sign.EITHER);
            //int num = buffer.getInt();

            //System.out.println((i + 1) + ". " + num);

            if (numbers[i] != num) {
                throw new RuntimeException(numbers[i] + " " + num);
            }
        }

        System.out.println("Program ran in " + (System.currentTimeMillis() - start) + " ms");
    }

	public enum Sign {
		POSITIVE, NEGATIVE, EITHER
	}

    private static final long[] MASKS = new long[Long.SIZE];

    static {
        for (int i = 0; i < MASKS.length; i++) {
            MASKS[i] = BigInteger.TWO.pow(i).subtract(BigInteger.ONE).longValue();
        }
        MASKS[MASKS.length - 1] = -1L;
    }
	
    /**
     * The maximum number of bits required to encode the bit-length
     * of a {@code short}.
     */
    private static final int MAX_SHORT_BITS = log2(Short.SIZE) - 1;

    /**
     * The maximum number of bits required to encode the bit-length
     * of an {@code int}.
     */
    private static final int MAX_INT_BITS = log2(Integer.SIZE) - 1;

    /**
     * The maximum number of bits required to encode the bit-length
     * of a {@code long}.
     */
    private static final int MAX_LONG_BITS = log2(Long.SIZE) - 1;

    private final ByteBuffer bytes;

    private int bit;

    private long buffer;

    private BitBuffer(ByteBuffer bytes) {
        this.bytes = bytes;
    }

    public static BitBuffer allocate(int capacity) {
        return new BitBuffer(ByteBuffer.allocate((capacity + 7) / 8 * 8));
    }

    public static BitBuffer allocateDirect(int capacity) {
        return new BitBuffer(ByteBuffer.allocateDirect((capacity + 7) / 8 * 8));
    }

    public BitBuffer putBits(long value, int numBits) {
        int bitsWritten = Math.min(Long.SIZE - bit, numBits);
        buffer |= ((value & MASKS[bitsWritten]) << bit);
        if ((bit += bitsWritten) == Long.SIZE) {
            bytes.putLong(buffer);
            buffer = (value >> bitsWritten) & MASKS[bit = numBits - bitsWritten];
        }
        return this;
    }

    public BitBuffer flip() {
        if (bit != 0) {
            bytes.putLong(buffer);
            bit = 0;
        }
        buffer = bytes.flip().getLong();
        return this;
    }

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
	 * Appends an uncompressed {@code boolean} to this {@link BitBuffer}.
	 *
	 * @param b
	 *      The {@code boolean} to append.
     * @param compressed
     *      Whether or not the {@code boolean} should be compressed.
	 * @return
	 *      This {@link BitBuffer} to allow for the convenience of method-chaining.
	 */
    public BitBuffer putBoolean(boolean b, boolean compressed) {
    	return compressed ? putBits(b ? 1 : 0, 1) : putByte(b ? 1 : 0);
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
    public BitBuffer putByte(int b) {
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
    public BitBuffer putInt(int i) {
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
        boolean shouldNegate = i < 0;

        if (shouldNegate) {
            i = -i;
        }

        int numBits = Long.SIZE - Long.numberOfLeadingZeros(i);

        if (numBits >= Integer.SIZE - MAX_INT_BITS) {
            putBoolean(false, true);

            if (sign == Sign.EITHER) {
                putBoolean(shouldNegate, true);
            }

            putBits(i, Integer.SIZE - 1);
        } else {
            putBoolean(true, true);

            if (sign == Sign.EITHER) {
                putBoolean(shouldNegate, true);
            }

            putBits(numBits >> 1, MAX_INT_BITS);
            putBits(i, numBits);

            if ((numBits & 1) == 0) {
                putBoolean(false, true);
            }
        }

        return this;
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
        return this;
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
        return this;
    }

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
        int value = 0;

        boolean shouldNegate;

        if (getBoolean(true)) {
            shouldNegate = sign == Sign.EITHER && getBoolean(true);

            int numBits = (int) getBits(MAX_INT_BITS);

            value = (int) getBits((numBits << 1) + 1);
        } else {
            shouldNegate = sign == Sign.EITHER && getBoolean(true);

            value = (int) getBits(Integer.SIZE - 1);
        }

        return shouldNegate || sign == Sign.NEGATIVE ? -value : value;
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
        return 0;
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
        return 0;
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