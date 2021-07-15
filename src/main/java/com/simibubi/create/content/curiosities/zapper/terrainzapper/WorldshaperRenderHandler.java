package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;

public class WorldshaperRenderHandler {

	private static Supplier<Collection<BlockPos>> renderedPositions;

	public static void tick() {
		gatherSelectedBlocks();
		if (renderedPositions == null)
			return;

		CreateClient.OUTLINER.showCluster("terrainZapper", renderedPositions.get())
				.colored(0xbfbfbf)
				.disableNormals()
				.lineWidth(1 / 32f)
				.withFaceTexture(AllSpecialTextures.CHECKERED);
	}

	protected static void gatherSelectedBlocks() {
		ClientPlayerEntity player = Minecraft.getInstance().player;
		ItemStack heldMain = player.getMainHandItem();
		ItemStack heldOff = player.getOffhandItem();
		boolean zapperInMain = AllItems.WORLDSHAPER.isIn(heldMain);
		boolean zapperInOff = AllItems.WORLDSHAPER.isIn(heldOff);

		if (zapperInMain) {
			CompoundNBT tag = heldMain.getOrCreateTag();
			if (!tag.contains("_Swap") || !zapperInOff) {
				createBrushOutline(tag, player, heldMain);
				return;
			}
		}

		if (zapperInOff) {
			CompoundNBT tag = heldOff.getOrCreateTag();
			createBrushOutline(tag, player, heldOff);
			return;
		}

		renderedPositions = null;
	}

	public static void createBrushOutline(CompoundNBT tag, ClientPlayerEntity player, ItemStack zapper) {
		if (!tag.contains("BrushParams")) {
			renderedPositions = null;
			return;
		}

		Brush brush = NBTHelper.readEnum(tag, "Brush", TerrainBrushes.class)
			.get();
		PlacementOptions placement = NBTHelper.readEnum(tag, "Placement", PlacementOptions.class);
		TerrainTools tool = NBTHelper.readEnum(tag, "Tool", TerrainTools.class);
		BlockPos params = NBTUtil.readBlockPos(tag.getCompound("BrushParams"));
		brush.set(params.getX(), params.getY(), params.getZ());

		Vector3d start = player.position()
			.add(0, player.getEyeHeight(), 0);
		Vector3d range = player.getLookAngle()
			.scale(128);
		BlockRayTraceResult raytrace = player.level
			.clip(new RayTraceContext(start, start.add(range), BlockMode.OUTLINE, FluidMode.NONE, player));
		if (raytrace == null || raytrace.getType() == Type.MISS) {
			renderedPositions = null;
			return;
		}

		BlockPos pos = raytrace.getBlockPos()
			.offset(brush.getOffset(player.getLookAngle(), raytrace.getDirection(), placement));
		renderedPositions =
			() -> brush.addToGlobalPositions(player.level, pos, raytrace.getDirection(), new ArrayList<>(), tool);
	}

}
