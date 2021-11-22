package com.simibubi.create.content.contraptions.components.actors;

import java.util.Optional;

import com.simibubi.create.content.contraptions.components.saw.SawBlock;
import com.simibubi.create.content.contraptions.components.saw.SawRenderer;
import com.simibubi.create.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.foundation.utility.AbstractBlockBreakQueue;
import com.simibubi.create.foundation.utility.TreeCutter;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public class SawMovementBehaviour extends BlockBreakingMovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.getValue(SawBlock.FACING)
			.getOpposite());
	}

	@Override
	public Vector3d getActiveAreaOffset(MovementContext context) {
		return Vector3d.atLowerCornerOf(context.state.getValue(SawBlock.FACING).getNormal()).scale(.65f);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		super.visitNewPosition(context, pos);
		Vector3d facingVec = Vector3d.atLowerCornerOf(context.state.getValue(SawBlock.FACING).getNormal());
		facingVec = context.rotation.apply(facingVec);

		Direction closestToFacing = Direction.getNearest(facingVec.x, facingVec.y, facingVec.z);
		if(closestToFacing.getAxis().isVertical() && context.data.contains("BreakingPos")) {
			context.data.remove("BreakingPos");
			context.stall = false;
		}
	}

	@Override
	public boolean canBreak(World world, BlockPos breakingPos, BlockState state) {
		return super.canBreak(world, breakingPos, state) && SawTileEntity.isSawable(state);
	}

	@Override
	protected void onBlockBroken(MovementContext context, BlockPos pos, BlockState brokenState) {
		if (brokenState.is(BlockTags.LEAVES))
			return;

		Optional<AbstractBlockBreakQueue> dynamicTree = TreeCutter.findDynamicTree(brokenState.getBlock(), pos);
		if (dynamicTree.isPresent()) {
			dynamicTree.get().destroyBlocks(context.world, null, (stack, dropPos) -> dropItemFromCutTree(context, stack, dropPos));
			return;
		}

		TreeCutter.findTree(context.world, pos).destroyBlocks(context.world, null, (stack, dropPos) -> dropItemFromCutTree(context, stack, dropPos));
	}

	public void dropItemFromCutTree(MovementContext context, BlockPos pos, ItemStack stack) {
		ItemStack remainder = ItemHandlerHelper.insertItem(context.contraption.inventory, stack, false);
		if (remainder.isEmpty())
			return;

		World world = context.world;
		Vector3d dropPos = VecHelper.getCenterOf(pos);
		float distance = context.position == null ? 1 : (float) dropPos.distanceTo(context.position);
		ItemEntity entity = new ItemEntity(world, dropPos.x, dropPos.y, dropPos.z, remainder);
		entity.setDeltaMovement(context.relativeMotion.scale(distance / 20f));
		world.addFreshEntity(entity);
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void renderInContraption(MovementContext context, PlacementSimulationWorld renderWorld,
									ContraptionMatrices matrices, IRenderTypeBuffer buffer) {
		SawRenderer.renderInContraption(context, renderWorld, matrices, buffer);
	}

	@Override
	protected boolean shouldDestroyStartBlock(BlockState stateToBreak) {
		return !TreeCutter.canDynamicTreeCutFrom(stateToBreak.getBlock());
	}

	@Override
	protected DamageSource getDamageSource() {
		return SawBlock.damageSourceSaw;
	}
}
