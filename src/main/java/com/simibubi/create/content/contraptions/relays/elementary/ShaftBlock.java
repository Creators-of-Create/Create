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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShaftBlock extends AbstractShaftBlock {

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	public ShaftBlock(Properties properties) {
		super(properties);
	}

	public static boolean isShaft(BlockState state) {
		return AllBlocks.SHAFT.has(state);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.SIX_VOXEL_POLE.get(state.getValue(AXIS));
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
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult ray) {
		if (player.isShiftKeyDown() || !player.mayBuild())
			return InteractionResult.PASS;

		ItemStack heldItem = player.getItemInHand(hand);
		for (EncasedShaftBlock encasedShaft : new EncasedShaftBlock[] { AllBlocks.ANDESITE_ENCASED_SHAFT.get(),
			AllBlocks.BRASS_ENCASED_SHAFT.get() }) {

			if (!encasedShaft.getCasing()
				.isIn(heldItem))
				continue;

			if (world.isClientSide)
				return InteractionResult.SUCCESS;
			
			AllTriggers.triggerFor(AllTriggers.CASING_SHAFT, player);
			KineticTileEntity.switchToBlockState(world, pos, encasedShaft.defaultBlockState()
				.setValue(AXIS, state.getValue(AXIS)));
			return InteractionResult.SUCCESS;
		}

		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		if (helper.matchesItem(heldItem))
			return helper.getOffset(player, world, state, pos, ray).placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);

		return InteractionResult.PASS;
	}

	@MethodsReturnNonnullByDefault
	private static class PlacementHelper extends PoleHelper<Direction.Axis> {
		//used for extending a shaft in its axis, like the piston poles. works with shafts and cogs

		private PlacementHelper(){
			super(
					state -> state.getBlock() instanceof AbstractShaftBlock,
					state -> state.getValue(AXIS),
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
