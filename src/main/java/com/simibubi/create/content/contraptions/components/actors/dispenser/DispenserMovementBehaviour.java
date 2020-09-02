package com.simibubi.create.content.contraptions.components.actors.dispenser;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;

public class DispenserMovementBehaviour extends DropperMovementBehaviour {
	private static final HashMap<Item, IMovedDispenseItemBehaviour> MOVED_DISPENSE_ITEM_BEHAVIOURS = new HashMap<>();

	public static void gatherMovedDispenseItemBehaviours() {
		IMovedDispenseItemBehaviour.init();
	}

	public static void registerMovedDispenseItemBehaviour(Item item, IMovedDispenseItemBehaviour movedDispenseItemBehaviour) {
		MOVED_DISPENSE_ITEM_BEHAVIOURS.put(item, movedDispenseItemBehaviour);
	}

	@Override
	protected void activate(MovementContext context, BlockPos pos) {
		int i = getDispenseSlot(context);
		if (i < 0) {
			context.world.playEvent(1001, pos, 0);
		} else {
			ItemStack itemstack = getStacks(context).get(i);

			// Special dispense item behaviour for moving contraptions
			if (MOVED_DISPENSE_ITEM_BEHAVIOURS.containsKey(itemstack.getItem())) {
				MOVED_DISPENSE_ITEM_BEHAVIOURS.get(itemstack.getItem()).dispense(itemstack, context, pos);
				return;
			}

			ItemStack backup = itemstack.copy();
			// If none is there, try vanilla registry
			try {
				Vec3d facingVec = new Vec3d(context.state.get(DispenserBlock.FACING).getDirectionVec());
				facingVec = VecHelper.rotate(facingVec, context.rotation.x, context.rotation.y, context.rotation.z);
				facingVec.normalize();
				Direction clostestFacing = Direction.getFacingFromVector(facingVec.x, facingVec.y, facingVec.z);
				ContraptionBlockSource blockSource = new ContraptionBlockSource(context, pos, clostestFacing);
				IDispenseItemBehavior idispenseitembehavior = ((DispenserBlock) Blocks.DISPENSER).getBehavior(itemstack);
				if (idispenseitembehavior.getClass() != DefaultDispenseItemBehavior.class) { // There is a dispense item behaviour registered for the vanilla dispenser
					idispenseitembehavior.dispense(blockSource, itemstack);
					return;
				}
			} catch (NullPointerException e) {
				itemstack = backup; // Something went wrong with the TE being null in ContraptionBlockSource, reset the stack
			}

			defaultBehaviour.dispense(itemstack, context, pos);  // the default: launch the item
		}
	}
}
