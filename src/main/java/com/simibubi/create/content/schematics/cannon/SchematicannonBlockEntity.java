package com.simibubi.create.content.schematics.cannon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.SchematicPrinter.PrintStage;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.FluidRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.ItemHelper.ExtractionCountMode;
import com.simibubi.create.foundation.mixin.accessor.ItemStackHandlerAccessor;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CSchematics;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Clearable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;

public class SchematicannonBlockEntity extends SmartBlockEntity implements MenuProvider, Clearable {
	public static final int NEIGHBOUR_CHECKING = 100;
	public static final int MAX_ANCHOR_DISTANCE = 256;

	// Inventory
	public SchematicannonInventory inventory;

	public boolean sendUpdate;
	// Sync
	public boolean dontUpdateChecklist;
	public int neighbourCheckCooldown;

	// Printer
	public SchematicPrinter printer;
	public ItemStack missingItem;
	public FluidStack missingFluid;
	public boolean positionNotLoaded;
	public boolean hasCreativeCrate;
	private int printerCooldown;
	private int skipsLeft;
	private boolean blockSkipped;
	private boolean waitingForStageBarrier;

	public BlockPos previousTarget;
	public LinkedHashSet<IItemHandler> attachedInventories;
	public LinkedHashSet<IFluidHandler> attachedFluidInventories;
	public List<LaunchedItem> flyingBlocks;
	public MaterialChecklist checklist;

	// Gui information
	public int remainingFuel;
	public float bookPrintingProgress;
	public float schematicProgress;
	public String statusMsg;
	public State state;
	public int blocksPlaced;
	public int blocksToPlace;

	// Settings
	public int replaceMode;
	public boolean skipMissing;
	public boolean skipMissingFluid;
	public boolean replaceBlockEntities;

	// Render
	public boolean firstRenderTick;
	public float defaultYaw;

	public SchematicannonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		setLazyTickRate(30);
		attachedInventories = new LinkedHashSet<>();
		attachedFluidInventories = new LinkedHashSet<>();
		flyingBlocks = new LinkedList<>();
		inventory = new SchematicannonInventory(this);
		statusMsg = "idle";
		this.state = State.STOPPED;
		replaceMode = 2;
		checklist = new MaterialChecklist();
		printer = new SchematicPrinter();
	}

	public void findInventories() {
		hasCreativeCrate = false;
		attachedInventories.clear();
		attachedFluidInventories.clear();
		for (Direction facing : Iterate.directions) {

			if (!level.isLoaded(worldPosition.relative(facing)))
				continue;

			if (AllBlocks.CREATIVE_CRATE.has(level.getBlockState(worldPosition.relative(facing))))
				hasCreativeCrate = true;

			BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(facing));
			if (blockEntity != null) {
				IItemHandler capability =
					level.getCapability(Capabilities.ItemHandler.BLOCK, blockEntity.getBlockPos(), facing.getOpposite());
				if (capability != null) {
					attachedInventories.add(capability);
				}
			}
			IFluidHandler fluidCapability = level.getCapability(Capabilities.FluidHandler.BLOCK,
				worldPosition.relative(facing), facing.getOpposite());
			if (fluidCapability != null)
				attachedFluidInventories.add(fluidCapability);
		}
	}

	@Override
	public void clearContent() {
		((ItemStackHandlerAccessor) inventory).create$getStacks().clear();
	}

	@Override
	protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		if (!clientPacket) {
			inventory.deserializeNBT(registries, compound.getCompound("Inventory"));
		}

		// Gui information
		statusMsg = compound.getString("Status");
		schematicProgress = compound.getFloat("Progress");
		bookPrintingProgress = compound.getFloat("PaperProgress");
		remainingFuel = compound.getInt("RemainingFuel");
		String stateString = compound.getString("State");
		state = stateString.isEmpty() ? State.STOPPED : State.valueOf(compound.getString("State"));
		blocksPlaced = compound.getInt("AmountPlaced");
		blocksToPlace = compound.getInt("AmountToPlace");

		missingItem = null;
		if (compound.contains("MissingItem")) {
			ItemStack.parse(registries, compound.getCompound("MissingItem")).ifPresent(i -> missingItem = i);
		}
		missingFluid = FluidStack.parseOptional(registries, compound.getCompound("MissingFluid"));
		waitingForStageBarrier = compound.getBoolean("WaitingForStageBarrier");

		// Settings
		SchematicannonOptions options = CatnipCodecUtils.decode(SchematicannonOptions.CODEC, registries, compound.getCompound("Options"))
			.orElse(new SchematicannonOptions(2, false, false, false));
		replaceMode = options.replaceMode;
		skipMissing = options.skipMissing;
		skipMissingFluid = options.skipMissingFluid;
		replaceBlockEntities = options.replaceBlockEntities;

		// Printer & Flying Blocks
		if (compound.contains("Printer"))
			printer.fromTag(compound.getCompound("Printer"), clientPacket);
		if (compound.contains("FlyingBlocks"))
			readFlyingBlocks(compound, registries);

		defaultYaw = compound.getFloat("DefaultYaw");

		super.read(compound, registries, clientPacket);
	}

	protected void readFlyingBlocks(CompoundTag compound, HolderLookup.Provider registries) {
		ListTag tagBlocks = compound.getList("FlyingBlocks", 10);
		if (tagBlocks.isEmpty())
			flyingBlocks.clear();

		boolean pastDead = false;

		for (int i = 0; i < tagBlocks.size(); i++) {
			CompoundTag c = tagBlocks.getCompound(i);
			LaunchedItem launched = LaunchedItem.fromNBT(c, registries, blockHolderGetter());
			BlockPos readBlockPos = launched.target;

			// Always write to Server block entity
			if (level == null || !level.isClientSide) {
				flyingBlocks.add(launched);
				continue;
			}

			// Delete all Client side blocks that are now missing on the server
			while (!pastDead && !flyingBlocks.isEmpty() && !flyingBlocks.get(0).target.equals(readBlockPos)) {
				flyingBlocks.remove(0);
			}

			pastDead = true;

			// Add new server side blocks
			if (i >= flyingBlocks.size()) {
				flyingBlocks.add(launched);
				continue;
			}

			// Don't do anything with existing
		}
	}

	@Override
	public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		if (!clientPacket) {
			compound.put("Inventory", inventory.serializeNBT(registries));
			if (state == State.RUNNING) {
				compound.putBoolean("Running", true);
			}
		}

		// Gui information
		compound.putFloat("Progress", schematicProgress);
		compound.putFloat("PaperProgress", bookPrintingProgress);
		compound.putInt("RemainingFuel", remainingFuel);
		compound.putString("Status", statusMsg);
		compound.putString("State", state.name());
		compound.putInt("AmountPlaced", blocksPlaced);
		compound.putInt("AmountToPlace", blocksToPlace);

		if (missingItem != null)
			compound.put("MissingItem", missingItem.saveOptional(registries));
		if (missingFluid != null && !missingFluid.isEmpty())
			compound.put("MissingFluid", missingFluid.saveOptional(registries));
		compound.putBoolean("WaitingForStageBarrier", waitingForStageBarrier);

		// Settings
		Tag options = CatnipCodecUtils.encode(SchematicannonOptions.CODEC, registries,
			new SchematicannonOptions(replaceMode, skipMissing, skipMissingFluid, replaceBlockEntities)).orElseThrow();
		compound.put("Options", options);

		// Printer & Flying Blocks
		CompoundTag printerData = new CompoundTag();
		printer.write(printerData);
		compound.put("Printer", printerData);

		ListTag tagFlyingBlocks = new ListTag();
		for (LaunchedItem b : flyingBlocks)
			tagFlyingBlocks.add(b.serializeNBT(registries));
		compound.put("FlyingBlocks", tagFlyingBlocks);

		compound.putFloat("DefaultYaw", defaultYaw);

		super.write(compound, registries, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();

		if (state != State.STOPPED && neighbourCheckCooldown-- <= 0) {
			neighbourCheckCooldown = NEIGHBOUR_CHECKING;
			findInventories();
		}

		firstRenderTick = true;
		previousTarget = printer.getCurrentTarget();
		tickFlyingBlocks();

		if (level.isClientSide)
			return;

		// Update Fuel and Paper
		tickPaperPrinter();
		refillFuelIfPossible();

		// Update Printer
		skipsLeft = 1000;
		blockSkipped = true;

		while (blockSkipped && skipsLeft-- > 0)
			tickPrinter();

		schematicProgress = 0;
		if (blocksToPlace > 0)
			schematicProgress = (float) blocksPlaced / blocksToPlace;

		// Update Client block entity
		if (sendUpdate) {
			sendUpdate = false;
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 6);
		}
	}

	public CSchematics config() {
		return AllConfigs.server().schematics;
	}

	protected void tickPrinter() {
		ItemStack blueprint = inventory.getStackInSlot(0);
		blockSkipped = false;

		if (blueprint.isEmpty() && !statusMsg.equals("idle") && inventory.getStackInSlot(1)
			.isEmpty()) {
			state = State.STOPPED;
			statusMsg = "idle";
			sendUpdate = true;
			return;
		}

		// Skip if not Active
		if (state == State.STOPPED) {
			if (printer.isLoaded())
				resetPrinter();
			return;
		}

		if (state == State.PAUSED && !positionNotLoaded && missingItem == null
			&& (missingFluid == null || missingFluid.isEmpty()) && remainingFuel > 0)
			return;

		// Initialize Printer
		if (!printer.isLoaded()) {
			initializePrinter(blueprint);
			return;
		}

		// Cooldown from last shot
		if (printerCooldown > 0) {
			printerCooldown--;
			return;
		}

		// Check Fuel
		if (remainingFuel <= 0 && !hasCreativeCrate) {
			refillFuelIfPossible();
			if (remainingFuel <= 0) {
				state = State.PAUSED;
				statusMsg = "noGunpowder";
				sendUpdate = true;
				return;
			}
		}

		if (hasCreativeCrate) {
			remainingFuel = 0;
			if (missingItem != null || missingFluid != null && !missingFluid.isEmpty()) {
				missingItem = null;
				missingFluid = FluidStack.EMPTY;
				state = State.RUNNING;
			}
		}

		// Update Target
		if (waitingForStageBarrier) {
			if (!flyingBlocks.isEmpty()) {
				statusMsg = "waitingForStructure";
				return;
			}
			waitingForStageBarrier = false;
		} else if (missingItem == null && (missingFluid == null || missingFluid.isEmpty()) && !positionNotLoaded) {
			PrintStage previousStage = printer.getPrintStage();
			if (!printer.advanceCurrentPos()) {
				finishedPrinting();
				return;
			}
			if (previousStage != printer.getPrintStage() && printer.getPrintStage() == PrintStage.FREE_FLUIDS
				&& !flyingBlocks.isEmpty()) {
				waitingForStageBarrier = true;
				statusMsg = "waitingForStructure";
				sendUpdate = true;
				return;
			}
			sendUpdate = true;
		}

		// Check block
		if (!getLevel().isLoaded(printer.getCurrentTarget())) {
			positionNotLoaded = true;
			statusMsg = "targetNotLoaded";
			state = State.PAUSED;
			return;
		} else {
			if (positionNotLoaded) {
				positionNotLoaded = false;
				state = State.RUNNING;
			}
		}

		// Get item requirement
		ItemRequirement requirement = printer.getCurrentRequirement(level);
		if (requirement.isInvalid() || !printer.shouldPlaceCurrent(level, this::shouldPlace)) {
			sendUpdate = !statusMsg.equals("searching");
			statusMsg = "searching";
			blockSkipped = true;
			return;
		}

		// Find resources
		List<ItemRequirement.StackRequirement> requiredItems = requirement.getRequiredItems();
		List<FluidRequirement> requiredFluids = requirement.getRequiredFluids();
		Map<Fluid, Integer> fluidAmounts = new LinkedHashMap<>();
		for (FluidRequirement required : requiredFluids)
			fluidAmounts.merge(required.fluid(), required.amount(), Integer::sum);
		boolean provideFluid = true;
		for (Map.Entry<Fluid, Integer> required : fluidAmounts.entrySet()) {
			FluidStack stack = new FluidStack(required.getKey(), required.getValue());
			if (required.getKey()
				.getFluidType()
				.canBePlacedInLevel(level, printer.getCurrentTarget(), stack))
				continue;
			if (requirement.isPureFluid()) {
				statusMsg = "skippingFluid";
				blockSkipped = true;
				return;
			}
			provideFluid = false;
		}
		if (!requirement.isEmpty()) {
			ItemRequirement.StackRequirement missingItemRequirement = null;
			for (ItemRequirement.StackRequirement required : requiredItems) {
				if (!grabItemsFromAttachedInventories(required, true)) {
					missingItemRequirement = required;
					break;
				}
			}

			if (missingItemRequirement != null) {
				if (skipMissing) {
					statusMsg = "skipping";
					blockSkipped = true;
					resetMissingState();
					return;
				}
				missingItem = missingItemRequirement.stack;
				missingFluid = FluidStack.EMPTY;
				state = State.PAUSED;
				statusMsg = "missingBlock";
				return;
			}

			FluidRequirement missingFluidRequirement = null;
			if (provideFluid) {
				for (Map.Entry<Fluid, Integer> required : fluidAmounts.entrySet()) {
					if (!grabFluidFromAttachedInventories(required.getKey(), required.getValue(), true)) {
						missingFluidRequirement = new FluidRequirement(required.getKey(), required.getValue());
						break;
					}
				}
			}
			if (missingFluidRequirement != null) {
				if (!skipMissingFluid) {
					missingItem = null;
					missingFluid =
						new FluidStack(missingFluidRequirement.fluid(), missingFluidRequirement.amount());
					state = State.PAUSED;
					statusMsg = "missingFluid";
					return;
				}
				if (requirement.isPureFluid()) {
					statusMsg = "skippingFluid";
					blockSkipped = true;
					resetMissingState();
					return;
				}
				provideFluid = false;
			}

			for (ItemRequirement.StackRequirement required : requiredItems)
				grabItemsFromAttachedInventories(required, false);
			if (provideFluid) {
				for (Map.Entry<Fluid, Integer> required : fluidAmounts.entrySet()) {
					if (grabFluidFromAttachedInventories(required.getKey(), required.getValue(), false))
						continue;
					missingFluid = new FluidStack(required.getKey(), required.getValue());
					state = State.PAUSED;
					statusMsg = "missingFluid";
					return;
				}
			}
		}

		// Success
		state = State.RUNNING;
		ItemStack icon = requirement.isEmpty() || requiredItems.isEmpty() ? ItemStack.EMPTY : requiredItems.get(0).stack;
		FluidStack fluidPayload = FluidStack.EMPTY;
		if (provideFluid && !fluidAmounts.isEmpty()) {
			Map.Entry<Fluid, Integer> firstFluid = fluidAmounts.entrySet()
				.iterator()
				.next();
			fluidPayload = new FluidStack(firstFluid.getKey(), firstFluid.getValue());
		}
		FluidStack launchedFluid = fluidPayload;
		printer.handleCurrentTarget((target, blockState, blockEntity) -> {
			// Launch block
			statusMsg = blockState.getBlock() != Blocks.AIR ? "placing" : "clearing";
			if (printer.getPrintStage() == PrintStage.FREE_FLUIDS)
				launchFluid(target, launchedFluid);
			else
				launchBlockOrBelt(target, icon, blockState, blockEntity, launchedFluid);
		}, (target, entity) -> {
			// Launch entity
			statusMsg = "placing";
			launchEntity(target, icon, entity);
		});

		printerCooldown = config().schematicannonDelay.get();
		remainingFuel -= 1;
		sendUpdate = true;
		missingItem = null;
		missingFluid = FluidStack.EMPTY;
	}

	private void resetMissingState() {
		if (missingItem != null || missingFluid != null && !missingFluid.isEmpty()) {
			missingItem = null;
			missingFluid = FluidStack.EMPTY;
			state = State.RUNNING;
		}
	}

	public int getShotsPerGunpowder() {
		return hasCreativeCrate ? 0 : config().schematicannonShotsPerGunpowder.get();
	}

	protected void initializePrinter(ItemStack blueprint) {
		if (!blueprint.has(AllDataComponents.SCHEMATIC_ANCHOR)) {
			state = State.STOPPED;
			statusMsg = "schematicInvalid";
			sendUpdate = true;
			return;
		}

		if (!blueprint.getOrDefault(AllDataComponents.SCHEMATIC_DEPLOYED, false)) {
			state = State.STOPPED;
			statusMsg = "schematicNotPlaced";
			sendUpdate = true;
			return;
		}

		// Load blocks into reader
		printer.loadSchematic(blueprint, level, true);

		if (printer.isErrored()) {
			state = State.STOPPED;
			statusMsg = "schematicErrored";
			inventory.setStackInSlot(0, ItemStack.EMPTY);
			inventory.setStackInSlot(1, new ItemStack(AllItems.EMPTY_SCHEMATIC.get()));
			printer.resetSchematic();
			sendUpdate = true;
			return;
		}

		if (printer.isWorldEmpty()) {
			state = State.STOPPED;
			statusMsg = "schematicExpired";
			inventory.setStackInSlot(0, ItemStack.EMPTY);
			inventory.setStackInSlot(1, new ItemStack(AllItems.EMPTY_SCHEMATIC.get()));
			printer.resetSchematic();
			sendUpdate = true;
			return;
		}

		if (!printer.getAnchor()
			.closerThan(getBlockPos(), MAX_ANCHOR_DISTANCE)) {
			state = State.STOPPED;
			statusMsg = "targetOutsideRange";
			printer.resetSchematic();
			sendUpdate = true;
			return;
		}

		state = State.PAUSED;
		statusMsg = "ready";
		updateChecklist();
		sendUpdate = true;
		blocksToPlace += blocksPlaced;
	}

	protected ItemStack getItemForBlock(BlockState blockState) {
		Item item = BlockItem.BY_BLOCK.getOrDefault(blockState.getBlock(), Items.AIR);
		return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
	}

	protected boolean grabItemsFromAttachedInventories(ItemRequirement.StackRequirement required, boolean simulate) {
		if (hasCreativeCrate)
			return true;

		attachedInventories.removeIf(Objects::isNull);

		ItemUseType usage = required.usage;

		// Find and apply damage
		if (usage == ItemUseType.DAMAGE) {
			for (IItemHandler cap : attachedInventories) {
				if (cap == null)
					cap = EmptyItemHandler.INSTANCE;
				for (int slot = 0; slot < cap.getSlots(); slot++) {
					ItemStack extractItem = cap.extractItem(slot, 1, true);
					if (!required.matches(extractItem))
						continue;
					if (!extractItem.isDamageableItem())
						continue;

					if (!simulate) {
						ItemStack stack = cap.extractItem(slot, 1, false);
						stack.setDamageValue(stack.getDamageValue() + 1);
						if (stack.getDamageValue() <= stack.getMaxDamage()) {
							if (cap.getStackInSlot(slot)
								.isEmpty())
								cap.insertItem(slot, stack, false);
							else
								ItemHandlerHelper.insertItem(cap, stack, false);
						}
					}

					return true;
				}
			}

			return false;
		}

		// Find and remove
		boolean success = false;
		int amountFound = 0;
		for (IItemHandler cap : attachedInventories) {
			if (cap == null)
				cap = EmptyItemHandler.INSTANCE;
			amountFound += ItemHelper
				.extract(cap, required::matches, ExtractionCountMode.UPTO,
					required.stack.getCount(), true)
				.getCount();

			if (amountFound < required.stack.getCount())
				continue;

			success = true;
			break;
		}

		if (!simulate && success) {
			amountFound = 0;
			for (IItemHandler cap : attachedInventories) {
				if (cap == null)
					cap = EmptyItemHandler.INSTANCE;
				amountFound += ItemHelper
					.extract(cap, required::matches, ExtractionCountMode.UPTO,
						required.stack.getCount(), false)
					.getCount();
				if (amountFound < required.stack.getCount())
					continue;
				break;
			}
		}

		return success;
	}

	protected boolean grabFluidFromAttachedInventories(Fluid fluid, int amount, boolean simulate) {
		if (hasCreativeCrate)
			return true;
		FluidDrainPlan plan = planFluidDrain(fluid, amount);
		if (plan == null)
			return false;
		if (simulate)
			return true;
		return executeFluidDrainPlan(plan);
	}

	@Nullable
	private FluidDrainPlan planFluidDrain(Fluid fluid, int amount) {
		attachedFluidInventories.removeIf(Objects::isNull);
		attachedInventories.removeIf(Objects::isNull);
		int remaining = amount;
		List<TankDrain> tankDrains = new ArrayList<>();
		List<ItemDrain> itemDrains = new ArrayList<>();

		for (IFluidHandler handler : attachedFluidInventories) {
			for (int tank = 0; tank < handler.getTanks(); tank++) {
				FluidStack stored = handler.getFluidInTank(tank);
				if (stored.isEmpty() || !stored.getFluid()
					.isSame(fluid))
					continue;
				FluidStack request = stored.copyWithAmount(Math.min(remaining, stored.getAmount()));
				FluidStack drained = handler.drain(request, FluidAction.SIMULATE);
				if (drained.isEmpty() || !FluidStack.isSameFluidSameComponents(request, drained))
					continue;
				FluidStack plannedDrain = drained.copyWithAmount(Math.min(remaining, drained.getAmount()));
				tankDrains.add(new TankDrain(handler, plannedDrain));
				remaining -= plannedDrain.getAmount();
				if (remaining == 0)
					return new FluidDrainPlan(tankDrains, itemDrains);
			}
		}

		for (IItemHandler inventory : attachedInventories) {
			for (int slot = 0; slot < inventory.getSlots(); slot++) {
				ItemStack inSlot = inventory.getStackInSlot(slot);
				ItemStack extracted = inventory.extractItem(slot, 1, true);
				if (extracted.isEmpty())
					continue;
				Optional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(extracted.copy());
				if (fluidHandler.isEmpty())
					continue;
				FluidStack available = fluidHandler.get()
					.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
				if (available.isEmpty() || !available.getFluid()
					.isSame(fluid))
					continue;
				FluidStack request = available.copyWithAmount(Math.min(remaining, available.getAmount()));
				FluidStack drained = fluidHandler.get()
					.drain(request, FluidAction.SIMULATE);
				if (drained.isEmpty() || !drained.getFluid()
					.isSame(fluid))
					continue;
				int drainAmount = Math.min(remaining, drained.getAmount());
				fluidHandler.get()
					.drain(drained.copyWithAmount(drainAmount), FluidAction.EXECUTE);
				ItemStack resultContainer = fluidHandler.get()
					.getContainer();
				if (!canReturnContainer(inventory, slot, inSlot.getCount(), resultContainer))
					continue;
				itemDrains.add(new ItemDrain(inventory, slot, drained.copyWithAmount(drainAmount)));
				remaining -= drainAmount;
				if (remaining == 0)
					return new FluidDrainPlan(tankDrains, itemDrains);
			}
		}

		return null;
	}

	private boolean canReturnContainer(IItemHandler source, int sourceSlot, int sourceCount, ItemStack container) {
		if (container.isEmpty())
			return true;
		if (sourceCount == 1 && source.isItemValid(sourceSlot, container)
			&& container.getCount() <= source.getSlotLimit(sourceSlot))
			return true;
		for (IItemHandler inventory : attachedInventories)
			if (ItemHandlerHelper.insertItem(inventory, container.copy(), true)
				.isEmpty())
				return true;
		return false;
	}

	private boolean executeFluidDrainPlan(FluidDrainPlan plan) {
		for (TankDrain planned : plan.tanks()) {
			FluidStack drained = planned.handler()
				.drain(planned.fluid(), FluidAction.EXECUTE);
			if (drained.getAmount() != planned.fluid()
				.getAmount() || !FluidStack.isSameFluidSameComponents(drained, planned.fluid()))
				return false;
		}
		for (ItemDrain planned : plan.items()) {
			ItemStack extracted = planned.inventory()
				.extractItem(planned.slot(), 1, false);
			Optional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(extracted);
			if (fluidHandler.isEmpty())
				return false;
			FluidStack drained = fluidHandler.get()
				.drain(planned.fluid(), FluidAction.EXECUTE);
			ItemStack resultContainer = fluidHandler.get()
				.getContainer();
			if (drained.getAmount() != planned.fluid()
				.getAmount() || !FluidStack.isSameFluidSameComponents(drained, planned.fluid())) {
				insertOrEjectContainer(planned.inventory(), extracted);
				return false;
			}
			insertOrEjectContainer(planned.inventory(), resultContainer);
		}
		return true;
	}

	private void insertOrEjectContainer(IItemHandler preferredInventory, ItemStack container) {
		if (container.isEmpty())
			return;
		ItemStack remainder = ItemHandlerHelper.insertItem(preferredInventory, container, false);
		for (IItemHandler inventory : attachedInventories) {
			if (remainder.isEmpty())
				return;
			if (inventory == preferredInventory)
				continue;
			remainder = ItemHandlerHelper.insertItem(inventory, remainder, false);
		}
		if (!remainder.isEmpty())
			Block.popResource(level, worldPosition.above(), remainder);
	}

	private record FluidDrainPlan(List<TankDrain> tanks, List<ItemDrain> items) {}

	private record TankDrain(IFluidHandler handler, FluidStack fluid) {}

	private record ItemDrain(IItemHandler inventory, int slot, FluidStack fluid) {}

	public void finishedPrinting() {
		if (replaceMode == ConfigureSchematicannonPacket.Option.REPLACE_EMPTY.ordinal())
			printer.sendBlockUpdates(level);
		inventory.setStackInSlot(0, ItemStack.EMPTY);
		inventory.setStackInSlot(1, new ItemStack(AllItems.EMPTY_SCHEMATIC.get(), inventory.getStackInSlot(1)
			.getCount() + 1));
		state = State.STOPPED;
		statusMsg = "finished";
		resetPrinter();
		AllSoundEvents.SCHEMATICANNON_FINISH.playOnServer(level, worldPosition);
		sendUpdate = true;
	}

	protected void resetPrinter() {
		printer.resetSchematic();
		missingItem = null;
		missingFluid = FluidStack.EMPTY;
		waitingForStageBarrier = false;
		sendUpdate = true;
		schematicProgress = 0;
		blocksPlaced = 0;
		blocksToPlace = 0;
	}

	protected boolean shouldPlace(BlockPos pos, BlockState state, BlockEntity be, BlockState toReplace,
		BlockState toReplaceOther, boolean isNormalCube) {
		if (pos.closerThan(getBlockPos(), 2f))
			return false;
		if (!replaceBlockEntities
			&& (toReplace.hasBlockEntity() || (toReplaceOther != null && toReplaceOther.hasBlockEntity())))
			return false;

		if (shouldIgnoreBlockState(state, be))
			return false;

		boolean placingAir = state.isAir();

		if (replaceMode == 3)
			return true;
		if (replaceMode == 2 && !placingAir)
			return true;
		if (replaceMode == 1 && (isNormalCube || (!toReplace.isRedstoneConductor(level, pos)
			&& (toReplaceOther == null || !toReplaceOther.isRedstoneConductor(level, pos)))) && !placingAir)
			return true;
		if (replaceMode == 0 && !toReplace.isRedstoneConductor(level, pos)
			&& (toReplaceOther == null || !toReplaceOther.isRedstoneConductor(level, pos)) && !placingAir)
			return true;

		return false;
	}

	protected boolean shouldIgnoreBlockState(BlockState state, BlockEntity be) {
		// Block doesn't have a mapping (Water, lava, etc)
		if (state.getBlock() == Blocks.STRUCTURE_VOID)
			return true;

		ItemRequirement requirement = ItemRequirement.of(state, be);
		if (requirement.isEmpty())
			return false;
		if (requirement.isInvalid())
			return false;

		// Block doesn't need to be placed twice (Doors, beds, double plants)
		if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
			&& state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER)
			return true;
		if (state.hasProperty(BlockStateProperties.BED_PART)
			&& state.getValue(BlockStateProperties.BED_PART) == BedPart.HEAD)
			return true;
		if (state.getBlock() instanceof PistonHeadBlock)
			return true;
		if (AllBlocks.BELT.has(state))
			return state.getValue(BeltBlock.PART) == BeltPart.MIDDLE;

		return false;
	}

	protected void tickFlyingBlocks() {
		List<LaunchedItem> toRemove = new LinkedList<>();
		for (LaunchedItem b : flyingBlocks)
			if (b.update(level))
				toRemove.add(b);
		flyingBlocks.removeAll(toRemove);
	}

	protected void refillFuelIfPossible() {
		if (hasCreativeCrate)
			return;
		if (remainingFuel > getShotsPerGunpowder()) {
			remainingFuel = getShotsPerGunpowder();
			sendUpdate = true;
			return;
		}

		if (remainingFuel > 0)
			return;

		if (!inventory.getStackInSlot(4)
			.isEmpty())
			inventory.getStackInSlot(4)
				.shrink(1);
		else {
			boolean externalGunpowderFound = false;
			for (IItemHandler cap : attachedInventories) {
				IItemHandler itemHandler = cap;

				if (itemHandler == null)
					itemHandler = EmptyItemHandler.INSTANCE;

				if (ItemHelper.extract(itemHandler, stack -> inventory.isItemValid(4, stack), 1, false)
					.isEmpty())
					continue;
				externalGunpowderFound = true;
				break;
			}
			if (!externalGunpowderFound)
				return;
		}

		remainingFuel += getShotsPerGunpowder();
		if (statusMsg.equals("noGunpowder")) {
			if (blocksPlaced > 0)
				state = State.RUNNING;
			statusMsg = "ready";
		}
		sendUpdate = true;
	}

	protected void tickPaperPrinter() {
		int BookInput = 2;
		int BookOutput = 3;

		ItemStack blueprint = inventory.getStackInSlot(0);
		ItemStack paper = inventory.extractItem(BookInput, 1, true);
		boolean outputFull = inventory.getStackInSlot(BookOutput)
			.getCount() == inventory.getSlotLimit(BookOutput);

		if (printer.isErrored())
			return;

		if (!printer.isLoaded()) {
			if (!blueprint.isEmpty())
				initializePrinter(blueprint);
			return;
		}

		if (paper.isEmpty() || outputFull) {
			if (bookPrintingProgress != 0)
				sendUpdate = true;
			bookPrintingProgress = 0;
			dontUpdateChecklist = false;
			return;
		}

		if (bookPrintingProgress >= 1) {
			bookPrintingProgress = 0;

			if (!dontUpdateChecklist)
				updateChecklist();

			dontUpdateChecklist = true;
			ItemStack extractItem = inventory.extractItem(BookInput, 1, false);
			ItemStack stack = AllBlocks.CLIPBOARD.isIn(extractItem) ? checklist.createWrittenClipboard()
				: checklist.createWrittenBook();
			stack.setCount(inventory.getStackInSlot(BookOutput)
				.getCount() + 1);
			inventory.setStackInSlot(BookOutput, stack);
			sendUpdate = true;
			return;
		}

		bookPrintingProgress += 0.05f;
		sendUpdate = true;
	}

	public static BlockState stripBeltIfNotLast(BlockState blockState) {
		BeltPart part = blockState.getValue(BeltBlock.PART);
		if (part == BeltPart.MIDDLE)
			return Blocks.AIR.defaultBlockState();

		// is highest belt?
		boolean isLastSegment = false;
		Direction facing = blockState.getValue(BeltBlock.HORIZONTAL_FACING);
		BeltSlope slope = blockState.getValue(BeltBlock.SLOPE);
		boolean positive = facing.getAxisDirection() == AxisDirection.POSITIVE;
		boolean start = part == BeltPart.START;
		boolean end = part == BeltPart.END;

		switch (slope) {
		case DOWNWARD:
			isLastSegment = start;
			break;
		case UPWARD:
			isLastSegment = end;
			break;
		default:
			isLastSegment = positive && end || !positive && start;
		}
		if (isLastSegment)
			return blockState;

		return AllBlocks.SHAFT.getDefaultState()
			.setValue(AbstractSimpleShaftBlock.AXIS, slope == BeltSlope.SIDEWAYS ? Axis.Y
				: facing.getClockWise()
					.getAxis());
	}

	protected void launchBlockOrBelt(BlockPos target, ItemStack icon, BlockState blockState, BlockEntity blockEntity,
									 FluidStack containedFluid) {
		if (AllBlocks.BELT.has(blockState)) {
			blockState = stripBeltIfNotLast(blockState);
			if (blockEntity instanceof BeltBlockEntity bbe && AllBlocks.BELT.has(blockState)) {
				CasingType[] casings = new CasingType[bbe.beltLength];
				Arrays.fill(casings, CasingType.NONE);
				BlockPos currentPos = target;
				for (int i = 0; i < bbe.beltLength; i++) {
					BlockState currentState = bbe.getLevel()
						.getBlockState(currentPos);
					if (!(currentState.getBlock() instanceof BeltBlock))
						break;
					if (!(bbe.getLevel()
						.getBlockEntity(currentPos) instanceof BeltBlockEntity beltAtSegment))
						break;
					casings[i] = beltAtSegment.casing;
					currentPos = BeltBlock.nextSegmentPosition(currentState, currentPos,
						blockState.getValue(BeltBlock.PART) != BeltPart.END);
				}
				launchBelt(target, blockState, bbe.beltLength, casings);
			} else if (blockState != Blocks.AIR.defaultBlockState())
				launchBlock(target, icon, blockState, null, containedFluid);
			return;
		}

		CompoundTag data = BlockHelper.prepareBlockEntityData(level, blockState, blockEntity);
		launchBlock(target, icon, blockState, data, containedFluid);
	}

	protected void launchBelt(BlockPos target, BlockState state, int length, CasingType[] casings) {
		blocksPlaced++;
		ItemStack connector = AllItems.BELT_CONNECTOR.asStack();
		flyingBlocks.add(new LaunchedItem.ForBelt(this.getBlockPos(), target, connector, state, casings));
		playFiringSound();
	}

	protected void launchBlock(BlockPos target, ItemStack stack, BlockState state, @Nullable CompoundTag data) {
		launchBlock(target, stack, state, data, FluidStack.EMPTY);
	}

	protected void launchBlock(BlockPos target, ItemStack stack, BlockState state, @Nullable CompoundTag data,
							   FluidStack containedFluid) {
		if (!state.isAir())
			blocksPlaced++;
		flyingBlocks.add(
			new LaunchedItem.ForBlockState(this.getBlockPos(), target, stack, state, data, containedFluid));
		playFiringSound();
	}

	protected void launchFluid(BlockPos target, FluidStack fluid) {
		blocksPlaced++;
		flyingBlocks.add(new LaunchedItem.ForFluid(this.getBlockPos(), target, fluid));
		playFiringSound();
	}

	protected void launchEntity(BlockPos target, ItemStack stack, Entity entity) {
		blocksPlaced++;
		flyingBlocks.add(new LaunchedItem.ForEntity(this.getBlockPos(), target, stack, entity));
		playFiringSound();
	}

	public void playFiringSound() {
		AllSoundEvents.SCHEMATICANNON_LAUNCH_BLOCK.playOnServer(level, worldPosition);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return SchematicannonMenu.create(id, inv, this);
	}

	@Override
	public Component getDisplayName() {
		return CreateLang.translateDirect("gui.schematicannon.title");
	}

	public void updateChecklist() {
		checklist.required.clear();
		checklist.damageRequired.clear();
		checklist.requiredFluids.clear();
		checklist.blocksNotLoaded = false;

		if (printer.isLoaded() && !printer.isErrored()) {
			blocksToPlace = blocksPlaced;
			blocksToPlace += printer.markAllBlockRequirements(checklist, level, this::shouldPlace);
			printer.markAllEntityRequirements(checklist);
		}

		checklist.gathered.clear();
		checklist.gatheredFluids.clear();
		findInventories();
		for (IItemHandler cap : attachedInventories) {
			if (cap == null)
				continue;
			for (int slot = 0; slot < cap.getSlots(); slot++) {
				ItemStack stackInSlot = cap.getStackInSlot(slot);
				if (cap.extractItem(slot, 1, true)
					.isEmpty())
					continue;
				checklist.collect(stackInSlot);
				FluidUtil.getFluidContained(stackInSlot)
					.ifPresent(fluid -> checklist.collect(fluid.copyWithAmount(fluid.getAmount() * stackInSlot.getCount())));
			}
		}
		for (IFluidHandler handler : attachedFluidInventories) {
			for (int tank = 0; tank < handler.getTanks(); tank++)
				checklist.collect(handler.getFluidInTank(tank));
		}
		sendUpdate = true;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

	@Override
	public void lazyTick() {
		super.lazyTick();
		findInventories();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AABB getRenderBoundingBox() {
		return AABB.INFINITE;
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput componentInput) {
		SchematicannonOptions options = componentInput.getOrDefault(AllDataComponents.SCHEMATICANNON_OPTIONS,
				new SchematicannonOptions(2, true, false, false));
		replaceMode = options.replaceMode;
		skipMissing = options.skipMissing;
		skipMissingFluid = options.skipMissingFluid;
		replaceBlockEntities = options.replaceBlockEntities;
	}

	@Override
	protected void collectImplicitComponents(Builder components) {
		components.set(AllDataComponents.SCHEMATICANNON_OPTIONS,
			new SchematicannonOptions(replaceMode, skipMissing, skipMissingFluid, replaceBlockEntities));
	}

	public enum State {
		STOPPED, PAUSED, RUNNING;
	}

	public record SchematicannonOptions(int replaceMode, boolean skipMissing, boolean skipMissingFluid,
										boolean replaceBlockEntities) {
		public static final Codec<SchematicannonOptions> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.INT.fieldOf("replace_mode").forGetter(SchematicannonOptions::replaceMode),
				Codec.BOOL.fieldOf("skip_missing").forGetter(SchematicannonOptions::skipMissing),
				Codec.BOOL.optionalFieldOf("skip_missing_fluid", false)
					.forGetter(SchematicannonOptions::skipMissingFluid),
				Codec.BOOL.fieldOf("replace_block_entities").forGetter(SchematicannonOptions::replaceBlockEntities)
		).apply(i, SchematicannonOptions::new));

		public static final StreamCodec<ByteBuf, SchematicannonOptions> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT, SchematicannonOptions::replaceMode,
				ByteBufCodecs.BOOL, SchematicannonOptions::skipMissing,
				ByteBufCodecs.BOOL, SchematicannonOptions::skipMissingFluid,
				ByteBufCodecs.BOOL, SchematicannonOptions::replaceBlockEntities,
				SchematicannonOptions::new
		);
	}
}
