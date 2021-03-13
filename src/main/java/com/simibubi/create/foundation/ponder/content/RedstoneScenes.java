package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerBlock;
import com.simibubi.create.content.contraptions.components.structureMovement.chassis.StickerTileEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class RedstoneScenes {

	public static void sticker(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("sticker", "Attaching blocks using the Sticker");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		Selection redstone = util.select.fromTo(0, 2, 2, 2, 2, 2);
		BlockPos stickerPos = util.grid.at(2, 2, 2);
		Selection stickerSelect = util.select.position(stickerPos);
		BlockPos buttonPos = util.grid.at(0, 2, 2);
		BlockPos bearingPos = util.grid.at(2, 1, 2);

		scene.world.showSection(util.select.fromTo(2, 1, 2, 0, 2, 2)
			.substract(stickerSelect), Direction.DOWN);
		scene.idle(10);
		ElementLink<WorldSectionElement> sticker = scene.world.showIndependentSection(stickerSelect, Direction.DOWN);
		scene.idle(10);
		ElementLink<WorldSectionElement> plank =
			scene.world.showIndependentSection(util.select.position(2, 2, 1), Direction.SOUTH);
		scene.world.configureCenterOfRotation(sticker, util.vector.centerOf(stickerPos));
		scene.world.configureCenterOfRotation(plank, util.vector.centerOf(stickerPos));
		scene.overlay.showText(60)
			.text("Stickers are ideal for Redstone-controlled block attachment")
			.attachKeyFrame()
			.pointAt(util.vector.blockSurface(stickerPos, Direction.WEST))
			.placeNearTarget();
		scene.idle(70);

		scene.world.toggleRedstonePower(redstone);
		scene.world.modifyBlock(stickerPos, s -> s.with(StickerBlock.EXTENDED, true), false);
		scene.effects.indicateRedstone(buttonPos);
		scene.world.modifyTileNBT(stickerSelect, StickerTileEntity.class, nbt -> {
		});
		scene.idle(20);

		scene.world.toggleRedstonePower(redstone);
		scene.idle(20);

		scene.overlay.showText(60)
			.text("Upon receiving a signal, it will toggle its state")
			.pointAt(util.vector.blockSurface(stickerPos, Direction.WEST))
			.placeNearTarget();
		scene.idle(70);

		scene.world.rotateBearing(bearingPos, 180 * 3, 80);
		scene.world.rotateSection(sticker, 0, 180 * 3, 0, 80);
		scene.world.rotateSection(plank, 0, 180 * 3, 0, 80);
		scene.overlay.showText(70)
			.text("If it is now moved in a contraption, the block will move with it")
			.pointAt(util.vector.topOf(stickerPos))
			.placeNearTarget();
		scene.idle(90);
		scene.addKeyframe();

		scene.world.toggleRedstonePower(redstone);
		scene.world.modifyBlock(stickerPos, s -> s.with(StickerBlock.EXTENDED, false), false);
		scene.effects.indicateRedstone(buttonPos);
		scene.world.modifyTileNBT(stickerSelect, StickerTileEntity.class, nbt -> {
		});
		scene.idle(20);

		scene.world.toggleRedstonePower(redstone);
		scene.idle(20);

		scene.overlay.showText(60)
			.text("Toggled once again, the block is no longer attached")
			.pointAt(util.vector.blockSurface(stickerPos, Direction.WEST))
			.placeNearTarget();
		scene.idle(70);
		
		scene.world.rotateBearing(bearingPos, 180 * 3, 80);
		scene.world.rotateSection(sticker, 0, 180 * 3, 0, 80);
	}

	public static void contact(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("redstone_contact", "Redstone Contacts");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);
		Selection contactAndRedstone = util.select.fromTo(1, 1, 0, 1, 1, 2);
		Selection topContact = util.select.position(1, 2, 2);

		scene.world.toggleRedstonePower(contactAndRedstone);
		scene.world.toggleRedstonePower(topContact);
		scene.world.showSection(contactAndRedstone, Direction.DOWN);

		BlockPos bearingPos = util.grid.at(3, 1, 2);
		scene.idle(25);

		ElementLink<WorldSectionElement> contact = scene.world.showIndependentSection(topContact, Direction.DOWN);
		scene.idle(10);
		scene.world.toggleRedstonePower(topContact);
		scene.world.toggleRedstonePower(contactAndRedstone);
		scene.effects.indicateRedstone(util.grid.at(1, 1, 2));
		scene.idle(10);
		scene.overlay.showText(60)
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector.of(1, 2, 2.5))
			.text("Redstone Contacts facing each other will emit a redstone signal");
		scene.idle(70);

		scene.world.showSection(util.select.position(bearingPos), Direction.DOWN);
		scene.idle(10);
		scene.world.showSectionAndMerge(util.select.fromTo(2, 2, 2, 4, 2, 2), Direction.DOWN, contact);
		scene.idle(10);
		scene.effects.superGlue(util.grid.at(1, 2, 2), Direction.EAST, true);
		scene.world.configureCenterOfRotation(contact, util.vector.centerOf(bearingPos));

		int speed = 2;

		scene.idle(10);
		scene.world.rotateBearing(bearingPos, 10, speed);
		scene.world.rotateSection(contact, 0, 10, 0, speed);
		scene.idle(speed);

		scene.world.toggleRedstonePower(topContact);
		scene.world.toggleRedstonePower(contactAndRedstone);
		scene.effects.indicateRedstone(util.grid.at(1, 1, 2));
		scene.world.rotateBearing(bearingPos, 340, 34 * speed);
		scene.world.rotateSection(contact, 0, 340, 0, 34 * speed);
		scene.addKeyframe();
		scene.idle(34 * speed);

		scene.overlay.showText(100)
			.placeNearTarget()
			.pointAt(util.vector.of(1, 1.5, 2.5))
			.text("This still applies when one of them is part of a moving Contraption");

		for (int i = 0; i < 5; i++) {
			scene.world.toggleRedstonePower(topContact);
			scene.world.toggleRedstonePower(contactAndRedstone);
			scene.effects.indicateRedstone(util.grid.at(1, 1, 2));
			scene.world.rotateBearing(bearingPos, 20, 2 * speed);
			scene.world.rotateSection(contact, 0, 20, 0, 2 * speed);
			scene.idle(2 * speed);

			scene.world.toggleRedstonePower(topContact);
			scene.world.toggleRedstonePower(contactAndRedstone);
			scene.world.rotateBearing(bearingPos, 340, 34 * speed);
			scene.world.rotateSection(contact, 0, 340, 0, 34 * speed);
			scene.idle(34 * speed);

			if (i == 0)
				scene.markAsFinished();
		}

		scene.world.toggleRedstonePower(topContact);
		scene.world.toggleRedstonePower(contactAndRedstone);
		scene.world.rotateBearing(bearingPos, 10, speed);
		scene.world.rotateSection(contact, 0, 10, 0, speed);
	}

}
