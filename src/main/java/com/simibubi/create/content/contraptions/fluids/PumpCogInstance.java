package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.render.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.instancing.InstancedModel;
import com.simibubi.create.foundation.render.instancing.InstancedTileRenderRegistry;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class PumpCogInstance extends SingleRotatingInstance {
    public static void register(TileEntityType<? extends KineticTileEntity> type) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
                InstancedTileRenderRegistry.instance.register(type, PumpCogInstance::new));
    }

    public PumpCogInstance(InstancedTileRenderer modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected InstancedModel<RotatingData> getModel() {
        return AllBlockPartials.MECHANICAL_PUMP_COG.renderOnDirectionalSouthRotating(modelManager, tile.getBlockState());
    }
}
