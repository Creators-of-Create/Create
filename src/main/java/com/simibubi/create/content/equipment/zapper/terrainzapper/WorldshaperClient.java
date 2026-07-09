package com.simibubi.create.content.equipment.zapper.terrainzapper;

import java.util.function.Consumer;

import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;

import net.createmod.catnip.api.client.gui.ScreenOpener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class WorldshaperClient {

	public static void initializeClient(WorldshaperItem item, Consumer<IClientItemExtensions> consumer) {
		consumer.accept(SimpleCustomRenderer.create(item, new WorldshaperItemRenderer()));
	}

	public static void openScreen(ItemStack item, InteractionHand hand) {
		ScreenOpener.open(new WorldshaperScreen(item, hand));
	}

}
