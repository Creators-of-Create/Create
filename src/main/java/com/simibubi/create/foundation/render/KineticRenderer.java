package com.simibubi.create.foundation.render;

import java.util.ArrayList;

import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.content.contraptions.base.RotatingInstancedModel;
import com.simibubi.create.content.contraptions.relays.belt.BeltInstancedModel;
import com.simibubi.create.content.logistics.block.FlapInstancedModel;
import com.simibubi.create.foundation.render.backend.gl.BasicProgram;
import com.simibubi.create.foundation.render.backend.gl.shader.ShaderCallback;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.RenderMaterial;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class KineticRenderer extends InstancedTileRenderer<BasicProgram> {
    public static int MAX_ORIGIN_DISTANCE = 100;

    public BlockPos originCoordinate = BlockPos.ZERO;

    @Override
    public void registerMaterials() {
        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(this, AllProgramSpecs.BELT, BeltInstancedModel::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(this, AllProgramSpecs.ROTATING, RotatingInstancedModel::new));
        materials.put(KineticRenderMaterials.FLAPS, new RenderMaterial<>(this, AllProgramSpecs.FLAPS, FlapInstancedModel::new));
    }

    @Override
    public BlockPos getOriginCoordinate() {
        return originCoordinate;
    }

    @Override
    public void tick() {
        super.tick();

        Minecraft mc = Minecraft.getInstance();
        Entity renderViewEntity = mc.renderViewEntity;

        if (renderViewEntity == null) return;

        BlockPos renderViewPosition = renderViewEntity.getPosition();

        int dX = Math.abs(renderViewPosition.getX() - originCoordinate.getX());
        int dY = Math.abs(renderViewPosition.getY() - originCoordinate.getY());
        int dZ = Math.abs(renderViewPosition.getZ() - originCoordinate.getZ());

        if (dX > MAX_ORIGIN_DISTANCE ||
            dY > MAX_ORIGIN_DISTANCE ||
            dZ > MAX_ORIGIN_DISTANCE) {

            originCoordinate = renderViewPosition;

            ArrayList<TileEntity> instancedTiles = new ArrayList<>(instances.keySet());
            invalidate();
            instancedTiles.forEach(this::add);
        }
    }

    @Override
    public void render(RenderType layer, Matrix4f viewProjection, double camX, double camY, double camZ, ShaderCallback<BasicProgram> callback) {
        BlockPos originCoordinate = getOriginCoordinate();

        camX -= originCoordinate.getX();
        camY -= originCoordinate.getY();
        camZ -= originCoordinate.getZ();

        Matrix4f translate = Matrix4f.translate((float) -camX, (float) -camY, (float) -camZ);

        translate.multiplyBackward(viewProjection);

        super.render(layer, translate, camX, camY, camZ, callback);
    }
}
