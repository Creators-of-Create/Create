package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.extensions.TerrainParticleExtensions;
import com.simibubi.create.lib.util.MixinHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
@Mixin(TerrainParticle.class)
public abstract class TerrainParticleMixin extends TextureSheetParticle implements TerrainParticleExtensions {
	@Unique
	private BlockState create$state;

	private TerrainParticleMixin(ClientLevel clientWorld, double d, double e, double f) {
		super(clientWorld, d, e, f);
		throw new AssertionError("Create Refabricated's TerrainParticleMixin dummy constructor called!");
	}

	@Inject(
			method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V",
			at = @At("RETURN")
	)
	private void create$init(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i,
							 BlockState blockState, BlockPos blockPos, CallbackInfo ci) {
		this.create$state = blockState;
	}

	@Unique
	@Override
	public Particle create$updateSprite(BlockPos pos) {
		if (pos != null)
			this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(create$state));
		return MixinHelper.cast(this);
	}
}
