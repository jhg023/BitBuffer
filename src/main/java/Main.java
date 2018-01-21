package main.java;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public final class Main {

    public static void main(String[] args) {
        BitBuffer buffer = new BitBuffer();

        int n = 1_000_000;

        for (int i = 0; i < n; i++) {
            int number = ThreadLocalRandom.current().nextInt(0, n);

            //System.out.println("BEFORE: " + number);

            buffer.putInt(number, true);
        }

        System.out.println(buffer.toByteArray().length);

        for (int i = 0; i < n; i++) {
            int number = buffer.getInt(true);

            //System.out.println("AFTER: " + number);
        }
    }

}
