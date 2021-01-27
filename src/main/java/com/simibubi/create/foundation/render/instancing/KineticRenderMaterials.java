package com.simibubi.create.foundation.render.instancing;

import com.simibubi.create.foundation.render.instancing.actors.StaticRotatingActorData;

public class KineticRenderMaterials {
    public static final MaterialType<InstancedModel<RotatingData>> ROTATING = new MaterialType<>();
    public static final MaterialType<InstancedModel<BeltData>> BELTS = new MaterialType<>();

    public static final MaterialType<InstancedModel<StaticRotatingActorData>> ACTORS = new MaterialType<>();
}
