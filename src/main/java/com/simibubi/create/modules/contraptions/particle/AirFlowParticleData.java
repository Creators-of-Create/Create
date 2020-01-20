package com.simibubi.create.modules.contraptions.particle;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.simibubi.create.AllParticles;

import net.minecraft.client.particle.ParticleManager.IParticleMetaFactory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AirFlowParticleData implements IParticleData, ICustomParticle<AirFlowParticleData> {

	public static final IParticleData.IDeserializer<AirFlowParticleData> DESERIALIZER = new IParticleData.IDeserializer<AirFlowParticleData>() {
		public AirFlowParticleData deserialize(ParticleType<AirFlowParticleData> particleTypeIn, StringReader reader)
				throws CommandSyntaxException {
			reader.expect(' ');
			int x = reader.readInt();
			reader.expect(' ');
			int y = reader.readInt();
			reader.expect(' ');
			int z = reader.readInt();
			return new AirFlowParticleData(x, y, z);
		}

		public AirFlowParticleData read(ParticleType<AirFlowParticleData> particleTypeIn, PacketBuffer buffer) {
			return new AirFlowParticleData(buffer.readInt(), buffer.readInt(), buffer.readInt());
		}
	};

	final int posX;
	final int posY;
	final int posZ;

	public AirFlowParticleData(Vec3i pos) {
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
		return AllParticles.AIR_FLOW.get();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeInt(posX);
		buffer.writeInt(posY);
		buffer.writeInt(posZ);
	}

	@Override
	public String getParameters() {
		return String.format(Locale.ROOT, "%s %d %d %d", AllParticles.ROTATION_INDICATOR.parameter(), posX, posY, posZ);
	}

	@Override
	public IDeserializer<AirFlowParticleData> getDeserializer() {
		return DESERIALIZER;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IParticleMetaFactory<AirFlowParticleData> getFactory() {
		return AirFlowParticle.Factory::new;
	}

}