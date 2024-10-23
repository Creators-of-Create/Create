package com.simibubi.create.foundation.mixin;

import javax.annotation.Nullable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import com.llamalad7.mixinextras.sugar.Local;

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
	@WrapOperation(
			method = "runTest(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/gametest/framework/MultipleTestTracker;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/gametest/framework/GameTestRegistry;getTestFunction(Ljava/lang/String;)Lnet/minecraft/gametest/framework/TestFunction;"
			),
			require = 0 // don't crash if this fails. non-critical
	)
	private static TestFunction create$getCorrectTestFunction(String pTestName, Operation<TestFunction> original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos) {
		StructureBlockEntity be = (StructureBlockEntity) level.getBlockEntity(pos);
		CompoundTag data = be.getPersistentData();
		if (!data.contains("CreateTestFunction", Tag.TAG_STRING))
			return original.call(pTestName);
		String name = data.getString("CreateTestFunction");
		CreateTestFunction function = CreateTestFunction.NAMES_TO_FUNCTIONS.get(name);
		if (function == null)
			throw new IllegalStateException("Structure block has CreateTestFunction attached, but test [" + name + "] doesn't exist");
		return function;
	}
}
