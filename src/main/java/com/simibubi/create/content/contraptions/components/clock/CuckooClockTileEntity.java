package com.simibubi.create.content.contraptions.components.clock;

import static com.simibubi.create.foundation.utility.AngleHelper.deg;
import static com.simibubi.create.foundation.utility.AngleHelper.getShortestAngleDiff;
import static com.simibubi.create.foundation.utility.AngleHelper.rad;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.gui.widgets.InterpolatedValue;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;

public class CuckooClockTileEntity extends KineticTileEntity {

	public static DamageSource CUCKOO_SURPRISE = new DamageSource("create.cuckoo_clock_explosion").setExplosion();

	public InterpolatedChasingValue hourHand = new InterpolatedChasingValue().withSpeed(.2f);
	public InterpolatedChasingValue minuteHand = new InterpolatedChasingValue().withSpeed(.2f);
	public InterpolatedValue animationProgress = new InterpolatedValue();
	public Animation animationType;
	private boolean sendAnimationUpdate;

	enum Animation {
		PIG, CREEPER, SURPRISE, NONE;
	}

	public CuckooClockTileEntity(TileEntityType<? extends CuckooClockTileEntity> type) {
		super(type);
		animationType = Animation.NONE;
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT compound) {
		if (sendAnimationUpdate)
			NBTHelper.writeEnum(compound, "Animation", animationType);
		sendAnimationUpdate = false;
		return super.writeToClient(compound);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		if (tag.contains("Animation")) {
			animationType = NBTHelper.readEnum(tag, "Animation", Animation.class);
			animationProgress.lastValue = 0;
			animationProgress.value = 0;
		}
		super.readClientUpdate(tag);
	}

	@Override
	public void tick() {
		super.tick();
		if (getSpeed() == 0)
			return;

		int dayTime = (int) (world.getDayTime() % 24000);
		int hours = (dayTime / 1000 + 6) % 24;
		int minutes = (dayTime % 1000) * 60 / 1000;

		if (!world.isRemote) {
			if (animationType == Animation.NONE) {
				if (hours == 12 && minutes < 5)
					startAnimation(Animation.PIG);
				if (hours == 18 && minutes < 36 && minutes > 31)
					startAnimation(Animation.CREEPER);
			} else {
				float value = animationProgress.value;
				animationProgress.set(value + 1);
				if (value > 100)
					animationType = Animation.NONE;

				if (animationType == Animation.SURPRISE && animationProgress.value == 50) {
					Vec3d center = VecHelper.getCenterOf(pos);
					world.destroyBlock(pos, false);
					world.createExplosion(null, CUCKOO_SURPRISE, center.x, center.y, center.z, 3, false,
						Explosion.Mode.BREAK);
				}

			}
		}

		if (world.isRemote) {
			moveHands(hours, minutes);

			if (animationType == Animation.NONE) {
				if (AnimationTickHolder.ticks % 32 == 0)
					playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT, 1 / 16f, 2f);
				else if (AnimationTickHolder.ticks % 16 == 0)
					playSound(SoundEvents.BLOCK_NOTE_BLOCK_HAT, 1 / 16f, 1.5f);
			} else {

				boolean isSurprise = animationType == Animation.SURPRISE;
				float value = animationProgress.value;
				animationProgress.set(value + 1);
				if (value > 100)
					animationType = null;

				// sounds

				if (value == 1)
					playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 2, .5f);
				if (value == 21)
					playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 2, 0.793701f);

				if (value > 30 && isSurprise) {
					Vec3d pos = VecHelper.offsetRandomly(VecHelper.getCenterOf(this.pos), world.rand, .5f);
					world.addParticle(ParticleTypes.LARGE_SMOKE, pos.x, pos.y, pos.z, 0, 0, 0);
				}
				if (value == 40 && isSurprise)
					playSound(SoundEvents.ENTITY_TNT_PRIMED, 1f, 1f);

				int step = isSurprise ? 3 : 15;
				for (int phase = 30; phase <= 60; phase += step) {
					if (value == phase - step / 3)
						playSound(SoundEvents.BLOCK_CHEST_OPEN, 1 / 16f, 2f);
					if (value == phase) {
						if (animationType == Animation.PIG)
							playSound(SoundEvents.ENTITY_PIG_AMBIENT, 1 / 4f, 1f);
						else
							playSound(SoundEvents.ENTITY_CREEPER_HURT, 1 / 4f, 3f);
					}
					if (value == phase + step / 3)
						playSound(SoundEvents.BLOCK_CHEST_CLOSE, 1 / 16f, 2f);

				}

			}

			return;
		}
	}

	public void startAnimation(Animation animation) {
		animationType = animation;
		if (animation != null && CuckooClockBlock.containsSurprise(getBlockState()))
			animationType = Animation.SURPRISE;
		animationProgress.lastValue = 0;
		animationProgress.value = 0;
		sendAnimationUpdate = true;
		sendData();
	}

	public void moveHands(int hours, int minutes) {
		float hourTarget = (float) (360 / 12 * (hours % 12));
		float minuteTarget = (float) (360 / 60 * minutes);

		hourHand.target(hourHand.value + rad(getShortestAngleDiff(deg(hourHand.value), hourTarget)));
		minuteHand.target(minuteHand.value + rad(getShortestAngleDiff(deg(minuteHand.value), minuteTarget)));

		hourHand.tick();
		minuteHand.tick();
	}

	private void playSound(SoundEvent sound, float volume, float pitch) {
		Vec3d vec = VecHelper.getCenterOf(pos);
		world.playSound(vec.x, vec.y, vec.z, sound, SoundCategory.BLOCKS, volume, pitch, false);
	}

}
