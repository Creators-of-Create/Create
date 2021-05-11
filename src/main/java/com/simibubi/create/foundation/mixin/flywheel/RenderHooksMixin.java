package com.simibubi.create.foundation.mixin.flywheel;

import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.OptifineHandler;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(WorldRenderer.class)
public class RenderHooksMixin {

	@Shadow
	private ClientWorld world;

	@Inject(at = @At(value = "INVOKE", target = "net.minecraft.client.renderer.WorldRenderer.updateChunks(J)V"), method = "render")
	private void setupFrame(MatrixStack stack, float p_228426_2_, long p_228426_3_, boolean p_228426_5_,
							ActiveRenderInfo info, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f p_228426_9_,
							CallbackInfo ci) {
		Backend.listeners.setupFrame(world, stack, info, gameRenderer, lightTexture);
	}

	/**
	 * JUSTIFICATION: This method is called once per layer per frame. It allows us to perform
	 * layer-correct custom rendering. RenderWorldLast is not refined enough for rendering world objects.
	 * This should probably be a forge event.
	 */
	@Inject(at = @At("TAIL"), method = "renderLayer")
	private void renderLayer(RenderType type, MatrixStack stack, double camX, double camY, double camZ,
							 CallbackInfo ci) {
		if (!Backend.available())
			return;

		Matrix4f view = stack.peek()
				.getModel();
		Matrix4f viewProjection = view.copy();
		viewProjection.multiplyBackward(Backend.getProjectionMatrix());

		Backend.listeners.renderLayer(world, type, viewProjection, camX, camY, camZ);
		GL20.glUseProgram(0);
	}

	@Inject(at = @At("TAIL"), method = "loadRenderers")
	private void refresh(CallbackInfo ci) {
		OptifineHandler.refresh();
		Backend.refresh();

		Backend.listeners.refresh(world);
	}


	@Inject(at =
	@At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/WorldRenderer;checkEmpty(Lcom/mojang/blaze3d/matrix/MatrixStack;)V",
			ordinal = 2 // after the game renders the breaking overlay normally
	),
			method = "render")
	private void renderBlockBreaking(MatrixStack stack, float p_228426_2_, long p_228426_3_, boolean p_228426_5_,
									 ActiveRenderInfo info, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f p_228426_9_,
									 CallbackInfo ci) {
		if (!Backend.available())
			return;

		Matrix4f view = stack.peek()
				.getModel();
		Matrix4f viewProjection = view.copy();
		viewProjection.multiplyBackward(Backend.getProjectionMatrix());

		Vector3d cameraPos = info.getProjectedView();
		Backend.renderBreaking(world, viewProjection, cameraPos.x, cameraPos.y, cameraPos.z);
		GL20.glUseProgram(0);
	}

	// Instancing

	@Inject(at = @At("TAIL"), method = "scheduleBlockRerenderIfNeeded")
	private void checkUpdate(BlockPos pos, BlockState lastState, BlockState newState, CallbackInfo ci) {
		Backend.tileRenderer.get(world)
				.update(world.getTileEntity(pos));
	}
}
