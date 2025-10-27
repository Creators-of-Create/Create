package com.simibubi.create.content.kinetics.steamEngine;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PoweredShaftBlock extends AbstractShaftBlock {

	public PoweredShaftBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.EIGHT_VOXEL_POLE.get(pState.getValue(AXIS));
	}

	@Override
	public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.POWERED_SHAFT.get();
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (player.isShiftKeyDown() || !player.mayBuild())
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		IPlacementHelper helper = PlacementHelpers.get(ShaftBlock.placementHelperId);
		if (helper.matchesItem(stack))
			return helper.getOffset(player, level, state, pos, hitResult)
				.placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult);

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
		if (!stillValid(pState, pLevel, pPos))
			pLevel.setBlock(pPos, AllBlocks.SHAFT.getDefaultState()
				.setValue(ShaftBlock.AXIS, pState.getValue(AXIS))
				.setValue(WATERLOGGED, pState.getValue(WATERLOGGED)), Block.UPDATE_ALL);
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
		return AllBlocks.SHAFT.asStack();
	}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		return stillValid(pState, pLevel, pPos);
	}

	public static boolean stillValid(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		for (Direction d : Iterate.directions) {
			if (d.getAxis() == pState.getValue(AXIS))
				continue;
			BlockPos enginePos = pPos.relative(d, 2);
			BlockState engineState = pLevel.getBlockState(enginePos);
			if (!(engineState.getBlock()instanceof SteamEngineBlock engine))
				continue;
			if (!SteamEngineBlock.getShaftPos(engineState, enginePos)
				.equals(pPos))
				continue;
			if (SteamEngineBlock.isShaftValid(engineState, pState))
				return true;
		}
		return false;
	}

	public static BlockState getEquivalent(BlockState stateForPlacement) {
		return AllBlocks.POWERED_SHAFT.getDefaultState()
			.setValue(PoweredShaftBlock.AXIS, stateForPlacement.getValue(ShaftBlock.AXIS))
			.setValue(WATERLOGGED, stateForPlacement.getValue(WATERLOGGED));
	}

}
