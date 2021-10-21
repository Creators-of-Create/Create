package com.simibubi.create.content.contraptions.components.actors;

import static net.minecraft.block.HorizontalBlock.FACING;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.material.MaterialManager;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ActorInstance;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.block.AbstractPlantBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

public class HarvesterMovementBehaviour extends MovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.getValue(FACING)
			.getOpposite());
	}

	@Override
	public boolean hasSpecialInstancedRendering() {
		return true;
	}

	@Nullable
	@Override
	public ActorInstance createInstance(MaterialManager<?> materialManager, PlacementSimulationWorld simulationWorld, MovementContext context) {
		return new HarvesterActorInstance(materialManager, simulationWorld, context);
	}

	@Override
	public void renderInContraption(MovementContext context, PlacementSimulationWorld renderWorld,
		ContraptionMatrices matrices, IRenderTypeBuffer buffers) {
		if (!Backend.getInstance().canUseInstancing())
			HarvesterRenderer.renderInContraption(context, renderWorld, matrices, buffers);
	}

	@Override
	public Vector3d getActiveAreaOffset(MovementContext context) {
		return Vector3d.atLowerCornerOf(context.state.getValue(FACING)
			.getNormal())
			.scale(.45);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		World world = context.world;
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
		
		if (stateVisited.getBlock().is(BlockTags.LEAVES)) {
			item = new ItemStack(Items.SHEARS);
			effectChance = .45f;
		}
		
		MutableBoolean seedSubtracted = new MutableBoolean(notCropButCuttable);
		BlockState state = stateVisited;
		BlockHelper.destroyBlockAs(world, pos, null, item, effectChance, stack -> {
			if (!seedSubtracted.getValue() && stack.sameItem(new ItemStack(state.getBlock()))) {
				stack.shrink(1);
				seedSubtracted.setTrue();
			}
			dropItem(context, stack);
		});

		world.setBlockAndUpdate(pos, cutCrop(world, pos, stateVisited));
	}

	private boolean isValidCrop(World world, BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof CropsBlock) {
			CropsBlock crop = (CropsBlock) state.getBlock();
			if (!crop.isMaxAge(state))
				return false;
			return true;
		}
		if (state.getCollisionShape(world, pos)
			.isEmpty() || state.getBlock() instanceof CocoaBlock) {
			for (Property<?> property : state.getProperties()) {
				if (!(property instanceof IntegerProperty))
					continue;
				if (!property.getName()
					.equals(BlockStateProperties.AGE_1.getName()))
					continue;
				if (((IntegerProperty) property).getPossibleValues()
					.size() - 1 != state.getValue((IntegerProperty) property)
						.intValue())
					continue;
				return true;
			}
		}

		return false;
	}

	private boolean isValidOther(World world, BlockPos pos, BlockState state) {
		if (state.getBlock() instanceof CropsBlock)
			return false;
		if (state.getBlock() instanceof SugarCaneBlock)
			return true;
		if (state.getBlock().is(BlockTags.LEAVES))
			return true;

		if (state.getCollisionShape(world, pos)
			.isEmpty() || state.getBlock() instanceof CocoaBlock) {
			if (state.getBlock() instanceof AbstractPlantBlock)
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

	private BlockState cutCrop(World world, BlockPos pos, BlockState state) {
		Block block = state.getBlock();
		if (block instanceof CropsBlock) {
			CropsBlock crop = (CropsBlock) block;
			return crop.getStateForAge(0);
		}
		if (block == Blocks.SWEET_BERRY_BUSH) {
			return state.setValue(BlockStateProperties.AGE_3, Integer.valueOf(1));
		}
		if (block == Blocks.SUGAR_CANE || block instanceof AbstractPlantBlock) {
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
