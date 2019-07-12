package com.simibubi.create.item;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.block.SchematicannonTileEntity;
import com.simibubi.create.schematic.SchematicHologram;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;

public class ItemBlueprint extends Item {

	public ItemBlueprint(Properties properties) {
		super(properties.maxStackSize(1));
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {

		World world = context.getWorld();

		CompoundNBT tag = context.getItem().getTag();
		if (tag.contains("File")) {

			BlockPos pos = context.getPos();
			BlockState blockState = world.getBlockState(pos);
			if (AllBlocks.SCHEMATICANNON.typeOf(blockState)) {
				if (world.isRemote) {
					SchematicHologram.reset();
					return ActionResultType.SUCCESS;
				}
				if (!tag.contains("Anchor"))
					return ActionResultType.FAIL;

				SchematicannonTileEntity te = (SchematicannonTileEntity) world.getTileEntity(pos);
				te.schematicToPrint = tag.getString("Owner") + "/" + tag.getString("File");
				te.anchor = NBTUtil.readBlockPos(tag.getCompound("Anchor"));
				context.getPlayer().setItemStackToSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
				return ActionResultType.SUCCESS;
			}

			tag.put("Anchor", NBTUtil.writeBlockPos(pos.offset(context.getFace())));

			if (!world.isRemote) {
				return ActionResultType.SUCCESS;
			}

			if (!(context.getPlayer().getName().getFormattedText().equals(tag.getString("Owner")))) {
				context.getPlayer()
						.sendStatusMessage(new StringTextComponent("You are not the Owner of this Schematic."), true);
			}

			String filepath = "schematics/" + tag.getString("File");
			Template t = new Template();

			InputStream stream = null;
			try {
				stream = Files.newInputStream(Paths.get(filepath), StandardOpenOption.READ);
				CompoundNBT nbt = CompressedStreamTools.readCompressed(stream);
				t.read(nbt);
				new SchematicHologram().startHologram(t, pos.offset(context.getFace()));

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (stream != null)
					IOUtils.closeQuietly(stream);
			}

		}

		context.getPlayer().getCooldownTracker().setCooldown(this, 10);
		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

}
