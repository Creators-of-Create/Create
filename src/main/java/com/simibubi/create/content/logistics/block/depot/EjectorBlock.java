package com.simibubi.create.content.logistics.block.depot;

import java.util.Optional;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.logistics.block.depot.EjectorTileEntity.State;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.lib.extensions.BlockExtensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EjectorBlock extends HorizontalKineticBlock implements ITE<EjectorTileEntity>, BlockExtensions {

	public EjectorBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_,
		CollisionContext p_220053_4_) {
		return AllShapes.DEPOT;
	}

	@Override
	public float create$getSlipperiness(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
		return getTileEntityOptional(world, pos).filter(ete -> ete.state == State.LAUNCHING)
			.map($ -> 1f)
			.orElse(super.getFriction());
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block p_220069_4_, BlockPos p_220069_5_,
		boolean p_220069_6_) {
		withTileEntityDo(world, pos, EjectorTileEntity::updateSignal);
	}

	@Override
	public void fallOn(Level p_180658_1_, BlockState p_152427_, BlockPos p_180658_2_, Entity p_180658_3_,
		float p_180658_4_) {
		Optional<EjectorTileEntity> tileEntityOptional = getTileEntityOptional(p_180658_1_, p_180658_2_);
		if (tileEntityOptional.isPresent() && !p_180658_3_.isSuppressingBounce()) {
			p_180658_3_.causeFallDamage(p_180658_4_, 1.0F, DamageSource.FALL);
			return;
		}
		super.fallOn(p_180658_1_, p_152427_, p_180658_2_, p_180658_3_, p_180658_4_);
	}

	@Override
	public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn) {
		super.updateEntityAfterFallOn(worldIn, entityIn);
		BlockPos position = entityIn.blockPosition();
		if (!AllBlocks.WEIGHTED_EJECTOR.has(worldIn.getBlockState(position)))
			return;
		if (!entityIn.isAlive())
			return;
		if (entityIn.isSuppressingBounce())
			return;
		if (entityIn instanceof ItemEntity) {
			SharedDepotBlockMethods.onLanded(worldIn, entityIn);
			return;
		}

		Optional<EjectorTileEntity> teProvider = getTileEntityOptional(worldIn, position);
		if (!teProvider.isPresent())
			return;

		EjectorTileEntity ejectorTileEntity = teProvider.get();
		if (ejectorTileEntity.getState() == State.RETRACTING)
			return;
		if (ejectorTileEntity.powered)
			return;
		if (ejectorTileEntity.launcher.getHorizontalDistance() == 0)
			return;

		if (entityIn.isOnGround()) {
			entityIn.setOnGround(false);
			Vec3 center = VecHelper.getCenterOf(position)
				.add(0, 7 / 16f, 0);
			Vec3 positionVec = entityIn.position();
			double diff = center.distanceTo(positionVec);
			entityIn.setDeltaMovement(0, -0.125, 0);
			Vec3 vec = center.add(positionVec)
				.scale(.5f);
			if (diff > 4 / 16f) {
				entityIn.setPos(vec.x, vec.y, vec.z);
				return;
			}
		}

		ejectorTileEntity.activate();
		ejectorTileEntity.notifyUpdate();
		if (entityIn.level.isClientSide)
			AllPackets.channel.sendToServer(new EjectorTriggerPacket(ejectorTileEntity.getBlockPos()));
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult ray) {
		if (AllItems.WRENCH.isIn(player.getItemInHand(hand)))
			return InteractionResult.PASS;
		return SharedDepotBlockMethods.onUse(state, world, pos, player, hand, ray);
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		withTileEntityDo(worldIn, pos, EjectorTileEntity::dropFlyingItems);
		SharedDepotBlockMethods.onReplaced(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING)
			.getClockWise()
			.getAxis();
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return getRotationAxis(state) == face.getAxis();
	}

	@Override
	public Class<EjectorTileEntity> getTileEntityClass() {
		return EjectorTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends EjectorTileEntity> getTileEntityType() {
		return AllTileEntities.WEIGHTED_EJECTOR.get();
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
		return SharedDepotBlockMethods.getComparatorInputOverride(blockState, worldIn, pos);
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

}
