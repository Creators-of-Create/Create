package com.simibubi.create.foundation.render;

import java.util.ArrayList;

import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.content.contraptions.base.RotatingInstancedModel;
import com.simibubi.create.content.contraptions.relays.belt.BeltInstancedModel;
import com.simibubi.create.content.logistics.block.FlapInstancedModel;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.gl.BasicProgram;
import com.simibubi.create.foundation.render.backend.gl.shader.ShaderCallback;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.RenderMaterial;

import com.simibubi.create.foundation.render.backend.instancing.impl.BasicInstancedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class KineticRenderer extends InstancedTileRenderer<BasicProgram> {
    public static int MAX_ORIGIN_DISTANCE = 100;

    public BlockPos originCoordinate = BlockPos.ZERO;

    @Override
    public void registerMaterials() {
        materials.put(RenderMaterials.MODELS, new RenderMaterial<>(this, AllProgramSpecs.MODEL, BasicInstancedModel::new));

        materials.put(KineticRenderMaterials.BELTS, new RenderMaterial<>(this, AllProgramSpecs.BELT, BeltInstancedModel::new));
        materials.put(KineticRenderMaterials.ROTATING, new RenderMaterial<>(this, AllProgramSpecs.ROTATING, RotatingInstancedModel::new));
        materials.put(KineticRenderMaterials.FLAPS, new RenderMaterial<>(this, AllProgramSpecs.FLAPS, FlapInstancedModel::new));
    }

    @Override
    public BlockPos getOriginCoordinate() {
        return originCoordinate;
    }

    @Override
    public void beginFrame(double cameraX, double cameraY, double cameraZ) {
        int cX = MathHelper.floor(cameraX);
        int cY = MathHelper.floor(cameraY);
        int cZ = MathHelper.floor(cameraZ);

        int dX = Math.abs(cX - originCoordinate.getX());
        int dY = Math.abs(cY - originCoordinate.getY());
        int dZ = Math.abs(cZ - originCoordinate.getZ());

        if (dX > MAX_ORIGIN_DISTANCE ||
                dY > MAX_ORIGIN_DISTANCE ||
                dZ > MAX_ORIGIN_DISTANCE) {

            originCoordinate = new BlockPos(cX, cY, cZ);

            ArrayList<TileEntity> instancedTiles = new ArrayList<>(instances.keySet());
            invalidate();
            instancedTiles.forEach(this::add);
        }

        super.beginFrame(cameraX, cameraY, cameraZ);
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
