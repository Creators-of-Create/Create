package com.simibubi.create.foundation.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.infrastructure.gametest.CreateTestFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.MultipleTestTracker;
import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.StructureBlockEntity;

@Mixin(TestCommand.class)
public class TestCommandMixin {
	@Redirect(
			method = "runTest(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/gametest/framework/MultipleTestTracker;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/gametest/framework/GameTestRegistry;getTestFunction(Ljava/lang/String;)Lnet/minecraft/gametest/framework/TestFunction;"
			),
			require = 0 // don't crash if this fails. non-critical
	)
	private static TestFunction create$getCorrectTestFunction(String testName,
															  ServerLevel level, BlockPos pos, @Nullable MultipleTestTracker tracker) {
		StructureBlockEntity be = (StructureBlockEntity) level.getBlockEntity(pos);
		CompoundTag data = be.getTileData();
		if (!data.contains("CreateTestFunction", Tag.TAG_STRING))
			return GameTestRegistry.getTestFunction(testName);
		String name = data.getString("CreateTestFunction");
		CreateTestFunction function = CreateTestFunction.NAMES_TO_FUNCTIONS.get(name);
		if (function == null)
			throw new IllegalStateException("Structure block has CreateTestFunction attached, but test [" + name + "] doesn't exist");
		return function;
	}
}
