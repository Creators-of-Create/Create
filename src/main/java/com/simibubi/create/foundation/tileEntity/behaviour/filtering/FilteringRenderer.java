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
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
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
		if (mc.player.isSneaking())
			return;
		if (behaviour == null)
			return;
		if (behaviour instanceof SidedFilteringBehaviour) {
			behaviour = ((SidedFilteringBehaviour) behaviour).get(result.getFace());
			if (behaviour == null)
				return;
		}
		if (!behaviour.isActive())
			return;
		if (behaviour.slotPositioning instanceof ValueBoxTransform.Sided)
			((Sided) behaviour.slotPositioning).fromSide(result.getFace());
		if (!behaviour.slotPositioning.shouldRender(state))
			return;

		ItemStack filter = behaviour.getFilter();
		boolean isFilterSlotted = filter.getItem() instanceof FilterItem;
		boolean showCount = behaviour.isCountVisible();
		String label = isFilterSlotted ? ""
			: Lang.translate(behaviour.recipeFilter ? "logistics.recipe_filter" : "logistics.filter");
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
			.isEmpty() && !(behaviour instanceof SidedFilteringBehaviour))
			return;

		ValueBoxTransform slotPositioning = behaviour.slotPositioning;
		BlockState blockState = tileEntityIn.getBlockState();

		if (slotPositioning instanceof ValueBoxTransform.Sided) {
			ValueBoxTransform.Sided sided = (ValueBoxTransform.Sided) slotPositioning;
			Direction side = sided.getSide();
			for (Direction d : Iterate.directions) {
				ItemStack filter = behaviour.getFilter(d);
				if (filter.isEmpty())
					continue;

				sided.fromSide(d);
				if (!slotPositioning.shouldRender(blockState))
					continue;

				ms.push();
				slotPositioning.transform(blockState, ms);
				ValueBoxRenderer.renderItemIntoValueBox(filter, ms, buffer, light, overlay);
				ms.pop();
			}
			sided.fromSide(side);
			return;
		}

		ms.push();
		slotPositioning.transform(blockState, ms);
		ValueBoxRenderer.renderItemIntoValueBox(behaviour.getFilter(), ms, buffer, light, overlay);
		ms.pop();
	}

}
