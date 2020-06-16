package com.simibubi.create.content.contraptions.fluids;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.IFluidTank;

import java.util.Collection;
import java.util.Collections;

public class FluidTankRenderer extends SafeTileEntityRenderer<FluidTankTileEntity> {

    public FluidTankRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    private static int[] decomposeColor(int color) {
        int[] res = new int[4];
        res[0] = color >> 24 & 0xff;
        res[1] = color >> 16 & 0xff;
        res[2] = color >> 8 & 0xff;
        res[3] = color & 0xff;
        return res;
    }

    @Override
    protected void renderSafe(FluidTankTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
                              int light, int overlay) {
        renderFluid(te, partialTicks, ms, buffer, light, overlay);
    }

    private void renderFluid(FluidTankTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
                             int light, int overlay) {
        if (te.getWorld().isAreaLoaded(te.getPos(), 0)) {
            IVertexBuilder builder = buffer.getBuffer(RenderType.getTranslucent());

            Matrix4f posMat = ms.peek().getModel();
            for (TankRenderInfo tankRenderInfo : getTanksToRender(te)) {
                doRender(builder, tankRenderInfo, posMat, light);
            }
        }


        // Minecraft.getInstance().getBlockRendererDispatcher().renderFluid(te.getPos().up(2), Minecraft.getInstance().world, buffer.getBuffer(RenderType.getSolid()), tank.getFluid().getRawFluid().getDefaultState());
    }

    private void doRender(IVertexBuilder builder, TankRenderInfo tankRenderInfo, Matrix4f posMat, int combinedLight) {
        IFluidTank tank = tankRenderInfo.getTank();
        if (tank.getFluidAmount() == 0) return;

        Fluid fluid = tank.getFluid().getFluid();
        ResourceLocation texture = fluid.getAttributes().getStillTexture(tank.getFluid());

        TextureAtlasSprite still = Minecraft.getInstance().getSpriteAtlas(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(texture);
        int[] cols = decomposeColor(fluid.getAttributes().getColor(tank.getFluid()));

        AxisAlignedBB bounds = getRenderBounds(tank, tankRenderInfo.getBounds());
        float x1 = (float) bounds.minX;
        float x2 = (float) bounds.maxX;
        float y1 = (float) bounds.minY;
        float y2 = (float) bounds.maxY;
        float z1 = (float) bounds.minZ;
        float z2 = (float) bounds.maxZ;
        double bx1 = bounds.minX * 16;
        double bx2 = bounds.maxX * 16;
        double by1 = bounds.minY * 16;
        double by2 = bounds.maxY * 16;
        double bz1 = bounds.minZ * 16;
        double bz2 = bounds.maxZ * 16;

        if (tankRenderInfo.shouldRender(Direction.DOWN)) {
            float u1 = still.getInterpolatedU(bx1);
            float u2 = still.getInterpolatedU(bx2);
            float v1 = still.getInterpolatedV(bz1);
            float v2 = still.getInterpolatedV(bz2);
            renderDown(builder, posMat, combinedLight, cols, x1, y1, z1, z2, u1, v1, v2);
            renderDown(builder, posMat, combinedLight, cols, x2, y1, z2, z1, u2, v2, v1);
        }

        if (tankRenderInfo.shouldRender(Direction.UP)) {
            float u1 = still.getInterpolatedU(bx1);
            float u2 = still.getInterpolatedU(bx2);
            float v1 = still.getInterpolatedV(bz1);
            float v2 = still.getInterpolatedV(bz2);
            renderUp(builder, posMat, combinedLight, cols, x2, x1, y2, z2, u2, u1, v2);
            renderUp(builder, posMat, combinedLight, cols, x1, x2, y2, z1, u1, u2, v1);
        }

        if (tankRenderInfo.shouldRender(Direction.NORTH)) {
            float u1 = still.getInterpolatedU(bx1);
            float u2 = still.getInterpolatedU(bx2);
            float v1 = still.getInterpolatedV(by1);
            float v2 = still.getInterpolatedV(by2);
            renderNorth(builder, posMat, combinedLight, cols, x1, y1, y2, z1, u1, v1, v2);
            renderNorth(builder, posMat, combinedLight, cols, x2, y2, y1, z1, u2, v2, v1);
        }

        if (tankRenderInfo.shouldRender(Direction.SOUTH)) {
            float u1 = still.getInterpolatedU(bx1);
            float u2 = still.getInterpolatedU(bx2);
            float v1 = still.getInterpolatedV(by1);
            float v2 = still.getInterpolatedV(by2);
            renderSouth(builder, posMat, combinedLight, cols, x2, y1, y2, z2, u2, v1, v2);
            renderSouth(builder, posMat, combinedLight, cols, x1, y2, y1, z2, u1, v2, v1);
        }

        if (tankRenderInfo.shouldRender(Direction.WEST)) {
            float u1 = still.getInterpolatedU(by1);
            float u2 = still.getInterpolatedU(by2);
            float v1 = still.getInterpolatedV(bz1);
            float v2 = still.getInterpolatedV(bz2);
            renderWest(builder, posMat, combinedLight, cols, x1, y1, y2, z2, u1, u2, v2);
            renderWest(builder, posMat, combinedLight, cols, x1, y2, y1, z1, u2, u1, v1);
        }

        if (tankRenderInfo.shouldRender(Direction.EAST)) {
            float u1 = still.getInterpolatedU(by1);
            float u2 = still.getInterpolatedU(by2);
            float v1 = still.getInterpolatedV(bz1);
            float v2 = still.getInterpolatedV(bz2);
            renderEast(builder, posMat, combinedLight, cols, x2, y1, y2, z1, u1, u2, v1);
            renderEast(builder, posMat, combinedLight, cols, x2, y2, y1, z2, u2, u1, v2);
        }
    }

    private void renderEast(IVertexBuilder builder, Matrix4f posMat, int combinedLight, int[] cols, float x2, float y1, float y2, float z1, float u1, float u2, float v1) {
        builder.vertex(posMat, x2, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).texture(u1, v1).light(combinedLight).normal(1f, 0f, 0f).endVertex();
        builder.vertex(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).texture(u2, v1).light(combinedLight).normal(1f, 0f, 0f).endVertex();
    }

    private void renderWest(IVertexBuilder builder, Matrix4f posMat, int combinedLight, int[] cols, float x1, float y1, float y2, float z2, float u1, float u2, float v2) {
        builder.vertex(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).texture(u1, v2).light(combinedLight).normal(-1f, 0f, 0f).endVertex();
        builder.vertex(posMat, x1, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).texture(u2, v2).light(combinedLight).normal(-1f, 0f, 0f).endVertex();
    }

    private void renderSouth(IVertexBuilder builder, Matrix4f posMat, int combinedLight, int[] cols, float x2, float y1, float y2, float z2, float u2, float v1, float v2) {
        builder.vertex(posMat, x2, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).texture(u2, v1).light(combinedLight).normal(0f, 0f, 1f).endVertex();
        builder.vertex(posMat, x2, y2, z2).color(cols[1], cols[2], cols[3], cols[0]).texture(u2, v2).light(combinedLight).normal(0f, 0f, 1f).endVertex();
    }

    private void renderNorth(IVertexBuilder builder, Matrix4f posMat, int combinedLight, int[] cols, float x1, float y1, float y2, float z1, float u1, float v1, float v2) {
        builder.vertex(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).texture(u1, v1).light(combinedLight).normal(0f, 0f, -1f).endVertex();
        builder.vertex(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).texture(u1, v2).light(combinedLight).normal(0f, 0f, -1f).endVertex();
    }

    private void renderUp(IVertexBuilder builder, Matrix4f posMat, int combinedLight, int[] cols, float x1, float x2, float y2, float z1, float u1, float u2, float v1) {
        builder.vertex(posMat, x2, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).texture(u2, v1).light(combinedLight).normal(0f, 1f, 0f).endVertex();
        builder.vertex(posMat, x1, y2, z1).color(cols[1], cols[2], cols[3], cols[0]).texture(u1, v1).light(combinedLight).normal(0f, 1f, 0f).endVertex();
    }

    private void renderDown(IVertexBuilder builder, Matrix4f posMat, int combinedLight, int[] cols, float x1, float y1, float z1, float z2, float u1, float v1, float v2) {
        builder.vertex(posMat, x1, y1, z2).color(cols[1], cols[2], cols[3], cols[0]).texture(u1, v2).light(combinedLight).normal(0f, -1f, 0f).endVertex();
        builder.vertex(posMat, x1, y1, z1).color(cols[1], cols[2], cols[3], cols[0]).texture(u1, v1).light(combinedLight).normal(0f, -1f, 0f).endVertex();
    }

    private AxisAlignedBB getRenderBounds(IFluidTank tank, AxisAlignedBB tankBounds) {
        float percent = (float) tank.getFluidAmount() / (float) tank.getCapacity();

        double tankHeight = tankBounds.maxY - tankBounds.minY;
        double y1 = tankBounds.minY;
        double y2 = tank.getFluidAmount() < tank.getCapacity() ? (3 / 16f + (10 / 16f * percent)) : tankBounds.maxY;
        if (tank.getFluid().getFluid().getAttributes().isLighterThanAir()) {
            double yOff = tankBounds.maxY - y2;  // lighter than air fluids move to the top of the tank
            y1 += yOff;
            y2 += yOff;
        }
        return new AxisAlignedBB(tankBounds.minX, y1, tankBounds.minZ, tankBounds.maxX, y2, tankBounds.maxZ);
    }

    private Collection<TankRenderInfo> getTanksToRender(FluidTankTileEntity te) {
        FluidTankTileEntity upTE = te.getOtherFluidTankTileEntity(Direction.UP);
        FluidTankTileEntity downTE = te.getOtherFluidTankTileEntity(Direction.DOWN);
        boolean up = upTE == null || upTE.getTank().getFluidAmount() == 0 || te.getTank().getFluid() != upTE.getTank().getFluid();
        boolean down = downTE == null || downTE.getTank().getFluidAmount() == downTE.getTank().getCapacity() || te.getTank().getFluid() != downTE.getTank().getFluid();
        return Collections.singletonList(new FluidTankRenderInfo(te.getTank(), up, down, ((FluidTankBlock) te.getBlockState().getBlock()).getTankBodyShape(te.getWorld(), te.getPos())));
    }

    private static class FluidTankRenderInfo extends TankRenderInfo {
        private final boolean up;
        private final boolean down;

        FluidTankRenderInfo(IFluidTank tank, boolean up, boolean down, AxisAlignedBB bounds) {
            super(tank, bounds);
            this.up = up;
            this.down = down;
        }

        @Override
        public boolean shouldRender(Direction face) {
            switch (face) {
                case UP:
                    return up
                            || getTank().getFluid().getAmount() < getTank().getCapacity()
                            && !getTank().getFluid().getFluid().getAttributes().isLighterThanAir();
                case DOWN:
                    return down
                            || getTank().getFluid().getAmount() < getTank().getCapacity()
                            && getTank().getFluid().getFluid().getAttributes().isLighterThanAir();
                default:
                    return true;
            }
        }
    }
}
