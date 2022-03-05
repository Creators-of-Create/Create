package com.simibubi.create.content.schematics.block;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import io.github.fabricators_of_create.porting_lib.util.NetworkUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SchematicTableBlock extends HorizontalDirectionalBlock implements ITE<SchematicTableTileEntity> {

	public SchematicTableBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos,
			CollisionContext context) {
		return AllShapes.TABLE_POLE_SHAPE;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.SCHEMATICS_TABLE.get(state.getValue(FACING));
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult hit) {
		if (worldIn.isClientSide)
			return InteractionResult.SUCCESS;

		withTileEntityDo(worldIn, pos,
				te -> NetworkUtil.openGui((ServerPlayer) player, te, te::sendToContainer));
		return InteractionResult.SUCCESS;
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
			return;

		withTileEntityDo(worldIn, pos, te -> ItemHelper.dropContents(worldIn, pos, te.inventory));
		worldIn.removeBlockEntity(pos);
	}

	@Override
	public Class<SchematicTableTileEntity> getTileEntityClass() {
		return SchematicTableTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends SchematicTableTileEntity> getTileEntityType() {
		return AllTileEntities.SCHEMATIC_TABLE.get();
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

}
