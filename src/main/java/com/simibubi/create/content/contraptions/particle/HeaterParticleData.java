package com.simibubi.create.content.contraptions.particle;

import java.util.Locale;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllParticleTypes;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HeaterParticleData implements ParticleOptions, ICustomParticleDataWithSprite<HeaterParticleData> {

	public static final Codec<HeaterParticleData> CODEC = RecordCodecBuilder.create(i -> 
		i.group(
			Codec.FLOAT.fieldOf("r").forGetter(p -> p.r),
			Codec.FLOAT.fieldOf("g").forGetter(p -> p.g),
			Codec.FLOAT.fieldOf("b").forGetter(p -> p.b))
		.apply(i, HeaterParticleData::new));

	public static final ParticleOptions.Deserializer<HeaterParticleData> DESERIALIZER =
		new ParticleOptions.Deserializer<HeaterParticleData>() {
			@Override
			public HeaterParticleData fromCommand(ParticleType<HeaterParticleData> arg0, StringReader reader)
				throws CommandSyntaxException {
				reader.expect(' ');
				float r = reader.readFloat();
				reader.expect(' ');
				float g = reader.readFloat();
				reader.expect(' ');
				float b = reader.readFloat();
				return new HeaterParticleData(r, g, b);
			}

			@Override
			public HeaterParticleData fromNetwork(ParticleType<HeaterParticleData> type, FriendlyByteBuf buffer) {
				return new HeaterParticleData(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
			}
		};

	final float r;
	final float g;
	final float b;

	public HeaterParticleData(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public HeaterParticleData() {
		this(0, 0, 0);
	}

	@Override
	public Deserializer<HeaterParticleData> getDeserializer() {
		return DESERIALIZER;
	}

	@Override
	public Codec<HeaterParticleData> getCodec(ParticleType<HeaterParticleData> type) {
		return CODEC;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public SpriteParticleRegistration<HeaterParticleData> getMetaFactory() {
		return HeaterParticle.Factory::new;
	}

	@Override
	public String writeToString() {
		return String.format(Locale.ROOT, "%s %f %f %f", AllParticleTypes.HEATER_PARTICLE.parameter(), r, g, b);
	}

	@Override
	public ParticleType<?> getType() {
		return AllParticleTypes.HEATER_PARTICLE.get();
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf buffer) {
		buffer.writeFloat(r);
		buffer.writeFloat(g);
		buffer.writeFloat(b);
	}

}
