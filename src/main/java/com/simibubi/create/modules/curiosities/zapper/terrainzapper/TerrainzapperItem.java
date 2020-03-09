package com.simibubi.create.modules.curiosities.zapper.terrainzapper;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.block.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.gui.ScreenOpener;
import com.simibubi.create.foundation.item.IHaveCustomItemModel;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.modules.curiosities.zapper.PlacementPatterns;
import com.simibubi.create.modules.curiosities.zapper.ZapperItem;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TerrainzapperItem extends ZapperItem implements IHaveCustomItemModel {

	public TerrainzapperItem(Properties properties) {
		super(properties);
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	protected void openHandgunGUI(ItemStack item, boolean b) {
		ScreenOpener.open(new TerrainzapperScreen(item, b));
	}

	@Override
	protected int getRange(ItemStack stack) {
		return 128;
	}

	@Override
	protected int getCooldownDelay(ItemStack item) {
		return 2;
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public CustomRenderedItemModel createModel(IBakedModel original) {
		return new TerrainzapperModel(original);
	}

	@Override
	public String validateUsage(ItemStack item) {
		if (!item.getOrCreateTag().contains("BrushParams"))
			return Lang.translate("terrainzapper.shiftRightClickToSet");
		return super.validateUsage(item);
	}

	@Override
	protected boolean canActivateWithoutSelectedBlock(ItemStack stack) {
		CompoundNBT tag = stack.getOrCreateTag();
		TerrainTools tool = NBTHelper.readEnum(tag.getString("Tool"), TerrainTools.class);
		return !tool.requiresSelectedBlock();
	}

	@Override
	protected boolean activate(World world, PlayerEntity player, ItemStack stack, BlockState stateToUse,
			BlockRayTraceResult raytrace) {

		BlockPos targetPos = raytrace.getPos();
		List<BlockPos> affectedPositions = new ArrayList<>();

		CompoundNBT tag = stack.getOrCreateTag();
		Brush brush = NBTHelper.readEnum(tag.getString("Brush"), TerrainBrushes.class).get();
		BlockPos params = NBTUtil.readBlockPos(tag.getCompound("BrushParams"));
		PlacementOptions option = NBTHelper.readEnum(tag.getString("Placement"), PlacementOptions.class);
		TerrainTools tool = NBTHelper.readEnum(tag.getString("Tool"), TerrainTools.class);

		brush.set(params.getX(), params.getY(), params.getZ());
		targetPos = targetPos.add(brush.getOffset(player.getLookVec(), raytrace.getFace(), option));
		for (BlockPos blockPos : brush.getIncludedPositions())
			affectedPositions.add(targetPos.add(blockPos));
		PlacementPatterns.applyPattern(affectedPositions, stack);
		tool.run(world, affectedPositions, raytrace.getFace(), stateToUse);

		return true;
	}

}
