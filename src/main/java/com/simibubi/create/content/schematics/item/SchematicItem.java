package com.simibubi.create.content.schematics.item;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.schematics.client.SchematicEditScreen;
import com.simibubi.create.content.schematics.filtering.SchematicInstances;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;

public class SchematicItem extends Item {

	public SchematicItem(Properties properties) {
		super(properties.maxStackSize(1));
	}

	public static ItemStack create(String schematic, String owner) {
		ItemStack blueprint = AllItems.SCHEMATIC.asStack();

		CompoundNBT tag = new CompoundNBT();
		tag.putBoolean("Deployed", false);
		tag.putString("Owner", owner);
		tag.putString("File", schematic);
		tag.put("Anchor", NBTUtil.writeBlockPos(BlockPos.ZERO));
		tag.putString("Rotation", Rotation.NONE.name());
		tag.putString("Mirror", Mirror.NONE.name());
		blueprint.setTag(tag);

		writeSize(blueprint);
		return blueprint;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTag()) {
			if (stack.getTag()
				.contains("File"))
				tooltip.add(new StringTextComponent(TextFormatting.GOLD + stack.getTag()
					.getString("File")));
		} else {
			tooltip.add(new StringTextComponent(TextFormatting.RED + Lang.translate("schematic.invalid")));
		}
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	public static void writeSize(ItemStack blueprint) {
		CompoundNBT tag = blueprint.getTag();
		Template t = loadSchematic(blueprint);
		tag.put("Bounds", NBTUtil.writeBlockPos(t.getSize()));
		blueprint.setTag(tag);
		SchematicInstances.clearHash(blueprint);
	}

	public static PlacementSettings getSettings(ItemStack blueprint) {
		CompoundNBT tag = blueprint.getTag();
		PlacementSettings settings = new PlacementSettings();
		settings.setRotation(Rotation.valueOf(tag.getString("Rotation")));
		settings.setMirror(Mirror.valueOf(tag.getString("Mirror")));
		return settings;
	}

	public static Template loadSchematic(ItemStack blueprint) {
		Template t = new Template();
		String owner = blueprint.getTag()
			.getString("Owner");
		String schematic = blueprint.getTag()
			.getString("File");

		String filepath = "";

		if (Thread.currentThread()
			.getThreadGroup() == SidedThreadGroups.SERVER)
			filepath = "schematics/uploaded/" + owner + "/" + schematic;
		else
			filepath = "schematics/" + schematic;

		InputStream stream = null;
		try {
			stream = Files.newInputStream(Paths.get(filepath), StandardOpenOption.READ);
			CompoundNBT nbt = CompressedStreamTools.readCompressed(stream);
			t.read(nbt);

		} catch (IOException e) {
			// Player/Server doesnt have schematic saved
		} finally {
			if (stream != null)
				IOUtils.closeQuietly(stream);
		}

		return t;
	}

	@Nonnull
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		if (context.getPlayer() != null && !onItemUse(context.getPlayer(), context.getHand()))
			return super.onItemUse(context);
		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if (!onItemUse(playerIn, handIn))
			return super.onItemRightClick(worldIn, playerIn, handIn);
		return new ActionResult<ItemStack>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
	}

	private boolean onItemUse(PlayerEntity player, Hand hand) {
		if (!player.isSneaking() || hand != Hand.MAIN_HAND)
			return false;
		if (!player.getHeldItem(hand)
			.hasTag())
			return false;
		DistExecutor.runWhenOn(Dist.CLIENT, () -> this::displayBlueprintScreen);
		return true;
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void displayBlueprintScreen() {
		ScreenOpener.open(new SchematicEditScreen());
	}

}
