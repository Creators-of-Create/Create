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
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		super.onLanded(worldIn, entityIn);
		if (!(entityIn instanceof ItemEntity))
			return;
		if (entityIn.world.isRemote)
			return;
		if (!entityIn.isAlive())
			return;
		DirectBeltInputBehaviour input = TileEntityBehaviour.get(entityIn.world, new BlockPos(entityIn.getPositionVec()
			.add(0, 0.5f, 0)).down(), DirectBeltInputBehaviour.TYPE);
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
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
		withTileEntityDo(world, pos, ChuteTileEntity::onAdded);
		if (p_220082_5_)
			return;
		updateDiagonalNeighbour(state, world, pos);
	}

	protected void updateDiagonalNeighbour(BlockState state, World world, BlockPos pos) {
		if (!isChute(state))
			return;
		AbstractChuteBlock block = (AbstractChuteBlock) state.getBlock();
		Direction facing = block.getFacing(state);
		BlockPos toUpdate = pos.down();
		if (facing.getAxis()
			.isHorizontal())
			toUpdate = toUpdate.offset(facing.getOpposite());

		BlockState stateToUpdate = world.getBlockState(toUpdate);
		BlockState updated = updateChuteState(stateToUpdate, world.getBlockState(toUpdate.up()), world, toUpdate);
		if (stateToUpdate != updated && !world.isRemote)
			world.setBlockState(toUpdate, updated);
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState p_196243_4_, boolean p_196243_5_) {
		boolean differentBlock = state.getBlock() != p_196243_4_.getBlock();
		if (state.hasTileEntity() && (differentBlock || !p_196243_4_.hasTileEntity())) {
			TileEntityBehaviour.destroy(world, pos, FilteringBehaviour.TYPE);
			withTileEntityDo(world, pos, c -> c.onRemoved(state));
			world.removeTileEntity(pos);
		}
		if (p_196243_5_ || !differentBlock)
			return;

		updateDiagonalNeighbour(state, world, pos);

		for (Direction direction : Iterate.horizontalDirections) {
			BlockPos toUpdate = pos.up()
				.offset(direction);
			BlockState stateToUpdate = world.getBlockState(toUpdate);
			if (!isChute(stateToUpdate))
				continue;
			BlockState updated = ((AbstractChuteBlock) stateToUpdate.getBlock()).updateChuteState(stateToUpdate,
				world.getBlockState(toUpdate.up()), world, toUpdate);
			if (stateToUpdate != updated && !world.isRemote)
				world.setBlockState(toUpdate, updated);
		}
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState above, IWorld world,
		BlockPos pos, BlockPos p_196271_6_) {
		if (direction != Direction.UP)
			return state;
		return updateChuteState(state, above, world, pos);
	}

	@Override
	public void neighborChanged(BlockState p_220069_1_, World world, BlockPos pos, Block p_220069_4_,
		BlockPos neighbourPos, boolean p_220069_6_) {
		if (pos.down()
			.equals(neighbourPos))
			withTileEntityDo(world, pos, ChuteTileEntity::blockBelowChanged);
		else if (pos.up()
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
	public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return ChuteShapes.getShape(p_220053_1_);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState p_220071_1_, IBlockReader p_220071_2_, BlockPos p_220071_3_,
		ISelectionContext p_220071_4_) {
		return ChuteShapes.getCollisionShape(p_220071_1_);
	}

	@Override
	public Class<ChuteTileEntity> getTileEntityClass() {
		return ChuteTileEntity.class;
	}

	@Override
	public ActionResultType onUse(BlockState p_225533_1_, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult p_225533_6_) {
		if (!player.getHeldItem(hand)
			.isEmpty())
			return ActionResultType.PASS;
		if (world.isRemote)
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
