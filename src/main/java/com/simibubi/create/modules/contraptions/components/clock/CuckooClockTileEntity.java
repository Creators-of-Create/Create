package com.simibubi.create.modules.contraptions.components.clock;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.gui.widgets.InterpolatedValue;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
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
		PIG, CREEPER, SURPRISE;
	}

	public CuckooClockTileEntity() {
		super(AllTileEntities.CUCKOO_CLOCK.type);
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT compound) {
		if (sendAnimationUpdate)
			compound.putString("Animation", animationType == null ? "none" : NBTHelper.writeEnum(animationType));
		sendAnimationUpdate = false;
		return super.writeToClient(compound);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		if (tag.contains("Animation")) {
			String string = tag.getString("Animation");
			if ("none".equals(string))
				animationType = null;
			else
				animationType = NBTHelper.readEnum(string, Animation.class);
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
			if (animationType == null) {
				if (hours == 12 && minutes < 5) 
					startAnimation(Animation.PIG);
				if (hours == 18 && minutes < 36 && minutes > 31) 
					startAnimation(Animation.CREEPER);
			} else {
				float value = animationProgress.value;
				animationProgress.set(value + 1);
				if (value > 100)
					animationType = null;
				
				if (animationType == Animation.SURPRISE && animationProgress.value == 50) {
					Vec3d center = VecHelper.getCenterOf(pos);
					world.destroyBlock(pos, false);
					world.createExplosion(null, CUCKOO_SURPRISE, center.x, center.y, center.z, 3, false, Explosion.Mode.BREAK);
				}
				
			}
		}

		if (world.isRemote) {
			moveHands(hours, minutes);

			if (animationType == null) {
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
		float hourTarget = (float) (2 * Math.PI / 12 * (hours % 12));
		float minuteTarget = (float) (2 * Math.PI / 60 * minutes);

		hourHand.target(hourTarget);
		minuteHand.target(minuteTarget);

		if (minuteTarget - minuteHand.value < 0) {
			minuteHand.value = (float) (minuteHand.value - Math.PI * 2);
			minuteHand.lastValue = minuteHand.value;
		}

		if (hourTarget - hourHand.value < 0) {
			hourHand.value = (float) (hourHand.value - Math.PI * 2);
			hourHand.lastValue = hourHand.value;
		}

		hourHand.tick();
		minuteHand.tick();
	}

	private void playSound(SoundEvent sound, float volume, float pitch) {
		Vec3d vec = VecHelper.getCenterOf(pos);
		world.playSound(vec.x, vec.y, vec.z, sound, SoundCategory.BLOCKS, volume, pitch, false);
	}

}
