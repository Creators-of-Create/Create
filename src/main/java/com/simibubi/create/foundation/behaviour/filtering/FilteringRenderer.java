package com.simibubi.create.foundation.behaviour.filtering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.behaviour.ValueBox;
import com.simibubi.create.foundation.behaviour.ValueBox.ItemValueBox;
import com.simibubi.create.foundation.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour.SlotPositioning;
import com.simibubi.create.foundation.utility.GlHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.TessellatorHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class FilteringRenderer {

	@SubscribeEvent
	public static void renderBlockHighlight(DrawBlockHighlightEvent event) {
		RayTraceResult target = event.getTarget();
		if (target == null || !(target instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult result = (BlockRayTraceResult) target;
		ClientWorld world = Minecraft.getInstance().world;
		BlockPos pos = result.getPos();
		BlockState state = world.getBlockState(pos);

		FilteringBehaviour behaviour = TileEntityBehaviour.get(world, pos, FilteringBehaviour.TYPE);
		if (behaviour == null)
			return;

		TessellatorHelper.prepareForDrawing();
		GlStateManager.translated(pos.getX(), pos.getY(), pos.getZ());

		SlotPositioning slotPositioning = behaviour.slotPositioning;
		renderTransformed(state, slotPositioning, () -> {

			AxisAlignedBB bb = new AxisAlignedBB(Vec3d.ZERO, Vec3d.ZERO).grow(.25f);
			String label = Lang.translate("logistics.filter");
			ValueBox box = behaviour.isCountVisible() ? new ItemValueBox(label, bb, behaviour.getFilter().getCount())
					: new ValueBox(label, bb);
			box.offsetLabel(behaviour.textShift).withColors(0x7777BB, 0xCCBBFF);
			ValueBoxRenderer.renderBox(box, behaviour.testHit(target.getHitVec()));

		});

		TessellatorHelper.cleanUpAfterDrawing();
	}

	public static void renderOnTileEntity(SmartTileEntity tileEntityIn, double x, double y, double z,
			float partialTicks, int destroyStage) {

		if (tileEntityIn == null || tileEntityIn.isRemoved())
			return;
		FilteringBehaviour behaviour = TileEntityBehaviour.get(tileEntityIn, FilteringBehaviour.TYPE);
		if (behaviour == null)
			return;
		if (behaviour.getFilter().isEmpty())
			return;

		BlockState state = tileEntityIn.getBlockState();
		SlotPositioning slotPositioning = behaviour.slotPositioning;

		TessellatorHelper.prepareForDrawing();
		BlockPos pos = tileEntityIn.getPos();
		GlStateManager.translated(pos.getX(), pos.getY(), pos.getZ());

		renderTransformed(state, slotPositioning, () -> {
			ValueBoxRenderer.renderItemIntoValueBox(behaviour.getFilter());
		});

		TessellatorHelper.cleanUpAfterDrawing();
	}

	private static void renderTransformed(BlockState state, SlotPositioning positioning, Runnable render) {
		Vec3d position = positioning.offset.apply(state);
		Vec3d rotation = positioning.rotation.apply(state);
		float scale = positioning.scale;
		GlHelper.renderTransformed(position, rotation, scale, render);
	}

}
