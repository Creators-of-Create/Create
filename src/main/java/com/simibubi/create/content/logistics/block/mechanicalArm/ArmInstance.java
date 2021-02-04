package com.simibubi.create.content.logistics.block.mechanicalArm;

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

public class ArmInstance extends SingleRotatingInstance {
    public static void register(TileEntityType<? extends KineticTileEntity> type) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
                InstancedTileRenderRegistry.instance.register(type, ArmInstance::new));
    }

    public ArmInstance(InstancedTileRenderer modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected InstancedModel<RotatingData> getModel() {
        return AllBlockPartials.ARM_COG.renderOnRotating(modelManager, tile.getBlockState());
    }
}
