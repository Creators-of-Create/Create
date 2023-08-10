package com.simibubi.create.content.kinetics.base;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.mounted.MountedContraption;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BlockBreakingMovementBehaviour implements MovementBehaviour {

	@Override
	public void startMoving(MovementContext context) {
		if (context.world.isClientSide)
			return;
		context.data.putInt("BreakerId", -BlockBreakingKineticBlockEntity.NEXT_BREAKER_ID.incrementAndGet());
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		Level world = context.world;
		BlockState stateVisited = world.getBlockState(pos);

		if (!stateVisited.isRedstoneConductor(world, pos))
			damageEntities(context, pos, world);
		if (world.isClientSide)
			return;
		
		if (!canBreak(world, pos, stateVisited))
			return;
		context.data.put("BreakingPos", NbtUtils.writeBlockPos(pos));
		context.stall = true;
	}

	public void damageEntities(MovementContext context, BlockPos pos, Level world) {
		if (context.contraption.entity instanceof OrientedContraptionEntity oce && oce.nonDamageTicks > 0)
			return;
		DamageSource damageSource = getDamageSource();
		if (damageSource == null && !throwsEntities())
			return;
		Entities: for (Entity entity : world.getEntitiesOfClass(Entity.class, new AABB(pos))) {
			if (entity instanceof ItemEntity)
				continue;
			if (entity instanceof AbstractContraptionEntity)
				continue;
			if (entity.isPassengerOfSameVehicle(context.contraption.entity))
				continue;
			if (entity instanceof AbstractMinecart)
				for (Entity passenger : entity.getIndirectPassengers())
					if (passenger instanceof AbstractContraptionEntity
						&& ((AbstractContraptionEntity) passenger).getContraption() == context.contraption)
						continue Entities;

			if (damageSource != null && !world.isClientSide) {
				float damage = (float) Mth.clamp(6 * Math.pow(context.relativeMotion.length(), 0.4) + 1, 2, 10);
				entity.hurt(damageSource, damage);
			}
			if (throwsEntities() && (world.isClientSide == (entity instanceof Player))) 
				throwEntity(context, entity);
		}
	}

	protected void throwEntity(MovementContext context, Entity entity) {
		Vec3 motionBoost = context.motion.add(0, context.motion.length() / 4f, 0);
		int maxBoost = 4;
		if (motionBoost.length() > maxBoost) {
			motionBoost = motionBoost.subtract(motionBoost.normalize()
				.scale(motionBoost.length() - maxBoost));
		}
		entity.setDeltaMovement(entity.getDeltaMovement()
			.add(motionBoost));
		entity.hurtMarked = true;
	}

	protected DamageSource getDamageSource() {
		return null;
	}

	protected boolean throwsEntities() {
		return getDamageSource() != null;
	}

	@Override
	public void cancelStall(MovementContext context) {
		CompoundTag data = context.data;
		if (context.world.isClientSide)
			return;
		if (!data.contains("BreakingPos"))
			return;

		Level world = context.world;
		int id = data.getInt("BreakerId");
		BlockPos breakingPos = NbtUtils.readBlockPos(data.getCompound("BreakingPos"));

		data.remove("Progress");
		data.remove("TicksUntilNextProgress");
		data.remove("BreakingPos");

		MovementBehaviour.super.cancelStall(context);
		world.destroyBlockProgress(id, breakingPos, -1);
	}

	@Override
	public void stopMoving(MovementContext context) {
		cancelStall(context);
	}

	@Override
	public void tick(MovementContext context) {
		tickBreaker(context);

		CompoundTag data = context.data;
		if (!data.contains("WaitingTicks"))
			return;

		int waitingTicks = data.getInt("WaitingTicks");
		if (waitingTicks-- > 0) {
			data.putInt("WaitingTicks", waitingTicks);
			context.stall = true;
			return;
		}

		BlockPos pos = NbtUtils.readBlockPos(data.getCompound("LastPos"));
		data.remove("WaitingTicks");
		data.remove("LastPos");
		context.stall = false;
		visitNewPosition(context, pos);
	}

	public void tickBreaker(MovementContext context) {
		CompoundTag data = context.data;
		if (context.world.isClientSide)
			return;
		if (!data.contains("BreakingPos")) {
			context.stall = false;
			return;
		}
		if (context.relativeMotion.equals(Vec3.ZERO)) {
			context.stall = false;
			return;
		}

		int ticksUntilNextProgress = data.getInt("TicksUntilNextProgress");
		if (ticksUntilNextProgress-- > 0) {
			data.putInt("TicksUntilNextProgress", ticksUntilNextProgress);
			return;
		}

		Level world = context.world;
		BlockPos breakingPos = NbtUtils.readBlockPos(data.getCompound("BreakingPos"));
		int destroyProgress = data.getInt("Progress");
		int id = data.getInt("BreakerId");
		BlockState stateToBreak = world.getBlockState(breakingPos);
		float blockHardness = stateToBreak.getDestroySpeed(world, breakingPos);

		if (!canBreak(world, breakingPos, stateToBreak)) {
			if (destroyProgress != 0) {
				destroyProgress = 0;
				data.remove("Progress");
				data.remove("TicksUntilNextProgress");
				data.remove("BreakingPos");
				world.destroyBlockProgress(id, breakingPos, -1);
			}
			context.stall = false;
			return;
		}

		float breakSpeed = getBlockBreakingSpeed(context);
		destroyProgress += Mth.clamp((int) (breakSpeed / blockHardness), 1, 10 - destroyProgress);
		world.playSound(null, breakingPos, stateToBreak.getSoundType()
			.getHitSound(), SoundSource.NEUTRAL, .25f, 1);

		if (destroyProgress >= 10) {
			world.destroyBlockProgress(id, breakingPos, -1);

			// break falling blocks from top to bottom
			BlockPos ogPos = breakingPos;
			BlockState stateAbove = world.getBlockState(breakingPos.above());
			while (stateAbove.getBlock() instanceof FallingBlock) {
				breakingPos = breakingPos.above();
				stateAbove = world.getBlockState(breakingPos.above());
			}
			stateToBreak = world.getBlockState(breakingPos);

			context.stall = false;
			if (shouldDestroyStartBlock(stateToBreak))
				destroyBlock(context, breakingPos);
			onBlockBroken(context, ogPos, stateToBreak);
			ticksUntilNextProgress = -1;
			data.remove("Progress");
			data.remove("TicksUntilNextProgress");
			data.remove("BreakingPos");
			return;
		}

		ticksUntilNextProgress = (int) (blockHardness / breakSpeed);
		world.destroyBlockProgress(id, breakingPos, (int) destroyProgress);
		data.putInt("TicksUntilNextProgress", ticksUntilNextProgress);
		data.putInt("Progress", destroyProgress);
	}

	protected void destroyBlock(MovementContext context, BlockPos breakingPos) {
		BlockHelper.destroyBlock(context.world, breakingPos, 1f, stack -> this.dropItem(context, stack));
	}

	protected float getBlockBreakingSpeed(MovementContext context) {
		float lowerLimit = 1 / 128f;
		if (context.contraption instanceof MountedContraption)
			lowerLimit = 1f;
		if (context.contraption instanceof CarriageContraption)
			lowerLimit = 2f;
		return Mth.clamp(Math.abs(context.getAnimationSpeed()) / 500f, lowerLimit, 16f);
	}

	protected boolean shouldDestroyStartBlock(BlockState stateToBreak) {
		return true;
	}

	public boolean canBreak(Level world, BlockPos breakingPos, BlockState state) {
		float blockHardness = state.getDestroySpeed(world, breakingPos);
		return BlockBreakingKineticBlockEntity.isBreakable(state, blockHardness);
	}

	protected void onBlockBroken(MovementContext context, BlockPos pos, BlockState brokenState) {
		// Check for falling blocks
		if (!(brokenState.getBlock() instanceof FallingBlock))
			return;

		CompoundTag data = context.data;
		data.putInt("WaitingTicks", 10);
		data.put("LastPos", NbtUtils.writeBlockPos(pos));
		context.stall = true;
	}

}
