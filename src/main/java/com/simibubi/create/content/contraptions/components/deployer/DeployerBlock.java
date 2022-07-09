package com.simibubi.create.content.contraptions.components.deployer;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.components.AssemblyOperatorUseContext;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DeployerBlock extends DirectionalAxisKineticBlock implements ITE<DeployerTileEntity> {

	public DeployerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.NORMAL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.CASING_12PX.get(state.getValue(FACING));
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		if (context.getClickedFace() == state.getValue(FACING)) {
			if (!context.getLevel().isClientSide)
				withTileEntityDo(context.getLevel(), context.getClickedPos(), DeployerTileEntity::changeMode);
			return InteractionResult.SUCCESS;
		}
		return super.onWrenched(state, context);
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.hasBlockEntity() && state.getBlock() != newState.getBlock()) {
			withTileEntityDo(worldIn, pos, te -> {
				if (te.player != null && !isMoving) {
					te.player.getInventory()
						.dropAll();
					te.overflowItems.forEach(itemstack -> te.player.drop(itemstack, true, false));
					te.player.discard();
					te.player = null;
				}
			});

			TileEntityBehaviour.destroy(worldIn, pos, FilteringBehaviour.TYPE);
			worldIn.removeBlockEntity(pos);
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		ItemStack heldByPlayer = player.getItemInHand(handIn)
			.copy();
		if (AllItems.WRENCH.isIn(heldByPlayer))
			return InteractionResult.PASS;

		if (hit.getDirection() != state.getValue(FACING))
			return InteractionResult.PASS;
		if (worldIn.isClientSide)
			return InteractionResult.SUCCESS;

		withTileEntityDo(worldIn, pos, te -> {
			ItemStack heldByDeployer = te.player.getMainHandItem()
				.copy();
			if (heldByDeployer.isEmpty() && heldByPlayer.isEmpty())
				return;

			player.setItemInHand(handIn, heldByDeployer);
			te.player.setItemInHand(InteractionHand.MAIN_HAND, heldByPlayer);
			te.sendData();
		});

		return InteractionResult.SUCCESS;
	}

	@Override
	public Class<DeployerTileEntity> getTileEntityClass() {
		return DeployerTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends DeployerTileEntity> getTileEntityType() {
		return AllTileEntities.DEPLOYER.get();
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, world, pos, oldState, isMoving);
		withTileEntityDo(world, pos, DeployerTileEntity::redstoneUpdate);
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block p_220069_4_, BlockPos p_220069_5_,
		boolean p_220069_6_) {
		withTileEntityDo(world, pos, DeployerTileEntity::redstoneUpdate);
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

	@Override
	protected Direction getFacingForPlacement(BlockPlaceContext context) {
		if (context instanceof AssemblyOperatorUseContext)
			return Direction.DOWN;
		else
			return super.getFacingForPlacement(context);
	}
}
