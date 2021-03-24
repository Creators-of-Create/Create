package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.content.contraptions.components.actors.ActorData;
import com.simibubi.create.content.contraptions.relays.belt.BeltData;
import com.simibubi.create.content.logistics.block.FlapData;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.MaterialType;

public class KineticRenderMaterials {
    public static final MaterialType<InstancedModel<RotatingData>> ROTATING = new MaterialType<>();
    public static final MaterialType<InstancedModel<BeltData>> BELTS = new MaterialType<>();

    public static final MaterialType<InstancedModel<ActorData>> ACTORS = new MaterialType<>();

    public static final MaterialType<InstancedModel<FlapData>> FLAPS = new MaterialType<>();
}
