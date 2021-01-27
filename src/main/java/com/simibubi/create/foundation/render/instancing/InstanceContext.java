package com.simibubi.create.foundation.render.instancing;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.render.InstancedTileRenderDispatcher;
import com.simibubi.create.foundation.render.contraption.RenderedContraption;
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

    public abstract InstancedTileRenderDispatcher getKinetics();

    public abstract boolean checkWorldLight();

    public static class Contraption<T extends TileEntity> extends InstanceContext<T> {

        public final RenderedContraption c;

        public Contraption(T te, RenderedContraption c) {
            super(te);
            this.c = c;
        }

        @Override
        public InstancedTileRenderDispatcher getKinetics() {
            return c.kinetics;
        }

        @Override
        public boolean checkWorldLight() {
            return false;
        }
    }

    public static class World<T extends TileEntity> extends InstanceContext<T> {

        public World(T te) {
            super(te);
        }

        @Override
        public InstancedTileRenderDispatcher getKinetics() {
            return CreateClient.kineticRenderer;
        }

        @Override
        public boolean checkWorldLight() {
            return true;
        }
    }
}
