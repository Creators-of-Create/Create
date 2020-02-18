package com.simibubi.create.foundation.behaviour.filtering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.behaviour.ValueBox;
import com.simibubi.create.foundation.behaviour.ValueBox.ItemValueBox;
import com.simibubi.create.foundation.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.logistics.item.filter.FilterItem;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
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
		if (Minecraft.getInstance().player.isSneaking())
			return;

		TessellatorHelper.prepareForDrawing();
		GlStateManager.translated(pos.getX(), pos.getY(), pos.getZ());

		behaviour.slotPositioning.renderTransformed(state, () -> {

			AxisAlignedBB bb = new AxisAlignedBB(Vec3d.ZERO, Vec3d.ZERO).grow(.25f);
			String label = Lang.translate("logistics.filter");
			ItemStack filter = behaviour.getFilter();
			if (filter.getItem() instanceof FilterItem)
				label = "";
			boolean showCount = behaviour.isCountVisible();
			ValueBox box =
				showCount ? new ItemValueBox(label, bb, filter, behaviour.scrollableValue) : new ValueBox(label, bb);
			if (showCount)
				box.scrollTooltip("[" + Lang.translate("action.scroll") + "]");
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
		TessellatorHelper.prepareForDrawing();
		BlockPos pos = tileEntityIn.getPos();
		GlStateManager.translated(pos.getX(), pos.getY(), pos.getZ());

		behaviour.slotPositioning.renderTransformed(state, () -> {
			ValueBoxRenderer.renderItemIntoValueBox(behaviour.getFilter());
		});

		TessellatorHelper.cleanUpAfterDrawing();
	}

}
