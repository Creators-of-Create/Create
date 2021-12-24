package com.simibubi.create.lib.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.event.ClientWorldEvents;
import com.simibubi.create.lib.event.InstanceRegistrationCallback;
import com.simibubi.create.lib.event.LeftClickAirCallback;
import com.simibubi.create.lib.event.ParticleManagerRegistrationCallback;
import com.simibubi.create.lib.event.RenderTickStartCallback;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow
	public LocalPlayer player;

	@Shadow
	public @Nullable ClientLevel level;

	@Inject(
			method = "<init>",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/Minecraft;particleEngine:Lnet/minecraft/client/particle/ParticleEngine;",
					shift = Shift.AFTER
			)
	)
	public void create$registerParticleManagers(GameConfig gameConfiguration, CallbackInfo ci) {
		ParticleManagerRegistrationCallback.EVENT.invoker().onParticleManagerRegistration();
	}

	// should inject to right after the initialization of resourceManager
	@Inject(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/packs/resources/SimpleReloadableResourceManager;<init>(Lnet/minecraft/server/packs/PackType;)V"
			)
	)
	public void create$instanceRegistration(GameConfig args, CallbackInfo ci) {
		InstanceRegistrationCallback.EVENT.invoker().registerInstance();
	}

	@Inject(method = "setLevel", at = @At("HEAD"))
	public void create$onHeadJoinWorld(ClientLevel world, CallbackInfo ci) {
		if (this.level != null) {
			ClientWorldEvents.UNLOAD.invoker().onWorldUnload((Minecraft) (Object) this, this.level);
		}
	}

	@Inject(
			method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V",
			at = @At(
					value = "JUMP",
					opcode = Opcodes.IFNULL,
					ordinal = 1,
					shift = Shift.AFTER
			)
	)
	public void create$onDisconnect(Screen screen, CallbackInfo ci) {
		ClientWorldEvents.UNLOAD.invoker().onWorldUnload((Minecraft) (Object) this, this.level);
	}

	@Inject(method = "startAttack", at = @At(value = "FIELD", ordinal = 2, target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;"))
	private void create$onClickMouse(CallbackInfo ci) {
		LeftClickAirCallback.EVENT.invoker().onLeftClickAir(player);
	}

	@Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/ToastComponent;render(Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = Shift.BEFORE))
	private void create$renderTickStart(CallbackInfo ci) {
		RenderTickStartCallback.EVENT.invoker().tick();
	}
}
