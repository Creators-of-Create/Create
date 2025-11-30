package com.simibubi.create.infrastructure.gametest.tests;

import static com.simibubi.create.infrastructure.gametest.CreateGameTestHelper.FIFTEEN_SECONDS;

import com.mojang.authlib.GameProfile;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.simibubi.create.content.schematics.SchematicExport;
import com.simibubi.create.content.schematics.SchematicItem;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import com.simibubi.create.foundation.utility.CreatePaths;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;

import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.UUID;

@GameTestGroup(path = "misc")
public class TestMisc {
	@GameTest(template = "schematicannon", timeoutTicks = FIFTEEN_SECONDS)
	public static void schematicannon(CreateGameTestHelper helper) {
		// load the structure
		BlockPos whiteEndBottom = helper.absolutePos(new BlockPos(5, 2, 1));
		BlockPos redEndTop = helper.absolutePos(new BlockPos(5, 4, 7));
		ServerLevel level = helper.getLevel();
		SchematicExport.saveSchematic(
			CreatePaths.UPLOADED_SCHEMATICS_DIR.resolve("Deployer"), "schematicannon_gametest", true,
			level, whiteEndBottom, redEndTop
		);
		ItemStack schematic =
			SchematicItem.create(level, "schematicannon_gametest.nbt", "Deployer");
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

	@GameTest(template = "smart_observer_blocks")
	public static void smartObserverBlocks(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(2, 2, 1);
		BlockPos leftLamp = new BlockPos(3, 4, 3);
		BlockPos rightLamp = new BlockPos(1, 4, 3);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			helper.assertBlockProperty(leftLamp, RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(rightLamp, RedstoneLampBlock.LIT, false);
		});
	}

	@GameTest(template = "threshold_switch_pulley")
	public static void thresholdSwitchPulley(CreateGameTestHelper helper) {
		BlockPos lever = new BlockPos(3, 7, 1);
		BlockPos switchPos = new BlockPos(1, 6, 1);
		BlockPos finalPos = new BlockPos(2, 2, 1);
		helper.pullLever(lever);
		helper.succeedWhen(() -> {
			ThresholdSwitchBlockEntity switchBe = helper.getBlockEntity(AllBlockEntityTypes.THRESHOLD_SWITCH.get(), switchPos);
			int level = switchBe.getStockLevel();
			int expectedLevel = helper.absolutePos(finalPos).getY();
			if (level != expectedLevel)
				helper.fail("Unexpected level: " + level);
		});
	}

	@GameTest(template = "netherite_backtank", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
	public static void netheriteBacktank(CreateGameTestHelper helper) {
		BlockPos lava = new BlockPos(2, 2, 3);
		BlockPos zombieSpawn = lava.above(2);
		BlockPos armorStandPos = new BlockPos(2, 2, 1);
		helper.runAtTickTime(5, () -> {
			Zombie zombie = helper.spawn(EntityType.ZOMBIE, zombieSpawn);
			ArmorStand armorStand = helper.getFirstEntity(EntityType.ARMOR_STAND, armorStandPos);
			for (EquipmentSlot slot : EquipmentSlot.values()) {
				zombie.setItemSlot(slot, armorStand.getItemBySlot(slot).copy());
			}
		});
		helper.succeedWhen(() -> {
			helper.assertSecondsPassed(9);
			helper.assertEntityPresent(EntityType.ZOMBIE, lava);
		});
	}

	@GameTest(template = "platform_3_3")
	public static void itemUseOnBlock(CreateGameTestHelper helper) {

		ServerLevel level = helper.getLevel();
		FakePlayer fakePlayer = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "TestPlayer"));
		fakePlayer.setGameMode(GameType.CREATIVE);

		ItemStack stone = new ItemStack(Blocks.STONE.asItem(), 1);

		BlockPos pos = new BlockPos(1,1,1);
		BlockPos absPos = helper.absolutePos(pos);

		helper.playerItemUseOn(fakePlayer,stone,0, absPos);

		helper.succeedIf(() -> {
			helper.assertBlockPresent(Blocks.STONE, pos.offset(0 , 1 ,0));
		});
	}

	@GameTest(template = "cow_platform")
	public static void itemUseOnEntityInteract (CreateGameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		FakePlayer fakePlayer = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "TestPlayer"));

		ItemStack bucket = new ItemStack(Items.BUCKET, 1);

		BlockPos pos = new BlockPos(2,1,2);
		BlockPos absPos = helper.absolutePos(pos);

		Cow cow =  helper.getFirstEntity(EntityType.COW, pos);
		helper.playerItemUseOn(fakePlayer,bucket,0,cow);

		helper.succeedIf(() -> {
			helper.assertItemInInventory(fakePlayer,new ItemStack(Items.MILK_BUCKET));
		});

	}
}
