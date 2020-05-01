package com.simibubi.create.modules.schematics.block;

import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.config.AllConfigs;
import com.simibubi.create.config.CSchematics;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.modules.schematics.MaterialChecklist;
import com.simibubi.create.modules.schematics.SchematicWorld;
import com.simibubi.create.modules.schematics.item.SchematicItem;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonHeadBlock;
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
import net.minecraft.state.properties.SlabType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

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
	public BlockState missingBlock;
	public boolean blockNotLoaded;
	public boolean hasCreativeCrate;
	private int printerCooldown;
	private int skipsLeft;
	private boolean blockSkipped;

	public BlockPos target;
	public BlockPos previousTarget;
	public List<IItemHandler> attachedInventories;
	public List<LaunchedBlock> flyingBlocks;
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

	public SchematicannonTileEntity() {
		this(AllTileEntities.SCHEMATICANNON.type);
	}

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

			if (AllBlocks.CREATIVE_CRATE.typeOf(world.getBlockState(pos.offset(facing)))) {
				hasCreativeCrate = true;
			}

			TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
			if (tileEntity != null) {
				LazyOptional<IItemHandler> capability = tileEntity
						.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
				if (capability.isPresent()) {
					attachedInventories.add(capability.orElse(null));
				}
			}
		}
	}

	@Override
	public void read(CompoundNBT compound) {
		inventory.deserializeNBT(compound.getCompound("Inventory"));

		if (compound.contains("Running"))
			currentPos = NBTUtil.readBlockPos(compound.getCompound("CurrentPos"));

		readClientUpdate(compound);
		super.read(compound);
	}

	@Override
	public void readClientUpdate(CompoundNBT compound) {

		// Gui information
		statusMsg = compound.getString("Status");
		schematicProgress = compound.getFloat("Progress");
		bookPrintingProgress = compound.getFloat("PaperProgress");
		fuelLevel = compound.getFloat("Fuel");
		state = State.valueOf(compound.getString("State"));
		blocksPlaced = compound.getInt("AmountPlaced");
		blocksToPlace = compound.getInt("AmountToPlace");

		if (compound.contains("MissingBlock"))
			missingBlock = NBTUtil.readBlockState(compound.getCompound("MissingBlock"));
		else
			missingBlock = null;

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

	}

	protected void readFlyingBlocks(CompoundNBT compound) {
		ListNBT tagBlocks = compound.getList("FlyingBlocks", 10);
		if (tagBlocks.isEmpty())
			flyingBlocks.clear();

		boolean pastDead = false;

		for (int i = 0; i < tagBlocks.size(); i++) {
			CompoundNBT c = tagBlocks.getCompound(i);

			BlockPos readBlockPos = NBTUtil.readBlockPos(c.getCompound("Target"));
			BlockState readBlockState = NBTUtil.readBlockState(c.getCompound("Block"));
			int int1 = c.getInt("TicksLeft");
			int int2 = c.getInt("TotalTicks");

			// Always write to Server tile
			if (world == null || !world.isRemote) {
				flyingBlocks.add(new LaunchedBlock(this, readBlockPos, readBlockState, int1, int2));
				continue;
			}

			// Delete all Client side blocks that are now missing on the server
			while (!pastDead && !flyingBlocks.isEmpty() && !flyingBlocks.get(0).target.equals(readBlockPos)) {
				flyingBlocks.remove(0);
			}

			pastDead = true;

			// Add new server side blocks
			if (i >= flyingBlocks.size()) {
				flyingBlocks.add(new LaunchedBlock(this, readBlockPos, readBlockState, int1, int2));
				continue;
			}

			// Don't do anything with existing
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("Inventory", inventory.serializeNBT());

		if (state == State.RUNNING) {
			compound.putBoolean("Running", true);
			compound.put("CurrentPos", NBTUtil.writeBlockPos(currentPos));
		}

		writeToClient(compound);
		return super.write(compound);
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT compound) {

		// Gui information
		compound.putFloat("Progress", schematicProgress);
		compound.putFloat("PaperProgress", bookPrintingProgress);
		compound.putFloat("Fuel", fuelLevel);
		compound.putString("Status", statusMsg);
		compound.putString("State", state.name());
		compound.putInt("AmountPlaced", blocksPlaced);
		compound.putInt("AmountToPlace", blocksToPlace);

		if (missingBlock != null)
			compound.put("MissingBlock", NBTUtil.writeBlockState(missingBlock));

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
		for (LaunchedBlock b : flyingBlocks) {
			CompoundNBT c = new CompoundNBT();
			c.putInt("TotalTicks", b.totalTicks);
			c.putInt("TicksLeft", b.ticksRemaining);
			c.put("Target", NBTUtil.writeBlockPos(b.target));
			c.put("Block", NBTUtil.writeBlockState(b.state));
			tagBlocks.add(c);
		}
		compound.put("FlyingBlocks", tagBlocks);

		return compound;
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

		if (state == State.PAUSED && !blockNotLoaded && missingBlock == null && fuelLevel > getFuelUsageRate())
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
		if (fuelLevel <= 0) {
			fuelLevel = 0;
			state = State.PAUSED;
			statusMsg = "noGunpowder";
			sendUpdate = true;
			return;
		}

		// Update Target
		if (hasCreativeCrate) {
			if (missingBlock != null) {
				missingBlock = null;
				state = State.RUNNING;
			}
		}

		if (missingBlock == null && !blockNotLoaded) {
			advanceCurrentPos();

			// End reached
			if (state == State.STOPPED)
				return;

			sendUpdate = true;
			target = schematicAnchor.add(currentPos);
		}

		// Check block
		if (!getWorld().isAreaLoaded(target, 0)) {
			blockNotLoaded = true;
			statusMsg = "targetNotLoaded";
			state = State.PAUSED;
			return;
		} else {
			if (blockNotLoaded) {
				blockNotLoaded = false;
				state = State.RUNNING;
			}
		}

		BlockState blockState = blockReader.getBlockState(target);
		ItemStack requiredItem = getItemForBlock(blockState);

		if (!shouldPlace(target, blockState) || requiredItem.isEmpty()) {
			statusMsg = "searching";
			blockSkipped = true;
			return;
		}

		// Find item
		if (blockState.has(BlockStateProperties.SLAB_TYPE)
				&& blockState.get(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE)
			requiredItem.setCount(2);

		if (!findItemInAttachedInventories(requiredItem)) {
			if (skipMissing) {
				statusMsg = "skipping";
				blockSkipped = true;
				if (missingBlock != null) {
					missingBlock = null;
					state = State.RUNNING;
				}
				return;
			}

			missingBlock = blockState;
			state = State.PAUSED;
			statusMsg = "missingBlock";
			return;
		}

		// Success
		state = State.RUNNING;
		if (blockState.getBlock() != Blocks.AIR)
			statusMsg = "placing";
		else
			statusMsg = "clearing";
		launchBlock(target, blockState);
		printerCooldown = config().schematicannonDelay.get();
		fuelLevel -= getFuelUsageRate();
		sendUpdate = true;
		missingBlock = null;
	}

	public double getFuelUsageRate() {
		return config().schematicannonFuelUsage.get() / 100f;
	}

	protected void initializePrinter(ItemStack blueprint) {
		if (!blueprint.hasTag()) {
			state = State.STOPPED;
			statusMsg = "schematicInvalid";
			sendUpdate = true;
			return;
		}

		if (!blueprint.getTag().getBoolean("Deployed")) {
			state = State.STOPPED;
			statusMsg = "schematicNotPlaced";
			sendUpdate = true;
			return;
		}

		// Load blocks into reader
		Template activeTemplate = SchematicItem.loadSchematic(blueprint);
		BlockPos anchor = NBTUtil.readBlockPos(blueprint.getTag().getCompound("Anchor"));

		if (activeTemplate.getSize().equals(BlockPos.ZERO)) {
			state = State.STOPPED;
			statusMsg = "schematicExpired";
			inventory.setStackInSlot(0, ItemStack.EMPTY);
			inventory.setStackInSlot(1, new ItemStack(AllItems.EMPTY_BLUEPRINT.get()));
			return;
		}

		if (!anchor.withinDistance(getPos(), MAX_ANCHOR_DISTANCE)) {
			state = State.STOPPED;
			statusMsg = "targetOutsideRange";
			return;
		}

		schematicAnchor = anchor;
		blockReader = new SchematicWorld(schematicAnchor, world);
		activeTemplate.addBlocksToWorld(blockReader, schematicAnchor, SchematicItem.getSettings(blueprint));
		schematicLoaded = true;
		state = State.PAUSED;
		statusMsg = "ready";
		updateChecklist();
		sendUpdate = true;
		blocksToPlace += blocksPlaced;
		currentPos = currentPos != null ? currentPos.west() : blockReader.getBounds().getOrigin().west();
	}

	protected ItemStack getItemForBlock(BlockState blockState) {
		Item item = BlockItem.BLOCK_TO_ITEM.getOrDefault(blockState.getBlock(), Items.AIR);
		return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
	}

	protected boolean findItemInAttachedInventories(ItemStack requiredItem) {
		if (hasCreativeCrate)
			return true;

		boolean two = requiredItem.getCount() == 2;
		int lastSlot = -1;

		for (IItemHandler iItemHandler : attachedInventories) {
			for (int slot = 0; slot < iItemHandler.getSlots(); slot++) {
				ItemStack stackInSlot = iItemHandler.getStackInSlot(slot);
				if (!stackInSlot.isItemEqual(requiredItem))
					continue;
				if (!two && !iItemHandler.extractItem(slot, 1, false).isEmpty())
					return true;

				// Two Items required (Double slabs)
				if (two) {
					int count = iItemHandler.extractItem(slot, 2, true).getCount();
					if (count == 2) {
						iItemHandler.extractItem(slot, 2, false);
						return true;
					} else if (count == 1) {
						if (lastSlot == -1)
							lastSlot = slot;
						else {
							iItemHandler.extractItem(lastSlot, 1, false);
							iItemHandler.extractItem(slot, 1, false);
							return true;
						}
					}
				}

			}
		}
		return false;
	}

	protected void advanceCurrentPos() {
		BlockPos size = blockReader.getBounds().getSize();
		currentPos = currentPos.offset(Direction.EAST);
		BlockPos posInBounds = currentPos.subtract(blockReader.getBounds().getOrigin());

		if (posInBounds.getX() > size.getX())
			currentPos = new BlockPos(blockReader.getBounds().x, currentPos.getY(), currentPos.getZ() + 1).west();
		if (posInBounds.getZ() > size.getZ())
			currentPos = new BlockPos(currentPos.getX(), currentPos.getY() + 1, blockReader.getBounds().z).west();

		// End reached
		if (currentPos.getY() > size.getY()) {
			inventory.setStackInSlot(0, ItemStack.EMPTY);
			inventory.setStackInSlot(1,
					new ItemStack(AllItems.EMPTY_BLUEPRINT.get(), inventory.getStackInSlot(1).getCount() + 1));
			state = State.STOPPED;
			statusMsg = "finished";
			resetPrinter();
			target = getPos().add(1, 0, 0);
			world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), AllSoundEvents.SCHEMATICANNON_FINISH.get(),
					SoundCategory.BLOCKS, 1, .7f);
			sendUpdate = true;
			return;
		}
	}

	protected void resetPrinter() {
		schematicLoaded = false;
		schematicAnchor = null;
		currentPos = null;
		blockReader = null;
		missingBlock = null;
		sendUpdate = true;
		schematicProgress = 0;
		blocksPlaced = 0;
		blocksToPlace = 0;
	}

	protected boolean shouldPlace(BlockPos pos, BlockState state) {
		BlockState toReplace = world.getBlockState(pos);
		boolean placingAir = state.getBlock() == Blocks.AIR;

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
		if (replaceMode == 1 && (state.isNormalCube(blockReader, pos.subtract(schematicAnchor))
				|| !toReplace.isNormalCube(world, pos)) && !placingAir)
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
		List<LaunchedBlock> toRemove = new LinkedList<>();
		for (LaunchedBlock b : flyingBlocks) {
			b.update();
			if (b.ticksRemaining <= 0 && !world.isRemote) {

				// Piston
				if (b.state.has(BlockStateProperties.EXTENDED)) {
					b.state = b.state.with(BlockStateProperties.EXTENDED, false);
				}

				world.setBlockState(b.target, b.state, 18);
				b.state.getBlock().onBlockPlacedBy(world, b.target, b.state, null, getItemForBlock(b.state));
				toRemove.add(b);
			}
		}
		flyingBlocks.removeAll(toRemove);
	}

	protected void refillFuelIfPossible() {
		if (1 - fuelLevel + 1 / 128f < getFuelAddedByGunPowder())
			return;
		if (inventory.getStackInSlot(4).isEmpty())
			return;

		inventory.getStackInSlot(4).shrink(1);
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
		boolean outputFull = inventory.getStackInSlot(BookOutput).getCount() == inventory.getSlotLimit(BookOutput);

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
			stack.setCount(inventory.getStackInSlot(BookOutput).getCount() + 1);
			inventory.setStackInSlot(BookOutput, stack);
			sendUpdate = true;
			return;
		}

		bookPrintingProgress += 0.05f;
		sendUpdate = true;
	}

	protected void launchBlock(BlockPos target, BlockState state) {
		if (state.getBlock() != Blocks.AIR)
			blocksPlaced++;
		flyingBlocks.add(new LaunchedBlock(this, target, state));
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
		return new StringTextComponent(getType().getRegistryName().toString());
	}

	public void updateChecklist() {
		checklist.required.clear();
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
				ItemStack requiredItem = getItemForBlock(required);
				if (requiredItem.isEmpty())
					continue;

				// Two items for double slabs
				if (required.has(BlockStateProperties.SLAB_TYPE)
						&& required.get(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE)
					checklist.require(requiredItem.getItem());

				checklist.require(requiredItem.getItem());
				blocksToPlace++;
			}
		}
		checklist.gathered.clear();
		for (IItemHandler inventory : attachedInventories) {
			for (int slot = 0; slot < inventory.getSlots(); slot++) {
				ItemStack stackInSlot = inventory.getStackInSlot(slot);
				if (inventory.extractItem(slot, 1, true).isEmpty())
					continue;
				checklist.collect(stackInSlot);
			}
		}
		sendUpdate = true;
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
	}
	
	@Override
	public void lazyTick() {
		super.lazyTick();
		findInventories();
	}

}
