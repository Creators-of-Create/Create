package com.simibubi.create.modules.contraptions.particle;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.AllParticles;

import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.Direction.Axis;

public class RotationIndicatorParticleData implements IParticleData {

	public static final IParticleData.IDeserializer<RotationIndicatorParticleData> DESERIALIZER = new IParticleData.IDeserializer<RotationIndicatorParticleData>() {
		public RotationIndicatorParticleData deserialize(ParticleType<RotationIndicatorParticleData> particleTypeIn,
				StringReader reader) throws CommandSyntaxException {
			reader.expect(' ');
			int color = reader.readInt();
			reader.expect(' ');
			float speed = (float) reader.readDouble();
			reader.expect(' ');
			float rad1 = (float) reader.readDouble();
			reader.expect(' ');
			float rad2 = (float) reader.readDouble();
			reader.expect(' ');
			int lifeSpan = reader.readInt();
			reader.expect(' ');
			char axis = reader.read();
			return new RotationIndicatorParticleData(color, speed, rad1, rad2, lifeSpan, axis);
		}

		public RotationIndicatorParticleData read(ParticleType<RotationIndicatorParticleData> particleTypeIn,
				PacketBuffer buffer) {
			return new RotationIndicatorParticleData(buffer.readInt(), buffer.readFloat(), buffer.readFloat(),
					buffer.readFloat(), buffer.readInt(), buffer.readChar());
		}
	};

	final int color;
	final float speed;
	final float radius1;
	final float radius2;
	final int lifeSpan;
	final char axis;

	public RotationIndicatorParticleData(int color, float speed, float radius1, float radius2, int lifeSpan,
			char axis) {
		this.color = color;
		this.speed = speed;
		this.radius1 = radius1;
		this.radius2 = radius2;
		this.lifeSpan = lifeSpan;
		this.axis = axis;
	}

	@Override
	public ParticleType<?> getType() {
		return AllParticles.ROTATION_INDICATOR.get();
	}

	public Axis getAxis() {
		return Axis.valueOf(axis + "");
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeInt(color);
		buffer.writeFloat(speed);
		buffer.writeFloat(radius1);
		buffer.writeFloat(radius2);
		buffer.writeInt(lifeSpan);
	}

	@Override
	public String getParameters() {
		return String.format(Locale.ROOT, "%s %d %.2f %.2f %.2f %d %c", AllParticles.ROTATION_INDICATOR.parameter(),
				color, speed, radius1, radius2, lifeSpan, axis);
	}

}