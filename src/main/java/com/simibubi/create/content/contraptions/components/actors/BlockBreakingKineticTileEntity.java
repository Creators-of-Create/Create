package com.simibubi.create.content.contraptions.components.actors;

import java.util.concurrent.atomic.AtomicInteger;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.GameRules;
import net.minecraft.server.level.ServerLevel;

public abstract class BlockBreakingKineticTileEntity extends KineticTileEntity {

	public static final AtomicInteger NEXT_BREAKER_ID = new AtomicInteger();
	protected int ticksUntilNextProgress;
	protected int destroyProgress;
	protected int breakerId = -NEXT_BREAKER_ID.incrementAndGet();
	protected BlockPos breakingPos;

	public BlockBreakingKineticTileEntity(BlockPos pos, BlockState state, BlockEntityType<?> type) {
		super(type, pos, state);
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		if (destroyProgress == -1)
			destroyNextTick();
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		if (ticksUntilNextProgress == -1)
			destroyNextTick();
	}

	public void destroyNextTick() {
		ticksUntilNextProgress = 1;
	}

	protected abstract BlockPos getBreakingPos();

	protected boolean shouldRun() {
		return true;
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("Progress", destroyProgress);
		compound.putInt("NextTick", ticksUntilNextProgress);
		if (breakingPos != null)
			compound.put("Breaking", NbtUtils.writeBlockPos(breakingPos));
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		destroyProgress = compound.getInt("Progress");
		ticksUntilNextProgress = compound.getInt("NextTick");
		if (compound.contains("Breaking"))
			breakingPos = NbtUtils.readBlockPos(compound.getCompound("Breaking"));
		super.fromTag(state, compound, clientPacket);
	}

	@Override
	public void setRemoved() {
		if (!level.isClientSide && destroyProgress != 0)
			level.destroyBlockProgress(breakerId, breakingPos, -1);
		super.setRemoved();
	}

	@Override
	public void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		super.tick(level, pos, state, blockEntity);

		if (level.isClientSide)
			return;
		if (!shouldRun())
			return;
		if (getSpeed() == 0)
			return;

		breakingPos = getBreakingPos();

		if (ticksUntilNextProgress < 0)
			return;
		if (ticksUntilNextProgress-- > 0)
			return;

		BlockState stateToBreak = level.getBlockState(breakingPos);
		float blockHardness = stateToBreak.getDestroySpeed(level, breakingPos);

		if (!canBreak(stateToBreak, blockHardness)) {
			if (destroyProgress != 0) {
				destroyProgress = 0;
				level.destroyBlockProgress(breakerId, breakingPos, -1);
			}
			return;
		}

		float breakSpeed = getBreakSpeed();
		destroyProgress += Mth.clamp((int) (breakSpeed / blockHardness), 1, 10 - destroyProgress);
		level.playSound(null, worldPosition, stateToBreak.getSoundType().getHitSound(), SoundSource.NEUTRAL, .25f, 1);

		if (destroyProgress >= 10) {
			onBlockBroken(stateToBreak);
			destroyProgress = 0;
			ticksUntilNextProgress = -1;
			level.destroyBlockProgress(breakerId, breakingPos, -1);
			return;
		}

		ticksUntilNextProgress = (int) (blockHardness / breakSpeed);
		level.destroyBlockProgress(breakerId, breakingPos, (int) destroyProgress);
	}

	public boolean canBreak(BlockState stateToBreak, float blockHardness) {
		return isBreakable(stateToBreak, blockHardness);
	}

	public static boolean isBreakable(BlockState stateToBreak, float blockHardness) {
		return !(stateToBreak.getMaterial().isLiquid() || stateToBreak.getBlock() instanceof AirBlock
				|| blockHardness == -1);
	}

	public void onBlockBroken(BlockState stateToBreak) {
		FluidState FluidState = level.getFluidState(breakingPos);
		level.levelEvent(2001, breakingPos, Block.getId(stateToBreak));
		BlockEntity tileentity = level.getBlockEntity(breakingPos);
		Vec3 vec = VecHelper.offsetRandomly(VecHelper.getCenterOf(breakingPos), level.random, .125f);

		Block.getDrops(stateToBreak, (ServerLevel) level, breakingPos, tileentity).forEach((stack) -> {
			if (!stack.isEmpty() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)
					&& !level.restoringBlockSnapshots) {
				ItemEntity itementity = new ItemEntity(level, vec.x, vec.y, vec.z, stack);
				itementity.setDefaultPickUpDelay();
				itementity.setDeltaMovement(Vec3.ZERO);
				level.addFreshEntity(itementity);
			}
		});
		if (level instanceof ServerLevel)
			stateToBreak.spawnAfterBreak((ServerLevel) level, breakingPos, ItemStack.EMPTY);
		level.setBlock(breakingPos, FluidState.createLegacyBlock(), 3);
	}

	protected float getBreakSpeed() {
		return Math.abs(getSpeed() / 100f);
	}

}
