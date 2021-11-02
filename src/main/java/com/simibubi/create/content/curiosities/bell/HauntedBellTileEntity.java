package com.simibubi.create.content.curiosities.bell;

import java.util.List;
import java.util.Random;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class HauntedBellTileEntity extends AbstractBellTileEntity {

	public static final int DISTANCE = 10;
	public static final int RECHARGE_TICKS = 65;
	public static final int EFFECT_TICKS = 20;

	public int effectTicks = 0;

	public HauntedBellTileEntity(BlockEntityType<?> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) { }

	@Override
	public PartialModel getBellModel() {
		return AllBlockPartials.HAUNTED_BELL;
	}

	@Override
	public boolean ring(Level world, BlockPos pos, Direction direction) {
		if (isRinging && ringingTicks < RECHARGE_TICKS)
			return false;

		if (!super.ring(world, pos, direction))
			return false;

		if (!world.isClientSide)
			HauntedBellPulser.sendPulse(world, pos, DISTANCE, false);

		startEffect();

		return true;
	}

	public void startEffect() {
		effectTicks = EFFECT_TICKS;
		sendData();
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("EffectTicks", effectTicks);
	}

	@Override
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		effectTicks = compound.getInt("EffectTicks");
	}

	@Override
	public void tick() {
		super.tick();

		if (effectTicks <= 0)
			return;
		effectTicks--;

		if (!level.isClientSide)
			return;

		Random rand = level.getRandom();
		if (rand.nextFloat() > 0.25f)
			return;

		spawnParticle(rand);
		playSound(rand);
	}

	protected void spawnParticle(Random rand) {
		double x = worldPosition.getX() + rand.nextDouble();
		double y = worldPosition.getY() + 0.5;
		double z = worldPosition.getZ() + rand.nextDouble();
		double vx = rand.nextDouble() * 0.04 - 0.02;
		double vy = 0.1;
		double vz = rand.nextDouble() * 0.04 - 0.02;
		level.addParticle(ParticleTypes.SOUL, x, y, z, vx, vy, vz);
	}

	protected void playSound(Random rand) {
		float vol = rand.nextFloat() * 0.4F + rand.nextFloat() > 0.9F ? 0.6F : 0.0F;
		float pitch = 0.6F + rand.nextFloat() * 0.4F;
		level.playSound(null, worldPosition, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, vol, pitch);
	}

}
