package com.simibubi.create.infrastructure.gametest.tests;

import java.util.concurrent.atomic.AtomicBoolean;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.schematics.SchematicExport;
import com.simibubi.create.content.schematics.SchematicItem;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import com.simibubi.create.foundation.utility.CreatePaths;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

@GameTestGroup(path = "misc")
public class TestSchematicannonFluidIntegration {
	@GameTest(template = "schematicannon", timeoutTicks = CreateGameTestHelper.TWENTY_SECONDS)
	public static void freeFluidWaitsForStructure(CreateGameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos sourceMin = helper.absolutePos(new BlockPos(5, 2, 1));
		BlockPos sourceMax = helper.absolutePos(new BlockPos(7, 2, 3));
		BlockPos sourceCenter = helper.absolutePos(new BlockPos(6, 2, 2));
		for (BlockPos pos : BlockPos.betweenClosed(sourceMin, sourceMax))
			level.setBlockAndUpdate(pos, pos.equals(sourceCenter) ? Blocks.WATER.defaultBlockState()
				: Blocks.STONE.defaultBlockState());

		String schematicName = "schematicannon_fluid_gametest";
		SchematicExport.saveSchematic(CreatePaths.UPLOADED_SCHEMATICS_DIR.resolve("FluidGameTest"), schematicName,
			true, level, sourceMin, sourceMax);
		ItemStack schematic = SchematicItem.create(level, schematicName + ".nbt", "FluidGameTest");
		BlockPos anchor = helper.absolutePos(new BlockPos(30, 2, 1));
		level.getChunkAt(anchor);
		schematic.set(AllDataComponents.SCHEMATIC_DEPLOYED, true);
		schematic.set(AllDataComponents.SCHEMATIC_ANCHOR, anchor);

		for (BlockPos pos : BlockPos.betweenClosed(anchor, anchor.offset(2, 0, 2)))
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

		SchematicannonBlockEntity cannon =
			helper.getBlockEntity(AllBlockEntityTypes.SCHEMATICANNON.get(), new BlockPos(3, 2, 6));
		cannon.inventory.setStackInSlot(0, schematic);
		cannon.state = State.RUNNING;
		cannon.statusMsg = "running";

		AtomicBoolean fluidArrivedBeforeStructure = new AtomicBoolean();
		AtomicBoolean persistedStageBarrier = new AtomicBoolean();
		helper.succeedWhen(() -> {
			BlockPos targetCenter = anchor.offset(1, 0, 1);
			if ("waitingForStructure".equals(cannon.statusMsg)) {
				var saved = cannon.saveWithFullMetadata(level.registryAccess());
				if (saved.getBoolean("WaitingForStageBarrier") && "FREE_FLUIDS".equals(saved.getCompound("Printer")
					.getString("PrintStage")))
					persistedStageBarrier.set(true);
			}
			if (level.getFluidState(targetCenter)
				.isSource()) {
				for (BlockPos pos : BlockPos.betweenClosed(anchor, anchor.offset(2, 0, 2))) {
					if (pos.equals(targetCenter))
						continue;
					if (!level.getBlockState(pos)
						.is(Blocks.STONE))
						fluidArrivedBeforeStructure.set(true);
				}
			}
			if (fluidArrivedBeforeStructure.get())
				helper.fail("Free fluid arrived before all containing blocks");
			if (cannon.state != State.STOPPED || !cannon.flyingBlocks.isEmpty())
				helper.fail("Schematicannon has not finished placing fluid structure");
			if (!persistedStageBarrier.get())
				helper.fail("Fluid stage barrier was not persisted while printing");
			if (!level.getFluidState(anchor.offset(1, 0, 1))
				.isSource())
				helper.fail("Schematicannon did not place the final water source");
		});
	}

	@GameTest(template = "schematicannon", timeoutTicks = CreateGameTestHelper.THIRTY_SECONDS)
	public static void missingContainedFluidPlacesDryBlockWithoutExtraConsumption(CreateGameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos source = helper.absolutePos(new BlockPos(5, 2, 1));
		level.setBlockAndUpdate(source, Blocks.OAK_FENCE.defaultBlockState()
			.setValue(BlockStateProperties.WATERLOGGED, true));

		String schematicName = "schematicannon_missing_contained_fluid_gametest";
		SchematicExport.saveSchematic(CreatePaths.UPLOADED_SCHEMATICS_DIR.resolve("FluidGameTest"), schematicName,
			true, level, source, source);
		ItemStack schematic = SchematicItem.create(level, schematicName + ".nbt", "FluidGameTest");
		BlockPos anchor = helper.absolutePos(new BlockPos(30, 2, 1));
		level.getChunkAt(anchor);
		schematic.set(AllDataComponents.SCHEMATIC_DEPLOYED, true);
		schematic.set(AllDataComponents.SCHEMATIC_ANCHOR, anchor);
		level.setBlockAndUpdate(anchor, Blocks.AIR.defaultBlockState());

		SchematicannonBlockEntity cannon =
			helper.getBlockEntity(AllBlockEntityTypes.SCHEMATICANNON.get(), new BlockPos(3, 2, 6));
		clearAdjacentInventories(level, cannon);
		ChestBlockEntity chest = placeSupplyChest(helper, new ItemStack(Items.OAK_FENCE));
		cannon.inventory.setStackInSlot(0, schematic);
		cannon.inventory.setStackInSlot(4, new ItemStack(Items.GUNPOWDER));
		cannon.findInventories();
		cannon.skipMissingFluid = true;
		cannon.remainingFuel = cannon.getShotsPerGunpowder();
		cannon.state = State.RUNNING;
		cannon.statusMsg = "running";
		helper.runAfterDelay(5, () -> {
			cannon.state = State.RUNNING;
			cannon.statusMsg = "running";
		});

		helper.succeedWhen(() -> {
			if (cannon.state != State.STOPPED || !cannon.flyingBlocks.isEmpty())
				helper.fail("Schematicannon has not finished placing dry fallback; state=%s status=%s flying=%s fuel=%s"
					.formatted(cannon.state, cannon.statusMsg, cannon.flyingBlocks.size(), cannon.remainingFuel));
			if (!level.getBlockState(anchor)
				.is(Blocks.OAK_FENCE))
				helper.fail("Missing contained fluid should still place the containing block");
			if (level.getBlockState(anchor)
				.getValue(BlockStateProperties.WATERLOGGED))
				helper.fail("Missing contained fluid should not create a waterlogged state");
			if (!level.getFluidState(anchor)
				.isEmpty())
				helper.fail("Missing contained fluid should not create water");
			if (!chest.getItem(0)
				.isEmpty())
				helper.fail("Dry fallback should consume exactly one containing block item");
		});
	}

	@GameTest(template = "schematicannon", timeoutTicks = CreateGameTestHelper.THIRTY_SECONDS)
	public static void missingFluidForExistingDryWaterloggableBlockDoesNotConsumeBlock(CreateGameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos source = helper.absolutePos(new BlockPos(5, 2, 1));
		level.setBlockAndUpdate(source, Blocks.OAK_FENCE.defaultBlockState()
			.setValue(BlockStateProperties.WATERLOGGED, true));

		String schematicName = "schematicannon_existing_dry_waterloggable_gametest";
		SchematicExport.saveSchematic(CreatePaths.UPLOADED_SCHEMATICS_DIR.resolve("FluidGameTest"), schematicName,
			true, level, source, source);
		ItemStack schematic = SchematicItem.create(level, schematicName + ".nbt", "FluidGameTest");
		BlockPos anchor = helper.absolutePos(new BlockPos(30, 2, 1));
		level.getChunkAt(anchor);
		schematic.set(AllDataComponents.SCHEMATIC_DEPLOYED, true);
		schematic.set(AllDataComponents.SCHEMATIC_ANCHOR, anchor);
		level.setBlockAndUpdate(anchor, Blocks.OAK_FENCE.defaultBlockState());

		SchematicannonBlockEntity cannon =
			helper.getBlockEntity(AllBlockEntityTypes.SCHEMATICANNON.get(), new BlockPos(3, 2, 6));
		clearAdjacentInventories(level, cannon);
		ChestBlockEntity chest = placeSupplyChest(helper, new ItemStack(Items.OAK_FENCE));
		cannon.inventory.setStackInSlot(0, schematic);
		cannon.inventory.setStackInSlot(4, new ItemStack(Items.GUNPOWDER));
		cannon.findInventories();
		cannon.skipMissingFluid = true;
		cannon.remainingFuel = cannon.getShotsPerGunpowder();
		cannon.state = State.RUNNING;
		cannon.statusMsg = "running";
		helper.runAfterDelay(5, () -> {
			cannon.state = State.RUNNING;
			cannon.statusMsg = "running";
		});

		helper.succeedWhen(() -> {
			if (cannon.state != State.STOPPED || !cannon.flyingBlocks.isEmpty())
				helper.fail("Schematicannon has not finished skipping missing contained fluid; state=%s status=%s flying=%s fuel=%s"
					.formatted(cannon.state, cannon.statusMsg, cannon.flyingBlocks.size(), cannon.remainingFuel));
			if (!level.getBlockState(anchor)
				.is(Blocks.OAK_FENCE))
				helper.fail("Existing dry block should remain in place");
			if (level.getBlockState(anchor)
				.getValue(BlockStateProperties.WATERLOGGED))
				helper.fail("Existing dry block should not become waterlogged without fluid");
			if (!level.getFluidState(anchor)
				.isEmpty())
				helper.fail("Existing dry block should not receive water without fluid");
			if (chest.getItem(0)
				.getCount() != 1)
				helper.fail("Skipping missing fluid for an existing dry block must not consume the base block");
		});
	}

	@GameTest(template = "schematicannon", timeoutTicks = CreateGameTestHelper.THIRTY_SECONDS)
	public static void mixedFluidSchematicPrintsLavaAndWaterloggedBlock(CreateGameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos sourceMin = helper.absolutePos(new BlockPos(5, 2, 1));
		BlockPos lavaSource = sourceMin;
		BlockPos stoneSource = sourceMin.east();
		BlockPos waterloggedSource = sourceMin.east(2);
		level.setBlockAndUpdate(lavaSource, Blocks.LAVA.defaultBlockState());
		level.setBlockAndUpdate(stoneSource, Blocks.STONE.defaultBlockState());
		level.setBlockAndUpdate(waterloggedSource, Blocks.OAK_FENCE.defaultBlockState()
			.setValue(BlockStateProperties.WATERLOGGED, true));

		String schematicName = "schematicannon_mixed_fluid_gametest";
		SchematicExport.saveSchematic(CreatePaths.UPLOADED_SCHEMATICS_DIR.resolve("FluidGameTest"), schematicName,
			true, level, sourceMin, waterloggedSource);
		ItemStack schematic = SchematicItem.create(level, schematicName + ".nbt", "FluidGameTest");
		BlockPos anchor = helper.absolutePos(new BlockPos(30, 2, 1));
		level.getChunkAt(anchor);
		schematic.set(AllDataComponents.SCHEMATIC_DEPLOYED, true);
		schematic.set(AllDataComponents.SCHEMATIC_ANCHOR, anchor);
		for (BlockPos pos : BlockPos.betweenClosed(anchor, anchor.east(2)))
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

		SchematicannonBlockEntity cannon =
			helper.getBlockEntity(AllBlockEntityTypes.SCHEMATICANNON.get(), new BlockPos(3, 2, 6));
		clearAdjacentInventories(level, cannon);
		ChestBlockEntity chest = placeSupplyChest(helper, new ItemStack(Items.STONE));
		chest.setItem(1, new ItemStack(Items.OAK_FENCE));
		chest.setItem(2, new ItemStack(Items.LAVA_BUCKET));
		chest.setItem(3, new ItemStack(Items.WATER_BUCKET));
		cannon.inventory.setStackInSlot(0, schematic);
		cannon.inventory.setStackInSlot(4, new ItemStack(Items.GUNPOWDER, 4));
		cannon.findInventories();
		cannon.remainingFuel = cannon.getShotsPerGunpowder();
		cannon.state = State.RUNNING;
		cannon.statusMsg = "running";
		helper.runAfterDelay(5, () -> {
			cannon.state = State.RUNNING;
			cannon.statusMsg = "running";
		});

		helper.succeedWhen(() -> {
			if (cannon.state != State.STOPPED || !cannon.flyingBlocks.isEmpty())
				helper.fail("Schematicannon has not finished mixed fluid schematic; state=%s status=%s flying=%s"
					.formatted(cannon.state, cannon.statusMsg, cannon.flyingBlocks.size()));
			if (!level.getFluidState(anchor)
				.isSource() || !level.getFluidState(anchor)
					.is(Fluids.LAVA))
				helper.fail("Schematicannon did not place the lava source");
			if (!level.getBlockState(anchor.east())
				.is(Blocks.STONE))
				helper.fail("Schematicannon did not place the structure block between fluids");
			if (!level.getBlockState(anchor.east(2))
				.is(Blocks.OAK_FENCE))
				helper.fail("Schematicannon did not place the waterloggable block");
			if (!level.getBlockState(anchor.east(2))
				.getValue(BlockStateProperties.WATERLOGGED))
				helper.fail("Schematicannon did not fill the waterlogged block");
			if (!level.getFluidState(anchor.east(2))
				.isSource() || !level.getFluidState(anchor.east(2))
					.is(Fluids.WATER))
				helper.fail("Schematicannon did not place water inside the waterlogged block");
		});
	}

	private static void clearAdjacentInventories(ServerLevel level, SchematicannonBlockEntity cannon) {
		for (Direction direction : Direction.values())
			level.setBlockAndUpdate(cannon.getBlockPos()
				.relative(direction), Blocks.AIR.defaultBlockState());
		cannon.findInventories();
	}

	private static ChestBlockEntity placeSupplyChest(CreateGameTestHelper helper, ItemStack stack) {
		BlockPos chestPos = new BlockPos(2, 2, 6);
		helper.setBlock(chestPos, Blocks.CHEST.defaultBlockState());
		ChestBlockEntity chest = helper.getBlockEntity(BlockEntityType.CHEST, chestPos);
		chest.setItem(0, stack);
		return chest;
	}
}
