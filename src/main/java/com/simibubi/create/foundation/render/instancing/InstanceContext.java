package com.simibubi.create.foundation.render.instancing;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.render.InstancedTileRenderer;
import net.minecraft.tileentity.TileEntity;

@Deprecated
public abstract class InstanceContext<T extends TileEntity> {

    public final T te;

    public InstanceContext(T te) {
        this.te = te;
    }

    public RenderMaterial<InstancedModel<RotatingData>> getRotating() {
        return getKinetics().get(KineticRenderMaterials.ROTATING);
    }

    public RenderMaterial<InstancedModel<BeltData>> getBelts() {
        return getKinetics().get(KineticRenderMaterials.BELTS);
    }

    public abstract InstancedTileRenderer getKinetics();

    public abstract boolean checkWorldLight();

    public static class World<T extends TileEntity> extends InstanceContext<T> {

        public World(T te) {
            super(te);
        }

        @Override
        public InstancedTileRenderer getKinetics() {
            return CreateClient.kineticRenderer;
        }

        @Override
        public boolean checkWorldLight() {
            return true;
        }
    }
}
