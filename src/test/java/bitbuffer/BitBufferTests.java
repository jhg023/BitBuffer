package bitbuffer;

import java.nio.ByteOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

final class BitBufferTests {
    
    @ParameterizedTest
    @ValueSource(bytes = {123, 0, -123})
    void testReadByte(byte value) {
        Assertions.assertEquals(value, BitBuffer.allocate(Byte.BYTES).putByte(value).flip().getByte());
    }
    
    @ParameterizedTest
    @ValueSource(shorts = {1234, 0, -1234})
    void testReadShortBigEndian(short value) {
        Assertions.assertEquals(value, BitBuffer.allocate(Short.BYTES).putShort(value).flip().getShort());
    }
    
    @ParameterizedTest
    @ValueSource(shorts = {1234, 0, -1234})
    void testReadShortLittleEndian(short value) {
        Assertions.assertEquals(value, BitBuffer.allocate(Short.BYTES).putShort(value,
                ByteOrder.LITTLE_ENDIAN).flip().getShort(ByteOrder.LITTLE_ENDIAN));
    }
    
    @ParameterizedTest
    @ValueSource(ints = {123456, 0, -123456})
    void testReadIntBigEndian(int value) {
        Assertions.assertEquals(value, BitBuffer.allocate(Integer.BYTES).putInt(value).flip().getInt());
    }
    
    @ParameterizedTest
    @ValueSource(ints = {123456, 0, -123456})
    void testReadIntLittleEndian(int value) {
        Assertions.assertEquals(value, BitBuffer.allocate(Integer.BYTES).putInt(value,
                ByteOrder.LITTLE_ENDIAN).flip().getInt(ByteOrder.LITTLE_ENDIAN));
    }
    
    @ParameterizedTest
    @ValueSource(longs = {12345678912L, 0L, -12345678912L})
    void testReadLongBigEndian(long value) {
        Assertions.assertEquals(value, BitBuffer.allocate(Long.BYTES).putLong(value).flip().getLong());
    }
    
    @ParameterizedTest
    @ValueSource(longs = {12345678912L, 0L, -12345678912L})
    void testReadLongLittleEndian(long value) {
        Assertions.assertEquals(value, BitBuffer.allocate(Long.BYTES).putLong(value,
                ByteOrder.LITTLE_ENDIAN).flip().getLong(ByteOrder.LITTLE_ENDIAN));
    }
    
    /**
     * This method tests whether the {@code long} cache is cleared properly.
     */
    @Test
    void testDoubleFlip() {
        var buffer = BitBuffer.allocate(Long.BYTES);
        buffer.putInt(42).putInt(26).flip();
        Assertions.assertEquals(buffer.getInt(), 42); // Position: 4   Limit: 8
        buffer.flip(); // Position: 0   Limit: 4
        buffer.flip();
        Assertions.assertEquals(buffer.getInt(), 26);
    }
    
    @Test
    void testPutData() {
        var buffer = BitBuffer.allocate(Long.BYTES);
        buffer.putBoolean(true, true).putLong(-1);
        System.out.println(Long.toBinaryString(buffer.cache));
        System.out.println(buffer.buffer.flip().getLong());
    }
    
}
