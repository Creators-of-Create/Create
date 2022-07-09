package com.simibubi.create.content.logistics.block.chute;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.block.render.ReducedDestroyEffects;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IBlockRenderProperties;
import net.minecraftforge.common.util.LazyOptional;

public abstract class AbstractChuteBlock extends Block implements IWrenchable, ITE<ChuteTileEntity> {

	public AbstractChuteBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
	}
	
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IBlockRenderProperties> consumer) {
		consumer.accept(new ReducedDestroyEffects());
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
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
		AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
	}
	
	@Override
	public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn) {
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
			itemEntity.discard();
		if (remainder.getCount() < toInsert.getCount())
			itemEntity.setItem(remainder);
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
		withTileEntityDo(world, pos, ChuteTileEntity::onAdded);
		if (p_220082_5_)
			return;
		updateDiagonalNeighbour(state, world, pos);
	}

	protected void updateDiagonalNeighbour(BlockState state, Level world, BlockPos pos) {
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
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState p_196243_4_, boolean p_196243_5_) {
		boolean differentBlock = state.getBlock() != p_196243_4_.getBlock();
		if (state.hasBlockEntity() && (differentBlock || !p_196243_4_.hasBlockEntity())) {
			TileEntityBehaviour.destroy(world, pos, FilteringBehaviour.TYPE);
			withTileEntityDo(world, pos, c -> c.onRemoved(state));
			world.removeBlockEntity(pos);
		}
		if (p_196243_5_ || !differentBlock)
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
	public BlockState updateShape(BlockState state, Direction direction, BlockState above, LevelAccessor world,
		BlockPos pos, BlockPos p_196271_6_) {
		if (direction != Direction.UP)
			return state;
		return updateChuteState(state, above, world, pos);
	}

	@Override
	public void neighborChanged(BlockState p_220069_1_, Level world, BlockPos pos, Block p_220069_4_,
		BlockPos neighbourPos, boolean p_220069_6_) {
		if (pos.below()
			.equals(neighbourPos))
			withTileEntityDo(world, pos, ChuteTileEntity::blockBelowChanged);
		else if (pos.above()
			.equals(neighbourPos))
			withTileEntityDo(world, pos, chute -> chute.capAbove = LazyOptional.empty());
	}

	public abstract BlockState updateChuteState(BlockState state, BlockState above, BlockGetter world, BlockPos pos);

	@Override
	public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_,
		CollisionContext p_220053_4_) {
		return ChuteShapes.getShape(p_220053_1_);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState p_220071_1_, BlockGetter p_220071_2_, BlockPos p_220071_3_,
		CollisionContext p_220071_4_) {
		return ChuteShapes.getCollisionShape(p_220071_1_);
	}

	@Override
	public Class<ChuteTileEntity> getTileEntityClass() {
		return ChuteTileEntity.class;
	}

	@Override
	public InteractionResult use(BlockState p_225533_1_, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult p_225533_6_) {
		if (!player.getItemInHand(hand)
				.isEmpty())
			return InteractionResult.PASS;
		if (world.isClientSide)
			return InteractionResult.SUCCESS;

		return onTileEntityUse(world, pos, te -> {
			if (te.item.isEmpty())
				return InteractionResult.PASS;
			player.getInventory().placeItemBackInInventory(te.item);
			te.setItem(ItemStack.EMPTY);
			return InteractionResult.SUCCESS;
		});
	}

}
