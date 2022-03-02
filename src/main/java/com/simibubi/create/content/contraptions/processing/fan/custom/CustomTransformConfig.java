package com.simibubi.create.content.contraptions.processing.fan.custom;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings({"rawtypes", "ConstantConditions"})
public record CustomTransformConfig(String block_type, ResourceLocation old_type, ResourceLocation new_type,
									ProgressionSoundConfig progression, CompletionSoundConfig completion,
									List<ProcessingParticleConfig> particles) {

	public record ProgressionSoundConfig(int interval, ResourceLocation id, float volume, float pitch,
										 float volume_slope,
										 float pitch_slope) {

		public static final Codec<ProgressionSoundConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.fieldOf("interval").forGetter(e -> e.interval),
			ResourceLocation.CODEC.fieldOf("id").forGetter(e -> e.id),
			Codec.FLOAT.fieldOf("volume").forGetter(e -> e.volume),
			Codec.FLOAT.fieldOf("pitch").forGetter(e -> e.pitch),
			Codec.FLOAT.optionalFieldOf("volume_slope").forGetter(e -> Optional.of(e.volume_slope)),
			Codec.FLOAT.optionalFieldOf("pitch_slope").forGetter(e -> Optional.of(e.pitch_slope))
		).apply(i, (interval, id, volume, pitch, volume_slope, pitch_slope) ->
			new ProgressionSoundConfig(interval, id, volume, pitch, volume_slope.orElse(0f), pitch_slope.orElse(0f))));

		public SoundEvent getSound() {
			return ForgeRegistries.SOUND_EVENTS.getValue(id);
		}
	}

	public record CompletionSoundConfig(ResourceLocation id, float volume, float pitch) {

		public static final Codec<CompletionSoundConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
			ResourceLocation.CODEC.fieldOf("id").forGetter(e -> e.id),
			Codec.FLOAT.fieldOf("volume").forGetter(e -> e.volume),
			Codec.FLOAT.fieldOf("pitch").forGetter(e -> e.pitch)
		).apply(i, CompletionSoundConfig::new));

		public SoundEvent getSound() {
			return ForgeRegistries.SOUND_EVENTS.getValue(id);
		}

	}

	public static final Codec<CustomTransformConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.STRING.fieldOf("block_type").forGetter(e -> e.block_type),
		ResourceLocation.CODEC.fieldOf("old_type").forGetter(e -> e.old_type),
		ResourceLocation.CODEC.fieldOf("new_type").forGetter(e -> e.new_type),
		ProgressionSoundConfig.CODEC.optionalFieldOf("progression").forGetter(e -> Optional.ofNullable(e.progression)),
		CompletionSoundConfig.CODEC.optionalFieldOf("completion").forGetter(e -> Optional.ofNullable(e.completion)),
		Codec.list(ProcessingParticleConfig.CODEC).optionalFieldOf("particles").forGetter(e -> Optional.ofNullable(e.particles))
	).apply(i, (block_type, old_type, new_type, progression, completion, particles) ->
		new CustomTransformConfig(block_type, old_type, new_type, progression.orElse(null),
			completion.orElse(null), particles.orElse(null))));

	public EntityType getNewType() {
		return ForgeRegistries.ENTITIES.getValue(new_type);
	}

	public EntityType getOldType() {
		return ForgeRegistries.ENTITIES.getValue(old_type);
	}

	public Class getOldClass() {
		return ForgeRegistries.ENTITIES.getValue(old_type).getBaseClass();
	}


}
