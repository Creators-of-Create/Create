package com.simibubi.create.content.contraptions.processing.fan;

import com.mojang.math.Vector3f;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.particle.AirFlowParticle;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public class TypeSplashing extends AbstractRecipeFanType<SplashingRecipe> {

	public TypeSplashing(int priority, ResourceLocation name) {
		super(priority, name, AllRecipeTypes.SPLASHING::find);
	}

	@Override
	public void spawnParticlesForProcessing(Level level, Vec3 pos) {
		if (level.random.nextInt(8) != 0)
			return;
		Vector3f color = new Color(0x0055FF).asVectorF();
		level.addParticle(new DustParticleOptions(color, 1), pos.x + (level.random.nextFloat() - .5f) * .5f,
				pos.y + .5f, pos.z + (level.random.nextFloat() - .5f) * .5f, 0, 1 / 8f, 0);
		level.addParticle(ParticleTypes.SPIT, pos.x + (level.random.nextFloat() - .5f) * .5f, pos.y + .5f,
				pos.z + (level.random.nextFloat() - .5f) * .5f, 0, 1 / 8f, 0);
	}

	@Override
	public void affectEntity(Entity entity, Level level) {
		if (level.isClientSide)
			return;

		if (entity instanceof EnderMan || entity.getType() == EntityType.SNOW_GOLEM
				|| entity.getType() == EntityType.BLAZE) {
			entity.hurt(DamageSource.DROWN, 2);
		}
		if (entity.isOnFire()) {
			entity.clearFire();
			level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.NEUTRAL,
					0.7F, 1.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
		}
	}

	@Override
	public boolean isApplicable(BlockGetter reader, BlockPos pos) {
		FluidState fluidState = reader.getFluidState(pos);
		return fluidState.getType() == Fluids.WATER || fluidState.getType() == Fluids.FLOWING_WATER;
	}

	@Override
	public void morphType(AirFlowParticle particle) {
		particle.setProperties(0x4499FF, 0x2277FF, 1f, 3);
		particle.addParticle(ParticleTypes.BUBBLE, 1 / 32f, .125f);
		particle.addParticle(ParticleTypes.BUBBLE_POP, 1 / 32f, .125f);
	}

}
