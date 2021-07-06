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
	public void remove() {
		if (!world.isRemote && destroyProgress != 0)
			world.sendBlockBreakProgress(breakerId, breakingPos, -1);
		super.remove();
	}

	@Override
	public void tick() {
		super.tick();

		if (world.isRemote)
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

		BlockState stateToBreak = world.getBlockState(breakingPos);
		float blockHardness = stateToBreak.getBlockHardness(world, breakingPos);

		if (!canBreak(stateToBreak, blockHardness)) {
			if (destroyProgress != 0) {
				destroyProgress = 0;
				world.sendBlockBreakProgress(breakerId, breakingPos, -1);
			}
			return;
		}

		float breakSpeed = getBreakSpeed();
		destroyProgress += MathHelper.clamp((int) (breakSpeed / blockHardness), 1, 10 - destroyProgress);
		world.playSound(null, pos, stateToBreak.getSoundType().getHitSound(), SoundCategory.NEUTRAL, .25f, 1);

		if (destroyProgress >= 10) {
			onBlockBroken(stateToBreak);
			destroyProgress = 0;
			ticksUntilNextProgress = -1;
			world.sendBlockBreakProgress(breakerId, breakingPos, -1);
			return;
		}

		ticksUntilNextProgress = (int) (blockHardness / breakSpeed);
		world.sendBlockBreakProgress(breakerId, breakingPos, (int) destroyProgress);
	}

	public boolean canBreak(BlockState stateToBreak, float blockHardness) {
		return isBreakable(stateToBreak, blockHardness);
	}

	public static boolean isBreakable(BlockState stateToBreak, float blockHardness) {
		return !(stateToBreak.getMaterial().isLiquid() || stateToBreak.getBlock() instanceof AirBlock
				|| blockHardness == -1);
	}

	public void onBlockBroken(BlockState stateToBreak) {
		FluidState FluidState = world.getFluidState(breakingPos);
		world.playEvent(2001, breakingPos, Block.getStateId(stateToBreak));
		TileEntity tileentity = stateToBreak.hasTileEntity() ? world.getTileEntity(breakingPos) : null;
		Vector3d vec = VecHelper.offsetRandomly(VecHelper.getCenterOf(breakingPos), world.rand, .125f);

		Block.getDrops(stateToBreak, (ServerWorld) world, breakingPos, tileentity).forEach((stack) -> {
			if (!stack.isEmpty() && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)
					&& !world.restoringBlockSnapshots) {
				ItemEntity itementity = new ItemEntity(world, vec.x, vec.y, vec.z, stack);
				itementity.setDefaultPickupDelay();
				itementity.setMotion(Vector3d.ZERO);
				world.addEntity(itementity);
			}
		});
		if (world instanceof ServerWorld)
			stateToBreak.spawnAdditionalDrops((ServerWorld) world, breakingPos, ItemStack.EMPTY);
		world.setBlockState(breakingPos, FluidState.getBlockState(), 3);
	}

	protected float getBreakSpeed() {
		return Math.abs(getSpeed() / 100f);
	}

}
