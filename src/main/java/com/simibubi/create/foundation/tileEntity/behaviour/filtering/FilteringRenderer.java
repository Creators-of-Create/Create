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
import com.simibubi.create.foundation.utility.Pair;

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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class FilteringRenderer {

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		RayTraceResult target = mc.hitResult;
		if (target == null || !(target instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult result = (BlockRayTraceResult) target;
		ClientWorld world = mc.level;
		BlockPos pos = result.getBlockPos();
		BlockState state = world.getBlockState(pos);

		FilteringBehaviour behaviour = TileEntityBehaviour.get(world, pos, FilteringBehaviour.TYPE);
		if (mc.player.isShiftKeyDown())
			return;
		if (behaviour == null)
			return;
		if (behaviour instanceof SidedFilteringBehaviour) {
			behaviour = ((SidedFilteringBehaviour) behaviour).get(result.getDirection());
			if (behaviour == null)
				return;
		}
		if (!behaviour.isActive())
			return;
		if (behaviour.slotPositioning instanceof ValueBoxTransform.Sided)
			((Sided) behaviour.slotPositioning).fromSide(result.getDirection());
		if (!behaviour.slotPositioning.shouldRender(state))
			return;

		ItemStack filter = behaviour.getFilter();
		boolean isFilterSlotted = filter.getItem() instanceof FilterItem;
		boolean showCount = behaviour.isCountVisible();
		boolean fluids = behaviour.fluidFilter;
		ITextComponent label = isFilterSlotted ? StringTextComponent.EMPTY
			: Lang.translate(behaviour.recipeFilter ? "logistics.recipe_filter"
				: fluids ? "logistics.fluid_filter" : "logistics.filter");
		boolean hit = behaviour.slotPositioning.testHit(state, target.getLocation()
			.subtract(Vector3d.atLowerCornerOf(pos)));

		AxisAlignedBB emptyBB = new AxisAlignedBB(Vector3d.ZERO, Vector3d.ZERO);
		AxisAlignedBB bb = isFilterSlotted ? emptyBB.inflate(.45f, .31f, .2f) : emptyBB.inflate(.25f);

		ValueBox box = showCount ? new ItemValueBox(label, bb, pos, filter, behaviour.scrollableValue)
				: new ValueBox(label, bb, pos);

		box.offsetLabel(behaviour.textShift)
				.withColors(fluids ? 0x407088 : 0x7A6A2C, fluids ? 0x70adb5 : 0xB79D64)
				.scrollTooltip(showCount && !isFilterSlotted ? new StringTextComponent("[").append(Lang.translate("action.scroll")).append("]") : StringTextComponent.EMPTY)
				.passive(!hit);

		CreateClient.OUTLINER.showValueBox(Pair.of("filter", pos), box.transform(behaviour.slotPositioning))
				.lineWidth(1 / 64f)
				.withFaceTexture(hit ? AllSpecialTextures.THIN_CHECKERED : null)
				.highlightFace(result.getDirection());
	}

	public static void renderOnTileEntity(SmartTileEntity tileEntityIn, float partialTicks, MatrixStack ms,
		IRenderTypeBuffer buffer, int light, int overlay) {

		if (tileEntityIn == null || tileEntityIn.isRemoved())
			return;
		FilteringBehaviour behaviour = tileEntityIn.getBehaviour(FilteringBehaviour.TYPE);
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

				ms.pushPose();
				slotPositioning.transform(blockState, ms);
				ValueBoxRenderer.renderItemIntoValueBox(filter, ms, buffer, light, overlay);
				ms.popPose();
			}
			sided.fromSide(side);
			return;
		} else if (slotPositioning.shouldRender(blockState)) {
			ms.pushPose();
			slotPositioning.transform(blockState, ms);
			ValueBoxRenderer.renderItemIntoValueBox(behaviour.getFilter(), ms, buffer, light, overlay);
			ms.popPose();
		}
	}

}
