package xyz.synse.packetnet.common.packet;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class PacketTest {
	@Test
	public void serialize() throws IOException {
		Packet packet = new Packet((short)123);
		packet.getBuffer().putString("Hello, world!");
		packet.getBuffer().putFloat(3.141592653589793238462643383279502884197169399375105820974944592307816406286f);
		packet.getBuffer().putDouble(3.141592653589793238462643383279502884197169399375105820974944592307816406286);
		packet.getBuffer().putInt(1234567890);
		packet.getBuffer().putLong(1234567890123456789L);
		packet.getBuffer().putShort((short)12345);
		packet.getBuffer().put((byte)123);
		packet.getBuffer().putBoolean(true);
		packet.getBuffer().putChar('a');

		ByteBuffer buffer = ByteBuffer.allocate(packet.getBuffer().capacity() + Short.BYTES + Integer.BYTES);
		packet.write(buffer);
		buffer.rewind();

		Packet actual = Packet.read(buffer);

		assertEquals(packet.getID(), actual.getID());
		assertArrayEquals(packet.getBuffer().array(), actual.getBuffer().array());
	}
}
