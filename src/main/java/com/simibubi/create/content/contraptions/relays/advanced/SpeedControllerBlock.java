package com.simibubi.create.content.contraptions.relays.advanced;

import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SpeedControllerBlock extends HorizontalAxisKineticBlock implements ITE<SpeedControllerTileEntity> {

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	public SpeedControllerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.ROTATION_SPEED_CONTROLLER.create();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState above = context.getLevel()
			.getBlockState(context.getClickedPos()
				.above());
		if (ICogWheel.isLargeCog(above) && above.getValue(CogWheelBlock.AXIS)
			.isHorizontal())
			return defaultBlockState().setValue(HORIZONTAL_AXIS, above.getValue(CogWheelBlock.AXIS) == Axis.X ? Axis.Z : Axis.X);
		return super.getStateForPlacement(context);
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block p_220069_4_, BlockPos neighbourPos,
		boolean p_220069_6_) {
		if (neighbourPos.equals(pos.above()))
			withTileEntityDo(world, pos, SpeedControllerTileEntity::updateBracket);
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult ray) {

		ItemStack heldItem = player.getItemInHand(hand);
		IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
		if (helper.matchesItem(heldItem))
			return helper.getOffset(player, world, state, pos, ray).placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);

		return ActionResultType.PASS;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
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
		public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockRayTraceResult ray) {
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
	public Class<SpeedControllerTileEntity> getTileEntityClass() {
		return SpeedControllerTileEntity.class;
	}
}
