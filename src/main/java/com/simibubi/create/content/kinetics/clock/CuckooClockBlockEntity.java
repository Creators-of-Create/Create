package com.simibubi.create.content.kinetics.clock;

import java.util.List;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.damageTypes.CreateDamageSources;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CuckooClockBlockEntity extends KineticBlockEntity {

	public LerpedFloat hourHand = LerpedFloat.angular();
	public LerpedFloat minuteHand = LerpedFloat.angular();
	public LerpedFloat animationProgress = LerpedFloat.linear();
	public Animation animationType;
	private boolean sendAnimationUpdate;

	enum Animation {
		PIG, CREEPER, SURPRISE, NONE;
	}

	public CuckooClockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		animationType = Animation.NONE;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		registerAwardables(behaviours, AllAdvancements.CUCKOO_CLOCK);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		if (clientPacket && compound.contains("Animation")) {
			animationType = NBTHelper.readEnum(compound, "Animation", Animation.class);
			animationProgress.startWithValue(0);
		}
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		if (clientPacket && sendAnimationUpdate)
			NBTHelper.writeEnum(compound, "Animation", animationType);
		sendAnimationUpdate = false;
		super.write(compound, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();
		if (getSpeed() == 0)
			return;


		boolean isNatural = level.dimensionType().natural();
		int dayTime = (int) ((level.getDayTime() * (isNatural ? 1 : 24)) % 24000);
		int hours = (dayTime / 1000 + 6) % 24;
		int minutes = (dayTime % 1000) * 60 / 1000;

		if (!isNatural) {
			if (level.isClientSide) {
				moveHands(hours, minutes);

				if (AnimationTickHolder.getTicks() % 6 == 0)
					playSound(SoundEvents.NOTE_BLOCK_HAT.get(), 1 / 16f, 2f);
				else if (AnimationTickHolder.getTicks() % 3 == 0)
					playSound(SoundEvents.NOTE_BLOCK_HAT.get(), 1 / 16f, 1.5f);
			}
			return;
		}

		if (!level.isClientSide) {
			if (animationType == Animation.NONE) {
				if (hours == 12 && minutes < 5)
					startAnimation(Animation.PIG);
				if (hours == 18 && minutes < 36 && minutes > 31)
					startAnimation(Animation.CREEPER);
			} else {
				float value = animationProgress.getValue();
				animationProgress.setValue(value + 1);
				if (value > 100)
					animationType = Animation.NONE;

				if (animationType == Animation.SURPRISE && Mth.equal(animationProgress.getValue(), 50)) {
					Vec3 center = VecHelper.getCenterOf(worldPosition);
					level.destroyBlock(worldPosition, false);
					DamageSource damageSource = CreateDamageSources.cuckooSurprise(level);
					level.explode(null, damageSource, null, center.x, center.y, center.z, 3, false,
						ExplosionInteraction.BLOCK);
				}

			}
		}

		if (level.isClientSide) {
			moveHands(hours, minutes);

			if (animationType == Animation.NONE) {
				if (AnimationTickHolder.getTicks() % 32 == 0)
					playSound(SoundEvents.NOTE_BLOCK_HAT.get(), 1 / 16f, 2f);
				else if (AnimationTickHolder.getTicks() % 16 == 0)
					playSound(SoundEvents.NOTE_BLOCK_HAT.get(), 1 / 16f, 1.5f);
			} else {

				boolean isSurprise = animationType == Animation.SURPRISE;
				float value = animationProgress.getValue();
				animationProgress.setValue(value + 1);
				if (value > 100)
					animationType = null;

				// sounds

				if (value == 1)
					playSound(SoundEvents.NOTE_BLOCK_CHIME.get(), 2, .5f);
				if (value == 21)
					playSound(SoundEvents.NOTE_BLOCK_CHIME.get(), 2, 0.793701f);

				if (value > 30 && isSurprise) {
					Vec3 pos = VecHelper.offsetRandomly(VecHelper.getCenterOf(this.worldPosition), level.random, .5f);
					level.addParticle(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, 0, 0, 0);
				}
				if (value == 40 && isSurprise)
					playSound(SoundEvents.TNT_PRIMED, 1f, 1f);

				int step = isSurprise ? 3 : 15;
				for (int phase = 30; phase <= 60; phase += step) {
					if (value == phase - step / 3)
						playSound(SoundEvents.CHEST_OPEN, 1 / 16f, 2f);
					if (value == phase) {
						if (animationType == Animation.PIG)
							playSound(SoundEvents.PIG_AMBIENT, 1 / 4f, 1f);
						else
							playSound(SoundEvents.CREEPER_HURT, 1 / 4f, 3f);
					}
					if (value == phase + step / 3)
						playSound(SoundEvents.CHEST_CLOSE, 1 / 16f, 2f);

				}

			}

			return;
		}
	}

	public void startAnimation(Animation animation) {
		animationType = animation;
		if (animation != null && CuckooClockBlock.containsSurprise(getBlockState()))
			animationType = Animation.SURPRISE;
		animationProgress.startWithValue(0);
		sendAnimationUpdate = true;

		if (animation == Animation.CREEPER)
			awardIfNear(AllAdvancements.CUCKOO_CLOCK, 32);

		sendData();
	}

	public void moveHands(int hours, int minutes) {
		float hourTarget = (float) (360 / 12 * (hours % 12));
		float minuteTarget = (float) (360 / 60 * minutes);

		hourHand.chase(hourTarget, .2f, Chaser.EXP);
		minuteHand.chase(minuteTarget, .2f, Chaser.EXP);

		hourHand.tickChaser();
		minuteHand.tickChaser();
	}

	private void playSound(SoundEvent sound, float volume, float pitch) {
		Vec3 vec = VecHelper.getCenterOf(worldPosition);
		level.playLocalSound(vec.x, vec.y, vec.z, sound, SoundSource.BLOCKS, volume, pitch, false);
	}
}
