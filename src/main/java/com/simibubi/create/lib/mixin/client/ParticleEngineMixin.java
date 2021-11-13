package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.extensions.BlockStateExtensions;
import com.simibubi.create.lib.extensions.ParticleEngineExtensions;
import com.simibubi.create.lib.utility.MixinHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin implements ParticleEngineExtensions {
	@Shadow
	protected ClientLevel level;

	@Shadow
	protected abstract <T extends ParticleOptions> void register(ParticleType<T> particleType, ParticleEngine.SpriteParticleRegistration<T> spriteAwareFactory);

	@Shadow
	protected abstract <T extends ParticleOptions> void register(ParticleType<T> type, ParticleProvider<T> factory);

	@Override
	public <T extends ParticleOptions> void create$registerFactory0(ParticleType<T> particleType, ParticleEngine.SpriteParticleRegistration<T> spriteAwareFactory) {
		register(particleType, spriteAwareFactory);
	}

	@Override
	public <T extends ParticleOptions> void create$registerFactory1(ParticleType<T> type, ParticleProvider<T> factory) {
		register(type, factory);
	}

	@Inject(at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/level/block/state/BlockState;getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/shapes/VoxelShape;"),
			method = "destroy", cancellable = true)
	public void create$addBlockDestroyEffects(BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
		if (((BlockStateExtensions) blockState).create$addDestroyEffects(level, blockPos, MixinHelper.cast(this))) {
			ci.cancel();
		}
	}
}
