package com.simibubi.create.content.kinetics.speedController;

import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;

import net.createmod.catnip.utility.placement.IPlacementHelper;
import net.createmod.catnip.utility.placement.PlacementHelpers;
import net.createmod.catnip.utility.placement.PlacementOffset;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SpeedControllerBlock extends HorizontalAxisKineticBlock implements IBE<SpeedControllerBlockEntity> {

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	public SpeedControllerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState above = context.getLevel()
			.getBlockState(context.getClickedPos()
				.above());
		if (ICogWheel.isLargeCog(above) && above.getValue(CogWheelBlock.AXIS)
			.isHorizontal())
			return defaultBlockState().setValue(HORIZONTAL_AXIS, above.getValue(CogWheelBlock.AXIS) == Axis.X ? Axis.Z : Axis.X);
		return super.getStateForPlacement(context);
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block p_220069_4_, BlockPos neighbourPos,
		boolean p_220069_6_) {
		if (neighbourPos.equals(pos.above()))
			withBlockEntityDo(world, pos, SpeedControllerBlockEntity::updateBracket);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult ray) {

		ItemStack heldItem = player.getItemInHand(hand);
		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		if (helper.matchesItem(heldItem))
			return helper.getOffset(player, world, state, pos, ray).placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);

		return InteractionResult.PASS;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.SPEED_CONTROLLER;
	}

	@MethodsReturnNonnullByDefault
	private static class PlacementHelper implements IPlacementHelper {
		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return ((Predicate<ItemStack>) ICogWheel::isLargeCogItem).and(ICogWheel::isDedicatedCogItem);
		}

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return AllBlocks.ROTATION_SPEED_CONTROLLER::has;
		}

		@Override
		public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
			BlockPos newPos = pos.above();
			if (!world.getBlockState(newPos)
				.getMaterial()
				.isReplaceable())
				return PlacementOffset.fail();

			Axis newAxis = state.getValue(HORIZONTAL_AXIS) == Axis.X ? Axis.Z : Axis.X;

			if (!CogWheelBlock.isValidCogwheelPosition(true, world, newPos, newAxis))
				return PlacementOffset.fail();

			return PlacementOffset.success(newPos, s -> s.setValue(CogWheelBlock.AXIS, newAxis));
		}
	}

	@Override
	public Class<SpeedControllerBlockEntity> getBlockEntityClass() {
		return SpeedControllerBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends SpeedControllerBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.ROTATION_SPEED_CONTROLLER.get();
	}
}
