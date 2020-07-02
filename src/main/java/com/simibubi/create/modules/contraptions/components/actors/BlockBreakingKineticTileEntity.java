package com.simibubi.create.modules.contraptions.components.actors;

import java.util.concurrent.atomic.AtomicInteger;

import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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
	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("Progress", destroyProgress);
		compound.putInt("NextTick", ticksUntilNextProgress);
		if (breakingPos != null)
			compound.put("Breaking", NBTUtil.writeBlockPos(breakingPos));
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		destroyProgress = compound.getInt("Progress");
		ticksUntilNextProgress = compound.getInt("NextTick");
		if (compound.contains("Breaking"))
			breakingPos = NBTUtil.readBlockPos(compound.getCompound("Breaking"));
		super.read(compound);
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
		IFluidState ifluidstate = world.getFluidState(breakingPos);
		world.playEvent(2001, breakingPos, Block.getStateId(stateToBreak));
		TileEntity tileentity = stateToBreak.hasTileEntity() ? world.getTileEntity(breakingPos) : null;
		Vec3d vec = VecHelper.offsetRandomly(VecHelper.getCenterOf(breakingPos), world.rand, .125f);

		Block.getDrops(stateToBreak, (ServerWorld) world, breakingPos, tileentity).forEach((stack) -> {
			if (!stack.isEmpty() && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)
					&& !world.restoringBlockSnapshots) {
				ItemEntity itementity = new ItemEntity(world, vec.x, vec.y, vec.z, stack);
				itementity.setDefaultPickupDelay();
				itementity.setMotion(Vec3d.ZERO);
				world.addEntity(itementity);
			}
		});

		stateToBreak.spawnAdditionalDrops(world, breakingPos, ItemStack.EMPTY);
		world.setBlockState(breakingPos, ifluidstate.getBlockState(), 3);
	}

	protected float getBreakSpeed() {
		return Math.abs(getSpeed() / 100f);
	}

}
