package com.simibubi.create.content.contraptions.wrench;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;

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

	public static int COOLDOWN = 0;

	public static void clientTick() {
		if (COOLDOWN > 0 && !AllKeys.ROTATE_MENU.isPressed())
			COOLDOWN--;
	}

	public static void onKeyInput(int key, boolean pressed) {
		if (!pressed)
			return;

		if (!AllKeys.ROTATE_MENU.doesModifierAndCodeMatch(key))
			return;

		if (COOLDOWN > 0)
			return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
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

		RadialWrenchMenu.tryCreateFor(state, blockHitResult.getBlockPos(), level).ifPresent(ScreenOpener::open);
	}

}
