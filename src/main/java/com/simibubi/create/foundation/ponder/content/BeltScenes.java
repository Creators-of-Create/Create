package com.simibubi.create.foundation.ponder.content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class BeltScenes {

	public static void beltsCanBeEncased(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("belt_casing", "Encasing Belts");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);
		scene.world.showSection(util.select.layersFrom(1), Direction.DOWN);
		scene.idle(20);

		ItemStack brassCasingItem = AllBlocks.BRASS_CASING.asStack();
		ItemStack andesiteCasingItem = AllBlocks.ANDESITE_CASING.asStack();

		BlockPos beltPos = util.grid.at(3, 1, 0);
		BlockPos beltPos2 = util.grid.at(0, 2, 3);
		BlockPos beltPos3 = util.grid.at(1, 4, 4);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(beltPos), Pointing.DOWN).rightClick()
			.withItem(brassCasingItem), 20);
		scene.idle(7);
		scene.world.modifyBlock(beltPos, s -> s.with(BeltBlock.CASING, true), true);
		scene.idle(20);

		scene.overlay.showControls(new InputWindowElement(util.vector.topOf(beltPos2), Pointing.DOWN).rightClick()
			.withItem(andesiteCasingItem), 20);
		scene.idle(7);
		scene.world.modifyBlock(beltPos2, s -> s.with(BeltBlock.CASING, true), true);
		scene.world.modifyTileNBT(util.select.position(beltPos2), BeltTileEntity.class, nbt -> {
			NBTHelper.writeEnum(nbt, "Casing", BeltTileEntity.CasingType.ANDESITE);
		});
		scene.idle(20);

		scene.overlay.showControls(
			new InputWindowElement(util.vector.blockSurface(beltPos3, Direction.EAST), Pointing.RIGHT).rightClick()
				.withItem(brassCasingItem),
			20);
		scene.idle(7);
		scene.world.modifyBlock(beltPos3, s -> s.with(BeltBlock.CASING, true), true);
		scene.idle(20);

		scene.overlay.showText(80)
			.text("Brass or Andesite Casing can be used to decorate Mechanical Belts")
			.pointAt(util.vector.centerOf(beltPos2));

		scene.idle(40);

		List<BlockPos> brassBelts = new ArrayList<>();
		List<BlockPos> andesiteBelts = new ArrayList<>();

		for (int z = 1; z <= 3; z++)
			brassBelts.add(beltPos.south(z));
		for (int x = 1; x <= 3; x++)
			brassBelts.add(beltPos3.east(x)
				.down(x));
		for (int x = 1; x <= 3; x++)
			andesiteBelts.add(beltPos2.east(x));

		Collections.shuffle(andesiteBelts);
		Collections.shuffle(brassBelts);

		for (BlockPos pos : andesiteBelts) {
			scene.idle(4);
			scene.world.modifyBlock(pos, s -> s.with(BeltBlock.CASING, true), true);
			scene.world.modifyTileNBT(util.select.position(pos), BeltTileEntity.class, nbt -> {
				NBTHelper.writeEnum(nbt, "Casing", BeltTileEntity.CasingType.ANDESITE);
			});
		}
		for (BlockPos pos : brassBelts) {
			scene.idle(4);
			scene.world.modifyBlock(pos, s -> s.with(BeltBlock.CASING, true), true);
		}
		scene.idle(30);

		scene.overlay
			.showControls(new InputWindowElement(util.vector.topOf(beltPos.south()), Pointing.DOWN).rightClick()
				.withWrench(), 40);
		scene.idle(7);
		scene.world.modifyBlock(beltPos.south(), s -> s.with(BeltBlock.CASING, false), true);
		scene.overlay.showText(80)
			.text("A wrench can be used to remove it again")
			.placeNearTarget()
			.pointAt(util.vector.blockSurface(beltPos.south(), Direction.WEST));
	}

}
