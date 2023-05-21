package com.simibubi.create.foundation.particle;

import java.util.Locale;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllParticleTypes;

import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AirParticleData implements ParticleOptions, ICustomParticleDataWithSprite<AirParticleData> {

	public static final Codec<AirParticleData> CODEC = RecordCodecBuilder.create(i -> 
		i.group(
			Codec.FLOAT.fieldOf("drag").forGetter(p -> p.drag),
			Codec.FLOAT.fieldOf("speed").forGetter(p -> p.speed))
		.apply(i, AirParticleData::new));
	
	public static final ParticleOptions.Deserializer<AirParticleData> DESERIALIZER =
		new ParticleOptions.Deserializer<AirParticleData>() {
			public AirParticleData fromCommand(ParticleType<AirParticleData> particleTypeIn, StringReader reader)
				throws CommandSyntaxException {
				reader.expect(' ');
				float drag = reader.readFloat();
				reader.expect(' ');
				float speed = reader.readFloat();
				return new AirParticleData(drag, speed);
			}

			public AirParticleData fromNetwork(ParticleType<AirParticleData> particleTypeIn, FriendlyByteBuf buffer) {
				return new AirParticleData(buffer.readFloat(), buffer.readFloat());
			}
		};

	float drag; 
	float speed;

	public AirParticleData(float drag, float speed) {
		this.drag = drag;
		this.speed = speed;
	}

	public AirParticleData() {
		this(0, 0);
	}

	@Override
	public ParticleType<?> getType() {
		return AllParticleTypes.AIR.get();
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf buffer) {
		buffer.writeFloat(drag);
		buffer.writeFloat(speed);
	}

	@Override
	public String writeToString() {
		return String.format(Locale.ROOT, "%s %f %f", AllParticleTypes.AIR.parameter(), drag, speed);
	}

	@Override
	public Deserializer<AirParticleData> getDeserializer() {
		return DESERIALIZER;
	}

	@Override
	public Codec<AirParticleData> getCodec(ParticleType<AirParticleData> type) {
		return CODEC;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public SpriteParticleRegistration<AirParticleData> getMetaFactory() {
		return AirParticle.Factory::new;
	}

}