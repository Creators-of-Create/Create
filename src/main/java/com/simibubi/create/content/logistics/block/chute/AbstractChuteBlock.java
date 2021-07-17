package com.simibubi.create.content.logistics.block.chute;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;

import net.minecraft.block.AbstractBlock.Properties;

public abstract class AbstractChuteBlock extends Block implements IWrenchable, ITE<ChuteTileEntity> {

	public AbstractChuteBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	public static boolean isChute(BlockState state) {
		return state.getBlock() instanceof AbstractChuteBlock;
	}

	public static boolean isOpenChute(BlockState state) {
		return isChute(state) && ((AbstractChuteBlock) state.getBlock()).isOpen(state);
	}

	public static boolean isTransparentChute(BlockState state) {
		return isChute(state) && ((AbstractChuteBlock) state.getBlock()).isTransparent(state);
	}

	@Nullable
	public static Direction getChuteFacing(BlockState state) {
		return !isChute(state) ? null : ((AbstractChuteBlock) state.getBlock()).getFacing(state);
	}

	public Direction getFacing(BlockState state) {
		return Direction.DOWN;
	}

	public boolean isOpen(BlockState state) {
		return true;
	}

	public boolean isTransparent(BlockState state) {
		return false;
	}

	@Override
	public abstract TileEntity createTileEntity(BlockState state, IBlockReader world);

	@Override
	public void updateEntityAfterFallOn(IBlockReader worldIn, Entity entityIn) {
		super.updateEntityAfterFallOn(worldIn, entityIn);
		if (!(entityIn instanceof ItemEntity))
			return;
		if (entityIn.level.isClientSide)
			return;
		if (!entityIn.isAlive())
			return;
		DirectBeltInputBehaviour input = TileEntityBehaviour.get(entityIn.level, new BlockPos(entityIn.position()
			.add(0, 0.5f, 0)).below(), DirectBeltInputBehaviour.TYPE);
		if (input == null)
			return;
		if (!input.canInsertFromSide(Direction.UP))
			return;

		ItemEntity itemEntity = (ItemEntity) entityIn;
		ItemStack toInsert = itemEntity.getItem();
		ItemStack remainder = input.handleInsertion(toInsert, Direction.UP, false);

		if (remainder.isEmpty())
			itemEntity.remove();
		if (remainder.getCount() < toInsert.getCount())
			itemEntity.setItem(remainder);
	}

	@Override
	public void onPlace(BlockState state, World world, BlockPos pos, BlockState pOldState, boolean pIsMoving) {
		withTileEntityDo(world, pos, ChuteTileEntity::onAdded);
		if (pIsMoving)
			return;
		updateDiagonalNeighbour(state, world, pos);
	}

	protected void updateDiagonalNeighbour(BlockState state, World world, BlockPos pos) {
		if (!isChute(state))
			return;
		AbstractChuteBlock block = (AbstractChuteBlock) state.getBlock();
		Direction facing = block.getFacing(state);
		BlockPos toUpdate = pos.below();
		if (facing.getAxis()
			.isHorizontal())
			toUpdate = toUpdate.relative(facing.getOpposite());

		BlockState stateToUpdate = world.getBlockState(toUpdate);
		BlockState updated = updateChuteState(stateToUpdate, world.getBlockState(toUpdate.above()), world, toUpdate);
		if (stateToUpdate != updated && !world.isClientSide)
			world.setBlockAndUpdate(toUpdate, updated);
	}

	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
		boolean differentBlock = state.getBlock() != pNewState.getBlock();
		if (state.hasTileEntity() && (differentBlock || !pNewState.hasTileEntity())) {
			TileEntityBehaviour.destroy(world, pos, FilteringBehaviour.TYPE);
			withTileEntityDo(world, pos, c -> c.onRemoved(state));
			world.removeBlockEntity(pos);
		}
		if (pIsMoving || !differentBlock)
			return;

		updateDiagonalNeighbour(state, world, pos);

		for (Direction direction : Iterate.horizontalDirections) {
			BlockPos toUpdate = pos.above()
				.relative(direction);
			BlockState stateToUpdate = world.getBlockState(toUpdate);
			if (!isChute(stateToUpdate))
				continue;
			BlockState updated = ((AbstractChuteBlock) stateToUpdate.getBlock()).updateChuteState(stateToUpdate,
				world.getBlockState(toUpdate.above()), world, toUpdate);
			if (stateToUpdate != updated && !world.isClientSide)
				world.setBlockAndUpdate(toUpdate, updated);
		}
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState above, IWorld world,
		BlockPos pos, BlockPos pFacingPos) {
		if (direction != Direction.UP)
			return state;
		return updateChuteState(state, above, world, pos);
	}

	@Override
	public void neighborChanged(BlockState pState, World world, BlockPos pos, Block pBlockIn,
		BlockPos neighbourPos, boolean pIsMoving) {
		if (pos.below()
			.equals(neighbourPos))
			withTileEntityDo(world, pos, ChuteTileEntity::blockBelowChanged);
		else if (pos.above()
			.equals(neighbourPos))
			withTileEntityDo(world, pos, chute -> chute.capAbove = LazyOptional.empty());
	}

	public abstract BlockState updateChuteState(BlockState state, BlockState above, IBlockReader world, BlockPos pos);

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
		BlockHelper.addReducedDestroyEffects(state, world, pos, manager);
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState pState, IBlockReader pWorldIn, BlockPos pPos,
		ISelectionContext pContext) {
		return ChuteShapes.getShape(pState);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState pState, IBlockReader pWorldIn, BlockPos pPos,
		ISelectionContext pContext) {
		return ChuteShapes.getCollisionShape(pState);
	}

	@Override
	public Class<ChuteTileEntity> getTileEntityClass() {
		return ChuteTileEntity.class;
	}

	@Override
	public ActionResultType use(BlockState pState, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult pHit) {
		if (!player.getItemInHand(hand)
				.isEmpty())
			return ActionResultType.PASS;
		if (world.isClientSide)
			return ActionResultType.SUCCESS;

		return onTileEntityUse(world, pos, te -> {
			if (te.item.isEmpty())
				return ActionResultType.PASS;
			player.inventory.placeItemBackInInventory(world, te.item);
			te.setItem(ItemStack.EMPTY);
			return ActionResultType.SUCCESS;
		});
	}

}
