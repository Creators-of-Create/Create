package com.simibubi.create.compat.dynamictrees;

import net.minecraft.world.level.block.Block;

public class DynamicTree {
	public static boolean isDynamicBranch(Block startBlock) {
		return false;
	}
	/* extends AbstractBlockBreakQueue {
	private BlockPos startCutPos;

	public DynamicTree(BlockPos startCutPos) {
		this.startCutPos = startCutPos;
	}

	@Override
	public void destroyBlocks(Level world, ItemStack toDamage, @Nullable Player playerEntity, BiConsumer<BlockPos, ItemStack> drop) {
		BranchBlock start = TreeHelper.getBranch(world.getBlockState(startCutPos));
		if (start == null) //if start is null, it was not a branch
			start = setBranchToShellMuse(world, world.getBlockState(startCutPos)); //we check for a trunk shell instead

		if (start == null) //if it is null again, it was neither a branch nor a trunk shell and thus we return
			return;

		// Play and render block break sound and particles
		world.levelEvent(null, 2001, startCutPos, Block.getId(world.getBlockState(startCutPos)));
		// Actually breaks the tree

		BranchDestructionData data = start.destroyBranchFromNode(world, startCutPos, Direction.DOWN, false, playerEntity);

		// Feed all the tree drops to drop bi-consumer
		data.leavesDrops.forEach(stackPos -> drop.accept(stackPos.pos.offset(startCutPos), stackPos.stack));
		start.getLogDrops(world, startCutPos, data.species, data.woodVolume).forEach(stack -> drop.accept(startCutPos, stack));
	}

	private BranchBlock setBranchToShellMuse(Level world, BlockState state){
		Block block = state.getBlock();
		if (block instanceof TrunkShellBlock){
			TrunkShellBlock.ShellMuse muse = ((TrunkShellBlock)block).getMuse(world, startCutPos);
			if (muse != null){
				startCutPos = muse.pos; //the cut pos is moved to the center of the trunk
				return TreeHelper.getBranch(muse.state);
			}
		}
		return null;
	}

	public static boolean isDynamicBranch(Block block) {
		return TreeHelper.isBranch(block) || block instanceof TrunkShellBlock;
	}*/
}
