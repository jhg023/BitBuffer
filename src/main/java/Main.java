package main.java;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
        BitBuffer test = new BitBuffer();

        final int factor = 8;

        test.putDouble(4.12345678, true, factor);

        System.out.println(test.getDouble(true, factor));
    }

}
