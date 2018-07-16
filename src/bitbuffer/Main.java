package bitbuffer;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {
        int n = 2;

        //while (true) {
            BitBuffer buffer = BitBuffer.allocate(n * 4);

            int[] numbers = new int[n];
            int[] bits = new int[n];

            for (int i = 0; i < n; i++) {
                numbers[i] = /*i == 0 ? 994374772 : i == 1 ? 1433087769 : 367730393;*/ ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
                bits[i] = Integer.SIZE - Integer.numberOfLeadingZeros(numbers[i]);
                System.out.println((i + 1) + ". " + numbers[i] + "  Bits: " + bits[i]);
                buffer.putBits(numbers[i], bits[i]);
            }

            buffer.flip();
            System.out.println();

            for (int i = 0; i < n; i++) {
                int number = (int) buffer.getBits(bits[i]);
                System.out.println((i + 1) + ". " + number);
                if (number != numbers[i]) {
                    throw new IllegalStateException("Correct: " + numbers[i] + "   Read: " + number);
                }
            }
        //}
    }

}
