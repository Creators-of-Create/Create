package com.simibubi.create.content.redstone.displayLink;

import com.simibubi.create.AllDataComponents;

import net.createmod.catnip.api.client.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class ClickToLinkClient {

	private static BlockPos lastShownPos = null;
	private static AABB lastShownAABB = null;

	public static void clientTick() {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		Level world = minecraft.level;
		if (player == null || world == null)
			return;
		ItemStack heldItemMainhand = player.getMainHandItem();
		if (!(heldItemMainhand.getItem() instanceof ClickToLinkBlockItem blockItem))
			return;
		if (!heldItemMainhand.has(AllDataComponents.CLICK_TO_LINK_DATA))
			return;

		BlockPos selectedPos = heldItemMainhand.get(AllDataComponents.CLICK_TO_LINK_DATA)
			.selectedPos();

		if (!selectedPos.equals(lastShownPos)) {
			lastShownAABB = blockItem.getSelectionBounds(world, selectedPos);
			lastShownPos = selectedPos;
		}

		Outliner.getInstance()
			.showAABB("target", lastShownAABB)
			.colored(0xffcb74)
			.lineWidth(1 / 16f);
	}

}
