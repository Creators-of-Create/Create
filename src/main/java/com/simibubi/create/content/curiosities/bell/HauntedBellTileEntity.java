package com.simibubi.create.content.curiosities.bell;

import java.util.List;
import java.util.Random;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HauntedBellTileEntity extends AbstractBellTileEntity {

	public static final int DISTANCE = 10;
	public static final int RECHARGE_TICKS = 65;
	public static final int EFFECT_TICKS = 20;

	public int effectTicks = 0;

	public HauntedBellTileEntity(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) { }

	@Override
	public PartialModel getBellModel() {
		return AllBlockPartials.HAUNTED_BELL;
	}

	@Override
	public boolean ring(World world, BlockPos pos, Direction direction) {
		if (isRinging && ringingTicks < RECHARGE_TICKS)
			return false;

		if (!super.ring(world, pos, direction))
			return false;

		if (!world.isRemote)
			HauntedBellPulser.sendPulse(world, pos, DISTANCE, false);

		startEffect();

		return true;
	}

	public void startEffect() {
		effectTicks = EFFECT_TICKS;
		sendData();
	}

	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("EffectTicks", effectTicks);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		super.fromTag(state, compound, clientPacket);
		effectTicks = compound.getInt("EffectTicks");
	}

	@Override
	public void tick() {
		super.tick();

		if (effectTicks <= 0)
			return;
		effectTicks--;

		if (!world.isRemote)
			return;

		Random rand = world.getRandom();
		if (rand.nextFloat() > 0.25f)
			return;

		spawnParticle(rand);
		playSound(rand);
	}

	protected void spawnParticle(Random rand) {
		double x = pos.getX() + rand.nextDouble();
		double y = pos.getY() + 0.5;
		double z = pos.getZ() + rand.nextDouble();
		double vx = rand.nextDouble() * 0.04 - 0.02;
		double vy = 0.1;
		double vz = rand.nextDouble() * 0.04 - 0.02;
		world.addParticle(ParticleTypes.SOUL, x, y, z, vx, vy, vz);
	}

	protected void playSound(Random rand) {
		float vol = rand.nextFloat() * 0.4F + rand.nextFloat() > 0.9F ? 0.6F : 0.0F;
		float pitch = 0.6F + rand.nextFloat() * 0.4F;
		world.playSound(null, pos, SoundEvents.PARTICLE_SOUL_ESCAPE, SoundCategory.BLOCKS, vol, pitch);
	}

}
