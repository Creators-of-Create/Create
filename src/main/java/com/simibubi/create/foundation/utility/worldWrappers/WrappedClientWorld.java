package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
public class WrappedClientWorld extends ClientLevel {
	private static final Minecraft mc = Minecraft.getInstance();
	protected Level world;

	private WrappedClientWorld(Level world) {
		super(mc.getConnection(), mc.level.getLevelData(), world.dimension(), world.dimensionTypeRegistration(),
			mc.getConnection().serverChunkRadius, mc.level.getServerSimulationDistance(), world.getProfilerSupplier(),
			mc.levelRenderer, world.isDebug(), world.getBiomeManager().biomeZoomSeed);
		this.world = world;
	}

	public static WrappedClientWorld of(Level world) {
		return new WrappedClientWorld(world);
	}

	@Override
	public boolean hasChunkAt(BlockPos pos) {
		return world.hasChunkAt(pos);
	}

	@Override
	public boolean isLoaded(BlockPos pos) {
		return world.isLoaded(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return world.getBlockState(pos);
	}

	// FIXME: blockstate#getCollisionShape with WrappedClientWorld gives unreliable
	// data (maybe)

	@Override
	public int getBrightness(LightLayer type, BlockPos pos) {
		return world.getBrightness(type, pos);
	}

	@Override
	public int getLightEmission(BlockPos pos) {
		return world.getLightEmission(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return world.getFluidState(pos);
	}

	@Nullable
	@Override
	public <T extends LivingEntity> T getNearestEntity(List<? extends T> p_217361_1_, TargetingConditions p_217361_2_,
		@Nullable LivingEntity p_217361_3_, double p_217361_4_, double p_217361_6_, double p_217361_8_) {
		return world.getNearestEntity(p_217361_1_, p_217361_2_, p_217361_3_, p_217361_4_, p_217361_6_, p_217361_8_);
	}

	@Override
	public int getBlockTint(BlockPos p_225525_1_, ColorResolver p_225525_2_) {
		return world.getBlockTint(p_225525_1_, p_225525_2_);
	}

	// FIXME: Emissive Lighting might not light stuff properly

	@Override
	public void addParticle(ParticleOptions p_195594_1_, double p_195594_2_, double p_195594_4_, double p_195594_6_,
		double p_195594_8_, double p_195594_10_, double p_195594_12_) {
		world.addParticle(p_195594_1_, p_195594_2_, p_195594_4_, p_195594_6_, p_195594_8_, p_195594_10_, p_195594_12_);
	}

	@Override
	public void addParticle(ParticleOptions p_195590_1_, boolean p_195590_2_, double p_195590_3_, double p_195590_5_,
		double p_195590_7_, double p_195590_9_, double p_195590_11_, double p_195590_13_) {
		world.addParticle(p_195590_1_, p_195590_2_, p_195590_3_, p_195590_5_, p_195590_7_, p_195590_9_, p_195590_11_,
			p_195590_13_);
	}

	@Override
	public void addAlwaysVisibleParticle(ParticleOptions p_195589_1_, double p_195589_2_, double p_195589_4_,
		double p_195589_6_, double p_195589_8_, double p_195589_10_, double p_195589_12_) {
		world.addAlwaysVisibleParticle(p_195589_1_, p_195589_2_, p_195589_4_, p_195589_6_, p_195589_8_, p_195589_10_,
			p_195589_12_);
	}

	@Override
	public void addAlwaysVisibleParticle(ParticleOptions p_217404_1_, boolean p_217404_2_, double p_217404_3_,
		double p_217404_5_, double p_217404_7_, double p_217404_9_, double p_217404_11_, double p_217404_13_) {
		world.addAlwaysVisibleParticle(p_217404_1_, p_217404_2_, p_217404_3_, p_217404_5_, p_217404_7_, p_217404_9_,
			p_217404_11_, p_217404_13_);
	}

	@Override
	public void playLocalSound(double p_184134_1_, double p_184134_3_, double p_184134_5_, SoundEvent p_184134_7_,
		SoundSource p_184134_8_, float p_184134_9_, float p_184134_10_, boolean p_184134_11_) {
		world.playLocalSound(p_184134_1_, p_184134_3_, p_184134_5_, p_184134_7_, p_184134_8_, p_184134_9_, p_184134_10_,
			p_184134_11_);
	}

	@Override
	public void playSound(@Nullable Player p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_,
		SoundEvent p_184148_8_, SoundSource p_184148_9_, float p_184148_10_, float p_184148_11_) {
		world.playSound(p_184148_1_, p_184148_2_, p_184148_4_, p_184148_6_, p_184148_8_, p_184148_9_, p_184148_10_,
			p_184148_11_);
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos p_175625_1_) {
		return world.getBlockEntity(p_175625_1_);
	}

	public Level getWrappedWorld() {
		return world;
	}
}
