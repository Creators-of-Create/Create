package com.simibubi.create.foundation.render.instancing;

import com.simibubi.create.foundation.render.instancing.actors.StaticRotatingActorData;

public class KineticRenderMaterials {
    public static final MaterialType<InstanceBuffer<RotatingData>> ROTATING = new MaterialType<>();
    public static final MaterialType<InstanceBuffer<BeltData>> BELTS = new MaterialType<>();

    public static final MaterialType<InstanceBuffer<StaticRotatingActorData>> ACTORS = new MaterialType<>();
}
