package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.logistics.block.display.DisplayLinkTileEntity;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayTileEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.PonderPalette;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.ParrotElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.AABB;

public class DisplayScenes {

	public static void link(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("display_link", "Setting up Display Links");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		ItemStack linkItem = AllBlocks.DISPLAY_LINK.asStack();
		BlockPos invalidLinkPos = util.grid.at(2, 1, 2);
		Selection invalidLinkSel = util.select.position(invalidLinkPos);
		BlockPos linkPos = util.grid.at(2, 1, 1);
		Selection linkSel = util.select.position(linkPos);

		BlockPos signPos = util.grid.at(2, 1, 4);
		BlockPos signTarget = util.grid.at(2, 2, 4);

		scene.idle(20);

		scene.world.showSection(invalidLinkSel, Direction.DOWN);

		scene.idle(10);
		scene.effects.indicateRedstone(invalidLinkPos);
		scene.overlay.showSelectionWithText(invalidLinkSel, 60)
				.attachKeyFrame()
				.colored(PonderPalette.RED)
				.text("Display Links should be assigned a target before they are placed")
				.pointAt(util.vector.blockSurface(invalidLinkPos, Direction.WEST))
				.placeNearTarget();

		scene.idle(60);

		ElementLink<WorldSectionElement> signSection = scene.world.showIndependentSection(util.select.position(signPos), Direction.DOWN);
		scene.world.moveSection(signSection, util.vector.of(0, 1, 0), 0);

		ElementLink<WorldSectionElement> concreteSection = scene.world.showIndependentSection(util.select.position(signPos.below()), Direction.UP);
		scene.world.moveSection(concreteSection, util.vector.of(0, 1, 0), 0);

		scene.world.hideSection(invalidLinkSel, Direction.UP);

		scene.idle(20);

		scene.overlay.showControls(
				new InputWindowElement(util.vector.blockSurface(signTarget, Direction.UP), Pointing.DOWN)
						.rightClick().withItem(linkItem),
				50
		);

		scene.idle(5);
		AABB signBounds = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D).bounds();
		scene.overlay.chaseBoundingBoxOutline(PonderPalette.OUTPUT, new Object(), signBounds.move(signTarget), 60);

		scene.overlay.showText(70)
				.colored(PonderPalette.OUTPUT)
				.text("First select a target by Right- Clicking while holding the Link ...")
				.pointAt(util.vector.topOf(signTarget))
				.placeNearTarget();

		scene.idle(50);

		BlockPos observerPos = util.grid.at(3, 1, 1);

		scene.world.showSection(util.select.fromTo(observerPos, observerPos.relative(Direction.SOUTH)), Direction.DOWN);

		scene.idle(10);

		scene.overlay.showControls(
				new InputWindowElement(util.vector.blockSurface(observerPos, Direction.WEST), Pointing.RIGHT)
						.rightClick().withItem(linkItem),
				50
		);
		scene.overlay.showText(60)
				.attachKeyFrame()
				.colored(PonderPalette.INPUT)
				.text("... then select the source by placing the Link against it")
				.pointAt(util.vector.blockSurface(observerPos, Direction.UP))
				.placeNearTarget();

		scene.idle(55);

		scene.world.showSection(linkSel, Direction.EAST);

		scene.idle(20);

		scene.world.modifyTileEntity(linkPos, DisplayLinkTileEntity.class, linkTile ->
				linkTile.glow.setValue(2));
		scene.world.modifyTileEntity(signPos, SignBlockEntity.class, signTile ->
				signTile.setMessage(1, new TextComponent("42 Cinder Flour")));

		scene.idle(60);

		scene.world.hideIndependentSection(signSection, Direction.UP);
		scene.world.hideIndependentSection(concreteSection, Direction.DOWN);
		scene.idle(25);

		Selection boards = util.select.fromTo(4, 1, 4, 1, 2, 4)
				.substract(util.select.position(signPos));
		Selection cogs = util.select.position(5, 2, 4)
				.add(util.select.position(5, 2, 5))
				.add(util.select.position(5, 1, 5))
				.add(util.select.position(4, 0, 5));

		scene.world.showSection(boards, Direction.DOWN);
		scene.world.showSection(cogs, Direction.DOWN);

		scene.overlay.showText(70)
						.attachKeyFrame()
						.colored(PonderPalette.WHITE)
						.text("Replacing Targets or Sources at a later time is possible as well")
						.placeNearTarget();

		scene.idle(7);

		scene.world.modifyTileEntity(linkPos, DisplayLinkTileEntity.class, linkTile ->
				linkTile.glow.setValue(2));

		scene.idle(43);

		scene.world.hideSection(util.select.fromTo(observerPos, observerPos.relative(Direction.SOUTH)), Direction.SOUTH);

		scene.idle(15);

		ElementLink<WorldSectionElement> seatSection = scene.world.showIndependentSection(util.select.position(3, 1, 0), Direction.SOUTH);
		scene.world.moveSection(seatSection, util.vector.of(0, 0, 1), 0);

		scene.world.modifyTileEntity(util.grid.at(4, 2, 4), FlapDisplayTileEntity.class, displayTile -> {
			displayTile.applyTextManually(0, "");
			displayTile.applyTextManually(1, "");
		});


		scene.idle(30);
		scene.special.createBirb(util.vector.of(3.5, 1.4, 1.5), ParrotElement.DancePose::new);

		scene.idle(10);

		//scene.world.modifyTileEntity(util.grid.at(4, 2, 4), FlapDisplayTileEntity.class, displayTile -> {
		//	displayTile.applyTextManually(0, "Sitting here:");
		//	displayTile.applyTextManually(1, "Party Parrot");
		//});

		ElementLink<WorldSectionElement> replacementBoards = scene.world.showIndependentSectionImmediately(util.select.fromTo(4, 2, 3, 1, 2, 3));
		scene.world.moveSection(replacementBoards, util.vector.of(0, 0, 1), 0);

		scene.world.modifyTileEntity(linkPos, DisplayLinkTileEntity.class, linkTile ->
				linkTile.glow.setValue(2));

		scene.idle(10);

	}

}
