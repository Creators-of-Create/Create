package com.simibubi.create.foundation.tileEntity.behaviour.filtering;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox.ItemValueBox;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class FilteringRenderer {

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		RayTraceResult target = mc.objectMouseOver;
		if (target == null || !(target instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult result = (BlockRayTraceResult) target;
		ClientWorld world = mc.world;
		BlockPos pos = result.getPos();
		BlockState state = world.getBlockState(pos);

		FilteringBehaviour behaviour = TileEntityBehaviour.get(world, pos, FilteringBehaviour.TYPE);
		if (behaviour == null)
			return;
		if (!behaviour.isActive())
			return;
		if (mc.player.isSneaking())
			return;
		if (!behaviour.slotPositioning.shouldRender(state))
			return;

		ItemStack filter = behaviour.getFilter();
		boolean isFilterSlotted = filter.getItem() instanceof FilterItem;
		boolean showCount = behaviour.isCountVisible();
		String label = isFilterSlotted ? "" : Lang.translate("logistics.filter");
		boolean hit = behaviour.slotPositioning.testHit(state, target.getHitVec()
			.subtract(new Vec3d(pos)));

		AxisAlignedBB emptyBB = new AxisAlignedBB(Vec3d.ZERO, Vec3d.ZERO);
		AxisAlignedBB bb = isFilterSlotted ? emptyBB.grow(.45f, .31f, .2f) : emptyBB.grow(.25f);

		ValueBox box = showCount ? new ItemValueBox(label, bb, pos, filter, behaviour.scrollableValue)
			: new ValueBox(label, bb, pos);

		box.offsetLabel(behaviour.textShift)
			.withColors(0x7A6A2C, 0xB79D64)
			.scrollTooltip(showCount ? "[" + Lang.translate("action.scroll") + "]" : "")
			.passive(!hit);

		CreateClient.outliner.showValueBox(pos, box.transform(behaviour.slotPositioning))
			.lineWidth(1 / 64f)
			.withFaceTexture(hit ? AllSpecialTextures.THIN_CHECKERED : null)
			.highlightFace(result.getFace());
	}

	public static void renderOnTileEntity(SmartTileEntity tileEntityIn, float partialTicks, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {

		if (tileEntityIn == null || tileEntityIn.isRemoved())
			return;
		FilteringBehaviour behaviour = TileEntityBehaviour.get(tileEntityIn, FilteringBehaviour.TYPE);
		if (behaviour == null)
			return;
		if (!behaviour.isActive())
			return;
		if (behaviour.getFilter()
			.isEmpty())
			return;

		ms.push();
		behaviour.slotPositioning.transform(tileEntityIn.getBlockState(), ms);
		ValueBoxRenderer.renderItemIntoValueBox(behaviour.getFilter(), ms, buffer, light, overlay);
		ms.pop();
	}

}
