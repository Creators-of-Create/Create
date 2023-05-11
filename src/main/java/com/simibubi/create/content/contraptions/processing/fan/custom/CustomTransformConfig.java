package com.simibubi.create.content.contraptions.processing.fan.custom;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * - block_type: required. Capitalized string from block type. Example: SPLASHING, BLASTING, HAUNTING
 * - old_type: required. entity id for entities to be transformed. Must be living entity
 * - new_type: required. entity id for entity to transform to. Must be living entity
 * ? {} progression: optional. Sound to play when certain progress is made
 * ? {} completion: optional. Sound to play when the transform is complete
 * ? [{}] particles: optional. List of particles to generate. See particle config documentation for details.
 */
public record CustomTransformConfig(ResourceLocation blockType, EntityType<?> oldType,
									EntityType<?> newType,
									ProgressionSoundConfig progression, CompletionSoundConfig completion,
									List<ProcessingParticleConfig> particles) {

	public record ProgressionSoundConfig(int interval, SoundEvent sound, float volume, float pitch,
										 float volumeSlope,
										 float pitchSlope) {

		public static final Codec<ProgressionSoundConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.INT.fieldOf("interval").forGetter(e -> e.interval),
				ResourceLocation.CODEC.fieldOf("id").forGetter(e -> e.sound.getRegistryName()),
				Codec.FLOAT.fieldOf("volume").forGetter(e -> e.volume),
				Codec.FLOAT.fieldOf("pitch").forGetter(e -> e.pitch),
				Codec.FLOAT.optionalFieldOf("volume_slope").forGetter(e -> Optional.of(e.volumeSlope)),
				Codec.FLOAT.optionalFieldOf("pitch_slope").forGetter(e -> Optional.of(e.pitchSlope))
		).apply(i, (interval, id, volume, pitch, volume_slope, pitch_slope) ->
				new ProgressionSoundConfig(interval, checkSound(id), volume, pitch, volume_slope.orElse(0f), pitch_slope.orElse(0f))));

		public static SoundEvent checkSound(ResourceLocation id) {
			if (!ForgeRegistries.SOUND_EVENTS.containsKey(id))
				throw new IllegalArgumentException(id + " is not a valid sound event");
			return ForgeRegistries.SOUND_EVENTS.getValue(id);
		}
	}

	public record CompletionSoundConfig(SoundEvent sound, float volume, float pitch) {

		public static final Codec<CompletionSoundConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
				ResourceLocation.CODEC.fieldOf("id").forGetter(e -> e.sound.getRegistryName()),
				Codec.FLOAT.fieldOf("volume").forGetter(e -> e.volume),
				Codec.FLOAT.fieldOf("pitch").forGetter(e -> e.pitch)
		).apply(i, (id, v, p) -> new CompletionSoundConfig(ProgressionSoundConfig.checkSound(id), v, p)));

	}

	public static final Codec<CustomTransformConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
			ResourceLocation.CODEC.fieldOf("block_type").forGetter(e -> e.blockType),
			ResourceLocation.CODEC.fieldOf("old_type").forGetter(e -> e.oldType.getRegistryName()),
			ResourceLocation.CODEC.fieldOf("new_type").forGetter(e -> e.newType.getRegistryName()),
			ProgressionSoundConfig.CODEC.optionalFieldOf("progression").forGetter(e -> Optional.ofNullable(e.progression)),
			CompletionSoundConfig.CODEC.optionalFieldOf("completion").forGetter(e -> Optional.ofNullable(e.completion)),
			Codec.list(ProcessingParticleConfig.CODEC).optionalFieldOf("particles").forGetter(e -> Optional.ofNullable(e.particles))
	).apply(i, (block_type, old_type, new_type, progression, completion, particles) ->
			new CustomTransformConfig(block_type,
					checkType(old_type),
					checkType(new_type),
					progression.orElse(null),
					completion.orElse(null), particles.orElse(null))));

	public static EntityType<?> checkType(ResourceLocation id) {
		if (id == null || id.getPath().length() == 0) throw new IllegalArgumentException("id cannot be empty");
		if (!ForgeRegistries.ENTITIES.containsKey(id))
			throw new IllegalArgumentException("entity type " + id + " not found");
		return ForgeRegistries.ENTITIES.getValue(id);
	}

}
