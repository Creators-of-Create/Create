package com.simibubi.create.modules.contraptions.components.actors;

import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.TreeCutter;
import com.simibubi.create.foundation.utility.TreeCutter.Tree;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.components.contraptions.MovementContext;
import com.simibubi.create.modules.contraptions.components.saw.SawBlock;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class SawMovementBehaviour extends BlockBreakingMovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return SawBlock.isHorizontal(context.state);
	}

	@Override
	public Vec3d getActiveAreaOffset(MovementContext context) {
		return new Vec3d(context.state.get(SawBlock.FACING).getDirectionVec()).scale(.65f);
	}

	@Override
	public boolean canBreak(World world, BlockPos breakingPos, BlockState state) {
		return super.canBreak(world, breakingPos, state) && state.isIn(BlockTags.LOGS);
	}

	@Override
	protected void onBlockBroken(MovementContext context, BlockPos pos) {
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
	protected DamageSource getDamageSource() {
		return SawBlock.damageSourceSaw;
	}
}
