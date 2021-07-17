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

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DeployerBlock extends DirectionalAxisKineticBlock implements ITE<DeployerTileEntity> {

	public DeployerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.DEPLOYER.create();
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state) {
		return PushReaction.NORMAL;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.CASING_12PX.get(state.getValue(FACING));
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (context.getClickedFace() == state.getValue(FACING)) {
			if (!context.getLevel().isClientSide)
				withTileEntityDo(context.getLevel(), context.getClickedPos(), DeployerTileEntity::changeMode);
			return ActionResultType.SUCCESS;
		}
		return super.onWrenched(state, context);
	}

	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			withTileEntityDo(worldIn, pos, te -> {
				if (te.player != null && !isMoving) {
					te.player.inventory.dropAll();
					te.overflowItems.forEach(itemstack -> te.player.drop(itemstack, true, false));
					te.player.remove();
					te.player = null;
				}
			});

			TileEntityBehaviour.destroy(worldIn, pos, FilteringBehaviour.TYPE);
			worldIn.removeBlockEntity(pos);
		}
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		ItemStack heldByPlayer = player.getItemInHand(handIn)
			.copy();
		if (AllItems.WRENCH.isIn(heldByPlayer))
			return ActionResultType.PASS;

		if (hit.getDirection() != state.getValue(FACING))
			return ActionResultType.PASS;
		if (worldIn.isClientSide)
			return ActionResultType.SUCCESS;

		withTileEntityDo(worldIn, pos, te -> {
			ItemStack heldByDeployer = te.player.getMainHandItem()
				.copy();
			if (heldByDeployer.isEmpty() && heldByPlayer.isEmpty())
				return;

			player.setItemInHand(handIn, heldByDeployer);
			te.player.setItemInHand(Hand.MAIN_HAND, heldByPlayer);
			te.sendData();
		});

		return ActionResultType.SUCCESS;
	}

	@Override
	public Class<DeployerTileEntity> getTileEntityClass() {
		return DeployerTileEntity.class;
	}

	@Override
	public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, world, pos, oldState, isMoving);
		withTileEntityDo(world, pos, DeployerTileEntity::redstoneUpdate);
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block p_220069_4_,
		BlockPos p_220069_5_, boolean p_220069_6_) {
		withTileEntityDo(world, pos, DeployerTileEntity::redstoneUpdate);
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

	@Override
	protected Direction getFacingForPlacement(BlockItemUseContext context) {
		if (context instanceof AssemblyOperatorUseContext) return Direction.DOWN;
		else return super.getFacingForPlacement(context);
	}
}
