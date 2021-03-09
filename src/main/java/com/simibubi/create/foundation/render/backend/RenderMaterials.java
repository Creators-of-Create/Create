package com.simibubi.create.foundation.render.backend;

import com.simibubi.create.foundation.render.backend.instancing.MaterialType;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;

public class RenderMaterials {
    public static final MaterialType<InstancedModel<ModelData>> MODELS = new MaterialType<>();
}
