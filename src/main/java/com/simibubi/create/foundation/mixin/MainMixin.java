package com.simibubi.create.foundation.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.Main;

import net.minecraft.server.MinecraftServer;

import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collection;

@Mixin(Main.class)
public class MainMixin {

	/**
	 * Forge completely bypasses vanilla's
	 * {@link GameTestServer#create(Thread, LevelStorageAccess, PackRepository, Collection, BlockPos)},
	 * which causes tests to generate at bedrock level in a regular world. This causes interference
	 * (ex. darkness, liquids, gravel) that makes tests fail and act inconsistently. Replacing the server Forge
	 * makes with one made by vanilla's factory causes tests to run on a superflat, as they should.
	 * <p>
	 * The system property 'create.useOriginalGametestServer' may be set to true to avoid this behavior.
	 * This may be desirable for other mods which pull Create into their development environments.
	 */
	@ModifyVariable(
			method = "lambda$main$5",
			at = @At(
					value = "STORE",
					ordinal = 0
			),
			require = 0 // don't crash if this fails
	)
	private static MinecraftServer create$correctlyInitializeGametestServer(MinecraftServer original) {
		if (original instanceof GameTestServer && !Boolean.getBoolean("create.useOriginalGametestServer")) {
			return GameTestServer.create(
					original.getRunningThread(),
					original.storageSource,
					original.getPackRepository(),
					GameTestRunner.groupTestsIntoBatches(GameTestRegistry.getAllTestFunctions()),
					BlockPos.ZERO
			);
		}
		return original;
	}
}
