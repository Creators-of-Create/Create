package com.simibubi.create.foundation.render.instancing;

import com.simibubi.create.foundation.render.FastRenderDispatcher;
import com.simibubi.create.foundation.render.gl.BasicProgram;
import com.simibubi.create.foundation.render.gl.shader.ShaderCallback;
import com.simibubi.create.foundation.render.gl.shader.ShaderHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class InstancedTileRenderer<P extends BasicProgram> {
    protected Map<TileEntity, TileEntityInstance<?>> instances = new HashMap<>();

    protected Map<MaterialType<?>, RenderMaterial<P, ?>> materials = new HashMap<>();

    protected InstancedTileRenderer() {
        registerMaterials();
    }

    public abstract void registerMaterials();

    @SuppressWarnings("unchecked")
    public <M extends InstancedModel<?>> RenderMaterial<P, M> getMaterial(MaterialType<M> materialType) {
        return (RenderMaterial<P, M>) materials.get(materialType);
    }

    @Nullable
    public <T extends TileEntity> TileEntityInstance<? super T> getInstance(T tile) {
        return getInstance(tile, true);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends TileEntity> TileEntityInstance<? super T> getInstance(T tile, boolean create) {
        TileEntityInstance<?> instance = instances.get(tile);

        if (instance != null) {
            return (TileEntityInstance<? super T>) instance;
        } else if (create) {
            TileEntityInstance<? super T> renderer = InstancedTileRenderRegistry.instance.create(this, tile);

            if (renderer != null) {
                FastRenderDispatcher.addedLastTick.get(tile.getWorld()).add(tile);
                instances.put(tile, renderer);
            }

            return renderer;
        } else {
            return null;
        }
    }

    public <T extends TileEntity> void onLightUpdate(T tile) {
        if (tile instanceof IInstanceRendered) {
            TileEntityInstance<? super T> instance = getInstance(tile, false);

            if (instance != null)
                instance.updateLight();
        }
    }

    public <T extends TileEntity> void add(T tile) {
        if (tile instanceof IInstanceRendered) {
            getInstance(tile);
        }
    }

    public <T extends TileEntity> void update(T tile) {
        if (tile instanceof IInstanceRendered) {
            TileEntityInstance<? super T> instance = getInstance(tile, false);

            if (instance != null)
                instance.update();
        }
    }

    public <T extends TileEntity> void remove(T tile) {
        if (tile instanceof IInstanceRendered) {
            TileEntityInstance<? super T> instance = getInstance(tile, false);

            if (instance != null) {
                instance.remove();
                instances.remove(tile);
            }
        }
    }

    public void clean() {
        // Clean up twice a second. This doesn't have to happen every tick,
        // but this does need to be run to ensure we don't miss anything.
        if (AnimationTickHolder.ticks % 10 == 0) {
            instances.keySet().stream().filter(TileEntity::isRemoved).forEach(instances::remove);
        }
    }

    public void invalidate() {
        for (RenderMaterial<?, ?> material : materials.values()) {
            material.delete();
        }
        instances.clear();
    }

    public void render(RenderType layer, Matrix4f viewProjection) {
        render(layer, viewProjection, null);
    }

    public void render(RenderType layer, Matrix4f viewProjection, ShaderCallback<P> callback) {
        for (RenderMaterial<P, ?> material : materials.values()) {
            if (material.canRenderInLayer(layer))
                material.render(layer, viewProjection, callback);
        }

        ShaderHelper.releaseShader();
    }
}
