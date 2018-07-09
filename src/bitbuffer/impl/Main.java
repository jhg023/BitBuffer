package bitbuffer.impl;

import bitbuffer.BitBuffer;
import bitbuffer.Sign;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

public final class Main {

    public static void main(String[] args) {
        int n = 60_000_000;

        int[] numbers = new int[n];

        BitBuffer buffer = new HeapBitBuffer(n * 4 * 8);

        for (int i = 0; i < n; i++) {
            int num = ThreadLocalRandom.current().nextInt();

            numbers[i] = num;

            //System.out.println((i + 1) + ". " + num + " " + (Long.SIZE - Long.numberOfLeadingZeros(num)));

            buffer.putCompressedInt(num, Sign.EITHER);
            //buffer.putInt(num);
        }

        byte[] array = buffer.toByteArray();

        System.out.println("Compressed: " + NumberFormat.getInstance().format(array.length) + " Bytes");
        System.out.println("Uncompressed: " + NumberFormat.getInstance().format(n * 4) + " Bytes");
        System.out.println("Compressed " + String.format("%.8f", 100 - (array.length / (n * 4D) * 100)) + "% (Higher is better)");
        System.out.println("Bits/Integer: " + array.length * 8D / n);

        for (int i = 0; i < n; i++) {
            int num = buffer.getCompressedInt(Sign.EITHER);
            //int num = buffer.getInt();

            //System.out.println((i + 1) + ". " + num);

            if (numbers[i] != num) {
                throw new RuntimeException(numbers[i] + " " + num);
            }
        }
    }

}
