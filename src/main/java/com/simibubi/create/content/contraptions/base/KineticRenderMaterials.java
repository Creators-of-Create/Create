package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.backend.MaterialType;
import com.jozufozu.flywheel.backend.instancing.InstancedModel;
import com.simibubi.create.content.contraptions.components.actors.ActorData;
import com.simibubi.create.content.contraptions.relays.belt.BeltData;
import com.simibubi.create.content.logistics.block.FlapData;

public class KineticRenderMaterials {
    public static final MaterialType<InstancedModel<RotatingData>> ROTATING = new MaterialType<>();
    public static final MaterialType<InstancedModel<BeltData>> BELTS = new MaterialType<>();

    public static final MaterialType<InstancedModel<ActorData>> ACTORS = new MaterialType<>();

    public static final MaterialType<InstancedModel<FlapData>> FLAPS = new MaterialType<>();
}
