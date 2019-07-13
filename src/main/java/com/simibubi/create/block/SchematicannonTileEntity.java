package com.simibubi.create.block;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.ServerSchematicLoader;
import com.simibubi.create.schematic.Cuboid;
import com.simibubi.create.schematic.SchematicWorld;
import com.simibubi.create.utility.TileEntitySynced;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class SchematicannonTileEntity extends TileEntitySynced implements ITickableTileEntity {

	public static final int PLACEMENT_DELAY = 10;

	private SchematicWorld reader;
	public BlockPos currentPos;
	public BlockPos anchor;
	public String schematicToPrint;
	public boolean missingBlock;
	public boolean creative;

	public BlockPos target;
	public BlockPos previousTarget;

	public List<IItemHandler> attachedInventories;
	public List<LaunchedBlock> flyingBlocks;

	private int cooldown;

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
	}

	public void findInventories() {
		creative = false;
		for (Direction facing : Direction.values()) {

			if (AllBlocks.CREATIVE_CRATE.typeOf(world.getBlockState(pos.offset(facing)))) {
				creative = true;
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
		if (compound.contains("Target")) {
			target = NBTUtil.readBlockPos(compound.getCompound("Target"));
		}

		if (compound.contains("FlyingBlocks")) {

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
				if (!world.isRemote) {
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

		super.read(compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		if (target != null) {
			compound.put("Target", NBTUtil.writeBlockPos(target));
		}

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

		return super.write(compound);
	}

	@Override
	public void tick() {
		previousTarget = target;

		List<LaunchedBlock> toRemove = new LinkedList<>();
		for (LaunchedBlock b : flyingBlocks) {
			b.update();
			if (b.ticksRemaining <= 0 && !world.isRemote) {
				world.setBlockState(b.target, b.state);
				toRemove.add(b);
			}
		}
		flyingBlocks.removeAll(toRemove);

		if (world.isRemote)
			return;
		if (schematicToPrint == null)
			return;
		if (cooldown-- > 0)
			return;
		cooldown = PLACEMENT_DELAY;

		if (reader == null) {
			currentPos = BlockPos.ZERO;
			currentPos = currentPos.offset(Direction.WEST);

			String filepath = ServerSchematicLoader.PATH + "/" + schematicToPrint;
			Template activeTemplate = new Template();

			InputStream stream = null;
			try {
				stream = Files.newInputStream(Paths.get(filepath), StandardOpenOption.READ);
				CompoundNBT nbt = CompressedStreamTools.readCompressed(stream);
				activeTemplate.read(nbt);
				reader = new SchematicWorld(new HashMap<>(), new Cuboid(BlockPos.ZERO, 0, 0, 0), anchor);
				activeTemplate.addBlocksToWorld(reader, anchor, new PlacementSettings());

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (stream != null)
					IOUtils.closeQuietly(stream);
			}
			return;
		}

		BlockPos size = reader.getBounds().getSize();
		BlockState state;
		do {
			// Find next block to place
			if (!missingBlock || creative)
				currentPos = currentPos.offset(Direction.EAST);
			if (currentPos.getX() > size.getX()) {
				currentPos = new BlockPos(0, currentPos.getY(), currentPos.getZ() + 1);
			}
			if (currentPos.getZ() > size.getZ()) {
				currentPos = new BlockPos(currentPos.getX(), currentPos.getY() + 1, 0);
			}
			if (currentPos.getY() > size.getY()) {
				schematicToPrint = null;
				currentPos = null;
				anchor = null;
				reader = null;
				missingBlock = false;
				return;
			}
			state = reader.getBlockState(anchor.add(currentPos));
		} while (state.getBlock() == Blocks.AIR);

		target = anchor.add(currentPos);

		// Update orientation
		world.notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 3);

		if (creative) {
			launchBlock(currentPos.add(anchor), state);
			missingBlock = false;
			return;
		}

		if (world.getBlockState(target).getBlock() == state.getBlock()) {
			// Don't overwrite tiles
			if (world.getTileEntity(target) != null)
				return;

			// Overwrite in case its rotated
			launchBlock(target, state);
			missingBlock = false;
		}

		// Search for required item
		missingBlock = true;
		ItemStack requiredItem = new ItemStack(BlockItem.BLOCK_TO_ITEM.getOrDefault(state.getBlock(), Items.AIR));
		for (IItemHandler iItemHandler : attachedInventories) {
			for (int slot = 0; slot < iItemHandler.getSlots(); slot++) {
				ItemStack stackInSlot = iItemHandler.getStackInSlot(slot);
				if (!stackInSlot.isItemEqual(requiredItem))
					continue;
				iItemHandler.extractItem(slot, 1, false);
				launchBlock(target, state);
				missingBlock = false;
				return;
			}
		}
	}

	private void launchBlock(BlockPos target, BlockState state) {
		flyingBlocks.add(new LaunchedBlock(target, state));
		Vec3d explosionPos = new Vec3d(pos).add(new Vec3d(target.subtract(pos)).normalize());
		this.world.createExplosion((Entity) null, explosionPos.x, explosionPos.y + 1.5f, explosionPos.z, 0,
				Explosion.Mode.NONE);
	}

}
