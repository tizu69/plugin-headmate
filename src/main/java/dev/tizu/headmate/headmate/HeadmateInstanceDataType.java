package dev.tizu.headmate.headmate;

import java.nio.ByteBuffer;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

public class HeadmateInstanceDataType implements PersistentDataType<byte[], HeadmateInstance> {
	@Override
	public Class<byte[]> getPrimitiveType() {
		return byte[].class;
	}

	@Override
	public Class<HeadmateInstance> getComplexType() {
		return HeadmateInstance.class;
	}

	private static final int MAGIC_BYTE_LEN = 4; // both ints and floats share this!

	@Override
	public byte[] toPrimitive(HeadmateInstance complex, PersistentDataAdapterContext context) {
		ByteBuffer bb = ByteBuffer.allocate(MAGIC_BYTE_LEN * 7);
		bb.putInt((complex.miniX ? 1 : 0) // flags
				| (complex.miniY ? 2 : 0)
				| (complex.miniZ ? 4 : 0));
		bb.putFloat(complex.offsetX);
		bb.putFloat(complex.offsetY);
		bb.putFloat(complex.offsetZ);
		bb.putFloat(complex.scale);
		bb.putInt(complex.rotH);
		bb.putInt(complex.rotV);
		return bb.array();
	}

	@Override
	public HeadmateInstance fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
		ByteBuffer bb = ByteBuffer.wrap(primitive);
		switch (bb.limit() / MAGIC_BYTE_LEN) {
			case 6:
				return new HeadmateInstance(
						bb.getFloat(), // offsetX
						bb.getFloat(), // offsetY
						bb.getFloat(), // offsetZ
						bb.getFloat(), // scale
						bb.getInt(), // rotH
						bb.getInt(), // rotV
						false, false, false // miniX, miniY, miniZ
				);
			case 7:
				var flags = bb.getInt();
				return new HeadmateInstance(
						bb.getFloat(), // offsetX
						bb.getFloat(), // offsetY
						bb.getFloat(), // offsetZ
						bb.getFloat(), // scale
						bb.getInt(), // rotH
						bb.getInt(), // rotV
						(flags & 1) != 0, // miniX
						(flags & 2) != 0, // miniY
						(flags & 4) != 0 // miniZ
				);
			default:
				throw new IllegalArgumentException("Invalid byte array length, got " + bb.limit());
		}
	}
}
