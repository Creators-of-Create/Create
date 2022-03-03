package com.simibubi.create.content.contraptions.processing.fan.custom;

import java.util.Optional;

import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * - chance: required. Chance to spawn every tick of processing
 * ! color: optional. color of particle if not using id. In hex format. Example: FFFFFF for white
 * ! id: optional. Should be present if and only if color is not present. ID for simple particle.
 * ? base_offset: optional. Vector representing the relative position to spawn particle
 * ? random_offset: optional. Vector representing how far the particle can spawn off the base offset
 * ? speed: optional. Initial speed of the particle.
 * ? random_speed: optional. Random addition of the speed of the particle
 */
public record ProcessingParticleConfig(int chance, String col, SimpleParticleType simple,
									   Vec3Config baseOffset,
									   Vec3Config randomOffset,
									   Vec3Config speed,
									   Vec3Config randomSpeed) {

	public static final Codec<ProcessingParticleConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.fieldOf("chance").forGetter(e -> e.chance),
			Codec.STRING.optionalFieldOf("color").forGetter(e -> Optional.ofNullable(e.col)),
			ResourceLocation.CODEC.optionalFieldOf("id").forGetter(e -> Optional.ofNullable(e.simple).map(ForgeRegistryEntry::getRegistryName)),
			Vec3Config.CODEC.optionalFieldOf("base_offset").forGetter(e -> Optional.of(e.baseOffset)),
			Vec3Config.CODEC.optionalFieldOf("random_offset").forGetter(e -> Optional.of(e.randomOffset)),
			Vec3Config.CODEC.optionalFieldOf("speed").forGetter(e -> Optional.of(e.speed)),
			Vec3Config.CODEC.optionalFieldOf("random_speed").forGetter(e -> Optional.of(e.randomSpeed))
	).apply(i, (chance, col, id, base_offset, random_offset, speed, random_speed) -> new ProcessingParticleConfig(chance,
			colorCheck(col, id), id.map(CustomFanTypeConfig.MorphConfig.ParticleConfig::checkType).orElse(null),
			base_offset.orElse(Vec3Config.ZERO),
			random_offset.orElse(Vec3Config.ZERO),
			speed.orElse(Vec3Config.ZERO),
			random_speed.orElse(Vec3Config.ZERO))));

	private static String colorCheck(Optional<String> color, Optional<ResourceLocation> particle) {
		if (color.isPresent() && particle.isEmpty()) {
			// check if color can be parsed
			Integer.parseInt(color.get(), 16);
			return color.get();
		}
		if (color.isEmpty() && particle.isPresent())
			return null;
		throw new IllegalArgumentException("either particle or color should be present");
	}

	public void spawnParticlesForProcessing(Level level, Vec3 pos) {
		if (level.random.nextInt(chance) != 0)
			return;
		ParticleOptions option = null;
		if (simple != null) {
			option = simple;
		} else if (col != null && col.length() > 0) {
			Vector3f color = new Color(Integer.parseInt(col, 16)).asVectorF();
			option = new DustParticleOptions(color, 1);
		}
		double x = pos.x + baseOffset.x() + (level.random.nextFloat() - .5f) * randomOffset.x() * 2;
		double y = pos.y + baseOffset.y() + (level.random.nextFloat() - .5f) * randomOffset.y() * 2;
		double z = pos.z + baseOffset.z() + (level.random.nextFloat() - .5f) * randomOffset.z() * 2;
		double vx = speed.x() + (level.random.nextFloat() - .5f) * randomSpeed.x() * 2;
		double vy = speed.y() + (level.random.nextFloat() - .5f) * randomSpeed.y() * 2;
		double vz = speed.z() + (level.random.nextFloat() - .5f) * randomSpeed.z() * 2;
		if (option != null) {
			level.addParticle(option, x, y, z, vx, vy, vz);
		}
	}

}
