package com.simibubi.create.content.contraptions.relays.belt.transport;

import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelControllerBlock;
import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelControllerTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class BeltCrusherInteractionHandler {

    public static boolean checkForCrushers(BeltInventory beltInventory, TransportedItemStack currentItem,
                                           float nextOffset) {

        boolean beltMovementPositive = beltInventory.beltMovementPositive;
        int firstUpcomingSegment = (int) Math.floor(currentItem.beltPosition);
        int step = beltMovementPositive ? 1 : -1;
        firstUpcomingSegment = MathHelper.clamp(firstUpcomingSegment, 0, beltInventory.belt.beltLength - 1);

        for (int segment = firstUpcomingSegment; beltMovementPositive ? segment <= nextOffset
                : segment + 1 >= nextOffset; segment += step) {
            BlockPos crusherPos = BeltHelper.getPositionForOffset(beltInventory.belt, segment)
                    .up();
            World world = beltInventory.belt.getWorld();
            BlockState crusherState = world.getBlockState(crusherPos);
            if (!(crusherState.getBlock() instanceof CrushingWheelControllerBlock))
                continue;
            Direction crusherFacing = crusherState.get(CrushingWheelControllerBlock.FACING);
            Direction movementFacing = beltInventory.belt.getMovementFacing();
            if (crusherFacing != movementFacing)
                continue;

            float crusherEntry = segment + .5f;
            crusherEntry += .399f * (beltMovementPositive ? -1 : 1);
            float postCrusherEntry = crusherEntry + .799f * (!beltMovementPositive ? -1 : 1);

            boolean hasCrossed = nextOffset > crusherEntry && nextOffset < postCrusherEntry && beltMovementPositive
                    || nextOffset < crusherEntry && nextOffset > postCrusherEntry && !beltMovementPositive;
            if (!hasCrossed)
                return false;
            currentItem.beltPosition = crusherEntry;

            TileEntity te = world.getTileEntity(crusherPos);
            if (!(te instanceof CrushingWheelControllerTileEntity))
                return true;

            CrushingWheelControllerTileEntity crusherTE = (CrushingWheelControllerTileEntity) te;

            ItemStack toInsert = currentItem.stack.copy();

            ItemStack remainder = ItemHandlerHelper.insertItemStacked(crusherTE.inventory, toInsert, false);
            if (toInsert.equals(remainder, false))
                return true;

            int notFilled = currentItem.stack.getCount() - toInsert.getCount();
            if (!remainder.isEmpty()) {
                remainder.grow(notFilled);
            } else if (notFilled > 0)
                remainder = ItemHandlerHelper.copyStackWithSize(currentItem.stack, notFilled);

            currentItem.stack = remainder;
            beltInventory.belt.sendData();
            return true;
        }

        return false;
    }


}
