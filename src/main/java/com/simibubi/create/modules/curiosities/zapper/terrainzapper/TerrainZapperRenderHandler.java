package com.simibubi.create.modules.curiosities.zapper.terrainzapper;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;

public class TerrainZapperRenderHandler {

	private static VoxelShape renderedShape;
	private static BlockPos renderedPosition;

	public static void tick() {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		ItemStack heldMain = player.getHeldItemMainhand();
		ItemStack heldOff = player.getHeldItemOffhand();
		boolean zapperInMain = AllItems.TERRAIN_ZAPPER.typeOf(heldMain);
		boolean zapperInOff = AllItems.TERRAIN_ZAPPER.typeOf(heldOff);

		if (zapperInMain) {
			CompoundNBT tag = heldMain.getOrCreateTag();
			if (!tag.contains("_Swap")) {
				createBrushOutline(tag, player, heldMain);
				return;
			}
		}

		if (zapperInOff) {
			CompoundNBT tag = heldOff.getOrCreateTag();
			createBrushOutline(tag, player, heldOff);
			return;
		}

		renderedPosition = null;
	}

	public static void createBrushOutline(CompoundNBT tag, ClientPlayerEntity player, ItemStack zapper) {
		if (!tag.contains("BrushParams")) {
			renderedPosition = null;
			return;
		}

		Brush brush = NBTHelper.readEnum(tag.getString("Brush"), TerrainBrushes.class).get();
		PlacementOptions placement = NBTHelper.readEnum(tag.getString("Placement"), PlacementOptions.class);
		BlockPos params = NBTUtil.readBlockPos(tag.getCompound("BrushParams"));
		brush.set(params.getX(), params.getY(), params.getZ());
		renderedShape = brush.getSelectionBox();

		Vec3d start = player.getPositionVec().add(0, player.getEyeHeight(), 0);
		Vec3d range = player.getLookVec().scale(128);
		BlockRayTraceResult raytrace = player.world.rayTraceBlocks(
				new RayTraceContext(start, start.add(range), BlockMode.OUTLINE, FluidMode.NONE, player));
		if (raytrace == null || raytrace.getType() == Type.MISS) {
			renderedPosition = null;
			return;
		}

		BlockPos pos = raytrace.getPos();
		renderedPosition = pos.add(brush.getOffset(player.getLookVec(), raytrace.getFace(), placement));
	}

	public static void render(MatrixStack ms, IRenderTypeBuffer buffer) {
		if (renderedPosition == null)
			return;

		// TODO 1.15 buffered render
//		RenderSystem.lineWidth(2);
//		TessellatorHelper.prepareForDrawing();
//		RenderSystem.disableTexture();

		ms.push();
		ms.translate(renderedPosition.getX(), renderedPosition.getY(), renderedPosition.getZ());
		WorldRenderer.func_228431_a_(ms, buffer.getBuffer(RenderType.getLines()), renderedShape, 0, 0, 0, 0f, 0f, 0f, 0.5f);

//		RenderSystem.enableTexture();
//		TessellatorHelper.cleanUpAfterDrawing();
//		RenderSystem.lineWidth(1);
		
		ms.pop();
	}

}
