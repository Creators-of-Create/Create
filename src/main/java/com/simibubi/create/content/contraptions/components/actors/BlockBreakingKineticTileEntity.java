package com.simibubi.create.content.contraptions.components.actors;

import java.util.concurrent.atomic.AtomicInteger;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

public abstract class BlockBreakingKineticTileEntity extends KineticTileEntity {

	public static final AtomicInteger NEXT_BREAKER_ID = new AtomicInteger();
	protected int ticksUntilNextProgress;
	protected int destroyProgress;
	protected int breakerId = -NEXT_BREAKER_ID.incrementAndGet();
	protected BlockPos breakingPos;

	public BlockBreakingKineticTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
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
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putInt("Progress", destroyProgress);
		compound.putInt("NextTick", ticksUntilNextProgress);
		if (breakingPos != null)
			compound.put("Breaking", NBTUtil.writeBlockPos(breakingPos));
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		destroyProgress = compound.getInt("Progress");
		ticksUntilNextProgress = compound.getInt("NextTick");
		if (compound.contains("Breaking"))
			breakingPos = NBTUtil.readBlockPos(compound.getCompound("Breaking"));
		super.fromTag(state, compound, clientPacket);
	}

	@Override
	public void setRemoved() {
		if (!level.isClientSide && destroyProgress != 0)
			level.destroyBlockProgress(breakerId, breakingPos, -1);
		super.setRemoved();
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
		destroyProgress += MathHelper.clamp((int) (breakSpeed / blockHardness), 1, 10 - destroyProgress);
		level.playSound(null, worldPosition, stateToBreak.getSoundType().getHitSound(), SoundCategory.NEUTRAL, .25f, 1);

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
		TileEntity tileentity = stateToBreak.hasTileEntity() ? level.getBlockEntity(breakingPos) : null;
		Vector3d vec = VecHelper.offsetRandomly(VecHelper.getCenterOf(breakingPos), level.random, .125f);

		Block.getDrops(stateToBreak, (ServerWorld) level, breakingPos, tileentity).forEach((stack) -> {
			if (!stack.isEmpty() && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)
					&& !level.restoringBlockSnapshots) {
				ItemEntity itementity = new ItemEntity(level, vec.x, vec.y, vec.z, stack);
				itementity.setDefaultPickUpDelay();
				itementity.setDeltaMovement(Vector3d.ZERO);
				level.addFreshEntity(itementity);
			}
		});
		if (level instanceof ServerWorld)
			stateToBreak.spawnAfterBreak((ServerWorld) level, breakingPos, ItemStack.EMPTY);
		level.setBlock(breakingPos, FluidState.createLegacyBlock(), 3);
	}

	protected float getBreakSpeed() {
		return Math.abs(getSpeed() / 100f);
	}

}
