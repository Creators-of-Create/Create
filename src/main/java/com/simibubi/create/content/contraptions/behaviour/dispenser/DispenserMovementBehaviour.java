package com.simibubi.create.content.contraptions.behaviour.dispenser;

import java.util.HashMap;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.mixin.accessor.DispenserBlockAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public class DispenserMovementBehaviour extends DropperMovementBehaviour {
	private static final HashMap<Item, IMovedDispenseItemBehaviour> MOVED_DISPENSE_ITEM_BEHAVIOURS = new HashMap<>();
	private static final HashMap<Item, IMovedDispenseItemBehaviour> MOVED_PROJECTILE_DISPENSE_BEHAVIOURS = new HashMap<>();
	private static boolean spawnEggsRegistered = false;

	public static void gatherMovedDispenseItemBehaviours() {
		IMovedDispenseItemBehaviour.init();
	}

	public static void registerMovedDispenseItemBehaviour(Item item,
		IMovedDispenseItemBehaviour movedDispenseItemBehaviour) {
		MOVED_DISPENSE_ITEM_BEHAVIOURS.put(item, movedDispenseItemBehaviour);
	}

	public static DispenseItemBehavior getDispenseMethod(ItemStack itemstack) {
		return ((DispenserBlockAccessor) Blocks.DISPENSER).create$callGetDispenseMethod(itemstack);
	}

	@Override
	protected void activate(MovementContext context, BlockPos pos) {
		if (!spawnEggsRegistered) {
			spawnEggsRegistered = true;
			IMovedDispenseItemBehaviour.initSpawnEggs();
		}

		DispenseItemLocation location = getDispenseLocation(context);
		if (location.isEmpty()) {
			context.world.levelEvent(1001, pos, 0);
		} else {
			ItemStack itemStack = getItemStackAt(location, context);
			// Special dispense item behaviour for moving contraptions
			if (MOVED_DISPENSE_ITEM_BEHAVIOURS.containsKey(itemStack.getItem())) {
				setItemStackAt(location, MOVED_DISPENSE_ITEM_BEHAVIOURS.get(itemStack.getItem()).dispense(itemStack, context, pos), context);
				return;
			}

			ItemStack backup = itemStack.copy();
			// If none is there, try vanilla registry
			try {
				if (MOVED_PROJECTILE_DISPENSE_BEHAVIOURS.containsKey(itemStack.getItem())) {
					setItemStackAt(location, MOVED_PROJECTILE_DISPENSE_BEHAVIOURS.get(itemStack.getItem()).dispense(itemStack, context, pos), context);
					return;
				}

				DispenseItemBehavior behavior = getDispenseMethod(itemStack);
				if (behavior instanceof AbstractProjectileDispenseBehavior) { // Projectile behaviours can be converted most of the time
					IMovedDispenseItemBehaviour movedBehaviour = MovedProjectileDispenserBehaviour.of((AbstractProjectileDispenseBehavior) behavior);
					setItemStackAt(location, movedBehaviour.dispense(itemStack, context, pos), context);
					MOVED_PROJECTILE_DISPENSE_BEHAVIOURS.put(itemStack.getItem(), movedBehaviour); // buffer conversion if successful
					return;
				}

				Vec3 facingVec = Vec3.atLowerCornerOf(context.state.getValue(DispenserBlock.FACING).getNormal());
				facingVec = context.rotation.apply(facingVec);
				facingVec.normalize();
				Direction clostestFacing = Direction.getNearest(facingVec.x, facingVec.y, facingVec.z);
				ContraptionBlockSource blockSource = new ContraptionBlockSource(context, pos, clostestFacing);

				if (behavior.getClass() != DefaultDispenseItemBehavior.class) { // There is a dispense item behaviour registered for the vanilla dispenser
					setItemStackAt(location, behavior.dispense(blockSource, itemStack), context);
					return;
				}
			} catch (NullPointerException ignored) {
				itemStack = backup; // Something went wrong with the BE being null in ContraptionBlockSource, reset the stack
			}

			setItemStackAt(location, DEFAULT_BEHAVIOUR.dispense(itemStack, context, pos), context);  // the default: launch the item
		}
	}

}
