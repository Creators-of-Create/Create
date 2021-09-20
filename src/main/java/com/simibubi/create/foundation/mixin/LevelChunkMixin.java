package com.simibubi.create.foundation.mixin;

import com.simibubi.create.foundation.utility.worldWrappers.chunk.EmptierChunk;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;

import net.minecraft.world.level.levelgen.Heightmap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.EnumSet;

/**
 * Needed for EmptierChunk to work.
 */
@Mixin(LevelChunk.class)
public class LevelChunkMixin {

	@Redirect(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/ChunkBiomeContainer;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/level/TickList;Lnet/minecraft/world/level/TickList;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkStatus;heightmapsAfter()Ljava/util/EnumSet;"))
	private EnumSet<Heightmap.Types> redirectHeightmaps(ChunkStatus chunkStatus) {
		if((Object) this instanceof EmptierChunk){
			return EnumSet.noneOf(Heightmap.Types.class);
		}
		return ChunkStatus.FULL.heightmapsAfter();
	}

	@Redirect(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/ChunkBiomeContainer;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/level/TickList;Lnet/minecraft/world/level/TickList;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getSectionsCount()I"))
	private int redirectSectionCount(Level level) {
		if((Object) this instanceof EmptierChunk){
			return 1;
		}
		return level.getSectionsCount();
	}
}
