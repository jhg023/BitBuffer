package bitbuffer.impl;

import bitbuffer.BitBuffer;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public final class DirectBitBuffer extends BitBuffer {

    private static final long[] MASKS = new long[Long.SIZE];

    static {
        for (int i = 0; i < MASKS.length; i++) {
        	MASKS[i] = BigInteger.TWO.pow(i).subtract(BigInteger.ONE).longValue();
        }
		MASKS[MASKS.length - 1] = -1L;
    }

    private final ByteBuffer bytes;

    /**
     * The index of the next bit to begin writing to, which is located in the last byte that
     * was previously written to, or the byte located at {@code address} if nothing has been
     * written yet.
     */
    private int bit;
    
    private long buffer;

    public static void main(String[] args) {
        int n = 10_000_000;

        DirectBitBuffer buffer = new DirectBitBuffer(((n * 4) + 7) / 8 * 8);

        int[] bits = new int[n];
        int[] numbers = new int[n];

        for (int i = 0; i < n; i++) {
            int number = ThreadLocalRandom.current().nextInt();
            //int number = i == 0 ? -13580391 : 1383985879;
            bits[i] = Integer.SIZE - Integer.numberOfLeadingZeros(number);
            numbers[i] = number;
            //System.out.println(number + "  " + (Long.SIZE - Long.numberOfLeadingZeros(number)));
            buffer.putBits(number, bits[i]);
        }

        buffer.flip();

        //System.out.println(Arrays.toString(buffer.toByteArray()));

        for (int i = 0; i < n; i++) {
            long number = (int) buffer.getBits(bits[i]);

            //System.out.println("Read: " + number);

            if (numbers[i] != number) {
                throw new IllegalStateException(numbers[i] + " " + number);
            }
        }
    }

    public DirectBitBuffer(int bytes) {
        this.bytes = ByteBuffer.allocate(bytes);
    }

    @Override
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
    
    @Override
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

    @Override
    public BitBuffer putBits(long value, boolean compressed, int size, int maxBits, Sign sign) {
        return this;
    }

    @Override
    public Number readBits(boolean compressed, int size, Sign sign) {
        return null;
    }

    @Override
    public byte[] toByteArray() {
        return Arrays.copyOf(bytes.array(), bytes.capacity());
    }

}
