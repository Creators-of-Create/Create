package com.simibubi.create.foundation.mixin;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.backend.RenderWork;
import com.simibubi.create.foundation.render.backend.light.ILightListener;
import com.simibubi.create.foundation.render.backend.light.LightUpdater;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.play.server.SUpdateLightPacket;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.chunk.Chunk;

import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetHandler.class)
public class NetworkLightUpdateMixin {

    @Inject(at = @At("TAIL"), method = "handleUpdateLight")
    private void onLightPacket(SUpdateLightPacket packet, CallbackInfo ci) {
        RenderWork.enqueue(() -> {
            ClientWorld world = Minecraft.getInstance().world;

            if (world == null) return;

            int chunkX = packet.getChunkX();
            int chunkZ = packet.getChunkZ();

            Chunk chunk = world.getChunkProvider().getChunk(chunkX, chunkZ, false);

            if (chunk != null) {
                chunk.getTileEntityMap()
                        .values()
                        .forEach(tile -> {
                            CreateClient.kineticRenderer.get(world).onLightUpdate(tile);

                            if (tile instanceof ILightListener)
                                ((ILightListener) tile).onChunkLightUpdate();
                        });
            }

            LightUpdater.getInstance().onLightPacket(world, chunkX, chunkZ);
        });
    }
}
