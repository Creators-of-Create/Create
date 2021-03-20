package com.simibubi.create.foundation.render.backend.instancing;

import java.util.function.Consumer;

public class InstanceKey<D extends InstanceData> {
    public static final int INVALID = -1;

    InstancedModel<D> model;
    int index;

    public InstanceKey(InstancedModel<D> model, int index) {
        this.model = model;
        this.index = index;
    }

    void invalidate() {
        index = INVALID;
    }

    public boolean isValid() {
        return index != INVALID;
    }

    public D getInstance() {
        return model.getInstance(this);
    }

    public void delete() {
        model.deleteInstance(this);
    }
}
