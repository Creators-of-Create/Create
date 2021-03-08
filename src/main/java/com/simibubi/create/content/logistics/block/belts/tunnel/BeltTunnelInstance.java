package com.simibubi.create.content.logistics.block.belts.tunnel;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.content.logistics.block.FlapData;
import com.simibubi.create.foundation.gui.widgets.InterpolatedValue;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public class BeltTunnelInstance extends TileEntityInstance<BeltTunnelTileEntity> implements ITickableInstance {
    public static void register(TileEntityType<? extends BeltTunnelTileEntity> type) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                InstancedTileRenderRegistry.instance.register(type, BeltTunnelInstance::new));
    }

    private Map<Direction, ArrayList<InstanceKey<FlapData>>> tunnelFlaps;

    public BeltTunnelInstance(InstancedTileRenderer<?> modelManager, BeltTunnelTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected void init() {
        tunnelFlaps = new EnumMap<>(Direction.class);

        InstancedModel<FlapData> model = modelManager.getMaterial(KineticRenderMaterials.FLAPS)
                                                     .getModel(AllBlockPartials.BELT_TUNNEL_FLAP, lastState);

        int blockLight = world.getLightLevel(LightType.BLOCK, pos);
        int skyLight = world.getLightLevel(LightType.SKY, pos);

        tile.flaps.forEach((direction, flapValue) -> {

            float flapness = flapValue.get(AnimationTickHolder.getPartialTicks());

            float horizontalAngle = direction.getOpposite().getHorizontalAngle();

            float flapScale = direction.getAxis() == Direction.Axis.X ? 1 : -1;

            ArrayList<InstanceKey<FlapData>> flaps = new ArrayList<>(4);

            for (int segment = 0; segment <= 3; segment++) {
                float intensity = segment == 3 ? 1.5f : segment + 1;
                float segmentOffset = -3 / 16f * segment;

                flaps.add(model.createInstance(flapData -> {
                    flapData.setPosition(pos)
                            .setSegmentOffset(segmentOffset, 0, 0)
                            .setBlockLight(blockLight)
                            .setSkyLight(skyLight)
                            .setHorizontalAngle(horizontalAngle)
                            .setFlapness(flapness)
                            .setFlapScale(flapScale)
                            .setPivotVoxelSpace(0, 10, 1)
                            .setIntensity(intensity);
                }));
            }

            tunnelFlaps.put(direction, flaps);
        });
    }

    @Override
    public void tick() {
        tunnelFlaps.forEach((direction, keys) -> {
            InterpolatedValue flapValue = tile.flaps.get(direction);
            if (flapValue == null) {
                return;
            }

            float flapness = flapValue.get(AnimationTickHolder.getPartialTicks());
            for (InstanceKey<FlapData> key : keys) {
                key.modifyInstance(data -> data.setFlapness(flapness));
            }
        });
    }

    @Override
    protected void onUpdate() { }

    @Override
    public void updateLight() {
        int blockLight = world.getLightLevel(LightType.BLOCK, pos);
        int skyLight = world.getLightLevel(LightType.SKY, pos);

        for (ArrayList<InstanceKey<FlapData>> instanceKeys : tunnelFlaps.values()) {
            for (InstanceKey<FlapData> it : instanceKeys) {
                it.modifyInstance(data -> data.setBlockLight(blockLight)
                                              .setSkyLight(skyLight));
            }
        }
    }

    @Override
    public void remove() {
        tunnelFlaps.values()
                   .stream()
                   .flatMap(Collection::stream)
                   .forEach(InstanceKey::delete);
    }
}
