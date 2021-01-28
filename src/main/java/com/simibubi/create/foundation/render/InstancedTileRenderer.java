package com.simibubi.create.foundation.render;

import com.simibubi.create.foundation.render.gl.shader.Shader;
import com.simibubi.create.foundation.render.gl.shader.ShaderCallback;
import com.simibubi.create.foundation.render.gl.shader.ShaderHelper;
import com.simibubi.create.foundation.render.instancing.*;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InstancedTileRenderer {
    protected Map<TileEntity, TileEntityInstance<?>> renderers = new HashMap<>();

    protected Map<MaterialType<?>, RenderMaterial<?>> materials = new HashMap<>();

    public InstancedTileRenderer() {
        registerMaterials();
    }

    public void registerMaterials() {
        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(Shader.BELT, BeltModel::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(Shader.ROTATING, RotatingModel::new));
    }

    @SuppressWarnings("unchecked")
    public <M extends InstancedModel<?>> RenderMaterial<M> get(MaterialType<M> materialType) {
        return (RenderMaterial<M>) materials.get(materialType);
    }

    @Nullable
    public <T extends TileEntity> TileEntityInstance<? super T> getInstance(T tile) {
        return getInstance(tile, true);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends TileEntity> TileEntityInstance<? super T> getInstance(T tile, boolean create) {
        if (renderers.containsKey(tile)) {
            return (TileEntityInstance<? super T>) renderers.get(tile);
        } else if (create) {
            TileEntityInstance<? super T> renderer = InstancedTileRenderRegistry.instance.create(this, tile);

            if (renderer != null) {
                FastRenderDispatcher.addedLastTick.get(tile.getWorld()).add(renderer);
                renderers.put(tile, renderer);
            }

            return renderer;
        } else {
            return null;
        }
    }

    public <T extends TileEntity> void onLightUpdate(T tile) {
        if (tile instanceof IInstanceRendered) {
            TileEntityInstance<? super T> instance = getInstance(tile);

            if (instance != null)
                instance.updateLight();
        }
    }

    public <T extends TileEntity> void update(T tile) {
        if (tile instanceof IInstanceRendered) {
            TileEntityInstance<? super T> instance = getInstance(tile);

            if (instance != null)
                instance.update();
        }
    }

    public <T extends TileEntity> void remove(T tile) {
        if (tile instanceof IInstanceRendered) {
            TileEntityInstance<? super T> instance = getInstance(tile, false);

            if (instance != null) {
                instance.remove();
                renderers.remove(tile);
            }
        }
    }

    public void clean() {
        // Clean up twice a second. This doesn't have to happen every tick,
        // but this does need to be run to ensure we don't miss anything.
        if (AnimationTickHolder.ticks % 10 == 0) {
            List<TileEntity> removed = renderers.keySet().stream().filter(TileEntity::isRemoved).collect(Collectors.toList());
            removed.forEach(renderers::remove);
        }
    }

    public void invalidate() {
        for (RenderMaterial<?> material : materials.values()) {
            material.delete();
        }
        renderers.clear();
    }

    public void render(RenderType layer, Matrix4f projection, Matrix4f view) {
        render(layer, projection, view, null);
    }

    public void render(RenderType layer, Matrix4f projection, Matrix4f view, ShaderCallback callback) {
        for (RenderMaterial<?> material : materials.values()) {
            if (material.canRenderInLayer(layer))
                material.render(layer, projection, view, callback);
        }

        ShaderHelper.releaseShader();
    }
}
