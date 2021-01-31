package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.curiosities.zapper.PlacementPatterns;
import com.simibubi.create.content.curiosities.zapper.ZapperItem;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WorldshaperItem extends ZapperItem {

	public WorldshaperItem(Properties properties) {
		super(properties);
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	protected void openHandgunGUI(ItemStack item, boolean b) {
		ScreenOpener.open(new WorldshaperScreen(item, b));
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
	public ITextComponent validateUsage(ItemStack item) {
		if (!item.getOrCreateTag()
			.contains("BrushParams"))
			return Lang.createTranslationTextComponent("terrainzapper.shiftRightClickToSet");
		return super.validateUsage(item);
	}

	@Override
	protected boolean canActivateWithoutSelectedBlock(ItemStack stack) {
		CompoundNBT tag = stack.getOrCreateTag();
		TerrainTools tool = NBTHelper.readEnum(tag, "Tool", TerrainTools.class);
		return !tool.requiresSelectedBlock();
	}

	@Override
	protected boolean activate(World world, PlayerEntity player, ItemStack stack, BlockState stateToUse,
		BlockRayTraceResult raytrace, CompoundNBT data) {

		BlockPos targetPos = raytrace.getPos();
		List<BlockPos> affectedPositions = new ArrayList<>();

		CompoundNBT tag = stack.getOrCreateTag();
		Brush brush = NBTHelper.readEnum(tag, "Brush", TerrainBrushes.class)
			.get();
		BlockPos params = NBTUtil.readBlockPos(tag.getCompound("BrushParams"));
		PlacementOptions option = NBTHelper.readEnum(tag, "Placement", PlacementOptions.class);
		TerrainTools tool = NBTHelper.readEnum(tag, "Tool", TerrainTools.class);

		brush.set(params.getX(), params.getY(), params.getZ());
		targetPos = targetPos.add(brush.getOffset(player.getLookVec(), raytrace.getFace(), option));
		for (BlockPos blockPos : brush.getIncludedPositions())
			affectedPositions.add(targetPos.add(blockPos));
		PlacementPatterns.applyPattern(affectedPositions, stack);
		tool.run(world, affectedPositions, raytrace.getFace(), stateToUse, data, player);

		return true;
	}

}
