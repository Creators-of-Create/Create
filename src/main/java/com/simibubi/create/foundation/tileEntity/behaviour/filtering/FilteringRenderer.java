package com.simibubi.create.foundation.tileEntity.behaviour.filtering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox.ItemValueBox;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FilteringRenderer {

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		HitResult target = mc.hitResult;
		if (target == null || !(target instanceof BlockHitResult))
			return;

		BlockHitResult result = (BlockHitResult) target;
		ClientLevel world = mc.level;
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
		Component label = isFilterSlotted ? Components.immutableEmpty()
			: Lang.translateDirect(behaviour.recipeFilter ? "logistics.recipe_filter"
				: fluids ? "logistics.fluid_filter" : "logistics.filter");
		boolean hit = behaviour.slotPositioning.testHit(state, target.getLocation()
			.subtract(Vec3.atLowerCornerOf(pos)));

		AABB emptyBB = new AABB(Vec3.ZERO, Vec3.ZERO);
		AABB bb = isFilterSlotted ? emptyBB.inflate(.45f, .31f, .2f) : emptyBB.inflate(.25f);

		ValueBox box = showCount ? new ItemValueBox(label, bb, pos, filter, behaviour.scrollableValue)
				: new ValueBox(label, bb, pos);

		box.offsetLabel(behaviour.textShift)
				.withColors(fluids ? 0x407088 : 0x7A6A2C, fluids ? 0x70adb5 : 0xB79D64)
				.scrollTooltip(showCount && !isFilterSlotted ? Components.literal("[").append(Lang.translateDirect("action.scroll")).append("]") : Components.immutableEmpty())
				.passive(!hit);

		CreateClient.OUTLINER.showValueBox(Pair.of("filter", pos), box.transform(behaviour.slotPositioning))
				.lineWidth(1 / 64f)
				.withFaceTexture(hit ? AllSpecialTextures.THIN_CHECKERED : null)
				.highlightFace(result.getDirection());
	}

	public static void renderOnTileEntity(SmartTileEntity te, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {

		if (te == null || te.isRemoved())
			return;

		if (!te.isVirtual()) {
			Entity cameraEntity = Minecraft.getInstance().cameraEntity;
			if (cameraEntity != null && te.getLevel() == cameraEntity.getLevel()) {
				float max = AllConfigs.CLIENT.filterItemRenderDistance.getF();
				if (cameraEntity.position().distanceToSqr(VecHelper.getCenterOf(te.getBlockPos())) > (max * max)) {
					return;
				}
			}
		}

		FilteringBehaviour behaviour = te.getBehaviour(FilteringBehaviour.TYPE);
		if (behaviour == null)
			return;
		if (!behaviour.isActive())
			return;
		if (behaviour.getFilter()
			.isEmpty() && !(behaviour instanceof SidedFilteringBehaviour))
			return;

		ValueBoxTransform slotPositioning = behaviour.slotPositioning;
		BlockState blockState = te.getBlockState();

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
