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
    @ValueSource(bytes = {123, 0, -123})
    void testReadByte(byte value) {
        Assertions.assertEquals(value, buffer.putByte(value).flip().getByte());
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
    
}
