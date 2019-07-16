package com.simibubi.create.block;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.item.ItemBlueprint;
import com.simibubi.create.schematic.Cuboid;
import com.simibubi.create.schematic.SchematicWorld;
import com.simibubi.create.utility.TileEntitySynced;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class SchematicannonTileEntity extends TileEntitySynced implements ITickableTileEntity, INamedContainerProvider {

	public static final int PLACEMENT_DELAY = 10;
	public static final float FUEL_PER_GUNPOWDER = .2f;
	public static final float FUEL_USAGE_RATE = .0001f;

	public enum State {
		STOPPED, PAUSED, RUNNING;
	}

	// Inventory
	public SchematicannonInventory inventory;

	// Sync
	public boolean sendUpdate;

	// Printer
	private SchematicWorld blockReader;
	public BlockPos currentPos;
	public BlockPos schematicAnchor;
	public boolean schematicLoaded;
	public boolean missingBlock;
	public boolean hasCreativeCrate;
	private int printerCooldown;

	public BlockPos target;
	public BlockPos previousTarget;
	public List<IItemHandler> attachedInventories;
	public List<LaunchedBlock> flyingBlocks;

	// Gui information
	public float fuelLevel;
	public float paperPrintingProgress;
	public float schematicProgress;
	public String statusMsg;
	public State state;

	// Settings
	public int replaceMode;
	public boolean skipMissing;
	public boolean replaceTileEntities;

	public class SchematicannonInventory extends ItemStackHandler {
		public SchematicannonInventory() {
			super(5);
		}

		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			markDirty();
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			switch (slot) {
			case 0: // Blueprint Slot
				return AllItems.BLUEPRINT.typeOf(stack);
			case 1: // Blueprint output
				return false;
			case 2: // Paper input
				return stack.getItem() == Items.PAPER;
			case 3: // Material List output
				return false;
			case 4: // Gunpowder
				return stack.getItem() == Items.GUNPOWDER;
			default:
				return super.isItemValid(slot, stack);
			}
		}
	}

	public class LaunchedBlock {
		public int totalTicks;
		public int ticksRemaining;
		public BlockPos target;
		public BlockState state;

		public LaunchedBlock(BlockPos target, BlockState state) {
			this.target = target;
			this.state = state;
			totalTicks = (int) (Math.max(10, MathHelper.sqrt(MathHelper.sqrt(target.distanceSq(pos))) * 4f));
			ticksRemaining = totalTicks;
		}

		public LaunchedBlock(BlockPos target, BlockState state, int ticksLeft, int total) {
			this.target = target;
			this.state = state;
			this.totalTicks = total;
			this.ticksRemaining = ticksLeft;
		}

		public void update() {
			if (ticksRemaining > 0)
				ticksRemaining--;
		}
	}

	public SchematicannonTileEntity() {
		this(AllTileEntities.Schematicannon.type);
	}

	@Override
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}

	public SchematicannonTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		attachedInventories = new LinkedList<>();
		flyingBlocks = new LinkedList<>();
		inventory = new SchematicannonInventory();
		statusMsg = "Idle";
		state = State.STOPPED;
		replaceMode = 2;
	}

	public void findInventories() {
		hasCreativeCrate = false;
		for (Direction facing : Direction.values()) {

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
	public void onLoad() {
		findInventories();
		super.onLoad();
	}

	@Override
	public void readClientUpdate(CompoundNBT compound) {

		// Gui information
		statusMsg = compound.getString("Status");
		schematicProgress = compound.getFloat("Progress");
		paperPrintingProgress = compound.getFloat("PaperProgress");
		fuelLevel = compound.getFloat("Fuel");
		state = State.valueOf(compound.getString("State"));

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
				flyingBlocks.add(new LaunchedBlock(readBlockPos, readBlockState, int1, int2));
				continue;
			}

			// Delete all Client side blocks that are now missing on the server
			while (!pastDead && !flyingBlocks.isEmpty() && !flyingBlocks.get(0).target.equals(readBlockPos)) {
				flyingBlocks.remove(0);
			}

			pastDead = true;

			// Add new server side blocks
			if (i >= flyingBlocks.size()) {
				flyingBlocks.add(new LaunchedBlock(readBlockPos, readBlockState, int1, int2));
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
		compound.putFloat("PaperProgress", paperPrintingProgress);
		compound.putFloat("Fuel", fuelLevel);
		compound.putString("Status", statusMsg);
		compound.putString("State", state.name());

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
		previousTarget = target;
		tickFlyingBlocks();

		if (world.isRemote)
			return;

		// Update Fuel and Paper
		tickPaperPrinter();
		refillFuelIfPossible();

		// Update Printer
		tickPrinter();

		// Update Client Tile
		if (sendUpdate) {
			sendUpdate = false;
			world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 6);
		}
	}

	protected void tickPrinter() {
		ItemStack blueprint = inventory.getStackInSlot(0);

		// Skip if not Active
		if (state == State.STOPPED) {
			if (schematicLoaded)
				resetPrinter();
			return;
		}
		if (state == State.PAUSED && !missingBlock && fuelLevel > FUEL_USAGE_RATE)
			return;

		if (blueprint.isEmpty()) {
			state = State.STOPPED;
			statusMsg = "Idle";
			sendUpdate = true;
			return;
		}

		// Initialize Printer
		if (!schematicLoaded) {
			if (!blueprint.hasTag()) {
				state = State.STOPPED;
				statusMsg = "Invalid Blueprint";
				sendUpdate = true;
				return;
			}

			if (!blueprint.getTag().getBoolean("Deployed")) {
				state = State.STOPPED;
				statusMsg = "Blueprint not Deployed";
				sendUpdate = true;
				return;
			}

			currentPos = currentPos != null ? currentPos.west() : BlockPos.ZERO.west();
			schematicAnchor = NBTUtil.readBlockPos(blueprint.getTag().getCompound("Anchor"));

			// Load blocks into reader
			Template activeTemplate = ItemBlueprint.getSchematic(blueprint);
			blockReader = new SchematicWorld(new HashMap<>(), new Cuboid(), schematicAnchor);
			activeTemplate.addBlocksToWorld(blockReader, schematicAnchor, ItemBlueprint.getSettings(blueprint));
			schematicLoaded = true;
			sendUpdate = true;
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
			statusMsg = "Out of Gunpowder";
			sendUpdate = true;
			return;
		}

		// Update Target
		if (!missingBlock) {
			advanceCurrentPos();

			// End reached
			if (state == State.STOPPED)
				return;

			sendUpdate = true;
			target = schematicAnchor.add(currentPos);
		}

		// Check block
		BlockState blockState = blockReader.getBlockState(target);
		if (!shouldPlace(target, blockState))
			return;

		// Find Item
		ItemStack requiredItem = getItemForBlock(blockState);
		if (!findItemInAttachedInventories(requiredItem)) {
			if (skipMissing) {
				statusMsg = "Skipping";
				if (missingBlock) {
					missingBlock = false;
					state = State.RUNNING;
				}
				return;
			}

			missingBlock = true;
			state = State.PAUSED;
			statusMsg = "Missing " + blockState.getBlock().getNameTextComponent().getFormattedText();
			return;
		}

		// Success
		state = State.RUNNING;
		statusMsg = "Running...";
		launchBlock(target, blockState);
		printerCooldown = PLACEMENT_DELAY;
		fuelLevel -= FUEL_USAGE_RATE;
		sendUpdate = true;
		missingBlock = false;
	}

	protected ItemStack getItemForBlock(BlockState blockState) {
		return new ItemStack(BlockItem.BLOCK_TO_ITEM.getOrDefault(blockState.getBlock(), Items.AIR));
	}

	protected boolean findItemInAttachedInventories(ItemStack requiredItem) {
		if (hasCreativeCrate)
			return true;

		for (IItemHandler iItemHandler : attachedInventories) {
			for (int slot = 0; slot < iItemHandler.getSlots(); slot++) {
				ItemStack stackInSlot = iItemHandler.getStackInSlot(slot);
				if (!stackInSlot.isItemEqual(requiredItem))
					continue;
				if (!iItemHandler.extractItem(slot, 1, false).isEmpty())
					return true;
			}
		}
		return false;
	}

	protected void advanceCurrentPos() {
		BlockPos size = blockReader.getBounds().getSize();
		currentPos = currentPos.offset(Direction.EAST);

		schematicProgress += 1d / (size.getX() * size.getY() * size.getZ());
		
		if (currentPos.getX() > size.getX())
			currentPos = new BlockPos(0, currentPos.getY(), currentPos.getZ() + 1);
		if (currentPos.getZ() > size.getZ())
			currentPos = new BlockPos(currentPos.getX(), currentPos.getY() + 1, 0);

		// End reached
		if (currentPos.getY() > size.getY()) {
			inventory.setStackInSlot(0, ItemStack.EMPTY);
			inventory.setStackInSlot(1, new ItemStack(AllItems.EMPTY_BLUEPRINT.get()));
			state = State.STOPPED;
			statusMsg = "Finished";
			resetPrinter();
			target = getPos().add(1, 0, 0);
			world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_NOTE_BLOCK_BELL,
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
		missingBlock = false;
		sendUpdate = true;
		schematicProgress = 0;
	}

	protected boolean shouldPlace(BlockPos pos, BlockState state) {
		BlockState toReplace = world.getBlockState(pos);
		boolean placingAir = state.getBlock() == Blocks.AIR;

		if (toReplace.getBlockState() == state)
			return false;
		if (pos.withinDistance(getPos(), 2f))
			return false;
		if (!replaceTileEntities && toReplace.hasTileEntity())
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

	protected void tickFlyingBlocks() {
		List<LaunchedBlock> toRemove = new LinkedList<>();
		for (LaunchedBlock b : flyingBlocks) {
			b.update();
			if (b.ticksRemaining <= 0 && !world.isRemote) {
				world.setBlockState(b.target, b.state);
				toRemove.add(b);
			}
		}
		flyingBlocks.removeAll(toRemove);
	}

	protected void refillFuelIfPossible() {
		if (1 - fuelLevel + 1 / 128f < FUEL_PER_GUNPOWDER)
			return;
		if (inventory.getStackInSlot(4).isEmpty())
			return;

		inventory.getStackInSlot(4).shrink(1);
		fuelLevel += FUEL_PER_GUNPOWDER;
		sendUpdate = true;
	}

	protected void tickPaperPrinter() {
		int PaperInput = 2;
		int PaperOutput = 3;

		ItemStack paper = inventory.extractItem(PaperInput, 1, true);
		boolean outputFull = inventory.getStackInSlot(PaperOutput).getCount() == inventory.getSlotLimit(PaperOutput);

		if (paper.isEmpty() || outputFull) {
			if (paperPrintingProgress != 0)
				sendUpdate = true;
			paperPrintingProgress = 0;
			return;
		}

		if (paperPrintingProgress >= 1) {
			paperPrintingProgress = 0;
			inventory.extractItem(PaperInput, 1, false);
			inventory.setStackInSlot(PaperOutput,
					new ItemStack(Items.PAPER, inventory.getStackInSlot(PaperOutput).getCount() + 1));
			sendUpdate = true;
			return;
		}

		paperPrintingProgress += 0.05f;
		sendUpdate = true;
	}

	protected void launchBlock(BlockPos target, BlockState state) {
		flyingBlocks.add(new LaunchedBlock(target, state));
		Vec3d explosionPos = new Vec3d(pos).add(new Vec3d(target.subtract(pos)).normalize());
		this.world.createExplosion((Entity) null, explosionPos.x, explosionPos.y + 1.5f, explosionPos.z, 0,
				Explosion.Mode.NONE);
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

}
