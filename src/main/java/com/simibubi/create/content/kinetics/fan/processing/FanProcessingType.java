package com.simibubi.create.content.kinetics.fan.processing;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface FanProcessingType {
	boolean isValidAt(Level level, BlockPos pos);

	int getPriority();

	boolean canProcess(ItemStack stack, Level level);

	@Nullable
	List<ItemStack> process(ItemStack stack, Level level);

	void spawnProcessingParticles(Level level, Vec3 pos);

	void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random);

	void affectEntity(Entity entity, Level level);

	@Nullable
	static FanProcessingType parse(String str) {
		return CreateBuiltInRegistries.FAN_PROCESSING_TYPE.get(ResourceLocation.tryParse(str));
	}

	@Nullable
	static FanProcessingType getAt(Level level, BlockPos pos) {
		for (FanProcessingType type : FanProcessingTypeRegistry.SORTED_TYPES_VIEW) {
			if (type.isValidAt(level, pos)) {
				return type;
			}
		}
		return null;
	}

	interface AirFlowParticleAccess {
		void setColor(int color);

		void setAlpha(float alpha);

		void spawnExtraParticle(ParticleOptions options, float speedMultiplier);
	}
}
