package com.simibubi.create.foundation.render.backend;

import com.simibubi.create.content.contraptions.components.actors.ContraptionActorData;
import com.simibubi.create.foundation.render.backend.instancing.MaterialType;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.impl.TransformData;

public class RenderMaterials {
    public static final MaterialType<InstancedModel<TransformData>> MODELS = new MaterialType<>();
}
