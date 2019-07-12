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

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.ServerSchematicLoader;
import com.simibubi.create.schematic.Cuboid;
import com.simibubi.create.schematic.SchematicWorld;
import com.simibubi.create.utility.TileEntitySynced;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class SchematicannonTileEntity extends TileEntitySynced implements ITickableTileEntity {

	public static final int PLACEMENT_DELAY = 2;

	private SchematicWorld reader;
	private BlockPos currentPos;
	public BlockPos anchor;
	public String schematicToPrint;
	public boolean missingBlock;

	public List<IItemHandler> attachedInventories;

	private int cooldown;

	public SchematicannonTileEntity() {
		this(AllTileEntities.Schematicannon.type);
	}

	public SchematicannonTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		attachedInventories = new LinkedList<>();
	}

	public void findInventories() {
		for (Direction facing : Direction.values()) {
			TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
			if (tileEntity != null) {
				LazyOptional<IItemHandler> capability = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
				if (capability.isPresent()) {
					attachedInventories.add(capability.orElse(null));
				}
			}
		}
	}

	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		return super.write(compound);
	}

	@Override
	public void tick() {
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
			if (!missingBlock)
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
		
		missingBlock = true;
		ItemStack requiredItem = new ItemStack(BlockItem.BLOCK_TO_ITEM.getOrDefault(state.getBlock(), Items.AIR));
		for (IItemHandler iItemHandler : attachedInventories) {
			for (int slot = 0; slot < iItemHandler.getSlots(); slot++) {
				ItemStack stackInSlot = iItemHandler.getStackInSlot(slot);
				if (!stackInSlot.isItemEqual(requiredItem)) 
					continue;
				iItemHandler.extractItem(slot, 1, false);
				world.setBlockState(currentPos.add(anchor), state);
				missingBlock = false;
				return;
			}
		}

	}

}
