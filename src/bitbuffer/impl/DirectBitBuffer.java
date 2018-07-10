package bitbuffer.impl;

import bitbuffer.BitBuffer;
import bitbuffer.Sign;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class DirectBitBuffer implements BitBuffer {

    private static final long[] MASKS = new long[Long.SIZE];

    static {
        for (int i = 0; i < MASKS.length - 1; i++) {
        	MASKS[i] = BigInteger.TWO.pow(i + 1).subtract(BigInteger.ONE).longValue();
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
        int n = 10;
        
        DirectBitBuffer buffer = new DirectBitBuffer(ByteBuffer.allocate(n * 8));
       
        int[] bits = new int[n];
        long[] numbers = new long[n];

        for (int i = 0; i < n; i++) {
        	long number = ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
            //long number = i == 0 ? 1681186488542789119L : 5121553840518974009L;
			//long number = i == 0 ? 7341723592777308505L : 2172702275061705423L;
			bits[i] = Long.SIZE - Long.numberOfLeadingZeros(number);
			numbers[i] = number;
            System.out.println(number + "  " + (Long.SIZE - Long.numberOfLeadingZeros(number)));
            buffer.putBits(number, bits[i]);
        }

        buffer.flip();
	
		System.out.println(Arrays.toString(buffer.toByteArray()));

        for (int i = 0; i < n; i++) {
        	long number = buffer.getBits(bits[i]);
         
        	System.out.println("Read: " + number);
        	
        	if (numbers[i] != number) {
        		throw new IllegalStateException(numbers[i] + " " + number);
			}
        }
    }

    public DirectBitBuffer(ByteBuffer bytes) {
        Objects.requireNonNull(bytes);
        this.bytes = bytes;
    }

    @Override
    public void putBits(long value, int numBits) {
		buffer |= (value << bit);
		if ((bit += numBits) >= Long.SIZE) {
			bytes.putLong(buffer);
			buffer = value >> (Long.SIZE - (bit -= numBits));
		}
    }

    public void flip() {
    	bytes.putLong(buffer);
    	buffer = bytes.flip().getLong();
    	bit = 0;
	}
    
    @Override
    public long getBits(int numBits) {
    	long value = (buffer >>> bit) & MASKS[numBits - 1];
		if ((bit += numBits) >= Long.SIZE) {
			value |= ((buffer = bytes.getLong()) & MASKS[bit -= numBits]) << (Long.SIZE - bit);
		}
		return value;
    }

    @Override
    public BitBuffer putBits(long value, boolean compressed, int size, int maxBits, Sign sign) {
        /*int numBits;

        if (compressed) {
            numBits = size;
        } else {
            numBits = size;
        }

        // If the current byte to write to isn't empty and isn't full...
        while (numBits > 0) {
            int remainingBits = Byte.SIZE - writeBitIndex;

            bytes[writeIndex++] |= ((value & MASKS[remainingBits - 1]) << writeBitIndex);

            numBits -= remainingBits;
            value >>>= remainingBits;

            writeBitIndex = (writeBitIndex + remainingBits) % Byte.SIZE;
        }*/

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
