package com.simibubi.create.infrastructure.gametest.tests;

import com.mojang.serialization.DynamicOps;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.content.schematics.cannon.LaunchedItem;
import com.simibubi.create.content.schematics.cannon.MaterialChecklist;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.CreateGameTests;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

@GameTestGroup(path = "fluids")
public class TestSchematicannonFluids {
	private static final String TEMPLATE = "small_waterwheel";

	@GameTest(template = TEMPLATE)
	public static void requirementsDistinguishFreeContainedAndFlowingFluid(CreateGameTestHelper helper) {
		ItemRequirement source = ItemRequirement.of(Blocks.WATER.defaultBlockState(), null);
		assertTrue(source.isPureFluid(), "Water source should have a pure fluid requirement");
		assertEquals(FluidType.BUCKET_VOLUME, source.getRequiredFluids()
			.getFirst()
			.amount(), "Water source should require one bucket");

		BlockState waterloggedFence = Blocks.OAK_FENCE.defaultBlockState()
			.setValue(BlockStateProperties.WATERLOGGED, true);
		ItemRequirement contained = ItemRequirement.of(waterloggedFence, null);
		assertEquals(1, contained.getRequiredItems()
			.size(), "Waterlogged fence should require its block item");
		assertEquals(1, contained.getRequiredFluids()
			.size(), "Waterlogged fence should require water");

		BlockState flowingWater = Blocks.WATER.defaultBlockState()
			.setValue(BlockStateProperties.LEVEL, 1);
		assertTrue(ItemRequirement.of(flowingWater, null)
			.isInvalid(), "Flowing fluid states should not be printed");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void drainsAcrossMultipleFluidHandlers(CreateGameTestHelper helper) {
		TestCannon cannon = new TestCannon();
		FluidTank first = tank(Fluids.WATER, 400);
		FluidTank second = tank(Fluids.WATER, 600);
		cannon.attachedFluidInventories.add(first);
		cannon.attachedFluidInventories.add(second);

		assertTrue(cannon.drain(Fluids.WATER, FluidType.BUCKET_VOLUME, true),
			"Combined tanks should satisfy simulation");
		assertTrue(cannon.drain(Fluids.WATER, FluidType.BUCKET_VOLUME, false),
			"Combined tanks should satisfy execution");
		assertEquals(0, first.getFluidAmount(), "First tank should be empty");
		assertEquals(0, second.getFluidAmount(), "Second tank should be empty");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void partiallyDrainsLargeFluidHandler(CreateGameTestHelper helper) {
		TestCannon cannon = new TestCannon();
		FluidTank tank = tank(Fluids.LAVA, 14000);
		cannon.attachedFluidInventories.add(tank);

		assertTrue(cannon.drain(Fluids.LAVA, 10000, false), "Large handler should support partial draining");
		assertEquals(4000, tank.getFluidAmount(), "Large handler should retain unused fluid");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void drainsCustomFluidFromTankWithoutBucket(CreateGameTestHelper helper) {
		TestCannon cannon = new TestCannon();
		var honey = AllFluids.HONEY.get()
			.getSource();
		FluidTank tank = tank(honey, 2500);
		cannon.attachedFluidInventories.add(tank);

		assertTrue(cannon.drain(honey, FluidType.BUCKET_VOLUME, false),
			"Custom fluid should be supplied by fluid handlers without item buckets");
		assertEquals(1500, tank.getFluidAmount(), "Custom fluid tank should retain unused fluid");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void fluidItemReturnsItsContainer(CreateGameTestHelper helper) {
		TestCannon cannon = new TestCannon();
		ItemStackHandler inventory = new ItemStackHandler(1);
		inventory.setStackInSlot(0, new ItemStack(Items.WATER_BUCKET));
		cannon.attachedInventories.add(inventory);

		assertTrue(cannon.drain(Fluids.WATER, FluidType.BUCKET_VOLUME, false),
			"Fluid item should satisfy the requirement");
		assertTrue(inventory.getStackInSlot(0)
			.is(Items.BUCKET), "Empty container should be returned");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void partiallyDrainsComplexFluidItemContainer(CreateGameTestHelper helper) {
		TestCannon cannon = new TestCannon();
		ItemStackHandler inventory = new ItemStackHandler(1);
		inventory.setStackInSlot(0, CreateGameTests.testFluidContainer(2500));
		cannon.attachedInventories.add(inventory);

		assertTrue(cannon.drain(Fluids.WATER, FluidType.BUCKET_VOLUME, false),
			"Complex fluid item should satisfy the requirement");
		ItemStack remaining = inventory.getStackInSlot(0);
		assertTrue(remaining.is(Items.DEBUG_STICK), "Complex container item should be returned");
		assertEquals(1500, CreateGameTests.testFluidContainerAmount(remaining),
			"Complex container should retain unused fluid");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void refusesFluidItemWhenContainerCannotBeReturned(CreateGameTestHelper helper) {
		TestCannon cannon = new TestCannon();
		ItemStackHandler extractionOnly = new ItemStackHandler(1) {
			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				return false;
			}
		};
		extractionOnly.setStackInSlot(0, new ItemStack(Items.WATER_BUCKET));
		cannon.attachedInventories.add(extractionOnly);

		assertTrue(!cannon.drain(Fluids.WATER, FluidType.BUCKET_VOLUME, false),
			"Drain should fail when the empty container has nowhere to go");
		assertTrue(extractionOnly.getStackInSlot(0)
			.is(Items.WATER_BUCKET), "Failed drain must leave the original item untouched");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void fluidProjectilePlacesAndUpdatesWaterloggedBlock(CreateGameTestHelper helper) {
		BlockPos target = helper.absolutePos(new BlockPos(1, 3, 1));
		LaunchedItem.ForBlockState launched = new LaunchedItem.ForBlockState(target, target, Items.OAK_FENCE
			.getDefaultInstance(), Blocks.OAK_FENCE.defaultBlockState()
			.setValue(BlockStateProperties.WATERLOGGED, true), null,
			new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME));
		launched.ticksRemaining = 0;
		launched.update(helper.getLevel());

		BlockState placed = helper.getLevel()
			.getBlockState(target);
		assertTrue(placed.is(Blocks.OAK_FENCE), "Projectile should place the containing block");
		assertTrue(placed.getValue(BlockStateProperties.WATERLOGGED), "Projectile should fill the placed block");
		assertTrue(helper.getLevel()
			.getFluidState(target)
			.isSource(), "Placed contained fluid should be a source");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void missingContainedFluidPlacesDryBlock(CreateGameTestHelper helper) {
		BlockPos target = helper.absolutePos(new BlockPos(1, 3, 1));
		LaunchedItem.ForBlockState launched = new LaunchedItem.ForBlockState(target, target, Items.OAK_FENCE
			.getDefaultInstance(), Blocks.OAK_FENCE.defaultBlockState()
			.setValue(BlockStateProperties.WATERLOGGED, true), null, FluidStack.EMPTY);
		launched.ticksRemaining = 0;
		launched.update(helper.getLevel());

		BlockState placed = helper.getLevel()
			.getBlockState(target);
		assertTrue(placed.is(Blocks.OAK_FENCE), "Dry fallback should still place the containing block");
		assertTrue(!placed.getValue(BlockStateProperties.WATERLOGGED), "Dry fallback must not create free water");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void freeFluidProjectilePlacesSource(CreateGameTestHelper helper) {
		BlockPos target = helper.absolutePos(new BlockPos(1, 3, 1));
		helper.getLevel()
			.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
		LaunchedItem.ForFluid launched =
			new LaunchedItem.ForFluid(target, target, new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME));
		launched.ticksRemaining = 0;
		launched.update(helper.getLevel());

		assertTrue(helper.getLevel()
			.getFluidState(target)
			.isSource(), "Free fluid projectile should place a source");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void customFluidProjectilePlacesSource(CreateGameTestHelper helper) {
		BlockPos target = helper.absolutePos(new BlockPos(1, 3, 1));
		var honey = AllFluids.HONEY.get()
			.getSource();
		helper.getLevel()
			.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
		LaunchedItem.ForFluid launched =
			new LaunchedItem.ForFluid(target, target, new FluidStack(honey, FluidType.BUCKET_VOLUME));
		launched.ticksRemaining = 0;
		launched.update(helper.getLevel());

		assertTrue(helper.getLevel()
			.getFluidState(target)
			.isSource(), "Custom fluid projectile should place a source");
		assertTrue(helper.getLevel()
			.getFluidState(target)
			.getType()
			.isSame(honey), "Custom fluid projectile should retain its fluid type");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void freeFluidProjectileSchedulesFlow(CreateGameTestHelper helper) {
		BlockPos target = helper.absolutePos(new BlockPos(1, 5, 1));
		BlockPos below = target.below();
		helper.getLevel()
			.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
		helper.getLevel()
			.setBlockAndUpdate(below, Blocks.AIR.defaultBlockState());
		LaunchedItem.ForFluid launched =
			new LaunchedItem.ForFluid(target, target, new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME));
		launched.ticksRemaining = 0;
		launched.update(helper.getLevel());

		helper.succeedWhen(() -> assertTrue(!helper.getLevel()
			.getFluidState(below)
			.isEmpty(), "Placed source should receive a fluid tick and flow"));
	}

	@GameTest(template = TEMPLATE)
	public static void freeFluidProjectileTriggersLavaWaterInteraction(CreateGameTestHelper helper) {
		BlockPos target = helper.absolutePos(new BlockPos(1, 3, 1));
		BlockPos water = target.north();
		helper.getLevel()
			.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
		helper.getLevel()
			.setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
		LaunchedItem.ForFluid launched =
			new LaunchedItem.ForFluid(target, target, new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME));
		launched.ticksRemaining = 0;
		launched.update(helper.getLevel());

		assertTrue(helper.getLevel()
			.getBlockState(target)
			.is(Blocks.OBSIDIAN), "Placed lava should receive neighbor updates and interact with adjacent water");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void freeFluidProjectileReplacesApprovedTarget(CreateGameTestHelper helper) {
		BlockPos target = helper.absolutePos(new BlockPos(1, 3, 1));
		helper.getLevel()
			.setBlockAndUpdate(target, Blocks.STONE.defaultBlockState());
		LaunchedItem.ForFluid launched =
			new LaunchedItem.ForFluid(target, target, new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME));
		launched.ticksRemaining = 0;
		launched.update(helper.getLevel());

		assertTrue(helper.getLevel()
			.getFluidState(target)
			.isSource(), "Approved replacement target should become a fluid source");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void waterVaporizesInTheNether(CreateGameTestHelper helper) {
		ServerLevel nether = helper.getLevel()
			.getServer()
			.getLevel(Level.NETHER);
		if (nether == null)
			throw new GameTestAssertException("Nether level is unavailable");

		BlockPos target = new BlockPos(0, 100, 0);
		nether.getChunkAt(target);
		nether.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
		LaunchedItem.ForFluid launched =
			new LaunchedItem.ForFluid(target, target, new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME));
		launched.ticksRemaining = 0;
		launched.update(nether);

		assertTrue(nether.getFluidState(target)
			.isEmpty(), "Water should vaporize instead of leaving a source in the Nether");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void waterloggedBlockStaysDryInTheNether(CreateGameTestHelper helper) {
		ServerLevel nether = helper.getLevel()
			.getServer()
			.getLevel(Level.NETHER);
		if (nether == null)
			throw new GameTestAssertException("Nether level is unavailable");

		BlockPos target = new BlockPos(0, 100, 0);
		nether.getChunkAt(target);
		nether.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
		LaunchedItem.ForBlockState launched = new LaunchedItem.ForBlockState(target, target, Items.OAK_FENCE
			.getDefaultInstance(), Blocks.OAK_FENCE.defaultBlockState()
			.setValue(BlockStateProperties.WATERLOGGED, true), null,
			new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME));
		launched.ticksRemaining = 0;
		launched.update(nether);

		BlockState placed = nether.getBlockState(target);
		assertTrue(placed.is(Blocks.OAK_FENCE), "Contained block should still be placed");
		assertTrue(!placed.getValue(BlockStateProperties.WATERLOGGED),
			"Waterlogged block should stay dry when water cannot be placed");
		assertTrue(nether.getFluidState(target)
			.isEmpty(), "Water should not remain in a Nether waterlogged block");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void fluidProjectileSurvivesSerialization(CreateGameTestHelper helper) {
		BlockPos target = helper.absolutePos(new BlockPos(1, 3, 1));
		LaunchedItem original =
			new LaunchedItem.ForFluid(target, target, new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME));
		LaunchedItem decoded = LaunchedItem.fromNBT(original.serializeNBT(helper.getLevel()
			.registryAccess()), helper.getLevel()
			.registryAccess(), helper.getLevel()
			.holderLookup(Registries.BLOCK));
		assertTrue(decoded instanceof LaunchedItem.ForFluid, "Serialized fluid projectile should retain its type");
		FluidStack fluid = ((LaunchedItem.ForFluid) decoded).fluid;
		assertTrue(fluid.is(Fluids.LAVA), "Serialized fluid projectile should retain its fluid");
		assertEquals(FluidType.BUCKET_VOLUME, fluid.getAmount(), "Serialized fluid projectile should retain its amount");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void checklistTracksFluidSeparately(CreateGameTestHelper helper) {
		MaterialChecklist checklist = new MaterialChecklist();
		BlockState waterloggedFence = Blocks.OAK_FENCE.defaultBlockState()
			.setValue(BlockStateProperties.WATERLOGGED, true);
		checklist.require(ItemRequirement.of(waterloggedFence, null));
		assertEquals(1, checklist.required.getInt(Items.OAK_FENCE), "Checklist should require the containing block");
		assertEquals(FluidType.BUCKET_VOLUME, checklist.requiredFluids.getInt(Fluids.WATER),
			"Checklist should track water independently");
		checklist.collect(new FluidStack(Fluids.WATER, 400));
		assertEquals(400, checklist.gatheredFluids.getInt(Fluids.WATER), "Checklist should collect available fluid");
		helper.succeedWhen(() -> {});
	}

	@GameTest(template = TEMPLATE)
	public static void oldOptionsDecodeWithFluidSkippingDisabled(CreateGameTestHelper helper) {
		CompoundTag oldOptions = new CompoundTag();
		oldOptions.putInt("replace_mode", 2);
		oldOptions.putBoolean("skip_missing", true);
		oldOptions.putBoolean("replace_block_entities", false);
		DynamicOps<Tag> ops = helper.getLevel()
			.registryAccess()
			.createSerializationContext(NbtOps.INSTANCE);
		SchematicannonBlockEntity.SchematicannonOptions options =
			SchematicannonBlockEntity.SchematicannonOptions.CODEC.parse(ops, oldOptions)
				.result()
				.orElseThrow(() -> new GameTestAssertException("Legacy options failed to decode"));
		assertTrue(!options.skipMissingFluid(), "Legacy options should default fluid skipping to false");
		helper.succeedWhen(() -> {});
	}

	private static FluidTank tank(Fluid fluid, int amount) {
		FluidTank tank = new FluidTank(amount);
		tank.setFluid(new FluidStack(fluid, amount));
		return tank;
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition)
			throw new GameTestAssertException(message);
	}

	private static void assertEquals(int expected, int actual, String message) {
		if (expected != actual)
			throw new GameTestAssertException(message + ": expected " + expected + ", got " + actual);
	}

	private static class TestCannon extends SchematicannonBlockEntity {
		TestCannon() {
			super(AllBlockEntityTypes.SCHEMATICANNON.get(), BlockPos.ZERO, AllBlocks.SCHEMATICANNON.getDefaultState());
		}

		boolean drain(Fluid fluid, int amount, boolean simulate) {
			return grabFluidFromAttachedInventories(fluid, amount, simulate);
		}
	}
}
