package com.simibubi.create.foundation.item;

import static com.simibubi.create.foundation.item.AllToolTypes.AXE;
import static com.simibubi.create.foundation.item.AllToolTypes.HOE;
import static com.simibubi.create.foundation.item.AllToolTypes.PICKAXE;
import static com.simibubi.create.foundation.item.AllToolTypes.SHOVEL;
import static com.simibubi.create.foundation.item.AllToolTypes.SWORD;

import java.util.Collections;
import java.util.List;

import com.simibubi.create.foundation.utility.ITooltip;
import com.simibubi.create.foundation.utility.TooltipHolder;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public abstract class AbstractToolItem extends ToolItem implements ITooltip {

	protected TooltipHolder info;
	protected AllToolTypes[] toolTypes;

	public AbstractToolItem(float attackDamageIn, float attackSpeedIn, IItemTier tier, Properties builder,
			AllToolTypes... types) {
		super(attackDamageIn, attackSpeedIn, tier, Collections.emptySet(), setToolTypes(builder, tier, types));
		info = new TooltipHolder(this);
		toolTypes = types;
	}

	private static Properties setToolTypes(Properties builder, IItemTier tier, AllToolTypes... types) {
		for (AllToolTypes type : types) {
			if (type == PICKAXE)
				builder.addToolType(ToolType.PICKAXE, tier.getHarvestLevel());
			if (type == SHOVEL)
				builder.addToolType(ToolType.SHOVEL, tier.getHarvestLevel());
			if (type == AXE)
				builder.addToolType(ToolType.AXE, tier.getHarvestLevel());
		}
		return builder;
	}

	protected boolean hasType(AllToolTypes typeIn) {
		for (AllToolTypes type : toolTypes)
			if (type == typeIn)
				return true;
		return false;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		info.addInformation(tooltip);
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		if (hasType(HOE) && !context.isPlacerSneaking())
			return Items.WOODEN_HOE.onItemUse(context);
		if (hasType(SHOVEL))
			return Items.WOODEN_SHOVEL.onItemUse(context);
		return super.onItemUse(context);
	}

	@Override
	public boolean canHarvestBlock(ItemStack stack, BlockState state) {
		return super.canHarvestBlock(stack, state)
				|| hasType(SWORD) && Items.WOODEN_SWORD.canHarvestBlock(stack, state);
	}

	@Override
	public boolean canPlayerBreakBlockWhileHolding(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		return hasType(SWORD) && !player.isCreative();
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		if (hasType(SWORD))
			return Items.WOODEN_SWORD.getDestroySpeed(stack, state);
		return super.getDestroySpeed(stack, state);
	}

	@SubscribeEvent
	public static void onHarvestDrops(HarvestDropsEvent event) {
		PlayerEntity harvester = event.getHarvester();
		if (harvester == null)
			return;

		ItemStack tool = harvester.getHeldItemMainhand();
		if (tool.isEmpty() || !(tool.getItem() instanceof AbstractToolItem))
			return;

		if (event.getDrops() != null)
			((AbstractToolItem) tool.getItem()).modifyDrops(event.getDrops(), event.getWorld(), event.getPos(), tool,
					event.getState());
	}

	public void modifyDrops(final List<ItemStack> drops, IWorld world, BlockPos pos, ItemStack tool, BlockState state) {
	}

}
