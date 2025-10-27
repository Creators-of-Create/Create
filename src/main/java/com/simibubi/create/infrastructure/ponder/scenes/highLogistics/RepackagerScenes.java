package com.simibubi.create.infrastructure.ponder.scenes.highLogistics;

import java.util.List;

import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class RepackagerScenes {

	public static void repackager(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("repackager", "Merging packages from a request");
		scene.configureBasePlate(1, 0, 7);
		scene.setSceneOffsetY(-.5f);
		scene.showBasePlate();

		Selection belt2 = util.select()
			.fromTo(7, 1, 2, 6, 1, 1);
		Selection belt1 = util.select()
			.fromTo(1, 1, 2, 4, 1, 1);
		Selection belt3 = util.select()
			.fromTo(5, 1, 1, 5, 1, 6)
			.add(util.select()
				.fromTo(6, 1, 5, 6, 1, 6));
		Selection crafterCogs = util.select()
			.fromTo(1, 1, 6, 0, 2, 6)
			.add(util.select()
				.position(0, 2, 5))
			.add(util.select()
				.fromTo(1, 1, 7, 2, 0, 7));
		Selection crafter = util.select()
			.fromTo(1, 2, 5, 3, 4, 5);
		Selection crafterScaff = util.select()
			.fromTo(3, 1, 5, 1, 1, 5);
		Selection packager = util.select()
			.fromTo(4, 1, 5, 4, 2, 5);
		BlockPos pack = util.grid()
			.at(4, 2, 5);
		Selection packFunnel = util.select()
			.position(5, 2, 5);
		Selection largeCog1 = util.select()
			.position(0, 0, 2);
		Selection largeCog2 = util.select()
			.position(8, 0, 2);
		BlockPos funnel1 = util.grid()
			.at(4, 2, 1);
		BlockPos funnel2 = util.grid()
			.at(6, 2, 1);
		Selection f3s = util.select()
			.position(5, 2, 3);
		Selection f2s = util.select()
			.position(funnel2);
		Selection f1s = util.select()
			.position(funnel1);
		Selection barrel = util.select()
			.position(5, 2, 1);
		BlockPos repack = util.grid()
			.at(5, 2, 2);
		Selection repackS = util.select()
			.fromTo(5, 2, 2, 5, 3, 2);
		Selection largeCog3 = util.select()
			.position(6, 0, 7);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 1 / 2f);

		scene.idle(10);

		scene.world()
			.showSection(crafterScaff, Direction.NORTH);
		scene.idle(3);
		scene.world()
			.showSection(crafter, Direction.DOWN);
		scene.idle(5);
		scene.world()
			.showSection(packager, Direction.WEST);
		scene.world()
			.showSection(crafterCogs, Direction.EAST);
		scene.idle(5);

		scene.overlay()
			.showText(120)
			.text("Sometimes, it is crucial for logistical requests to arrive as a single package")
			.attachKeyFrame()
			.placeNearTarget()
			.independent(130);

		scene.idle(90);
		scene.world()
			.showSection(belt3, Direction.WEST);
		scene.world()
			.showSection(largeCog3, Direction.UP);
		scene.idle(5);
		scene.world()
			.showSection(belt2, Direction.WEST);
		scene.world()
			.showSection(largeCog2, Direction.UP);
		scene.idle(5);
		scene.world()
			.showSection(belt1, Direction.EAST);
		scene.world()
			.showSection(largeCog1, Direction.UP);
		scene.world()
			.showSection(packFunnel, Direction.DOWN);
		scene.idle(20);

		scene.world()
			.setBlock(util.grid()
				.at(4, 2, 1), Blocks.AIR.defaultBlockState(), false);
		scene.world()
			.setBlock(util.grid()
				.at(6, 2, 1), Blocks.AIR.defaultBlockState(), false);

		ItemStack box1 = PackageItem.containing(List.of());
		ItemStack box2 = PackageItem.containing(List.of());
		ItemStack box3 = PackageItem.containing(List.of());
		scene.world()
			.createItemOnBelt(util.grid()
				.at(3, 1, 1), Direction.DOWN, box1);
		scene.world()
			.createItemOnBelt(util.grid()
				.at(2, 1, 1), Direction.DOWN, box2);
		scene.idle(13);
		scene.world()
			.createItemOnBelt(util.grid()
				.at(7, 1, 1), Direction.DOWN, box3);
		scene.idle(3);
		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 1 / 16f);

		AABB bb1 = new AABB(util.grid()
			.at(2, 2, 1)).deflate(0.125, 0.5, 0.125)
				.inflate(0.65, 0, 0)
				.move(1.05, -.5, 0);
		AABB bb2 = new AABB(util.grid()
			.at(7, 2, 1)).deflate(0.125, 0.5, 0.125)
				.move(-.25, -.5, 0);

		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.INPUT, pack, new AABB(bb1.getCenter(), bb1.getCenter()), 1);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.OUTPUT, repack, new AABB(bb2.getCenter(), bb2.getCenter()), 1);
		scene.idle(1);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.INPUT, pack, bb1, 60);
		scene.overlay()
			.chaseBoundingBoxOutline(PonderPalette.OUTPUT, repack, bb2, 60);

		scene.overlay()
			.showText(60)
			.text("Order A")
			.attachKeyFrame()
			.colored(PonderPalette.INPUT)
			.pointAt(util.vector()
				.of(3, 2, 1.5))
			.placeNearTarget();

		scene.overlay()
			.showText(60)
			.text("Order B")
			.attachKeyFrame()
			.colored(PonderPalette.OUTPUT)
			.pointAt(util.vector()
				.of(7, 2, 1.5))
			.placeNearTarget();
		scene.idle(60);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 24f);
		scene.idle(60);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 1 / 24f);

		scene.overlay()
			.showText(60)
			.text("Otherwise, other packages could arrive inbetween")
			.attachKeyFrame()
			.colored(PonderPalette.RED)
			.pointAt(util.vector()
				.of(5.5, 2, 3))
			.placeNearTarget();
		scene.idle(60);

		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 16f);
		scene.idle(40);
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(5, 1, 5));
		PonderHilo.packagerUnpack(scene, pack, box1);
		scene.idle(15);
		insertItemsIntoCrafter(util, scene, new ItemStack(Items.IRON_INGOT, 4));
		scene.idle(15);
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(5, 1, 5));
		PonderHilo.packagerUnpack(scene, pack, box2);
		scene.idle(15);
		insertItemsIntoCrafter(util, scene, new ItemStack(Items.OAK_PLANKS, 3));
		scene.idle(15);

		scene.overlay()
			.showControls(util.vector()
				.blockSurface(util.grid()
					.at(2, 3, 5), Direction.NORTH),
				Pointing.DOWN, 40)
			.withItem(new ItemStack(Items.BARRIER));

		scene.idle(20);
		scene.world()
			.modifyBlockEntity(util.grid()
				.at(2, 2, 5), MechanicalCrafterBlockEntity.class, be -> be.ejectWholeGrid());
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(5, 1, 5));

		scene.idle(40);
		scene.world()
			.showSection(barrel, Direction.DOWN);
		scene.world()
			.showSection(repackS, Direction.NORTH);
		scene.rotateCameraY(-15);
		scene.idle(15);

		scene.overlay()
			.showText(80)
			.text("When this is the case, redirect packages to an inventory")
			.attachKeyFrame()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(5, 2, 1), Direction.WEST))
			.placeNearTarget();
		scene.idle(90);

		scene.overlay()
			.showText(60)
			.text("Attach a re-packager, and power it with redstone")
			.attachKeyFrame()
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(5, 2, 2), Direction.WEST))
			.placeNearTarget();
		scene.idle(40);

		scene.world()
			.toggleRedstonePower(repackS);
		scene.effects()
			.indicateRedstone(util.grid()
				.at(5, 3, 2));
		scene.idle(40);

		scene.world()
			.restoreBlocks(f1s);
		scene.world()
			.restoreBlocks(f2s);
		scene.world()
			.showSection(f1s, Direction.DOWN);
		scene.world()
			.showSection(f2s, Direction.DOWN);
		scene.idle(5);
		scene.world()
			.showSection(f3s, Direction.DOWN);
		scene.idle(20);

		scene.world()
			.createItemOnBelt(util.grid()
				.at(3, 1, 1), Direction.DOWN, box1);
		scene.world()
			.createItemOnBelt(util.grid()
				.at(1, 1, 1), Direction.DOWN, box2);
		scene.world()
			.createItemOnBelt(util.grid()
				.at(7, 1, 1), Direction.DOWN, box3);
		scene.idle(23);
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(4, 1, 1));
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(6, 1, 1));
		scene.world()
			.flapFunnel(util.grid()
				.at(4, 2, 1), false);
		scene.idle(63);
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(4, 1, 1));
		scene.world()
			.flapFunnel(util.grid()
				.at(4, 2, 1), false);
		scene.idle(3);

		PonderHilo.packagerCreate(scene, repack, box3);
		scene.effects()
			.indicateSuccess(repack);
		scene.idle(20);

		scene.overlay()
			.showText(60)
			.text("Once all fragments arrived, they will be exported as a new package")
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.pointAt(util.vector()
				.of(5.5, 2, 3))
			.placeNearTarget();
		scene.idle(60);
		scene.world()
			.multiplyKineticSpeed(util.select()
				.everywhere(), 2f);
		scene.rotateCameraY(15);
		scene.world()
			.createItemOnBelt(util.grid()
				.at(5, 1, 3), Direction.NORTH, box3);
		PonderHilo.packagerClear(scene, repack);
		scene.idle(20);
		PonderHilo.packagerCreate(scene, repack, box3);
		scene.idle(20);
		scene.world()
			.createItemOnBelt(util.grid()
				.at(5, 1, 3), Direction.NORTH, box3);
		PonderHilo.packagerClear(scene, repack);
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(5, 1, 5));
		PonderHilo.packagerUnpack(scene, pack, box3);
		scene.idle(15);
		insertItemsIntoCrafter(util, scene, new ItemStack(Items.IRON_INGOT, 9));
		scene.world()
			.setCraftingResult(util.grid()
				.at(2, 2, 5), new ItemStack(Items.IRON_BLOCK));
		scene.idle(15);

		scene.overlay()
			.showText(120)
			.text("Now, requested items arrive together and in a predictable order")
			.attachKeyFrame()
			.colored(PonderPalette.BLUE)
			.pointAt(util.vector()
				.blockSurface(util.grid()
					.at(2, 3, 5), Direction.NORTH))
			.placeNearTarget();

		scene.idle(75);
		scene.world()
			.removeItemsFromBelt(util.grid()
				.at(5, 1, 5));
		PonderHilo.packagerUnpack(scene, pack, box3);
		scene.idle(15);
		insertItemsIntoCrafter(util, scene, new ItemStack(Items.OAK_PLANKS, 3));
		scene.world()
			.setCraftingResult(util.grid()
				.at(2, 2, 5), new ItemStack(Items.OAK_SLAB));
		scene.idle(20);
		scene.world()
			.modifyBlockEntity(util.grid()
				.at(2, 2, 5), MechanicalCrafterBlockEntity.class, be -> be.checkCompletedRecipe(true));
	}

	private static void insertItemsIntoCrafter(SceneBuildingUtil util, CreateSceneBuilder scene, ItemStack stack) {
		scene.world()
			.modifyBlockEntity(util.grid()
				.at(3, 2, 5), BlockEntity.class, be -> {
					IItemHandler handler = be.getLevel().getCapability(ItemHandler.BLOCK, be.getBlockPos(), null);
					if (handler == null)
						return;
					ItemHandlerHelper.insertItemStacked(handler, stack, false);
				});
	}

}
