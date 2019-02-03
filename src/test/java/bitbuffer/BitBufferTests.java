package bitbuffer;

import java.nio.ByteOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

final class BitBufferTests {
    
    private BitBuffer buffer;
    
    @BeforeEach
    void initialize() {
        buffer = BitBuffer.allocate(Long.BYTES);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void testReadBoolean(String value) {
        boolean b = Boolean.parseBoolean(value);
        Assertions.assertEquals(b, buffer.putBoolean(b, false)
                .flip().getBoolean(false));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void testReadCompressedBoolean(String value) {
        boolean b = Boolean.parseBoolean(value);
        Assertions.assertEquals(b, buffer.putBoolean(b, true)
                .flip().getBoolean(true));
    }
    
    @ParameterizedTest
    @ValueSource(bytes = {123, 0, -123})
    void testReadByte(byte value) {
        Assertions.assertEquals(value, buffer.putByte(value).flip().getByte());
    }
    
    @Test
    void testReadBytes() {
        byte[] length = {12, 0};
        byte[] text = {72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100, 33};
        byte[] id = {-46, 4, 0, 0, 0, 0, 0, 0};
        
        BitBuffer buffer = BitBuffer.allocate(24);
        buffer.putBytes(length).putBytes(text).putBytes(id);
        Assertions.assertArrayEquals(new byte[] {12, 0, 72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100,
                33, -46, 4}, buffer.flip().getBytes(16));
    }
    
    @ParameterizedTest
    @ValueSource(shorts = {1234, 0, -1234})
    void testReadShortBigEndian(short value) {
        Assertions.assertEquals(value, buffer.putShort(value).flip().getShort());
    }
    
    @ParameterizedTest
    @ValueSource(shorts = {1234, 0, -1234})
    void testReadShortLittleEndian(short value) {
        Assertions.assertEquals(value, buffer.putShort(value, ByteOrder.LITTLE_ENDIAN)
                .flip().getShort(ByteOrder.LITTLE_ENDIAN));
    }
    
    @ParameterizedTest
    @ValueSource(ints = {123456, 0, -123456})
    void testReadIntBigEndian(int value) {
        Assertions.assertEquals(value, buffer.putInt(value).flip().getInt());
    }
    
    @ParameterizedTest
    @ValueSource(ints = {123456, 0, -123456})
    void testReadIntLittleEndian(int value) {
        Assertions.assertEquals(value, buffer.putInt(value, ByteOrder.LITTLE_ENDIAN)
                .flip().getInt(ByteOrder.LITTLE_ENDIAN));
    }
    
    @ParameterizedTest
    @ValueSource(floats = {123456,5F, 0.5F, -123456.5F})
    void testReadFloatBigEndian(float value) {
        Assertions.assertEquals(value, buffer.putFloat(value).flip().getFloat());
    }
    
    @ParameterizedTest
    @ValueSource(floats = {123456,5F, 0.5F, -123456.5F})
    void testReadFloatLittleEndian(float value) {
        Assertions.assertEquals(value, buffer.putFloat(value, ByteOrder.LITTLE_ENDIAN)
                .flip().getFloat(ByteOrder.LITTLE_ENDIAN));
    }
    
    @ParameterizedTest
    @ValueSource(longs = {12345678912L, 0L, -12345678912L})
    void testReadLongBigEndian(long value) {
        Assertions.assertEquals(value, buffer.putLong(value).flip().getLong());
    }
    
    @ParameterizedTest
    @ValueSource(longs = {12345678912L, 0L, -12345678912L})
    void testReadLongLittleEndian(long value) {
        Assertions.assertEquals(value, buffer.putLong(value, ByteOrder.LITTLE_ENDIAN)
                .flip().getLong(ByteOrder.LITTLE_ENDIAN));
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {123456,5, 0.5, -123456.5})
    void testReadDoubleBigEndian(double value) {
        Assertions.assertEquals(value, buffer.putDouble(value).flip().getDouble());
    }
    
    @ParameterizedTest
    @ValueSource(doubles = {123456,5, 0.5, -123456.5})
    void testReadDoubleLittleEndian(double value) {
        Assertions.assertEquals(value, buffer.putDouble(value, ByteOrder.LITTLE_ENDIAN)
                .flip().getDouble(ByteOrder.LITTLE_ENDIAN));
    }
    
    /**
     * This method tests whether the {@code long} cache is cleared properly.
     */
    @Test
    void testDoubleFlip() {
        buffer.putInt(42).putInt(26).flip();
        Assertions.assertEquals(42, buffer.getInt());
        buffer.flip();
        buffer.flip();
        Assertions.assertEquals(26, buffer.getInt());
    }
    
    @Test
    void testByteBuffer() {
        buffer.toByteBuffer().putShort((short) 1234);
        Assertions.assertEquals(1234, buffer.flip().getShort());
    }
    
}
