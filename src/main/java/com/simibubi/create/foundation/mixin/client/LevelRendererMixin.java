package com.simibubi.create.foundation.mixin.client;

import java.util.Set;
import java.util.SortedSet;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.google.common.collect.Sets;
import com.simibubi.create.foundation.block.render.BlockDestructionProgressExtension;
import com.simibubi.create.foundation.block.render.MultiPosDestructionHandler;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Shadow
	private ClientLevel level;

	@Shadow
	@Final
	private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

	@Inject(method = "destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/BlockDestructionProgress;updateTick(I)V", shift = Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	private void create$onDestroyBlockProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci, BlockDestructionProgress progressObj) {
		BlockState state = level.getBlockState(pos);
		IClientBlockExtensions properties = IClientBlockExtensions.of(state);
		if (properties instanceof MultiPosDestructionHandler handler) {
			Set<BlockPos> extraPositions = handler.getExtraPositions(level, pos, state, progress);
			if (extraPositions != null) {
				extraPositions.remove(pos);
				((BlockDestructionProgressExtension) progressObj).create$setExtraPositions(extraPositions);
				for (BlockPos extraPos : extraPositions) {
					destructionProgress.computeIfAbsent(extraPos.asLong(), l -> Sets.newTreeSet()).add(progressObj);
				}
			}
		}
	}

	@Inject(method = "removeProgress(Lnet/minecraft/server/level/BlockDestructionProgress;)V", at = @At("RETURN"))
	private void create$onRemoveProgress(BlockDestructionProgress progress, CallbackInfo ci) {
		Set<BlockPos> extraPositions = ((BlockDestructionProgressExtension) progress).create$getExtraPositions();
		if (extraPositions != null) {
			for (BlockPos extraPos : extraPositions) {
				long l = extraPos.asLong();
				Set<BlockDestructionProgress> set = destructionProgress.get(l);
				if (set != null) {
					set.remove(progress);
					if (set.isEmpty()) {
						destructionProgress.remove(l);
					}
				}
			}
		}
	}
}
