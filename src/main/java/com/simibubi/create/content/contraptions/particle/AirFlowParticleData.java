package com.simibubi.create.content.contraptions.particle;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllParticleTypes;

import net.minecraft.client.particle.ParticleManager.IParticleMetaFactory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.particles.IParticleData.IDeserializer;

public class AirFlowParticleData implements IParticleData, ICustomParticleDataWithSprite<AirFlowParticleData> {
	
	public static final Codec<AirFlowParticleData> CODEC = RecordCodecBuilder.create(i -> 
		i.group(
			Codec.INT.fieldOf("x").forGetter(p -> p.posX),
			Codec.INT.fieldOf("y").forGetter(p -> p.posY),
			Codec.INT.fieldOf("z").forGetter(p -> p.posZ))
		.apply(i, AirFlowParticleData::new));

	public static final IParticleData.IDeserializer<AirFlowParticleData> DESERIALIZER = new IParticleData.IDeserializer<AirFlowParticleData>() {
		public AirFlowParticleData fromCommand(ParticleType<AirFlowParticleData> particleTypeIn, StringReader reader)
				throws CommandSyntaxException {
			reader.expect(' ');
			int x = reader.readInt();
			reader.expect(' ');
			int y = reader.readInt();
			reader.expect(' ');
			int z = reader.readInt();
			return new AirFlowParticleData(x, y, z);
		}

		public AirFlowParticleData fromNetwork(ParticleType<AirFlowParticleData> particleTypeIn, PacketBuffer buffer) {
			return new AirFlowParticleData(buffer.readInt(), buffer.readInt(), buffer.readInt());
		}
	};

	final int posX;
	final int posY;
	final int posZ;

	public AirFlowParticleData(Vector3i pos) {
		this(pos.getX(), pos.getY(), pos.getZ());
	}

	public AirFlowParticleData(int posX, int posY, int posZ) {
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
	}

	public AirFlowParticleData() {
		this(0, 0, 0);
	}

	@Override
	public ParticleType<?> getType() {
		return AllParticleTypes.AIR_FLOW.get();
	}

	@Override
	public void writeToNetwork(PacketBuffer buffer) {
		buffer.writeInt(posX);
		buffer.writeInt(posY);
		buffer.writeInt(posZ);
	}

	@Override
	public String writeToString() {
		return String.format(Locale.ROOT, "%s %d %d %d", AllParticleTypes.AIR_FLOW.parameter(), posX, posY, posZ);
	}

	@Override
	public IDeserializer<AirFlowParticleData> getDeserializer() {
		return DESERIALIZER;
	}
	
	@Override
	public Codec<AirFlowParticleData> getCodec(ParticleType<AirFlowParticleData> type) {
		return CODEC;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IParticleMetaFactory<AirFlowParticleData> getMetaFactory() {
		return AirFlowParticle.Factory::new;
	}

}