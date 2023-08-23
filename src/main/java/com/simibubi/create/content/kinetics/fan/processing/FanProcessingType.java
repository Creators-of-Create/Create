package com.simibubi.create.content.kinetics.fan.processing;

import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
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

	void morphAirFlow(AirFlowParticleAccess particleAccess, Random random);

	void affectEntity(Entity entity, Level level);

	static FanProcessingType parse(String str) {
		ResourceLocation id = ResourceLocation.tryParse(str);
		if (id == null) {
			return AllFanProcessingTypes.NONE;
		}
		FanProcessingType type = FanProcessingTypeRegistry.getType(id);
		if (type == null) {
			return AllFanProcessingTypes.NONE;
		}
		return type;
	}

	static FanProcessingType getAt(Level level, BlockPos pos) {
		for (FanProcessingType type : FanProcessingTypeRegistry.getSortedTypesView()) {
			if (type.isValidAt(level, pos)) {
				return type;
			}
		}
		return AllFanProcessingTypes.NONE;
	}

	interface AirFlowParticleAccess {
		void setColor(int color);

		void setAlpha(float alpha);

		void spawnExtraParticle(ParticleOptions options, float speedMultiplier);
	}
}
