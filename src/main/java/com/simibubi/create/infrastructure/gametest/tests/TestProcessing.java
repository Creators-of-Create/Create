package com.simibubi.create.infrastructure.gametest.tests;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.items.IItemHandler;

@GameTestGroup(path = "processing")
public class TestProcessing {
	@GameTest(template = "brass_mixing", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void brassMixing(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(2, 3, 2);
		BlockPos chest = new BlockPos(7, 3, 1);
		helper.pullLever(lever);
		helper.succeedWhen(() -> helper.assertContainerContains(chest, AllItems.BRASS_INGOT.get()));
	}

	@GameTest(template = "brass_mixing_2", timeoutTicks = CreateGameTestHelper.TWENTY_SECONDS)
	public static void brassMixing2(CreateGameTestHelper helper) {
		BlockPos basinLever = new BlockPos(3, 3, 1);
		BlockPos armLever = new BlockPos(3, 3, 5);
		BlockPos output = new BlockPos(1, 2, 3);
		helper.pullLever(armLever);
		helper.whenSecondsPassed(7, () -> helper.pullLever(armLever));
		helper.whenSecondsPassed(10, () -> helper.pullLever(basinLever));
		helper.succeedWhen(() -> helper.assertContainerContains(output, AllItems.BRASS_INGOT.get()));
	}

	@GameTest(template = "crushing_wheel_crafting", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void crushingWheelCrafting(CreateGameTestHelper helper) {
		BlockPos chest = new BlockPos(1, 4, 3);
		List<BlockPos> levers = List.of(
				new BlockPos(2, 3, 2),
				new BlockPos(6, 3, 2),
				new BlockPos(3, 7, 3)
		);
		levers.forEach(helper::pullLever);
		ItemStack expected = new ItemStack(AllBlocks.CRUSHING_WHEEL.get(), 2);
		helper.succeedWhen(() -> helper.assertContainerContains(chest, expected));
	}

	@GameTest(template = "precision_mechanism_crafting", timeoutTicks = CreateGameTestHelper.TWENTY_SECONDS)
	public static void precisionMechanismCrafting(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(6, 3, 6);
		BlockPos output = new BlockPos(11, 3, 1);
		helper.pullLever(lever);

		SequencedAssemblyRecipe recipe = (SequencedAssemblyRecipe) helper.getLevel().getRecipeManager()
				.byKey(Create.asResource("sequenced_assembly/precision_mechanism"))
				.orElseThrow(() -> new GameTestAssertException("Precision Mechanism recipe not found"));
		Item result = recipe.getResultItem(helper.getLevel().registryAccess()).getItem();
		Item[] possibleResults = recipe.resultPool.stream()
				.map(ProcessingOutput::getStack)
				.map(ItemStack::getItem)
				.filter(item -> item != result)
				.toArray(Item[]::new);

		helper.succeedWhen(() -> {
			helper.assertContainerContains(output, result);
			helper.assertAnyContained(output, possibleResults);
		});
	}

	@GameTest(template = "sand_washing", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void sandWashing(CreateGameTestHelper helper) {
		BlockPos leverPos = new BlockPos(5, 3, 1);
		helper.pullLever(leverPos);
		BlockPos chestPos = new BlockPos(8, 3, 2);
		helper.succeedWhen(() -> helper.assertContainerContains(chestPos, Items.CLAY_BALL));
	}

	@GameTest(template = "stone_cobble_sand_crushing", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void stoneCobbleSandCrushing(CreateGameTestHelper helper) {
		BlockPos chest = new BlockPos(1, 6, 2);
		BlockPos lever = new BlockPos(2, 3, 1);
		helper.pullLever(lever);
		ItemStack expected = new ItemStack(Items.SAND, 5);
		helper.succeedWhen(() -> helper.assertContainerContains(chest, expected));
	}

	@GameTest(template = "track_crafting", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void trackCrafting(CreateGameTestHelper helper) {
		BlockPos output = new BlockPos(7, 3, 2);
		BlockPos lever = new BlockPos(2, 3, 1);
		helper.pullLever(lever);
		ItemStack expected = new ItemStack(AllBlocks.TRACK.get(), 6);
		helper.succeedWhen(() -> {
			helper.assertContainerContains(output, expected);
			IItemHandler handler = helper.itemStorageAt(output);
			ItemHelper.extract(handler, ItemHelper.sameItemPredicate(expected), 6, false);
			helper.assertContainerEmpty(output);
		});
	}

	@GameTest(template = "water_filling_bottle")
	public static void waterFillingBottle(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(3, 3, 3);
		BlockPos output = new BlockPos(2, 2, 4);
		ItemStack expected = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
		helper.pullLever(lever);
		helper.succeedWhen(() -> helper.assertContainerContains(output, expected));
	}

	@GameTest(template = "wheat_milling")
	public static void wheatMilling(CreateGameTestHelper helper) {
		BlockPos output = new BlockPos(1, 2, 1);
		BlockPos lever = new BlockPos(1, 7, 1);
		helper.pullLever(lever);
		ItemStack expected = new ItemStack(AllItems.WHEAT_FLOUR.get(), 3);
		helper.succeedWhen(() -> helper.assertContainerContains(output, expected));
	}
}
