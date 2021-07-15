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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public class EjectorBlock extends HorizontalKineticBlock implements ITE<EjectorTileEntity> {

	public EjectorBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.DEPOT;
	}

	@Override
	public float getSlipperiness(BlockState state, IWorldReader world, BlockPos pos, Entity entity) {
		return getTileEntityOptional(world, pos).filter(ete -> ete.state == State.LAUNCHING)
			.map($ -> 1f)
			.orElse(super.getSlipperiness(state, world, pos, entity));
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block p_220069_4_,
		BlockPos p_220069_5_, boolean p_220069_6_) {
		withTileEntityDo(world, pos, EjectorTileEntity::updateSignal);
	}
	
	@Override
	public void fallOn(World p_180658_1_, BlockPos p_180658_2_, Entity p_180658_3_, float p_180658_4_) {
		Optional<EjectorTileEntity> tileEntityOptional = getTileEntityOptional(p_180658_1_, p_180658_2_);
		if (tileEntityOptional.isPresent() && !p_180658_3_.isSuppressingBounce()) {
			p_180658_3_.causeFallDamage(p_180658_4_, 0.0F);
			return;
		}
		super.fallOn(p_180658_1_, p_180658_2_, p_180658_3_, p_180658_4_);
	}

	@Override
	public void updateEntityAfterFallOn(IBlockReader worldIn, Entity entityIn) {
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

		if (entityIn.onGround) {
			entityIn.onGround = false;
			Vector3d center = VecHelper.getCenterOf(position)
				.add(0, 7 / 16f, 0);
			Vector3d positionVec = entityIn.position();
			double diff = center.distanceTo(positionVec);
			entityIn.setDeltaMovement(0, -0.125, 0);
			Vector3d vec = center.add(positionVec)
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
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult ray) {
		if (AllItems.WRENCH.isIn(player.getItemInHand(hand)))
			return ActionResultType.PASS;
		return SharedDepotBlockMethods.onUse(state, world, pos, player, hand, ray);
	}

	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
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
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return getRotationAxis(state) == face.getAxis();
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.WEIGHTED_EJECTOR.create();
	}

	@Override
	public Class<EjectorTileEntity> getTileEntityClass() {
		return EjectorTileEntity.class;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
		return SharedDepotBlockMethods.getComparatorInputOverride(blockState, worldIn, pos);
	}
	
	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

}
