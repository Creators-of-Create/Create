package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BeehiveBlock;

@Mixin(BeehiveBlock.class)
public class BeehiveBlockMixin {
	@ModifyExpressionValue(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/CampfireBlock;isSmokeyPos(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
	private static boolean create$dontGetAngryAtDeployers(boolean original, @Local(argsOnly = true) Player player) {
		return original || player instanceof DeployerFakePlayer;
	}
}
