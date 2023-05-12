package com.simibubi.create.gametest.tests;

import static com.simibubi.create.gametest.infrastructure.CreateGameTestHelper.FIFTEEN_SECONDS;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.schematics.SchematicExport;
import com.simibubi.create.content.schematics.block.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.block.SchematicannonBlockEntity.State;
import com.simibubi.create.content.schematics.item.SchematicItem;
import com.simibubi.create.gametest.infrastructure.CreateGameTestHelper;
import com.simibubi.create.gametest.infrastructure.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

@GameTestGroup(path = "misc")
public class TestMisc {
	@GameTest(template = "schematicannon", timeoutTicks = FIFTEEN_SECONDS)
	public static void schematicannon(CreateGameTestHelper helper) {
		// load the structure
		BlockPos whiteEndBottom = helper.absolutePos(new BlockPos(5, 2, 1));
		BlockPos redEndTop = helper.absolutePos(new BlockPos(5, 4, 7));
		ServerLevel level = helper.getLevel();
		SchematicExport.saveSchematic(
				SchematicExport.SCHEMATICS.resolve("uploaded/Deployer"), "schematicannon_gametest", true,
				level, whiteEndBottom, redEndTop
		);
		ItemStack schematic = SchematicItem.create("schematicannon_gametest.nbt", "Deployer");
		// deploy to pos
		BlockPos anchor = helper.absolutePos(new BlockPos(1, 2, 1));
		schematic.getOrCreateTag().putBoolean("Deployed", true);
		schematic.getOrCreateTag().put("Anchor", NbtUtils.writeBlockPos(anchor));
		// setup cannon
		BlockPos cannonPos = new BlockPos(3, 2, 6);
		SchematicannonBlockEntity cannon = helper.getBlockEntity(AllBlockEntityTypes.SCHEMATICANNON.get(), cannonPos);
		cannon.inventory.setStackInSlot(0, schematic);
		// run
		cannon.state = State.RUNNING;
		cannon.statusMsg = "running";
		helper.succeedWhen(() -> {
			if (cannon.state != State.STOPPED) {
				helper.fail("Schematicannon not done");
			}
			BlockPos lastBlock = new BlockPos(1, 4, 7);
			helper.assertBlockPresent(Blocks.RED_WOOL, lastBlock);
		});
	}

	@GameTest(template = "shearing")
	public static void shearing(CreateGameTestHelper helper) {
		BlockPos sheepPos = new BlockPos(2, 1, 2);
		Sheep sheep = helper.getFirstEntity(EntityType.SHEEP, sheepPos);
		sheep.shear(SoundSource.NEUTRAL);
		helper.succeedWhen(() -> {
			helper.assertItemEntityPresent(Items.WHITE_WOOL, sheepPos, 2);
		});
	}
}
