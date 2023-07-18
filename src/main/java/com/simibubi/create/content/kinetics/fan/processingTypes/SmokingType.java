package com.simibubi.create.content.kinetics.fan.processingTypes;

import com.simibubi.create.content.kinetics.fan.AirFlowParticle;
import com.simibubi.create.content.kinetics.fan.FanProcessing;
import com.simibubi.create.content.kinetics.fan.AbstractFanProcessingType;
import com.simibubi.create.foundation.recipe.RecipeApplier;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class SmokingType extends AbstractFanProcessingType {
	public SmokingType() {
		super("SMOKING");
	}

	@Override
	public void spawnParticlesForProcessing(Level level, Vec3 pos) {
		if (level.random.nextInt(8) != 0)
			return;
		level.addParticle(ParticleTypes.POOF, pos.x, pos.y + .25f, pos.z, 0, 1 / 16f, 0);
	}

	@Override
	public void affectEntity(Entity entity, Level level) {
		if (level.isClientSide)
			return;

		if (!entity.fireImmune()) {
			entity.setSecondsOnFire(2);
			entity.hurt(FanProcessing.FIRE_DAMAGE_SOURCE, 2);
		}
	}

	@Override
	public void particleMorphType(AirFlowParticle particle) {
		particle.setProperties(0x0, 0x555555, 1f, 3);
		particle.addParticle(ParticleTypes.SMOKE, 1 / 32f, .125f);
		particle.addParticle(ParticleTypes.LARGE_SMOKE, 1 / 32f, .125f);
	}

	@Override
	public List<ItemStack> process(ItemStack stack, AbstractFanProcessingType type, Level world) {
		FanProcessing.RECIPE_WRAPPER.setItem(0, stack);
		Optional<SmokingRecipe> smokingRecipe = world.getRecipeManager()
				.getRecipeFor(RecipeType.SMOKING, FanProcessing.RECIPE_WRAPPER, world);
		if (smokingRecipe.isPresent()) return RecipeApplier.applyRecipeOn(stack, smokingRecipe.get());
		return null;
	}

	@Override
	public boolean canProcess(ItemStack stack, Level level) {
		FanProcessing.RECIPE_WRAPPER.setItem(0, stack);
		Optional<SmokingRecipe> recipe = level.getRecipeManager()
				.getRecipeFor(RecipeType.SMOKING, FanProcessing.RECIPE_WRAPPER, level);
		return recipe.isPresent();
	}
}
