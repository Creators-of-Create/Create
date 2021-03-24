package com.simibubi.create.foundation.render.backend;

import com.simibubi.create.foundation.render.backend.instancing.MaterialType;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.render.backend.instancing.impl.OrientedData;

public class RenderMaterials {
    public static final MaterialType<InstancedModel<ModelData>> TRANSFORMED = new MaterialType<>();
    public static final MaterialType<InstancedModel<OrientedData>> ORIENTED = new MaterialType<>();
}
