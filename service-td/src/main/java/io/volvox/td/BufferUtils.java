package io.volvox.td;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import java.io.IOException;

public class BufferUtils {

	public interface Writer {

		void write(ByteBufOutputStream os) throws IOException;
	}

	public interface Reader<T> {

		T read(ByteBufInputStream is) throws IOException;
	}

	public static void encode(Buffer buffer, Writer writer) {
		try (var os = new ByteBufOutputStream(((BufferImpl) buffer).byteBuf())) {
			writer.write(os);
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}


	public static <T> T decode(int pos, Buffer buffer, Reader<T> reader) {
		try (var is = new ByteBufInputStream(buffer.slice(pos, buffer.length()).getByteBuf())) {
			return reader.read(is);
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

}
