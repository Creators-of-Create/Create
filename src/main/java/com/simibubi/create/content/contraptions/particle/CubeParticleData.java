package com.simibubi.create.content.contraptions.particle;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.AllParticleTypes;

import net.minecraft.client.particle.ParticleManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CubeParticleData implements IParticleData, ICustomParticle<CubeParticleData> {

	public static final IParticleData.IDeserializer<CubeParticleData> DESERIALIZER = new IParticleData.IDeserializer<CubeParticleData>() {
		@Override
		public CubeParticleData deserialize(ParticleType<CubeParticleData> type, StringReader reader) throws CommandSyntaxException {
			reader.expect(' ');
			float r = reader.readFloat();
			reader.expect(' ');
			float g = reader.readFloat();
			reader.expect(' ');
			float b = reader.readFloat();
			reader.expect(' ');
			float scale = reader.readFloat();
			reader.expect(' ');
			int avgAge = reader.readInt();
			reader.expect(' ');
			boolean hot = reader.readBoolean();
			return new CubeParticleData(r, g, b, scale, avgAge, hot);
		}

		@Override
		public CubeParticleData read(ParticleType<CubeParticleData> type, PacketBuffer buffer) {
			return new CubeParticleData(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readInt(), buffer.readBoolean());
		}
	};

	final float r;
	final float g;
	final float b;
	final float scale;
	final int avgAge;
	final boolean hot;

	public CubeParticleData(float r, float g, float b, float scale, int avgAge, boolean hot) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.scale = scale;
		this.avgAge = avgAge;
		this.hot = hot;
	}

	public static CubeParticleData dummy() {
		return new CubeParticleData(0, 0, 0, 0, 0, false);
	}

	@Override
	public IDeserializer<CubeParticleData> getDeserializer() {
		return DESERIALIZER;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ParticleManager.IParticleMetaFactory<CubeParticleData> getFactory() {
		return null;
	}

	@Override
	public ParticleType<?> getType() {
		return AllParticleTypes.CUBE.get();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeFloat(r);
		buffer.writeFloat(g);
		buffer.writeFloat(b);
		buffer.writeFloat(scale);
		buffer.writeInt(avgAge);
		buffer.writeBoolean(hot);
	}

	@Override
	public String getParameters() {
		return String.format(Locale.ROOT, "%s %f %f %f %f %d %s", AllParticleTypes.CUBE.parameter(), r, g, b, scale, avgAge, hot);
	}
}
