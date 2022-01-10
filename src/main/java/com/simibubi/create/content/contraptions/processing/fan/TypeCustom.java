package com.simibubi.create.content.contraptions.processing.fan;

import static com.simibubi.create.content.contraptions.processing.InWorldProcessing.applyRecipeOn;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.particle.AirFlowParticle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class TypeCustom extends AbstractFanProcessingType {

	public static final CustomRecipeWrapper RECIPE_WRAPPER = new CustomRecipeWrapper();

	private final CustomFanTypeConfig config;

	public TypeCustom(CustomFanTypeConfig config) {
		super(config.priority(), config.name());
		this.config = config;
	}

	@Override
	public boolean canProcess(ItemStack stack, Level level) {
		RECIPE_WRAPPER.setItem(0, stack);
		RECIPE_WRAPPER.type = this;
		Optional<HauntingRecipe> recipe = AllRecipeTypes.CUSTOM_FAN.find(RECIPE_WRAPPER, level);
		return recipe.isPresent();
	}

	@Override
	public void spawnParticlesForProcessing(Level level, Vec3 pos) {
		if (config.processing_particle() != null && config.processing_particle().size() > 0) {
			for (CustomFanTypeConfig.ProcessingParticleConfig ppc : config.processing_particle())
				ppc.spawnParticlesForProcessing(level, pos);
		}
	}

	@Override
	public void affectEntity(Entity entity, Level level) {
		if (config.entity_effect() != null) {
			config.entity_effect().affectEntity(entity, level, config.name());
		}
	}

	@Override
	public boolean isApplicable(BlockGetter reader, BlockPos pos) {
		return config.block().isApplicable(reader, pos, config.name());
	}

	@Nullable
	@Override
	public List<ItemStack> process(ItemStack stack, Level world) {
		RECIPE_WRAPPER.setItem(0, stack);
		RECIPE_WRAPPER.type = this;
		Optional<CustomFanProcessingRecipe> recipe = AllRecipeTypes.CUSTOM_FAN.find(RECIPE_WRAPPER, world);
		if (recipe.isPresent())
			return applyRecipeOn(stack, recipe.get());
		return null;
	}

	@Override
	public void morphType(AirFlowParticle particle) {
		CustomFanTypeConfig.MorphConfig morph = config.morph();
		if (morph != null) {
			morph.morphType(particle);
		}

	}

	public static class CustomRecipeWrapper extends RecipeWrapper {

		public TypeCustom type = null;

		public CustomRecipeWrapper() {
			super(new ItemStackHandler(1));
		}
	}

}
