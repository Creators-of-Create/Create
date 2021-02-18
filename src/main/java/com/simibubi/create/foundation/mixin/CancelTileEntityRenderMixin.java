package com.simibubi.create.foundation.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.render.backend.instancing.IInstanceRendered;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(ChunkRenderDispatcher.CompiledChunk.class)
public class CancelTileEntityRenderMixin {

    /**
     * JUSTIFICATION: when instanced rendering is enabled, many tile entities no longer need
     * to be processed by the normal game renderer. This method is only called to retrieve the
     * list of tile entities to render. By filtering the output here, we prevent the game from
     * doing unnecessary light lookups and frustum checks.
     */
    @Inject(at = @At("RETURN"), method = "getTileEntities", cancellable = true)
    private void noRenderInstancedTiles(CallbackInfoReturnable<List<TileEntity>> cir) {
        if (FastRenderDispatcher.available()) {
            List<TileEntity> tiles = cir.getReturnValue();

            tiles.removeIf(tile -> tile instanceof IInstanceRendered && !((IInstanceRendered) tile).shouldRenderAsTE());
        }
    }
}
