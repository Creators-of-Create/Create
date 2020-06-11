package com.simibubi.create.content.contraptions.components.structureMovement.mounted;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class CartAssemblerBlockItem extends BlockItem {

    public CartAssemblerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    @Nonnull
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockPos pos = context.getPos();
        World world = context.getWorld();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        PlayerEntity player = context.getPlayer();
        if (block instanceof AbstractRailBlock && player != null) {
            RailShape shape = state.get(((AbstractRailBlock) block).getShapeProperty());
            if (shape == RailShape.EAST_WEST || shape == RailShape.NORTH_SOUTH) {
                BlockState defaultState = AllBlocks.CART_ASSEMBLER.getDefaultState().with(CartAssemblerBlock.RAIL_SHAPE, shape);
                if (block.getClass() == RailBlock.class) {
                    world.setBlockState(pos, defaultState.with(CartAssemblerBlock.RAIL_TYPE, CartAssemblerBlock.RAIL_NORMAL));
                } else if (block.getClass() == PoweredRailBlock.class) {
                    if (((PoweredRailBlock) block).isActivatorRail()) {
                        world.setBlockState(pos, defaultState.with(CartAssemblerBlock.RAIL_TYPE, CartAssemblerBlock.RAIL_ACTIVATOR));
                    } else {
                        world.setBlockState(pos, defaultState.with(CartAssemblerBlock.RAIL_TYPE, CartAssemblerBlock.RAIL_POWERED));
                    }

                } else if (block.getClass() == DetectorRailBlock.class) {
                    world.setBlockState(pos, defaultState.with(CartAssemblerBlock.RAIL_TYPE, CartAssemblerBlock.RAIL_DETECTOR));
                } else {
                    player.sendStatusMessage(new StringTextComponent(Lang.translate("block.cart_assembler.invalid")), true);
                    return super.onItemUse(context);
                }
                if (!player.isCreative())
                    context.getItem().setCount(context.getItem().getCount() - 1);
                return ActionResultType.SUCCESS;
            }
            player.sendStatusMessage(new StringTextComponent(Lang.translate("block.cart_assembler.invalid")), true);
        }
        return super.onItemUse(context);
    }
}