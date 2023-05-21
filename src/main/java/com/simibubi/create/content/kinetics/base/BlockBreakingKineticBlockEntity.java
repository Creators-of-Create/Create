package com.simibubi.create.content.kinetics.base;

import java.util.concurrent.atomic.AtomicInteger;

import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class BlockBreakingKineticBlockEntity extends KineticBlockEntity {

	public static final AtomicInteger NEXT_BREAKER_ID = new AtomicInteger();
	protected int ticksUntilNextProgress;
	protected int destroyProgress;
	protected int breakerId = -NEXT_BREAKER_ID.incrementAndGet();
	protected BlockPos breakingPos;

	public BlockBreakingKineticBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
	protected void read(CompoundTag compound, boolean clientPacket) {
		destroyProgress = compound.getInt("Progress");
		ticksUntilNextProgress = compound.getInt("NextTick");
		if (compound.contains("Breaking"))
			breakingPos = NbtUtils.readBlockPos(compound.getCompound("Breaking"));
		super.read(compound, clientPacket);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (!level.isClientSide && destroyProgress != 0)
			level.destroyBlockProgress(breakerId, breakingPos, -1);
	}

	@Override
	public void tick() {
		super.tick();

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
		level.playSound(null, worldPosition, stateToBreak.getSoundType()
			.getHitSound(), SoundSource.NEUTRAL, .25f, 1);

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
		return !(stateToBreak.getMaterial()
			.isLiquid() || stateToBreak.getBlock() instanceof AirBlock || blockHardness == -1);
	}

	public void onBlockBroken(BlockState stateToBreak) {
		Vec3 vec = VecHelper.offsetRandomly(VecHelper.getCenterOf(breakingPos), level.random, .125f);
		BlockHelper.destroyBlock(level, breakingPos, 1f, (stack) -> {
			if (stack.isEmpty())
				return;
			if (!level.getGameRules()
				.getBoolean(GameRules.RULE_DOBLOCKDROPS))
				return;
			if (level.restoringBlockSnapshots)
				return;
			
			ItemEntity itementity = new ItemEntity(level, vec.x, vec.y, vec.z, stack);
			itementity.setDefaultPickUpDelay();
			itementity.setDeltaMovement(Vec3.ZERO);
			level.addFreshEntity(itementity);
		});
	}

	protected float getBreakSpeed() {
		return Math.abs(getSpeed() / 100f);
	}

}
