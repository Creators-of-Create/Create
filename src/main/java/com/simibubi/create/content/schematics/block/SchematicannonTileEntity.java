package com.simibubi.create.content.schematics.block;

import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltPart;
import com.simibubi.create.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.content.schematics.MaterialChecklist;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.content.schematics.item.SchematicItem;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CSchematics;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.ItemHelper.ExtractionCountMode;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import com.simibubi.create.foundation.utility.BlockHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class SchematicannonTileEntity extends SmartTileEntity implements INamedContainerProvider {

	public static final int NEIGHBOUR_CHECKING = 100;
	public static final int MAX_ANCHOR_DISTANCE = 256;

	public enum State {
		STOPPED, PAUSED, RUNNING;
	}

	// Inventory
	public SchematicannonInventory inventory;

	public boolean sendUpdate;
	// Sync
	public boolean dontUpdateChecklist;
	public int neighbourCheckCooldown;

	// Printer
	private SchematicWorld blockReader;
	public BlockPos currentPos;
	public BlockPos schematicAnchor;
	public boolean schematicLoaded;
	public ItemStack missingItem;
	public boolean positionNotLoaded;
	public boolean hasCreativeCrate;
	private int printerCooldown;
	private int skipsLeft;
	private boolean blockSkipped;
	private int printingEntityIndex;

	public BlockPos target;
	public BlockPos previousTarget;
	public List<IItemHandler> attachedInventories;
	public List<LaunchedItem> flyingBlocks;
	public MaterialChecklist checklist;

	// Gui information
	public float fuelLevel;
	public float bookPrintingProgress;
	public float schematicProgress;
	public String statusMsg;
	public State state;
	public int blocksPlaced;
	public int blocksToPlace;

	// Settings
	public int replaceMode;
	public boolean skipMissing;
	public boolean replaceTileEntities;

	// Render
	public boolean firstRenderTick;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return super.getMaxRenderDistanceSquared() * 16;
	}

	public SchematicannonTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		setLazyTickRate(30);
		attachedInventories = new LinkedList<>();
		flyingBlocks = new LinkedList<>();
		inventory = new SchematicannonInventory(this);
		statusMsg = "idle";
		state = State.STOPPED;
		printingEntityIndex = -1;
		replaceMode = 2;
		neighbourCheckCooldown = NEIGHBOUR_CHECKING;
		checklist = new MaterialChecklist();
	}

	public void findInventories() {
		hasCreativeCrate = false;
		attachedInventories.clear();
		for (Direction facing : Direction.values()) {

			if (!world.isBlockPresent(pos.offset(facing)))
				continue;

			if (AllBlocks.CREATIVE_CRATE.has(world.getBlockState(pos.offset(facing))))
				hasCreativeCrate = true;

			TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
			if (tileEntity != null) {
				LazyOptional<IItemHandler> capability =
					tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
				if (capability.isPresent()) {
					attachedInventories.add(capability.orElse(null));
				}
			}
		}
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		if (!clientPacket) {
			inventory.deserializeNBT(compound.getCompound("Inventory"));
			if (compound.contains("CurrentPos"))
				currentPos = NBTUtil.readBlockPos(compound.getCompound("CurrentPos"));
		}
		
		// Gui information
		statusMsg = compound.getString("Status");
		schematicProgress = compound.getFloat("Progress");
		bookPrintingProgress = compound.getFloat("PaperProgress");
		fuelLevel = compound.getFloat("Fuel");
		state = State.valueOf(compound.getString("State"));
		blocksPlaced = compound.getInt("AmountPlaced");
		blocksToPlace = compound.getInt("AmountToPlace");
		printingEntityIndex = compound.getInt("EntityProgress");
		
		missingItem = null;
		if (compound.contains("MissingItem"))
			missingItem = ItemStack.read(compound.getCompound("MissingItem"));
		
		// Settings
		CompoundNBT options = compound.getCompound("Options");
		replaceMode = options.getInt("ReplaceMode");
		skipMissing = options.getBoolean("SkipMissing");
		replaceTileEntities = options.getBoolean("ReplaceTileEntities");
		
		// Printer & Flying Blocks
		if (compound.contains("Target"))
			target = NBTUtil.readBlockPos(compound.getCompound("Target"));
		if (compound.contains("FlyingBlocks"))
			readFlyingBlocks(compound);

		super.read(compound, clientPacket);
	}

	protected void readFlyingBlocks(CompoundNBT compound) {
		ListNBT tagBlocks = compound.getList("FlyingBlocks", 10);
		if (tagBlocks.isEmpty())
			flyingBlocks.clear();

		boolean pastDead = false;

		for (int i = 0; i < tagBlocks.size(); i++) {
			CompoundNBT c = tagBlocks.getCompound(i);
			LaunchedItem launched = LaunchedItem.fromNBT(c);
			BlockPos readBlockPos = launched.target;

			// Always write to Server tile
			if (world == null || !world.isRemote) {
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
	public void write(CompoundNBT compound, boolean clientPacket) {
		if (!clientPacket) {
			compound.put("Inventory", inventory.serializeNBT());
			if (state == State.RUNNING) {
				compound.putBoolean("Running", true);
				if (currentPos != null)
					compound.put("CurrentPos", NBTUtil.writeBlockPos(currentPos));
			}
		}
		
		// Gui information
		compound.putFloat("Progress", schematicProgress);
		compound.putFloat("PaperProgress", bookPrintingProgress);
		compound.putFloat("Fuel", fuelLevel);
		compound.putString("Status", statusMsg);
		compound.putString("State", state.name());
		compound.putInt("AmountPlaced", blocksPlaced);
		compound.putInt("AmountToPlace", blocksToPlace);
		compound.putInt("EntityProgress", printingEntityIndex);
		
		if (missingItem != null)
			compound.put("MissingItem", missingItem.serializeNBT());
		
		// Settings
		CompoundNBT options = new CompoundNBT();
		options.putInt("ReplaceMode", replaceMode);
		options.putBoolean("SkipMissing", skipMissing);
		options.putBoolean("ReplaceTileEntities", replaceTileEntities);
		compound.put("Options", options);
		
		// Printer & Flying Blocks
		if (target != null)
			compound.put("Target", NBTUtil.writeBlockPos(target));
		ListNBT tagBlocks = new ListNBT();
		for (LaunchedItem b : flyingBlocks)
			tagBlocks.add(b.serializeNBT());
		compound.put("FlyingBlocks", tagBlocks);

		super.write(compound, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();

		if (neighbourCheckCooldown-- <= 0) {
			neighbourCheckCooldown = NEIGHBOUR_CHECKING;
			findInventories();
		}

		firstRenderTick = true;
		previousTarget = target;
		tickFlyingBlocks();

		if (world.isRemote)
			return;

		// Update Fuel and Paper
		tickPaperPrinter();
		refillFuelIfPossible();

		// Update Printer
		skipsLeft = config().schematicannonSkips.get();
		blockSkipped = true;

		while (blockSkipped && skipsLeft-- > 0)
			tickPrinter();

		schematicProgress = 0;
		if (blocksToPlace > 0)
			schematicProgress = (float) blocksPlaced / blocksToPlace;

		// Update Client Tile
		if (sendUpdate) {
			sendUpdate = false;
			world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 6);
		}
	}

	public CSchematics config() {
		return AllConfigs.SERVER.schematics;
	}

	protected void tickPrinter() {
		ItemStack blueprint = inventory.getStackInSlot(0);
		blockSkipped = false;

		// Skip if not Active
		if (state == State.STOPPED) {
			if (schematicLoaded)
				resetPrinter();
			return;
		}

		if (blueprint.isEmpty()) {
			state = State.STOPPED;
			statusMsg = "idle";
			sendUpdate = true;
			return;
		}

		if (state == State.PAUSED && !positionNotLoaded && missingItem == null && fuelLevel > getFuelUsageRate())
			return;

		// Initialize Printer
		if (!schematicLoaded) {
			initializePrinter(blueprint);
			return;
		}

		// Cooldown from last shot
		if (printerCooldown > 0) {
			printerCooldown--;
			return;
		}

		// Check Fuel
		if (fuelLevel <= 0 && !hasCreativeCrate) {
			fuelLevel = 0;
			state = State.PAUSED;
			statusMsg = "noGunpowder";
			sendUpdate = true;
			return;
		}

		// Update Target
		if (hasCreativeCrate) {
			if (missingItem != null) {
				missingItem = null;
				state = State.RUNNING;
			}
		}

		if (missingItem == null && !positionNotLoaded) {
			advanceCurrentPos();

			// End reached
			if (state == State.STOPPED)
				return;

			sendUpdate = true;
			target = schematicAnchor.add(currentPos);
		}

		boolean entityMode = printingEntityIndex >= 0;

		// Check block
		if (!getWorld().isAreaLoaded(target, 0)) {
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

		boolean shouldSkip = false;
		BlockState blockState = Blocks.AIR.getDefaultState();
		ItemRequirement requirement;

		if (entityMode) {
			requirement = ItemRequirement.of(blockReader.getEntities()
				.get(printingEntityIndex));

		} else {
			blockState = BlockHelper.setZeroAge(blockReader.getBlockState(target));
			requirement = ItemRequirement.of(blockState);
			shouldSkip = !shouldPlace(target, blockState);
		}

		if (shouldSkip || requirement.isInvalid()) {
			statusMsg = "searching";
			blockSkipped = true;
			return;
		}

		// Find item
		List<ItemStack> requiredItems = requirement.getRequiredItems();
		if (!requirement.isEmpty()) {
			for (ItemStack required : requiredItems) {
				if (!grabItemsFromAttachedInventories(required, requirement.getUsage(), true)) {
					if (skipMissing) {
						statusMsg = "skipping";
						blockSkipped = true;
						if (missingItem != null) {
							missingItem = null;
							state = State.RUNNING;
						}
						return;
					}

					missingItem = required;
					state = State.PAUSED;
					statusMsg = "missingBlock";
					return;
				}
			}

			for (ItemStack required : requiredItems)
				grabItemsFromAttachedInventories(required, requirement.getUsage(), false);
		}

		// Success
		state = State.RUNNING;
		if (blockState.getBlock() != Blocks.AIR || entityMode)
			statusMsg = "placing";
		else
			statusMsg = "clearing";

		ItemStack icon = requirement.isEmpty() || requiredItems.isEmpty() ? ItemStack.EMPTY : requiredItems.get(0);
		if (entityMode)
			launchEntity(target, icon, blockReader.getEntities()
				.get(printingEntityIndex));
		else if (AllBlocks.BELT.has(blockState)) {
			TileEntity te = blockReader.getTileEntity(currentPos.add(schematicAnchor));
			blockState = stripBeltIfNotLast(blockState);
			if (te instanceof BeltTileEntity && AllBlocks.BELT.has(blockState))
				launchBelt(target, blockState, ((BeltTileEntity) te).beltLength);
			else
				launchBlock(target, icon, blockState);
		} else
			launchBlock(target, icon, blockState);

		printerCooldown = config().schematicannonDelay.get();
		fuelLevel -= getFuelUsageRate();
		sendUpdate = true;
		missingItem = null;
	}

	public BlockState stripBeltIfNotLast(BlockState blockState) {
		// is highest belt?
		boolean isLastSegment = false;
		Direction facing = blockState.get(BeltBlock.HORIZONTAL_FACING);
		BeltSlope slope = blockState.get(BeltBlock.SLOPE);
		boolean positive = facing.getAxisDirection() == AxisDirection.POSITIVE;
		boolean start = blockState.get(BeltBlock.PART) == BeltPart.START;
		boolean end = blockState.get(BeltBlock.PART) == BeltPart.END;

		switch (slope) {
		case DOWNWARD:
			isLastSegment = start;
			break;
		case UPWARD:
			isLastSegment = end;
			break;
		case HORIZONTAL:
		case VERTICAL:
		default:
			isLastSegment = positive && end || !positive && start;
		}
		if (!isLastSegment)
			blockState = (blockState.get(BeltBlock.PART) == BeltPart.MIDDLE) ? Blocks.AIR.getDefaultState()
				: AllBlocks.SHAFT.getDefaultState()
					.with(ShaftBlock.AXIS, facing.rotateY()
						.getAxis());
		return blockState;
	}

	public double getFuelUsageRate() {
		return hasCreativeCrate ? 0 : config().schematicannonFuelUsage.get() / 100f;
	}

	protected void initializePrinter(ItemStack blueprint) {
		if (!blueprint.hasTag()) {
			state = State.STOPPED;
			statusMsg = "schematicInvalid";
			sendUpdate = true;
			return;
		}

		if (!blueprint.getTag()
			.getBoolean("Deployed")) {
			state = State.STOPPED;
			statusMsg = "schematicNotPlaced";
			sendUpdate = true;
			return;
		}

		// Load blocks into reader
		Template activeTemplate = SchematicItem.loadSchematic(blueprint);
		BlockPos anchor = NBTUtil.readBlockPos(blueprint.getTag()
			.getCompound("Anchor"));

		if (activeTemplate.getSize()
			.equals(BlockPos.ZERO)) {
			state = State.STOPPED;
			statusMsg = "schematicExpired";
			inventory.setStackInSlot(0, ItemStack.EMPTY);
			inventory.setStackInSlot(1, new ItemStack(AllItems.EMPTY_SCHEMATIC.get()));
			return;
		}

		if (!anchor.withinDistance(getPos(), MAX_ANCHOR_DISTANCE)) {
			state = State.STOPPED;
			statusMsg = "targetOutsideRange";
			return;
		}

		schematicAnchor = anchor;
		blockReader = new SchematicWorld(schematicAnchor, world);
		PlacementSettings settings = SchematicItem.getSettings(blueprint);
		activeTemplate.addBlocksToWorld(blockReader, schematicAnchor, settings);
		schematicLoaded = true;
		state = State.PAUSED;
		statusMsg = "ready";
		printingEntityIndex = -1;
		updateChecklist();
		sendUpdate = true;
		blocksToPlace += blocksPlaced;
		MutableBoundingBox bounds = blockReader.getBounds();
		currentPos = currentPos != null ? currentPos.west() : new BlockPos(bounds.minX - 1, bounds.minY, bounds.minZ);
	}

	protected ItemStack getItemForBlock(BlockState blockState) {
		Item item = BlockItem.BLOCK_TO_ITEM.getOrDefault(blockState.getBlock(), Items.AIR);
		return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
	}

	protected boolean grabItemsFromAttachedInventories(ItemStack required, ItemUseType usage, boolean simulate) {
		if (hasCreativeCrate)
			return true;

		// Find and apply damage
		if (usage == ItemUseType.DAMAGE) {
			for (IItemHandler iItemHandler : attachedInventories) {
				for (int slot = 0; slot < iItemHandler.getSlots(); slot++) {
					ItemStack extractItem = iItemHandler.extractItem(slot, 1, true);
					if (!ItemRequirement.validate(required, extractItem))
						continue;
					if (!extractItem.isDamageable())
						continue;

					if (!simulate) {
						ItemStack stack = iItemHandler.extractItem(slot, 1, false);
						stack.setDamage(stack.getDamage() + 1);
						if (stack.getDamage() <= stack.getMaxDamage()) {
							if (iItemHandler.getStackInSlot(slot)
								.isEmpty())
								iItemHandler.insertItem(slot, stack, false);
							else
								ItemHandlerHelper.insertItem(iItemHandler, stack, false);
						}
					}

					return true;
				}
			}
		}

		// Find and remove
		boolean success = false;
		if (usage == ItemUseType.CONSUME) {
			int amountFound = 0;
			for (IItemHandler iItemHandler : attachedInventories) {

				amountFound += ItemHelper
					.extract(iItemHandler, s -> ItemRequirement.validate(required, s), ExtractionCountMode.UPTO,
						required.getCount(), true)
					.getCount();

				if (amountFound < required.getCount())
					continue;

				success = true;
				break;
			}
		}

		if (!simulate && success) {
			int amountFound = 0;
			for (IItemHandler iItemHandler : attachedInventories) {
				amountFound += ItemHelper
					.extract(iItemHandler, s -> ItemRequirement.validate(required, s), ExtractionCountMode.UPTO,
						required.getCount(), false)
					.getCount();
				if (amountFound < required.getCount())
					continue;
				break;
			}
		}

		return success;
	}

	protected void advanceCurrentPos() {
		List<Entity> entities = blockReader.getEntities();
		if (printingEntityIndex != -1) {
			printingEntityIndex++;

			// End of entities reached
			if (printingEntityIndex >= entities.size()) {
				finishedPrinting();
				return;
			}

			currentPos = entities.get(printingEntityIndex)
				.getPosition()
				.subtract(schematicAnchor);
			return;
		}

		MutableBoundingBox bounds = blockReader.getBounds();
		currentPos = currentPos.offset(Direction.EAST);
		BlockPos posInBounds = currentPos.add(-bounds.minX, -bounds.minY, -bounds.minZ);

		if (posInBounds.getX() > bounds.getXSize())
			currentPos = new BlockPos(bounds.minX, currentPos.getY(), currentPos.getZ() + 1).west();
		if (posInBounds.getZ() > bounds.getZSize())
			currentPos = new BlockPos(currentPos.getX(), currentPos.getY() + 1, bounds.minZ).west();

		// End of blocks reached
		if (currentPos.getY() > bounds.getYSize()) {
			printingEntityIndex = 0;
			if (entities.isEmpty()) {
				finishedPrinting();
				return;
			}
			currentPos = entities.get(0)
				.getPosition()
				.subtract(schematicAnchor);
		}
	}

	public void finishedPrinting() {
		inventory.setStackInSlot(0, ItemStack.EMPTY);
		inventory.setStackInSlot(1, new ItemStack(AllItems.EMPTY_SCHEMATIC.get(), inventory.getStackInSlot(1)
			.getCount() + 1));
		state = State.STOPPED;
		statusMsg = "finished";
		resetPrinter();
		target = getPos().add(1, 0, 0);
		world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), AllSoundEvents.SCHEMATICANNON_FINISH.get(),
			SoundCategory.BLOCKS, 1, .7f);
		sendUpdate = true;
	}

	protected void resetPrinter() {
		schematicLoaded = false;
		schematicAnchor = null;
		currentPos = null;
		blockReader = null;
		missingItem = null;
		sendUpdate = true;
		printingEntityIndex = -1;
		schematicProgress = 0;
		blocksPlaced = 0;
		blocksToPlace = 0;
	}

	protected boolean shouldPlace(BlockPos pos, BlockState state) {
		BlockState toReplace = world.getBlockState(pos);
		boolean placingAir = state.getBlock() == Blocks.AIR;

		if (!world.isBlockPresent(pos))
			return false;
		if (!world.getWorldBorder()
			.contains(pos))
			return false;
		if (toReplace == state)
			return false;
		if (toReplace.getBlockHardness(world, pos) == -1)
			return false;
		if (pos.withinDistance(getPos(), 2f))
			return false;
		if (!replaceTileEntities && toReplace.hasTileEntity())
			return false;

		if (shouldIgnoreBlockState(state))
			return false;

		if (replaceMode == 3)
			return true;
		if (replaceMode == 2 && !placingAir)
			return true;
		if (replaceMode == 1
			&& (state.isNormalCube(blockReader, pos.subtract(schematicAnchor)) || !toReplace.isNormalCube(world, pos))
			&& !placingAir)
			return true;
		if (replaceMode == 0 && !toReplace.isNormalCube(world, pos) && !placingAir)
			return true;

		return false;
	}

	protected boolean shouldIgnoreBlockState(BlockState state) {
		// Block doesnt have a mapping (Water, lava, etc)
		if (state.getBlock() == Blocks.STRUCTURE_VOID)
			return true;
		if (getItemForBlock(state).getItem() == Items.AIR && state.getBlock() != Blocks.AIR)
			return true;

		// Block doesnt need to be placed twice (Doors, beds, double plants)
		if (state.has(BlockStateProperties.DOUBLE_BLOCK_HALF)
			&& state.get(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER)
			return true;
		if (state.has(BlockStateProperties.BED_PART) && state.get(BlockStateProperties.BED_PART) == BedPart.HEAD)
			return true;
		if (state.getBlock() instanceof PistonHeadBlock)
			return true;

		return false;
	}

	protected void tickFlyingBlocks() {
		List<LaunchedItem> toRemove = new LinkedList<>();
		for (LaunchedItem b : flyingBlocks)
			if (b.update(world))
				toRemove.add(b);
		flyingBlocks.removeAll(toRemove);
	}

	protected void refillFuelIfPossible() {
		if (hasCreativeCrate)
			return;
		if (1 - fuelLevel + 1 / 128f < getFuelAddedByGunPowder())
			return;
		if (inventory.getStackInSlot(4)
			.isEmpty())
			return;

		inventory.getStackInSlot(4)
			.shrink(1);
		fuelLevel += getFuelAddedByGunPowder();
		sendUpdate = true;
	}

	public double getFuelAddedByGunPowder() {
		return config().schematicannonGunpowderWorth.get() / 100f;
	}

	protected void tickPaperPrinter() {
		int BookInput = 2;
		int BookOutput = 3;

		ItemStack blueprint = inventory.getStackInSlot(0);
		ItemStack paper = inventory.extractItem(BookInput, 1, true);
		boolean outputFull = inventory.getStackInSlot(BookOutput)
			.getCount() == inventory.getSlotLimit(BookOutput);

		if (paper.isEmpty() || outputFull) {
			if (bookPrintingProgress != 0)
				sendUpdate = true;
			bookPrintingProgress = 0;
			dontUpdateChecklist = false;
			return;
		}

		if (!schematicLoaded) {
			if (!blueprint.isEmpty())
				initializePrinter(blueprint);
			return;
		}

		if (bookPrintingProgress >= 1) {
			bookPrintingProgress = 0;

			if (!dontUpdateChecklist)
				updateChecklist();

			dontUpdateChecklist = true;
			inventory.extractItem(BookInput, 1, false);
			ItemStack stack = checklist.createItem();
			stack.setCount(inventory.getStackInSlot(BookOutput)
				.getCount() + 1);
			inventory.setStackInSlot(BookOutput, stack);
			sendUpdate = true;
			return;
		}

		bookPrintingProgress += 0.05f;
		sendUpdate = true;
	}

	protected void launchBelt(BlockPos target, BlockState state, int length) {
		blocksPlaced++;
		ItemStack connector = AllItems.BELT_CONNECTOR.asStack();
		flyingBlocks.add(new LaunchedItem.ForBelt(this.getPos(), target, connector, state, length));
		playFiringSound();
	}

	protected void launchBlock(BlockPos target, ItemStack stack, BlockState state) {
		if (state.getBlock() != Blocks.AIR)
			blocksPlaced++;
		flyingBlocks.add(new LaunchedItem.ForBlockState(this.getPos(), target, stack, state));
		playFiringSound();
	}

	protected void launchEntity(BlockPos target, ItemStack stack, Entity entity) {
		blocksPlaced++;
		flyingBlocks.add(new LaunchedItem.ForEntity(this.getPos(), target, stack, entity));
		playFiringSound();
	}

	public void playFiringSound() {
		world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), AllSoundEvents.SCHEMATICANNON_LAUNCH_BLOCK.get(),
			SoundCategory.BLOCKS, .1f, 1.1f);
	}

	public void sendToContainer(PacketBuffer buffer) {
		buffer.writeBlockPos(getPos());
		buffer.writeCompoundTag(getUpdateTag());
	}

	@Override
	public Container createMenu(int id, PlayerInventory inv, PlayerEntity player) {
		return new SchematicannonContainer(id, inv, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent(getType().getRegistryName()
			.toString());
	}

	public void updateChecklist() {
		checklist.required.clear();
		checklist.damageRequired.clear();
		checklist.blocksNotLoaded = false;

		if (schematicLoaded) {
			blocksToPlace = blocksPlaced;
			for (BlockPos pos : blockReader.getAllPositions()) {
				BlockState required = blockReader.getBlockState(pos.add(schematicAnchor));

				if (!getWorld().isAreaLoaded(pos.add(schematicAnchor), 0)) {
					checklist.warnBlockNotLoaded();
					continue;
				}
				if (!shouldPlace(pos.add(schematicAnchor), required))
					continue;
				ItemRequirement requirement = ItemRequirement.of(required);
				if (requirement.isEmpty())
					continue;
				if (requirement.isInvalid())
					continue;
				checklist.require(requirement);
				blocksToPlace++;
			}
			for (Entity entity : blockReader.getEntities()) {
				ItemRequirement requirement = ItemRequirement.of(entity);
				if (requirement.isEmpty())
					continue;
				if (requirement.isInvalid())
					continue;
				checklist.require(requirement);
			}
		}
		checklist.gathered.clear();
		for (IItemHandler inventory : attachedInventories) {
			for (int slot = 0; slot < inventory.getSlots(); slot++) {
				ItemStack stackInSlot = inventory.getStackInSlot(slot);
				if (inventory.extractItem(slot, 1, true)
					.isEmpty())
					continue;
				checklist.collect(stackInSlot);
			}
		}
		sendUpdate = true;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {}

	@Override
	public void lazyTick() {
		super.lazyTick();
		findInventories();
	}

}
