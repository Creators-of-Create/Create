package com.simibubi.create.content.contraptions.relays.belt.transport;

import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelControllerBlock;
import com.simibubi.create.content.contraptions.components.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;

public class BeltCrusherInteractionHandler {

    public static boolean checkForCrushers(BeltInventory beltInventory, TransportedItemStack currentItem,
                                           float nextOffset) {

        boolean beltMovementPositive = beltInventory.beltMovementPositive;
        int firstUpcomingSegment = (int) Math.floor(currentItem.beltPosition);
        int step = beltMovementPositive ? 1 : -1;
        firstUpcomingSegment = Mth.clamp(firstUpcomingSegment, 0, beltInventory.belt.beltLength - 1);

        for (int segment = firstUpcomingSegment; beltMovementPositive ? segment <= nextOffset
                : segment + 1 >= nextOffset; segment += step) {
            BlockPos crusherPos = BeltHelper.getPositionForOffset(beltInventory.belt, segment)
                    .above();
            Level world = beltInventory.belt.getLevel();
            BlockState crusherState = world.getBlockState(crusherPos);
            if (!(crusherState.getBlock() instanceof CrushingWheelControllerBlock))
                continue;
            Direction crusherFacing = crusherState.getValue(CrushingWheelControllerBlock.FACING);
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

            BlockEntity be = world.getBlockEntity(crusherPos);
            if (!(be instanceof CrushingWheelControllerBlockEntity))
                return true;

            CrushingWheelControllerBlockEntity crusherBE = (CrushingWheelControllerBlockEntity) be;

            ItemStack toInsert = currentItem.stack.copy();

            ItemStack remainder = ItemHandlerHelper.insertItemStacked(crusherBE.inventory, toInsert, false);
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
