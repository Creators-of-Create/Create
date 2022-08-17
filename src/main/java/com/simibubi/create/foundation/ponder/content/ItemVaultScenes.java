package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.block.vault.ItemVaultBlock;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ItemVaultScenes {

	public static void storage(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("item_vault_storage", "Storing Items in Vaults");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		Selection chests = util.select.fromTo(4, 3, 2, 3, 4, 3);
		Selection largeCog = util.select.position(3, 0, 5);
		Selection belt1 = util.select.fromTo(0, 2, 3, 2, 1, 5);
		Selection gearbox = util.select.position(2, 1, 2);
		Selection belt2 = util.select.fromTo(0, 1, 1, 2, 2, 1);
		Selection vault = util.select.fromTo(3, 2, 3, 4, 1, 1);

		scene.world.showSection(vault, Direction.NORTH);
		scene.idle(5);
		ElementLink<WorldSectionElement> chestLink = scene.world.showIndependentSection(chests, Direction.NORTH);
		scene.world.moveSection(chestLink, util.vector.of(-3, -2, 0), 0);
		scene.idle(10);

		scene.overlay.showOutline(PonderPalette.GREEN, "chestOutline", util.select.fromTo(1, 1, 2, 0, 2, 3), 40);
		scene.idle(10);
		scene.overlay.showLine(PonderPalette.GREEN, util.vector.of(2, 1, 2), util.vector.of(3, 1, 1), 30);
		scene.overlay.showLine(PonderPalette.GREEN, util.vector.of(2, 3, 2), util.vector.of(3, 3, 1), 30);
		scene.overlay.showLine(PonderPalette.GREEN, util.vector.of(2, 3, 4), util.vector.of(3, 3, 4), 30);
		scene.overlay.showOutline(PonderPalette.GREEN, "vaultOutline", vault, 40);
		scene.idle(10);

		BlockPos frontVault = util.grid.at(3, 2, 1);
		scene.overlay.showText(60)
			.text("Item Vaults can be used to store large amounts of items")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(frontVault, Direction.NORTH));
		scene.idle(70);

		scene.world.hideIndependentSection(chestLink, Direction.DOWN);
		scene.idle(15);

		ItemStack hand = AllItems.BRASS_HAND.asStack();
		scene.overlay
			.showControls(new InputWindowElement(util.vector.blockSurface(frontVault, Direction.NORTH), Pointing.RIGHT)
				.showing(AllIcons.I_MTD_CLOSE)
				.withItem(hand), 40);
		scene.idle(7);

		scene.overlay.showText(60)
			.text("However, contents cannot be added or taken manually")
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(frontVault, Direction.WEST));
		scene.idle(70);

		scene.world.showSection(largeCog, Direction.UP);
		scene.idle(5);
		scene.world.showSection(belt1, Direction.EAST);
		scene.idle(20);

		scene.overlay.showText(80)
			.text("Any components for item transfer can both insert...")
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.pointAt(util.vector.topOf(2, 2, 3));

		ItemStack[] junk =
			{ new ItemStack(Items.APPLE), new ItemStack(Items.GOLD_INGOT, 8), new ItemStack(Items.TUFF, 32) };
		for (int i = 0; i < junk.length + 2; i++) {
			scene.idle(4);

			if (i > 1) {
				scene.world.removeItemsFromBelt(util.grid.at(2, 1, 3));
				scene.world.flapFunnel(util.grid.at(2, 2, 3), false);
			}

			scene.idle(5);
			if (i < junk.length)
				scene.world.createItemOnBeltLike(util.grid.at(0, 1, 3), Direction.SOUTH, junk[i]);
			scene.idle(9);
		}

		scene.world.showSection(gearbox, Direction.SOUTH);
		scene.idle(5);
		scene.world.showSection(belt2, Direction.EAST);
		scene.idle(15);

		scene.overlay.showText(80)
			.text("...and take contents from this container")
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.pointAt(util.vector.topOf(2, 1, 1));

		for (int i = 0; i < junk.length; i++) {
			scene.world.createItemOnBeltLike(util.grid.at(2, 1, 1), Direction.EAST, junk[i]);
			scene.idle(18);
		}

	}

	public static void sizes(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("item_vault_sizes", "Dimensions of an Item Vault");
		scene.configureBasePlate(0, 0, 6);
		scene.scaleSceneView(0.9f);
		scene.showBasePlate();
		scene.idle(5);

		Selection single = util.select.position(2, 4, 2);
		Selection single2 = util.select.fromTo(2, 4, 3, 3, 5, 3);
		Selection single3 = util.select.fromTo(2, 4, 4, 4, 6, 4);

		ElementLink<WorldSectionElement> s1 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s1, util.vector.of(0, -3, 0), 0);
		scene.idle(10);

		scene.overlay.showText(60)
			.text("Item Vaults can be combined to increase the total capacity")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.topOf(2, 1, 2));
		scene.idle(40);

		ElementLink<WorldSectionElement> s2 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s2, util.vector.of(1, -3, 0), 0);
		scene.idle(5);
		ElementLink<WorldSectionElement> s3 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s3, util.vector.of(1, -2, 0), 0);
		scene.idle(5);
		ElementLink<WorldSectionElement> s4 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s4, util.vector.of(0, -2, 0), 0);
		scene.idle(10);

		scene.world.moveSection(s1, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s2, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s3, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s4, util.vector.of(0, -100, 0), 0);

		ElementLink<WorldSectionElement> d = scene.world.showIndependentSectionImmediately(single2);
		scene.world.moveSection(d, util.vector.of(0, -3, -1), 0);
		scene.effects.indicateSuccess(util.grid.at(2, 1, 2));
		scene.effects.indicateSuccess(util.grid.at(2, 2, 2));
		scene.effects.indicateSuccess(util.grid.at(3, 2, 2));
		scene.effects.indicateSuccess(util.grid.at(3, 1, 2));
		scene.world.hideIndependentSection(s1, Direction.DOWN);
		scene.world.hideIndependentSection(s2, Direction.DOWN);
		scene.world.hideIndependentSection(s3, Direction.DOWN);
		scene.world.hideIndependentSection(s4, Direction.DOWN);
		scene.idle(25);

		scene.overlay.showText(60)
			.text("Their base square can be up to 3 blocks wide...")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(util.grid.at(2, 2, 2), Direction.NORTH));
		scene.idle(40);

		s1 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s1, util.vector.of(2, -3, 0), 0);
		scene.idle(3);
		s2 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s2, util.vector.of(2, -2, 0), 0);
		scene.idle(3);
		s3 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s3, util.vector.of(2, -1, 0), 0);
		scene.idle(3);
		s4 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s4, util.vector.of(1, -1, 0), 0);
		scene.idle(3);
		ElementLink<WorldSectionElement> s5 = scene.world.showIndependentSection(single, Direction.DOWN);
		scene.world.moveSection(s5, util.vector.of(0, -1, 0), 0);
		scene.idle(10);

		scene.world.moveSection(d, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s1, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s2, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s3, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s4, util.vector.of(0, -100, 0), 0);
		scene.world.moveSection(s5, util.vector.of(0, -100, 0), 0);

		ElementLink<WorldSectionElement> t = scene.world.showIndependentSectionImmediately(single3);
		scene.world.moveSection(t, util.vector.of(0, -3, -2), 0);

		for (int i = 1; i < 4; i++)
			for (int j = 2; j < 5; j++)
				scene.effects.indicateSuccess(util.grid.at(j, i, 2));

		scene.world.hideIndependentSection(d, Direction.DOWN);
		scene.world.hideIndependentSection(s1, Direction.DOWN);
		scene.world.hideIndependentSection(s2, Direction.DOWN);
		scene.world.hideIndependentSection(s3, Direction.DOWN);
		scene.world.hideIndependentSection(s4, Direction.DOWN);
		scene.world.hideIndependentSection(s5, Direction.DOWN);
		scene.idle(25);

		scene.world.hideIndependentSection(t, Direction.DOWN);
		scene.idle(15);

		Selection full1 = util.select.fromTo(2, 1, 0, 4, 1, 0);
		Selection full2 = util.select.fromTo(0, 1, 1, 3, 2, 2);
		Selection full3 = util.select.fromTo(1, 1, 5, 5, 3, 3);

		scene.world.showSection(full3, Direction.WEST);
		scene.idle(5);
		scene.world.showSection(full2, Direction.EAST);
		scene.idle(5);
		scene.world.showSection(full1, Direction.WEST);
		scene.idle(10);

		Vec3 blockSurface = util.vector.blockSurface(util.grid.at(1, 3, 3), Direction.NORTH);
		scene.overlay.showText(60)
			.text("...and grow in length up to 3x their diameter")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(blockSurface);
		InputWindowElement dyeInput = new InputWindowElement(util.vector.centerOf(util.grid.at(1,1,1)), Pointing.RIGHT)
				.withItem(new ItemStack(Items.CYAN_DYE));
		InputWindowElement waterInput = new InputWindowElement(util.vector.centerOf(util.grid.at(1,1,1)), Pointing.RIGHT)
				.withItem(new ItemStack(Items.WATER_BUCKET));
		scene.idle(80);
		scene.overlay.showText(40)
				.colored(PonderPalette.BLUE)
				.text("Right-Click with Dye to paint them...")
				.attachKeyFrame()
				.pointAt(util.vector.centerOf(util.grid.at(1,1,1)))
				.placeNearTarget();
		scene.idle(25);
		scene.overlay.showControls(dyeInput, 30);
		scene.idle(20);
		scene.world.replaceBlocks(full2, AllBlocks.ITEM_VAULT.getDefaultState().setValue(ItemVaultBlock.COLOR, 9), false);
		scene.idle(20);
		scene.world.replaceBlocks(full1, AllBlocks.ITEM_VAULT.getDefaultState().setValue(ItemVaultBlock.COLOR, 14), false);
		scene.idle(20);
		scene.world.replaceBlocks(full3, AllBlocks.ITEM_VAULT.getDefaultState().setValue(ItemVaultBlock.COLOR, 5).setValue(ItemVaultBlock.LARGE, false), true);
		scene.idle(25);
		scene.overlay.showText(40)
				.colored(PonderPalette.BLUE)
				.text("...and with Water to clean them")
				.attachKeyFrame()
				.pointAt(util.vector.centerOf(util.grid.at(1,1,1)))
				.placeNearTarget();
		scene.idle(20);
		scene.overlay.showControls(waterInput, 30);
		scene.idle(20);
		scene.world.replaceBlocks(full2, AllBlocks.ITEM_VAULT.getDefaultState().setValue(ItemVaultBlock.COLOR, 16), false);
		scene.idle(20);
		scene.world.replaceBlocks(full1, AllBlocks.ITEM_VAULT.getDefaultState().setValue(ItemVaultBlock.COLOR, 16), false);
		scene.idle(20);
		scene.world.replaceBlocks(full3, AllBlocks.ITEM_VAULT.getDefaultState().setValue(ItemVaultBlock.COLOR, 16).setValue(ItemVaultBlock.LARGE, true), false);
		scene.idle(40);
	}

}
