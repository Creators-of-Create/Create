package com.simibubi.create.content.contraptions.relays.elementary;

import java.util.function.Predicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.util.PoleHelper;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class ShaftBlock extends AbstractShaftBlock {

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

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
		return .35f;
	}

	@Override
	public float getParticleInitialRadius() {
		return .125f;
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult ray) {
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
			
			AllTriggers.triggerFor(AllTriggers.CASING_SHAFT, player);
			KineticTileEntity.switchToBlockState(world, pos, encasedShaft.getDefaultState()
				.with(AXIS, state.get(AXIS)));
			return ActionResultType.SUCCESS;
		}

		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		if (helper.matchesItem(heldItem))
			return helper.getOffset(world, state, pos, ray).placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);

		return ActionResultType.PASS;
	}

	@MethodsReturnNonnullByDefault
	private static class PlacementHelper extends PoleHelper<Direction.Axis> {
		//used for extending a shaft in its axis, like the piston poles. works with shafts and cogs

		private PlacementHelper(){
			super(
					state -> state.getBlock() instanceof AbstractShaftBlock,
					state -> state.get(AXIS),
					AXIS
			);
		}

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return i -> i.getItem() instanceof BlockItem && ((BlockItem) i.getItem()).getBlock() instanceof AbstractShaftBlock;
		}

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return AllBlocks.SHAFT::has;
		}
	}
}
