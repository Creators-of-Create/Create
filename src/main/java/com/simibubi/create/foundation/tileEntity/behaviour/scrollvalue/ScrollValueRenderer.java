package com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox.IconValueBox;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox.TextValueBox;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ScrollValueRenderer {

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		RayTraceResult target = mc.objectMouseOver;
		if (target == null || !(target instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult result = (BlockRayTraceResult) target;
		ClientWorld world = mc.world;
		BlockPos pos = result.getPos();
		Direction face = result.getFace();

		ScrollValueBehaviour behaviour = TileEntityBehaviour.get(world, pos, ScrollValueBehaviour.TYPE);
		if (behaviour == null)
			return;
		if (behaviour.needsWrench && !AllItems.WRENCH.isIn(Minecraft.getInstance().player.getHeldItemMainhand()))
			return;
		boolean highlight = behaviour.testHit(target.getHitVec());

		if (behaviour instanceof BulkScrollValueBehaviour && AllKeys.ctrlDown()) {
			BulkScrollValueBehaviour bulkScrolling = (BulkScrollValueBehaviour) behaviour;
			for (SmartTileEntity smartTileEntity : bulkScrolling.getBulk()) {
				ScrollValueBehaviour other = smartTileEntity.getBehaviour(ScrollValueBehaviour.TYPE);
				if (other != null)
					addBox(world, smartTileEntity.getPos(), face, other, highlight);
			}
		} else
			addBox(world, pos, face, behaviour, highlight);
	}

	protected static void addBox(ClientWorld world, BlockPos pos, Direction face, ScrollValueBehaviour behaviour,
		boolean highlight) {
		AxisAlignedBB bb = new AxisAlignedBB(Vector3d.ZERO, Vector3d.ZERO).grow(.5f)
			.contract(0, 0, -.5f)
			.offset(0, 0, -.125f);
		ITextComponent label = behaviour.label;
		ValueBox box;

		if (behaviour instanceof ScrollOptionBehaviour) {
			box = new IconValueBox(label, ((ScrollOptionBehaviour<?>) behaviour).getIconForSelected(), bb, pos);
		} else {
			box = new TextValueBox(label, bb, pos, new StringTextComponent(behaviour.formatValue()));
			if (behaviour.unit != null)
				box.subLabel(new StringTextComponent("(").append(behaviour.unit.apply(behaviour.scrollableValue)).append(")"));
		}

		box.scrollTooltip(new StringTextComponent("[").append(Lang.translate("action.scroll")).append("]"));
		box.offsetLabel(behaviour.textShift.add(20, -10, 0))
			.withColors(0x5A5D5A, 0xB5B7B6)
			.passive(!highlight);

		CreateClient.outliner.showValueBox(pos, box.transform(behaviour.slotPositioning))
			.lineWidth(1 / 64f)
			.highlightFace(face);
	}

}
