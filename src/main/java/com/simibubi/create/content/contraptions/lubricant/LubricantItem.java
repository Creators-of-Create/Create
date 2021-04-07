package com.simibubi.create.content.contraptions.lubricant;

import com.simibubi.create.content.contraptions.base.IFriction;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

public class LubricantItem extends Item {

    public LubricantItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockState state = world.getBlockState(context.getPos());
        TileEntity tileEntity = state.hasTileEntity() ? world.getTileEntity(context.getPos()) : null;
        if(tileEntity != null) {
            if(tileEntity instanceof IFriction) {
                IFriction frictionTE = (IFriction) tileEntity;
                frictionTE.increaseLubricantAmount(0.1f);
            }
        }
        return ActionResultType.PASS;
    }
}
