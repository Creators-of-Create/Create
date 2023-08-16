package com.simibubi.create.content.equipment.zapper.terrainzapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.content.equipment.zapper.PlacementPatterns;
import com.simibubi.create.content.equipment.zapper.ZapperItem;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.catnip.utility.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class WorldshaperItem extends ZapperItem {

	public WorldshaperItem(Properties properties) {
		super(properties);
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	protected void openHandgunGUI(ItemStack item, InteractionHand hand) {
		ScreenOpener.open(new WorldshaperScreen(item, hand));
	}

	@Override
	protected int getZappingRange(ItemStack stack) {
		return 128;
	}

	@Override
	protected int getCooldownDelay(ItemStack item) {
		return 2;
	}

	@Override
	public Component validateUsage(ItemStack item) {
		if (!item.getOrCreateTag()
			.contains("BrushParams"))
			return CreateLang.translateDirect("terrainzapper.shiftRightClickToSet");
		return super.validateUsage(item);
	}

	@Override
	protected boolean canActivateWithoutSelectedBlock(ItemStack stack) {
		CompoundTag tag = stack.getOrCreateTag();
		TerrainTools tool = NBTHelper.readEnum(tag, "Tool", TerrainTools.class);
		return !tool.requiresSelectedBlock();
	}

	@Override
	protected boolean activate(Level world, Player player, ItemStack stack, BlockState stateToUse,
		BlockHitResult raytrace, CompoundTag data) {

		BlockPos targetPos = raytrace.getBlockPos();
		List<BlockPos> affectedPositions = new ArrayList<>();

		CompoundTag tag = stack.getOrCreateTag();
		Brush brush = NBTHelper.readEnum(tag, "Brush", TerrainBrushes.class)
			.get();
		BlockPos params = NbtUtils.readBlockPos(tag.getCompound("BrushParams"));
		PlacementOptions option = NBTHelper.readEnum(tag, "Placement", PlacementOptions.class);
		TerrainTools tool = NBTHelper.readEnum(tag, "Tool", TerrainTools.class);

		brush.set(params.getX(), params.getY(), params.getZ());
		targetPos = targetPos.offset(brush.getOffset(player.getLookAngle(), raytrace.getDirection(), option));
		brush.addToGlobalPositions(world, targetPos, raytrace.getDirection(), affectedPositions, tool);
		PlacementPatterns.applyPattern(affectedPositions, stack);
		brush.redirectTool(tool)
			.run(world, affectedPositions, raytrace.getDirection(), stateToUse, data, player);

		return true;
	}

	public static void configureSettings(ItemStack stack, PlacementPatterns pattern, TerrainBrushes brush,
		int brushParamX, int brushParamY, int brushParamZ, TerrainTools tool, PlacementOptions placement) {
		ZapperItem.configureSettings(stack, pattern);
		CompoundTag nbt = stack.getOrCreateTag();
		NBTHelper.writeEnum(nbt, "Brush", brush);
		nbt.put("BrushParams", NbtUtils.writeBlockPos(new BlockPos(brushParamX, brushParamY, brushParamZ)));
		NBTHelper.writeEnum(nbt, "Tool", tool);
		NBTHelper.writeEnum(nbt, "Placement", placement);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(SimpleCustomRenderer.create(this, new WorldshaperItemRenderer()));
	}

}
