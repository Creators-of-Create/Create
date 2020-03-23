package com.simibubi.create.foundation.behaviour.linked;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.behaviour.ValueBox;
import com.simibubi.create.foundation.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
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
		RenderSystem.translated(pos.getX(), pos.getY(), pos.getZ());

		String freq1 = Lang.translate("logistics.firstFrequency");
		String freq2 = Lang.translate("logistics.secondFrequency");

		renderEachSlot(state, behaviour, first -> {
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
		TessellatorHelper.prepareForDrawing();
		BlockPos pos = tileEntityIn.getPos();
		RenderSystem.translated(pos.getX(), pos.getY(), pos.getZ());

		renderEachSlot(state, behaviour, first -> {
			ValueBoxRenderer.renderItemIntoValueBox(
					first ? behaviour.frequencyFirst.getStack() : behaviour.frequencyLast.getStack());
		});

		TessellatorHelper.cleanUpAfterDrawing();
	}

	private static void renderEachSlot(BlockState state, LinkBehaviour behaviour, Consumer<Boolean> render) {
		behaviour.firstSlot.renderTransformed(state, () -> render.accept(true));
		behaviour.secondSlot.renderTransformed(state, () -> render.accept(false));
	}

}
