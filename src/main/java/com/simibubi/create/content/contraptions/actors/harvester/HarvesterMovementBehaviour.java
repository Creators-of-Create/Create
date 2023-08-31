package com.simibubi.create.content.contraptions.actors.harvester;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ActorInstance;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.utility.VecHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import org.apache.commons.lang3.mutable.MutableBoolean;

import javax.annotation.Nullable;

public class HarvesterMovementBehaviour implements MovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return MovementBehaviour.super.isActive(context)
			&& !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.getValue(HarvesterBlock.FACING)
				.getOpposite());
	}

	@Override
	public boolean hasSpecialInstancedRendering() {
		return true;
	}

	@Nullable
	@Override
	public ActorInstance createInstance(MaterialManager materialManager, VirtualRenderWorld simulationWorld,
		MovementContext context) {
		return new HarvesterActorInstance(materialManager, simulationWorld, context);
	}

	@Override
	public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffers) {
        if (!ContraptionRenderDispatcher.canInstance())
			HarvesterRenderer.renderInContraption(context, renderWorld, matrices, buffers);
	}

	@Override
	public Vec3 getActiveAreaOffset(MovementContext context) {
		return Vec3.atLowerCornerOf(context.state.getValue(HarvesterBlock.FACING)
			.getNormal())
			.scale(.45);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		Level world = context.world;
		BlockState stateVisited = world.getBlockState(pos);
		boolean notCropButCuttable = false;

		if (world.isClientSide)
			return;

		if (!isValidCrop(world, pos, stateVisited)) {
			if (isValidOther(world, pos, stateVisited))
				notCropButCuttable = true;
			else
				return;
		}

		ItemStack item = ItemStack.EMPTY;
		float effectChance = 1;

		if (stateVisited.is(BlockTags.LEAVES)) {
			item = new ItemStack(Items.SHEARS);
			effectChance = .45f;
		}

		MutableBoolean seedSubtracted = new MutableBoolean(notCropButCuttable);
		BlockState state = stateVisited;
		BlockHelper.destroyBlockAs(world, pos, null, item, effectChance, stack -> {
			if (AllConfigs.server().kinetics.harvesterReplants.get() && !seedSubtracted.getValue()
				&& ItemHelper.sameItem(stack, new ItemStack(state.getBlock()))) {
				stack.shrink(1);
				seedSubtracted.setTrue();
			}
			dropItem(context, stack);
		});

		BlockState cutCrop = cutCrop(world, pos, stateVisited);
		world.setBlockAndUpdate(pos, cutCrop.canSurvive(world, pos) ? cutCrop : Blocks.AIR.defaultBlockState());
	}

	public boolean isValidCrop(Level world, BlockPos pos, BlockState state) {
		boolean harvestPartial = AllConfigs.server().kinetics.harvestPartiallyGrown.get();
		boolean replant = AllConfigs.server().kinetics.harvesterReplants.get();

		if (state.getBlock() instanceof CropBlock) {
			CropBlock crop = (CropBlock) state.getBlock();
			if (harvestPartial)
				return state != crop.getStateForAge(0) || !replant;
			return crop.isMaxAge(state);
		}

		if (state.getCollisionShape(world, pos)
			.isEmpty() || state.getBlock() instanceof CocoaBlock) {
			for (Property<?> property : state.getProperties()) {
				if (!(property instanceof IntegerProperty))
					continue;
				IntegerProperty ageProperty = (IntegerProperty) property;
				if (!property.getName()
					.equals(BlockStateProperties.AGE_1.getName()))
					continue;
				int age = state.getValue(ageProperty)
					.intValue();
				if (state.getBlock() instanceof SweetBerryBushBlock && age <= 1 && replant)
					continue;
				if (age == 0 && replant || !harvestPartial && (ageProperty.getPossibleValues()
					.size() - 1 != age))
					continue;
				return true;
			}
		}

		return false;
	}

	public boolean isValidOther(Level world, BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof CropBlock)
			return false;
		if (state.getBlock() instanceof SugarCaneBlock)
			return true;
		if (state.is(BlockTags.LEAVES))
			return true;
		if (state.getBlock() instanceof CocoaBlock)
			return state.getValue(CocoaBlock.AGE) == CocoaBlock.MAX_AGE;

		if (state.getCollisionShape(world, pos)
			.isEmpty()) {
			if (state.getBlock() instanceof GrowingPlantBlock)
				return true;

			for (Property<?> property : state.getProperties()) {
				if (!(property instanceof IntegerProperty))
					continue;
				if (!property.getName()
					.equals(BlockStateProperties.AGE_1.getName()))
					continue;
				return false;
			}

			if (state.getBlock() instanceof IPlantable)
				return true;
		}

		return false;
	}

	private BlockState cutCrop(Level world, BlockPos pos, BlockState state) {
		if (!AllConfigs.server().kinetics.harvesterReplants.get()) {
			if (state.getFluidState()
				.isEmpty())
				return Blocks.AIR.defaultBlockState();
			return state.getFluidState()
				.createLegacyBlock();
		}

		Block block = state.getBlock();
		if (block instanceof CropBlock) {
			CropBlock crop = (CropBlock) block;
			return crop.getStateForAge(0);
		}
		if (block == Blocks.SWEET_BERRY_BUSH) {
			return state.setValue(BlockStateProperties.AGE_3, Integer.valueOf(1));
		}
		if (block == Blocks.SUGAR_CANE || block instanceof GrowingPlantBlock) {
			if (state.getFluidState()
				.isEmpty())
				return Blocks.AIR.defaultBlockState();
			return state.getFluidState()
				.createLegacyBlock();
		}
		if (state.getCollisionShape(world, pos)
			.isEmpty() || block instanceof CocoaBlock) {
			for (Property<?> property : state.getProperties()) {
				if (!(property instanceof IntegerProperty))
					continue;
				if (!property.getName()
					.equals(BlockStateProperties.AGE_1.getName()))
					continue;
				return state.setValue((IntegerProperty) property, Integer.valueOf(0));
			}
		}

		if (state.getFluidState()
			.isEmpty())
			return Blocks.AIR.defaultBlockState();
		return state.getFluidState()
			.createLegacyBlock();
	}

}
