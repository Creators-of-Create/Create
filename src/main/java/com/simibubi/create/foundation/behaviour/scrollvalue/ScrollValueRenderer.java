package com.simibubi.create.foundation.behaviour.scrollvalue;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.foundation.behaviour.ValueBox;
import com.simibubi.create.foundation.behaviour.ValueBox.IconValueBox;
import com.simibubi.create.foundation.behaviour.ValueBox.TextValueBox;
import com.simibubi.create.foundation.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.behaviour.ValueBoxTransform.Sided;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.TessellatorHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class ScrollValueRenderer {

	@SubscribeEvent
	public static void renderBlockHighlight(DrawHighlightEvent event) {
		RayTraceResult target = event.getTarget();
		if (target == null || !(target instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult result = (BlockRayTraceResult) target;
		ClientWorld world = Minecraft.getInstance().world;
		BlockPos pos = result.getPos();
		Direction face = result.getFace();

		ScrollValueBehaviour behaviour = TileEntityBehaviour.get(world, pos, ScrollValueBehaviour.TYPE);
		if (behaviour == null)
			return;
		if (behaviour.needsWrench && !AllItems.WRENCH.typeOf(Minecraft.getInstance().player.getHeldItemMainhand()))
			return;
		boolean highlight = behaviour.testHit(target.getHitVec());

		TessellatorHelper.prepareForDrawing();
		if (behaviour instanceof BulkScrollValueBehaviour && AllKeys.ctrlDown()) {
			BulkScrollValueBehaviour bulkScrolling = (BulkScrollValueBehaviour) behaviour;
			for (SmartTileEntity smartTileEntity : bulkScrolling.getBulk()) {
				RenderSystem.pushMatrix();
				ScrollValueBehaviour other = TileEntityBehaviour.get(smartTileEntity, ScrollValueBehaviour.TYPE);
				if (other != null)
					render(world, smartTileEntity.getPos(), face, other, highlight);
				RenderSystem.popMatrix();
			}
		} else
			render(world, pos, face, behaviour, highlight);
		
		TessellatorHelper.cleanUpAfterDrawing();
		GlStateManager.enableAlphaTest();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
	}

	protected static void render(ClientWorld world, BlockPos pos, Direction face, ScrollValueBehaviour behaviour,
			boolean highlight) {
		RenderSystem.translated(pos.getX(), pos.getY(), pos.getZ());
		BlockState state = world.getBlockState(pos);
		if (behaviour.slotPositioning instanceof Sided)
			((Sided) behaviour.slotPositioning).fromSide(face);
		behaviour.slotPositioning.renderTransformed(state, () -> {
			AxisAlignedBB bb = new AxisAlignedBB(Vec3d.ZERO, Vec3d.ZERO).grow(.5f).contract(0, 0, -.5f).offset(0, 0,
					-.125f);
			String label = behaviour.label;
			ValueBox box;

			if (behaviour instanceof ScrollOptionBehaviour) {
				box = new IconValueBox(label, ((ScrollOptionBehaviour<?>) behaviour).getIconForSelected(), bb);
			} else {
				box = new TextValueBox(label, bb, behaviour.formatValue());
				if (behaviour.unit != null)
					box.subLabel("(" + behaviour.unit.apply(behaviour.scrollableValue) + ")");
			}

			box.scrollTooltip("[" + Lang.translate("action.scroll") + "]");
			box.offsetLabel(behaviour.textShift.add(20, -10, 0)).withColors(0xbe970b, 0xffe75e);
			ValueBoxRenderer.renderBox(box, highlight);
		});
	}

}
