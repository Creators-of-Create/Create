package com.simibubi.create.foundation.entity;

import java.util.Optional;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.util.Direction;

public class CreateDataSerializers {

	public static final IDataSerializer<Optional<Direction>> OPTIONAL_DIRECTION =
		new IDataSerializer<Optional<Direction>>() {

			public void write(PacketBuffer buffer, Optional<Direction> opt) {
				buffer.writeVarInt(opt.map(Direction::ordinal)
					.orElse(-1) + 1);
			}

			public Optional<Direction> read(PacketBuffer buffer) {
				int i = buffer.readVarInt();
				return i == 0 ? Optional.empty() : Optional.of(Direction.values()[i - 1]);
			}

			public Optional<Direction> copyValue(Optional<Direction> opt) {
				return Optional.ofNullable(opt.orElse(null));
			}
		};

	static {
		DataSerializers.registerSerializer(OPTIONAL_DIRECTION);
	}

	public static void register() {}

}
