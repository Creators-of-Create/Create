package com.simibubi.create.content.kinetics.waterwheel;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.goggles.IProxyHoveringInformation;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.render.MultiPosDestructionHandler;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

public class WaterWheelStructuralBlock extends DirectionalBlock implements IWrenchable, IProxyHoveringInformation {

	public WaterWheelStructuralBlock(Properties p_52591_) {
		super(p_52591_);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(FACING));
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState pState) {
		return PushReaction.BLOCK;
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return InteractionResult.PASS;
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
		return AllBlocks.LARGE_WATER_WHEEL.asStack();
	}

	@Override
	public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
		BlockPos clickedPos = context.getClickedPos();
		Level level = context.getLevel();

		if (stillValid(level, clickedPos, state, false)) {
			BlockPos masterPos = getMaster(level, clickedPos, state);
			context = new UseOnContext(level, context.getPlayer(), context.getHand(), context.getItemInHand(),
				new BlockHitResult(context.getClickLocation(), context.getClickedFace(), masterPos,
					context.isInside()));
			state = level.getBlockState(masterPos);
		}

		return IWrenchable.super.onSneakWrenched(state, context);
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
		BlockHitResult pHit) {
		if (!stillValid(pLevel, pPos, pState, false))
			return InteractionResult.FAIL;
		if (!(pLevel.getBlockEntity(getMaster(pLevel, pPos, pState))instanceof WaterWheelBlockEntity wwt))
			return InteractionResult.FAIL;
		return wwt.applyMaterialIfValid(pPlayer.getItemInHand(pHand));
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		if (stillValid(pLevel, pPos, pState, false))
			pLevel.destroyBlock(getMaster(pLevel, pPos, pState), true);
	}

	public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
		if (stillValid(pLevel, pPos, pState, false)) {
			BlockPos masterPos = getMaster(pLevel, pPos, pState);
			pLevel.destroyBlockProgress(masterPos.hashCode(), masterPos, -1);
			if (!pLevel.isClientSide() && pPlayer.isCreative())
				pLevel.destroyBlock(masterPos, false);
		}
		super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel,
		BlockPos pCurrentPos, BlockPos pFacingPos) {
		if (stillValid(pLevel, pCurrentPos, pState, false)) {
			BlockPos masterPos = getMaster(pLevel, pCurrentPos, pState);
			if (!pLevel.getBlockTicks()
				.hasScheduledTick(masterPos, AllBlocks.LARGE_WATER_WHEEL.get()))
				pLevel.scheduleTick(masterPos, AllBlocks.LARGE_WATER_WHEEL.get(), 1);
			return pState;
		}
		if (!(pLevel instanceof Level level) || level.isClientSide())
			return pState;
		if (!level.getBlockTicks()
			.hasScheduledTick(pCurrentPos, this))
			level.scheduleTick(pCurrentPos, this, 1);
		return pState;
	}

	public static BlockPos getMaster(BlockGetter level, BlockPos pos, BlockState state) {
		Direction direction = state.getValue(FACING);
		BlockPos targetedPos = pos.relative(direction);
		BlockState targetedState = level.getBlockState(targetedPos);
		if (targetedState.is(AllBlocks.WATER_WHEEL_STRUCTURAL.get()))
			return getMaster(level, targetedPos, targetedState);
		return targetedPos;
	}

	public boolean stillValid(BlockGetter level, BlockPos pos, BlockState state, boolean directlyAdjacent) {
		if (!state.is(this))
			return false;

		Direction direction = state.getValue(FACING);
		BlockPos targetedPos = pos.relative(direction);
		BlockState targetedState = level.getBlockState(targetedPos);

		if (!directlyAdjacent && stillValid(level, targetedPos, targetedState, true))
			return true;
		return targetedState.getBlock() instanceof LargeWaterWheelBlock
			&& targetedState.getValue(LargeWaterWheelBlock.AXIS) != direction.getAxis();
	}

	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
		if (!stillValid(pLevel, pPos, pState, false))
			pLevel.setBlockAndUpdate(pPos, Blocks.AIR.defaultBlockState());
	}

	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
		consumer.accept(new RenderProperties());
	}

	@Override
	public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2,
		LivingEntity entity, int numberOfParticles) {
		return true;
	}

	public static class RenderProperties implements IClientBlockExtensions, MultiPosDestructionHandler {

		@Override
		public boolean addDestroyEffects(BlockState state, Level Level, BlockPos pos, ParticleEngine manager) {
			return true;
		}

		@Override
		public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
			if (target instanceof BlockHitResult bhr) {
				BlockPos targetPos = bhr.getBlockPos();
				WaterWheelStructuralBlock waterWheelStructuralBlock = AllBlocks.WATER_WHEEL_STRUCTURAL.get();
				if (waterWheelStructuralBlock.stillValid(level, targetPos, state, false))
					manager.crack(WaterWheelStructuralBlock.getMaster(level, targetPos, state), bhr.getDirection());
				return true;
			}
			return IClientBlockExtensions.super.addHitEffects(state, level, target, manager);
		}

		@Override
		@Nullable
		public Set<BlockPos> getExtraPositions(ClientLevel level, BlockPos pos, BlockState blockState, int progress) {
			WaterWheelStructuralBlock waterWheelStructuralBlock = AllBlocks.WATER_WHEEL_STRUCTURAL.get();
			if (!waterWheelStructuralBlock.stillValid(level, pos, blockState, false))
				return null;
			HashSet<BlockPos> set = new HashSet<>();
			set.add(WaterWheelStructuralBlock.getMaster(level, pos, blockState));
			return set;
		}
	}

	@Override
	public BlockPos getInformationSource(Level level, BlockPos pos, BlockState state) {
		return stillValid(level, pos, state, false) ? getMaster(level, pos, state) : pos;
	}

	@Override
	public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return false;
	}
}
