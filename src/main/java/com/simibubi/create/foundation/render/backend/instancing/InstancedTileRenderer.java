package com.simibubi.create.foundation.render.backend.instancing;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.render.backend.Backend;
import com.simibubi.create.foundation.render.backend.gl.BasicProgram;
import com.simibubi.create.foundation.render.backend.gl.shader.ShaderCallback;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public abstract class InstancedTileRenderer<P extends BasicProgram> {
    protected Map<TileEntity, TileEntityInstance<?>> instances = new HashMap<>();

    protected Map<MaterialType<?>, RenderMaterial<P, ?>> materials = new HashMap<>();

    protected InstancedTileRenderer() {
        registerMaterials();
    }

    public abstract BlockPos getOriginCoordinate();

    public abstract void registerMaterials();

    public void tick() {
        int ticks = AnimationTickHolder.getTicks();

        // Clean up twice a second. This doesn't have to happen every tick,
        // but this does need to be run to ensure we don't miss anything.
        if (ticks % 10 == 0) {
            clean();
        }
    }

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
        if (!Backend.canUseInstancing()) return null;

        TileEntityInstance<?> instance = instances.get(tile);

        if (instance != null) {
            return (TileEntityInstance<? super T>) instance;
        } else if (create) {
            TileEntityInstance<? super T> renderer = InstancedTileRenderRegistry.instance.create(this, tile);

            if (renderer != null) {
                instances.put(tile, renderer);
            }

            return renderer;
        } else {
            return null;
        }
    }

    public <T extends TileEntity> void onLightUpdate(T tile) {
        if (!Backend.canUseInstancing()) return;

        if (tile instanceof IInstanceRendered) {
            TileEntityInstance<? super T> instance = getInstance(tile, false);

            if (instance != null)
                instance.updateLight();
        }
    }

    public <T extends TileEntity> void add(T tile) {
        if (!Backend.canUseInstancing()) return;

        if (tile instanceof IInstanceRendered) {
            getInstance(tile);
        }
    }

    public <T extends TileEntity> void update(T tile) {
        if (!Backend.canUseInstancing()) return;

        if (tile instanceof IInstanceRendered) {
            TileEntityInstance<? super T> instance = getInstance(tile, false);

            if (instance != null)
                instance.update();
        }
    }

    public <T extends TileEntity> void remove(T tile) {
        if (!Backend.canUseInstancing()) return;

        if (tile instanceof IInstanceRendered) {
            TileEntityInstance<? super T> instance = getInstance(tile, false);

            if (instance != null) {
                instance.remove();
                instances.remove(tile);
            }
        }
    }

    public void clean() {
        instances.keySet().removeIf(TileEntity::isRemoved);
    }

    public void invalidate() {
        for (RenderMaterial<?, ?> material : materials.values()) {
            material.delete();
        }
        instances.clear();
    }

    public void render(RenderType layer, Matrix4f viewProjection, double camX, double camY, double camZ) {
        render(layer, viewProjection, camX, camY, camZ, null);
    }

    public void render(RenderType layer, Matrix4f viewProjection, double camX, double camY, double camZ, ShaderCallback<P> callback) {
        for (RenderMaterial<P, ?> material : materials.values()) {
            if (material.canRenderInLayer(layer))
                material.render(layer, viewProjection, camX, camY, camZ, callback);
        }
    }
}
