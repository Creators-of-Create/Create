package com.simibubi.create.foundation.mixin;

import com.simibubi.create.foundation.render.instancing.IInstanceRendered;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

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
        List<TileEntity> tiles = cir.getReturnValue();

        List<TileEntity> out = new ArrayList<>(tiles.size());

        for (TileEntity tile : tiles) {
            if (tile instanceof IInstanceRendered) {
                IInstanceRendered instanceRendered = (IInstanceRendered) tile;

                if (!instanceRendered.shouldRenderAsTE()) continue;
            }

            out.add(tile);
        }

        cir.setReturnValue(out);
    }
}
