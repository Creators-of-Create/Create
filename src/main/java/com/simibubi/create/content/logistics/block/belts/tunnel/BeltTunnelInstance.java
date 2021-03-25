package com.simibubi.create.content.logistics.block.belts.tunnel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.content.logistics.block.FlapData;
import com.simibubi.create.foundation.gui.widgets.InterpolatedValue;
import com.simibubi.create.foundation.render.backend.instancing.IDynamicInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.TileEntityInstance;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.Direction;
import net.minecraft.world.LightType;

public class BeltTunnelInstance extends TileEntityInstance<BeltTunnelTileEntity> implements IDynamicInstance {

    private final Map<Direction, ArrayList<InstanceKey<FlapData>>> tunnelFlaps;

    public BeltTunnelInstance(InstancedTileRenderer<?> modelManager, BeltTunnelTileEntity tile) {
        super(modelManager, tile);

        tunnelFlaps = new EnumMap<>(Direction.class);

        InstancedModel<FlapData> model = modelManager.getMaterial(KineticRenderMaterials.FLAPS)
                                                     .getModel(AllBlockPartials.BELT_TUNNEL_FLAP, blockState);

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

                InstanceKey<FlapData> key = model.createInstance();

                key.getInstance()
                   .setPosition(pos)
                   .setSegmentOffset(segmentOffset, 0, 0)
                   .setBlockLight(blockLight)
                   .setSkyLight(skyLight)
                   .setHorizontalAngle(horizontalAngle)
                   .setFlapness(flapness)
                   .setFlapScale(flapScale)
                   .setPivotVoxelSpace(0, 10, 1)
                   .setIntensity(intensity);

                flaps.add(key);
            }

            tunnelFlaps.put(direction, flaps);
        });
    }
    
	@Override
	public boolean shouldReset() {
		return super.shouldReset() || tunnelFlaps.size() != tile.flaps.size();
	}

    @Override
    public void beginFrame() {
        tunnelFlaps.forEach((direction, keys) -> {
            InterpolatedValue flapValue = tile.flaps.get(direction);
            if (flapValue == null) {
                return;
            }

            float flapness = flapValue.get(AnimationTickHolder.getPartialTicks());
            for (InstanceKey<FlapData> key : keys) {
                key.getInstance().setFlapness(flapness);
            }
        });
    }

    @Override
    public void updateLight() {
        int blockLight = world.getLightLevel(LightType.BLOCK, pos);
        int skyLight = world.getLightLevel(LightType.SKY, pos);

        for (ArrayList<InstanceKey<FlapData>> instanceKeys : tunnelFlaps.values()) {
            for (InstanceKey<FlapData> it : instanceKeys) {
                it.getInstance().setBlockLight(blockLight)
                                .setSkyLight(skyLight);
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
