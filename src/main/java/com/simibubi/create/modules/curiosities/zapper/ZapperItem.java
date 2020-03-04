package com.simibubi.create.modules.curiosities.zapper;

import java.util.List;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public abstract class ZapperItem extends Item {

	public ZapperItem(Properties properties) {
		super(properties.maxStackSize(1).rarity(Rarity.UNCOMMON));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTag() && stack.getTag().contains("BlockUsed")) {
			String usedblock = NBTUtil.readBlockState(stack.getTag().getCompound("BlockUsed")).getBlock()
					.getTranslationKey();
			ItemDescription.add(tooltip, TextFormatting.DARK_GRAY + Lang.translate("blockzapper.usingBlock",
					TextFormatting.GRAY + new TranslationTextComponent(usedblock).getFormattedText()));
		}
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		// Shift -> open GUI
		if (context.isPlacerSneaking()) {
			if (context.getWorld().isRemote) {
				DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
					openHandgunGUI(context.getItem(), context.getHand() == Hand.OFF_HAND);
				});
				applyCooldown(context.getPlayer(), context.getItem(), false);
			}
			return ActionResultType.SUCCESS;
		}
		return super.onItemUse(context);
	}

	@OnlyIn(Dist.CLIENT)
	protected abstract void openHandgunGUI(ItemStack item, boolean b);

	protected abstract int getCooldownDelay(ItemStack item);

	protected void applyCooldown(PlayerEntity playerIn, ItemStack item, boolean dual) {
		int delay = getCooldownDelay(item);
		playerIn.getCooldownTracker().setCooldown(item.getItem(), dual ? delay * 2 / 3 : delay);
	}

	@Override
	public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		return false;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.NONE;
	}

}
