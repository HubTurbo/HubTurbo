package storage;

import java.io.IOException;
import java.time.LocalDateTime;

import util.Utility;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
	public LocalDateTime read(JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.NULL) {
			reader.nextNull();
			return null;
		}
		long value = reader.nextLong();
		return Utility.longToLocalDateTime(value);
	}

	public void write(JsonWriter writer, LocalDateTime value) throws IOException {
		if (value == null) {
			writer.nullValue();
			return;
		}
		writer.value(Utility.localDateTimeToLong(value));
	}
}