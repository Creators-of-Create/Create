package com.simibubi.create.content.equipment.bell;

import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.simibubi.create.AllPartialModels;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HauntedBellBlockEntity extends AbstractBellBlockEntity {

	public static final int DISTANCE = 10;
	public static final int RECHARGE_TICKS = 65;
	public static final int EFFECT_TICKS = 20;

	public int effectTicks = 0;

	public HauntedBellBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public PartialModel getBellModel() {
		return AllPartialModels.HAUNTED_BELL;
	}

	@Override
	public boolean ring(Level world, BlockPos pos, Direction direction) {
		if (isRinging && ringingTicks < RECHARGE_TICKS)
			return false;
		HauntedBellPulser.sendPulse(world, pos, DISTANCE, false);
		effectTicks = EFFECT_TICKS;
		return super.ring(world, pos, direction);
	}

	@Override
	protected void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("EffectTicks", effectTicks);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
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

		RandomSource rand = level.getRandom();
		if (rand.nextFloat() > 0.25f)
			return;

		spawnParticle(rand);
		playSound(rand);
	}

	protected void spawnParticle(RandomSource rand) {
		double x = worldPosition.getX() + rand.nextDouble();
		double y = worldPosition.getY() + 0.5;
		double z = worldPosition.getZ() + rand.nextDouble();
		double vx = rand.nextDouble() * 0.04 - 0.02;
		double vy = 0.1;
		double vz = rand.nextDouble() * 0.04 - 0.02;
		level.addParticle(ParticleTypes.SOUL, x, y, z, vx, vy, vz);
	}

	protected void playSound(RandomSource rand) {
		float vol = rand.nextFloat() * 0.4F + rand.nextFloat() > 0.9F ? 0.6F : 0.0F;
		float pitch = 0.6F + rand.nextFloat() * 0.4F;
		level.playSound(null, worldPosition, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, vol, pitch);
	}

}
