package com.simibubi.create.content.logistics.depot;

import java.util.Optional;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.logistics.depot.EjectorBlockEntity.State;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import net.createmod.catnip.api.platform.CatnipServices;
import com.simibubi.create.foundation.item.ItemHelper;

import net.createmod.catnip.api.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EjectorBlock extends HorizontalKineticBlock implements IBE<EjectorBlockEntity>, ProperWaterloggedBlock {

	public EjectorBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(WATERLOGGED));
	}

	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}

	@Override
	public BlockState updateShape(BlockState pState, LevelReader pLevel, ScheduledTickAccess ticks,
		BlockPos pCurrentPos, Direction pDirection, BlockPos pNeighborPos, BlockState pNeighborState,
		RandomSource random) {
		updateWater(ticks, pLevel, pState, pCurrentPos);
		return pState;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		return withWater(super.getStateForPlacement(pContext), pContext);
	}

	@Override
	public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_,
		CollisionContext p_220053_4_) {
		return AllShapes.CASING_13PX.get(Direction.UP);
	}

	@Override
	public float getFriction(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
		return getBlockEntityOptional(world, pos).filter(ete -> ete.state == State.LAUNCHING)
			.map($ -> 1f)
			.orElse(super.getFriction(state, world, pos, entity));
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block p_220069_4_, Orientation orientation,
		boolean p_220069_6_) {
		withBlockEntityDo(world, pos, EjectorBlockEntity::updateSignal);
	}

	@Override
	public void fallOn(Level p_180658_1_, BlockState p_152427_, BlockPos p_180658_2_, Entity p_180658_3_,
		double p_180658_4_) {
		Optional<EjectorBlockEntity> blockEntityOptional = getBlockEntityOptional(p_180658_1_, p_180658_2_);
		if (blockEntityOptional.isPresent() && !p_180658_3_.isSuppressingBounce()) {
			p_180658_3_.causeFallDamage(p_180658_4_, 1.0F, p_180658_1_.damageSources().fall());
		} else {
			super.fallOn(p_180658_1_, p_152427_, p_180658_2_, p_180658_3_, p_180658_4_);
		}
		BlockPos position = p_180658_3_.getOnPosLegacy();
		if (!AllBlocks.WEIGHTED_EJECTOR.has(p_180658_1_.getBlockState(position)))
			return;
		if (!p_180658_3_.isAlive())
			return;
		if (p_180658_3_.isSuppressingBounce())
			return;
		if (!ItemHelper.fromItemEntity(p_180658_3_).isEmpty()) {
			SharedDepotBlockMethods.onLanded(p_180658_1_, p_180658_3_);
			return;
		}

		Optional<EjectorBlockEntity> teProvider = getBlockEntityOptional(p_180658_1_, position);
		if (!teProvider.isPresent())
			return;

		EjectorBlockEntity ejectorBlockEntity = teProvider.get();
		if (ejectorBlockEntity.getState() == State.RETRACTING)
			return;
		if (ejectorBlockEntity.powered)
			return;
		if (ejectorBlockEntity.launcher.getHorizontalDistance() == 0)
			return;

		if (p_180658_3_.onGround()) {
			p_180658_3_.setOnGround(false);
			Vec3 center = VecHelper.getCenterOf(position)
				.add(0, 7 / 16f, 0);
			Vec3 positionVec = p_180658_3_.position();
			double diff = center.distanceTo(positionVec);
			p_180658_3_.setDeltaMovement(0, -0.125, 0);
			Vec3 vec = center.add(positionVec)
				.scale(.5f);
			if (diff > 4 / 16f) {
				p_180658_3_.setPos(vec.x, vec.y, vec.z);
				return;
			}
		}

		ejectorBlockEntity.activate();
		ejectorBlockEntity.notifyUpdate();
		if (p_180658_3_.level().isClientSide())
			net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(new EjectorTriggerPacket(ejectorBlockEntity.getBlockPos()));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (AllItems.WRENCH.isIn(stack))
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		return SharedDepotBlockMethods.onUse(stack, state, level, pos, player, hand, hitResult);
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
	public Class<EjectorBlockEntity> getBlockEntityClass() {
		return EjectorBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends EjectorBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.WEIGHTED_EJECTOR.get();
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos, Direction direction) {
		return SharedDepotBlockMethods.getComparatorInputOverride(blockState, worldIn, pos);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

}
