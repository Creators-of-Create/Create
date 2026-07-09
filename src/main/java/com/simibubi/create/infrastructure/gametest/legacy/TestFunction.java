package com.simibubi.create.infrastructure.gametest.legacy;

import java.util.function.Consumer;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Rotation;

/**
 * Temporary Create 26.2 bridge for legacy Create test discovery code.
 * TODO 26.2: Replace with FunctionGameTestInstance/GeneratedTest instances.
 */
@Deprecated(forRemoval = true)
public record TestFunction(String batchName, String testName, String structureName, Rotation rotation,
						   int maxTicks, long setupTicks, boolean required, boolean manualOnly,
						   int maxAttempts, int requiredSuccesses, boolean skyAccess,
						   Consumer<GameTestHelper> function) {
}
