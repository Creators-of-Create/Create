package com.simibubi.create.infrastructure.gametest.tests;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import com.simibubi.create.foundation.block.CopperRegistries;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.UUID;

@GameTestGroup(path = "copper")
public class TestCopperWax {
	// Position of the copper block inside both test structures
	private static final BlockPos copperBlock = new BlockPos(1, 2, 1);

	@GameTest(template = "wax_on")
	public static void testWaxOn(CreateGameTestHelper helper) {
		// Check if the block is waxable
		BlockState state = helper.getBlockState(copperBlock);
		if (!CopperRegistries.getWaxableView().containsKey(state.getBlockHolder()))
			helper.fail("Block is not waxable");

		// Create a FakePlayer and try to award the advancement directly
		GameProfile profile = new GameProfile(UUID.randomUUID(), "test_player");
		FakePlayer player = FakePlayerFactory.get(helper.getLevel(), profile);
		// awardVanilla returns true if the advancement was successfully awarded
		boolean awarded = CreateAdvancement.awardVanilla(player,
			ResourceLocation.withDefaultNamespace("husbandry/wax_on"));
		if (!awarded)
			helper.fail("Failed to award advancement");
		helper.succeed();
	}

	@GameTest(template = "wax_off")
	public static void testWaxOff(CreateGameTestHelper helper) {
		// Check if the block is unwaxable
		BlockState state = helper.getBlockState(copperBlock);
		if (!CopperRegistries.getWaxableView().containsValue(state.getBlockHolder()))
			helper.fail("Block is not a waxed block");

		// Create a FakePlayer and try to award the advancement directly
		GameProfile profile = new GameProfile(UUID.randomUUID(), "test_player");
		FakePlayer player = FakePlayerFactory.get(helper.getLevel(), profile);
		// awardVanilla returns true if the advancement was successfully awarded
		boolean awarded = CreateAdvancement.awardVanilla(player,
			ResourceLocation.withDefaultNamespace("husbandry/wax_off"));
		if (!awarded)
			helper.fail("Failed to award advancement");
		helper.succeed();
	}
}
