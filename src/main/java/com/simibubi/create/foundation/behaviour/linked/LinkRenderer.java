package com.simibubi.create.foundation.behaviour.linked;

import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.behaviour.ValueBox;
import com.simibubi.create.foundation.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.linked.LinkBehaviour.SlotPositioning;
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
public class LinkRenderer {

	@SubscribeEvent
	public static void renderBlockHighlight(DrawBlockHighlightEvent event) {
		RayTraceResult target = event.getTarget();
		if (target == null || !(target instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult result = (BlockRayTraceResult) target;
		ClientWorld world = Minecraft.getInstance().world;
		BlockPos pos = result.getPos();
		BlockState state = world.getBlockState(pos);

		LinkBehaviour behaviour = TileEntityBehaviour.get(world, pos, LinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		TessellatorHelper.prepareForDrawing();
		GlStateManager.translated(pos.getX(), pos.getY(), pos.getZ());

		SlotPositioning slotPositioning = behaviour.slotPositioning;
		String freq1 = Lang.translate("logistics.firstFrequency");
		String freq2 = Lang.translate("logistics.secondFrequency");

		renderEachSlot(state, slotPositioning, first -> {
			AxisAlignedBB bb = new AxisAlignedBB(Vec3d.ZERO, Vec3d.ZERO).grow(.25f);
			String label = first ? freq2 : freq1;
			ValueBox box = new ValueBox(label, bb).withColors(0x992266, 0xFF55AA).offsetLabel(behaviour.textShift);
			ValueBoxRenderer.renderBox(box, behaviour.testHit(first, target.getHitVec()));
		});

		TessellatorHelper.cleanUpAfterDrawing();
	}

	public static void renderOnTileEntity(SmartTileEntity tileEntityIn, double x, double y, double z,
			float partialTicks, int destroyStage) {

		if (tileEntityIn == null || tileEntityIn.isRemoved())
			return;
		LinkBehaviour behaviour = TileEntityBehaviour.get(tileEntityIn, LinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		BlockState state = tileEntityIn.getBlockState();
		SlotPositioning slotPositioning = behaviour.slotPositioning;

		TessellatorHelper.prepareForDrawing();
		BlockPos pos = tileEntityIn.getPos();
		GlStateManager.translated(pos.getX(), pos.getY(), pos.getZ());

		renderEachSlot(state, slotPositioning, first -> {
			ValueBoxRenderer.renderItemIntoValueBox(
					first ? behaviour.frequencyFirst.getStack() : behaviour.frequencyLast.getStack());
		});

		TessellatorHelper.cleanUpAfterDrawing();
	}

	private static void renderEachSlot(BlockState state, SlotPositioning positioning, Consumer<Boolean> render) {
		Pair<Vec3d, Vec3d> position = positioning.offsets.apply(state);
		Vec3d rotation = positioning.rotation.apply(state);
		float scale = positioning.scale;

		GlHelper.renderTransformed(position.getKey(), rotation, scale, () -> render.accept(true));
		GlHelper.renderTransformed(position.getValue(), rotation, scale, () -> render.accept(false));
	}

}
