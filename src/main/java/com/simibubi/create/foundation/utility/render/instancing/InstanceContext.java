package com.simibubi.create.foundation.utility.render.instancing;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.render.FastContraptionRenderer;
import com.simibubi.create.foundation.utility.render.FastKineticRenderer;
import net.minecraft.tileentity.TileEntity;

public abstract class InstanceContext<T extends TileEntity> {

    public final T te;

    public InstanceContext(T te) {
        this.te = te;
    }

    public abstract FastKineticRenderer getKinetics();

    public abstract boolean checkWorldLight();

    public static class Contraption<T extends TileEntity> extends InstanceContext<T> {

        public final FastContraptionRenderer c;

        public Contraption(T te, FastContraptionRenderer c) {
            super(te);
            this.c = c;
        }

        @Override
        public FastKineticRenderer getKinetics() {
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
        public FastKineticRenderer getKinetics() {
            return CreateClient.kineticRenderer;
        }

        @Override
        public boolean checkWorldLight() {
            return true;
        }
    }
}
