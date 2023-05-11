package com.simibubi.create.content.contraptions.processing.fan;

import static com.simibubi.create.content.contraptions.processing.InWorldProcessing.RECIPE_WRAPPER;
import static com.simibubi.create.content.contraptions.processing.InWorldProcessing.applyRecipeOn;
import static com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.getHeatLevelOf;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.particle.AirFlowParticle;
import com.simibubi.create.content.contraptions.processing.InWorldProcessing;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.processing.burner.LitBlazeBurnerBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TypeSmoking extends AbstractFanProcessingType {
	public TypeSmoking(int priority, ResourceLocation name) {
		super(priority, name);
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
			entity.hurt(InWorldProcessing.FIRE_DAMAGE_SOURCE, 2);
		}
	}

	@Override
	public boolean isApplicable(BlockGetter reader, BlockPos pos) {
		BlockState blockState = reader.getBlockState(pos);
		Block block = blockState.getBlock();
		return block == Blocks.FIRE
				|| block.equals(Blocks.CAMPFIRE) && blockState.getOptionalValue(CampfireBlock.LIT)
				.orElse(false)
				|| block.equals(Blocks.SOUL_CAMPFIRE) && blockState.getOptionalValue(CampfireBlock.LIT)
				.orElse(false)
				|| AllBlocks.LIT_BLAZE_BURNER.has(blockState) && blockState.getOptionalValue(LitBlazeBurnerBlock.FLAME_TYPE)
				.map(flame -> flame == LitBlazeBurnerBlock.FlameType.REGULAR).orElse(false)
				|| getHeatLevelOf(blockState) == BlazeBurnerBlock.HeatLevel.SMOULDERING;
	}

	@Nullable
	@Override
	public List<ItemStack> process(ItemStack stack, Level world) {
		RECIPE_WRAPPER.setItem(0, stack);
		Optional<SmokingRecipe> smokingRecipe = world.getRecipeManager()
				.getRecipeFor(RecipeType.SMOKING, RECIPE_WRAPPER, world);
		if (!smokingRecipe.isPresent())
			return null;
		return applyRecipeOn(stack, smokingRecipe.get());
	}

	@Override
	public void morphType(AirFlowParticle particle) {
		particle.setProperties(0x0, 0x555555, 1f, 3);
		particle.addParticle(ParticleTypes.SMOKE, 1 / 32f, .125f);
		particle.addParticle(ParticleTypes.LARGE_SMOKE, 1 / 32f, .125f);
	}

	@Override
	public boolean canProcess(ItemStack stack, Level level) {
		RECIPE_WRAPPER.setItem(0, stack);
		Optional<SmokingRecipe> recipe = level.getRecipeManager()
				.getRecipeFor(RecipeType.SMOKING, RECIPE_WRAPPER, level);
		return recipe.isPresent();
	}
}
