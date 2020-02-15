package com.simibubi.create.modules.contraptions.components.actors;

import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.modules.contraptions.components.contraptions.MovementBehaviour;
import com.simibubi.create.modules.contraptions.components.contraptions.MovementContext;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BlockBreakingMovementBehaviour extends MovementBehaviour {

	@Override
	public void startMoving(MovementContext context) {
		if (context.world.isRemote)
			return;
		context.data.putInt("BreakerId", -BlockBreakingKineticTileEntity.NEXT_BREAKER_ID.incrementAndGet());
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		World world = context.world;
		BlockState stateVisited = world.getBlockState(pos);

		if (world.isRemote)
			return;
		if (stateVisited.getCollisionShape(world, pos).isEmpty())
			return;
		if (stateVisited.getBlockHardness(world, pos) == -1)
			return;
		if (!canBreak(stateVisited))
			return;

		context.data.put("BreakingPos", NBTUtil.writeBlockPos(pos));
		context.stall = true;
	}

	@Override
	public void stopMoving(MovementContext context) {
		CompoundNBT data = context.data;
		if (context.world.isRemote)
			return;
		if (!data.contains("BreakingPos"))
			return;

		World world = context.world;
		int id = data.getInt("BreakerId");
		BlockPos breakingPos = NBTUtil.readBlockPos(data.getCompound("BreakingPos"));

		data.remove("Progress");
		data.remove("TicksUntilNextProgress");
		data.remove("BreakingPos");

		context.stall = false;
		world.sendBlockBreakProgress(id, breakingPos, -1);
	}

	@Override
	public void tick(MovementContext context) {
		CompoundNBT data = context.data;
		if (context.world.isRemote)
			return;
		if (!data.contains("BreakingPos"))
			return;
		if (context.relativeMotion.equals(Vec3d.ZERO)) {
			context.stall = false;
			return;
		}

		int ticksUntilNextProgress = data.getInt("TicksUntilNextProgress");
		if (ticksUntilNextProgress-- > 0) {
			data.putInt("TicksUntilNextProgress", ticksUntilNextProgress);
			return;
		}

		World world = context.world;
		BlockPos breakingPos = NBTUtil.readBlockPos(data.getCompound("BreakingPos"));
		int destroyProgress = data.getInt("Progress");
		int id = data.getInt("BreakerId");
		BlockState stateToBreak = world.getBlockState(breakingPos);
		float blockHardness = stateToBreak.getBlockHardness(world, breakingPos);

		if (!BlockBreakingKineticTileEntity.isBreakable(stateToBreak, blockHardness) || !canBreak(stateToBreak)) {
			if (destroyProgress != 0) {
				destroyProgress = 0;
				data.remove("Progress");
				data.remove("TicksUntilNextProgress");
				data.remove("BreakingPos");
				context.stall = false;
				world.sendBlockBreakProgress(id, breakingPos, -1);
			}
			return;
		}

		float breakSpeed = MathHelper.clamp(Math.abs(context.getAnimationSpeed()) / 500f, 1 / 128f, 16f);
		destroyProgress += MathHelper.clamp((int) (breakSpeed / blockHardness), 1, 10 - destroyProgress);

		if (destroyProgress >= 10) {
			BlockHelper.destroyBlock(context.world, breakingPos, 1f, stack -> this.dropItem(context, stack));
			onBlockBroken(context, breakingPos);
			ticksUntilNextProgress = -1;
			world.sendBlockBreakProgress(id, breakingPos, -1);
			data.remove("Progress");
			data.remove("TicksUntilNextProgress");
			data.remove("BreakingPos");
			context.stall = false;
			return;
		}

		ticksUntilNextProgress = (int) (blockHardness / breakSpeed);
		world.sendBlockBreakProgress(id, breakingPos, (int) destroyProgress);
		data.putInt("TicksUntilNextProgress", ticksUntilNextProgress);
		data.putInt("Progress", destroyProgress);
	}

	protected boolean canBreak(BlockState state) {
		return true;
	}

	protected void onBlockBroken(MovementContext context, BlockPos pos) {

	}

}
