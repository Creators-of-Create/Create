package com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox.IconValueBox;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox.TextValueBox;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ScrollValueRenderer {

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		HitResult target = mc.hitResult;
		if (target == null || !(target instanceof BlockHitResult))
			return;

		BlockHitResult result = (BlockHitResult) target;
		ClientLevel world = mc.level;
		BlockPos pos = result.getBlockPos();
		Direction face = result.getDirection();

		ScrollValueBehaviour behaviour = TileEntityBehaviour.get(world, pos, ScrollValueBehaviour.TYPE);
		if (behaviour == null)
			return;
		if (!behaviour.isActive())
			return;
		if (behaviour.needsWrench && !AllItems.WRENCH.isIn(mc.player.getMainHandItem()))
			return;
		boolean highlight = behaviour.testHit(target.getLocation());

		if (behaviour instanceof BulkScrollValueBehaviour && AllKeys.ctrlDown()) {
			BulkScrollValueBehaviour bulkScrolling = (BulkScrollValueBehaviour) behaviour;
			for (SmartTileEntity smartTileEntity : bulkScrolling.getBulk()) {
				ScrollValueBehaviour other = smartTileEntity.getBehaviour(ScrollValueBehaviour.TYPE);
				if (other != null)
					addBox(world, smartTileEntity.getBlockPos(), face, other, highlight);
			}
		} else
			addBox(world, pos, face, behaviour, highlight);
	}

	protected static void addBox(ClientLevel world, BlockPos pos, Direction face, ScrollValueBehaviour behaviour,
		boolean highlight) {
		AABB bb = new AABB(Vec3.ZERO, Vec3.ZERO).inflate(.5f)
			.contract(0, 0, -.5f)
			.move(0, 0, -.125f);
		Component label = behaviour.label;
		ValueBox box;

		if (behaviour instanceof ScrollOptionBehaviour) {
			box = new IconValueBox(label, ((ScrollOptionBehaviour<?>) behaviour).getIconForSelected(), bb, pos);
		} else {
			box = new TextValueBox(label, bb, pos, Components.literal(behaviour.formatValue()));
			if (behaviour.unit != null)
				box.subLabel(Components.literal("(").append(behaviour.unit.apply(behaviour.scrollableValue)).append(")"));
		}

		box.scrollTooltip(Components.literal("[").append(Lang.translateDirect("action.scroll")).append("]"));
		box.offsetLabel(behaviour.textShift.add(20, -10, 0))
				.withColors(0x5A5D5A, 0xB5B7B6)
				.passive(!highlight);

		CreateClient.OUTLINER.showValueBox(pos, box.transform(behaviour.slotPositioning))
				.lineWidth(1 / 64f)
				.highlightFace(face);
	}

}
