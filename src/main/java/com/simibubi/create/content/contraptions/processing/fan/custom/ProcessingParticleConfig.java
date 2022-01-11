package com.simibubi.create.content.contraptions.processing.fan.custom;

import java.util.Optional;

import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public record ProcessingParticleConfig(int chance, String col, String id,
									   Vec3Config base_offset,
									   Vec3Config random_offset,
									   Vec3Config speed,
									   Vec3Config random_speed) {

	public static final Codec<ProcessingParticleConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.fieldOf("chance").forGetter(e -> e.chance),
			Codec.STRING.optionalFieldOf("color").forGetter(e -> Optional.ofNullable(e.col)),
			Codec.STRING.optionalFieldOf("id").forGetter(e -> Optional.ofNullable(e.id)),
			Vec3Config.CODEC.optionalFieldOf("base_offset").forGetter(e -> Optional.of(e.base_offset)),
			Vec3Config.CODEC.optionalFieldOf("random_offset").forGetter(e -> Optional.of(e.random_offset)),
			Vec3Config.CODEC.optionalFieldOf("speed").forGetter(e -> Optional.of(e.speed)),
			Vec3Config.CODEC.optionalFieldOf("random_speed").forGetter(e -> Optional.of(e.random_speed))
	).apply(i, (chance, col, id, base_offset, random_offset, speed, random_speed) -> new ProcessingParticleConfig(chance,
			col.orElse(null), id.orElse(null),
			base_offset.orElse(Vec3Config.ZERO),
			random_offset.orElse(Vec3Config.ZERO),
			speed.orElse(Vec3Config.ZERO),
			random_speed.orElse(Vec3Config.ZERO))));

	public void spawnParticlesForProcessing(Level level, Vec3 pos) {
		if (level.random.nextInt(chance) != 0)
			return;
		ParticleOptions option = null;
		if (id != null && id.length() > 0) {
			ParticleType<?> type = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(id));
			if (type instanceof SimpleParticleType simple) {
				option = simple;
			} else throw new IllegalArgumentException("particle type " + id + " is not simple particle type");
		} else if (col != null && col.length() > 0) {
			Vector3f color = new Color(Integer.parseInt(col, 16)).asVectorF();
			option = new DustParticleOptions(color, 1);
		}
		double x = pos.x + base_offset.x() + (level.random.nextFloat() - .5f) * random_offset.x() * 2;
		double y = pos.y + base_offset.y() + (level.random.nextFloat() - .5f) * random_offset.y() * 2;
		double z = pos.z + base_offset.z() + (level.random.nextFloat() - .5f) * random_offset.z() * 2;
		double vx = speed.x() + (level.random.nextFloat() - .5f) * random_speed.x() * 2;
		double vy = speed.y() + (level.random.nextFloat() - .5f) * random_speed.y() * 2;
		double vz = speed.z() + (level.random.nextFloat() - .5f) * random_speed.z() * 2;
		if (option != null) {
			level.addParticle(option, x, y, z, vx, vy, vz);
		} else throw new IllegalArgumentException("particle not known for " + id);
	}

}
