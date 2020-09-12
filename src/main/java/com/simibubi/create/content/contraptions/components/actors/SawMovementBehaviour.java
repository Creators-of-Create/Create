package com.simibubi.create.content.contraptions.components.actors;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.saw.SawBlock;
import com.simibubi.create.content.contraptions.components.saw.SawRenderer;
import com.simibubi.create.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.TreeCutter;
import com.simibubi.create.foundation.utility.TreeCutter.Tree;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public class SawMovementBehaviour extends BlockBreakingMovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.get(SawBlock.FACING)
			.getOpposite());
	}

	@Override
	public Vec3d getActiveAreaOffset(MovementContext context) {
		return new Vec3d(context.state.get(SawBlock.FACING).getDirectionVec()).scale(.65f);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		super.visitNewPosition(context, pos);
		if(!SawBlock.isHorizontal(context.state) && context.data.contains("BreakingPos")) {
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
		if (brokenState.isIn(BlockTags.LEAVES))
			return;
		Tree tree = TreeCutter.cutTree(context.world, pos);
		if (tree != null) {
			for (BlockPos log : tree.logs)
				BlockHelper.destroyBlock(context.world, log, 1 / 2f, stack -> dropItemFromCutTree(context, log, stack));
			for (BlockPos leaf : tree.leaves)
				BlockHelper.destroyBlock(context.world, leaf, 1 / 8f,
						stack -> dropItemFromCutTree(context, leaf, stack));
		}
	}

	public void dropItemFromCutTree(MovementContext context, BlockPos pos, ItemStack stack) {
		ItemStack remainder = ItemHandlerHelper.insertItem(context.contraption.inventory, stack, false);
		if (remainder.isEmpty())
			return;

		World world = context.world;
		Vec3d dropPos = VecHelper.getCenterOf(pos);
		float distance = (float) dropPos.distanceTo(context.position);
		ItemEntity entity = new ItemEntity(world, dropPos.x, dropPos.y, dropPos.z, remainder);
		entity.setMotion(context.relativeMotion.scale(distance / 20f));
		world.addEntity(entity);
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void renderInContraption(MovementContext context, MatrixStack ms, MatrixStack msLocal,
									IRenderTypeBuffer buffer) {
		SawRenderer.renderInContraption(context, ms, msLocal, buffer);
	}

	@Override
	protected DamageSource getDamageSource() {
		return SawBlock.damageSourceSaw;
	}
}
