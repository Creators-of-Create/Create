package com.simibubi.create.content.contraptions.relays.elementary;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftBlock;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class ShaftBlock extends AbstractShaftBlock {

	public ShaftBlock(Properties properties) {
		super(properties);
	}

	public static boolean isShaft(BlockState state) {
		return AllBlocks.SHAFT.has(state);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.SIX_VOXEL_POLE.get(state.get(AXIS));
	}

	@Override
	public float getParticleTargetRadius() {
		return .25f;
	}

	@Override
	public float getParticleInitialRadius() {
		return 0f;
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult p_225533_6_) {
		if (player.isSneaking() || !player.isAllowEdit())
			return ActionResultType.PASS;

		ItemStack heldItem = player.getHeldItem(hand);
		for (EncasedShaftBlock encasedShaft : new EncasedShaftBlock[] { AllBlocks.ANDESITE_ENCASED_SHAFT.get(),
			AllBlocks.BRASS_ENCASED_SHAFT.get() }) {

			if (!encasedShaft.getCasing()
				.isIn(heldItem))
				continue;

			if (world.isRemote)
				return ActionResultType.SUCCESS;

			KineticTileEntity.switchToBlockState(world, pos, encasedShaft.getDefaultState()
				.with(AXIS, state.get(AXIS)));
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}
}
