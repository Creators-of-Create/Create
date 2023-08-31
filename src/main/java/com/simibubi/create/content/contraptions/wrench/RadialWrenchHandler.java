package com.simibubi.create.content.contraptions.wrench;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class RadialWrenchHandler {

	public static void onKeyInput(int key, boolean pressed) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
			return;

		if (key != AllKeys.ROTATE_MENU.getBoundCode())
			return;

		LocalPlayer player = mc.player;
		if (player == null)
			return;

		Level level = player.level();

		ItemStack heldItem = player.getMainHandItem();
		if (heldItem.getItem() != AllItems.WRENCH.get())
			return;

		HitResult objectMouseOver = mc.hitResult;
		if (!(objectMouseOver instanceof BlockHitResult blockHitResult))
			return;

		BlockState state = level.getBlockState(blockHitResult.getBlockPos());

		if (!(state.getBlock() instanceof IWrenchable))
			return;

		ScreenOpener.open(new RadialWrenchMenu(state));

	}

}
