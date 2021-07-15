package com.simibubi.create.content.contraptions.components.actors.dispenser;

import java.util.HashMap;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import net.minecraft.block.AbstractBlock;

public class DispenserMovementBehaviour extends DropperMovementBehaviour {
	private static final HashMap<Item, IMovedDispenseItemBehaviour> MOVED_DISPENSE_ITEM_BEHAVIOURS = new HashMap<>();
	private static final HashMap<Item, IMovedDispenseItemBehaviour> MOVED_PROJECTILE_DISPENSE_BEHAVIOURS = new HashMap<>();
	private static final DispenserLookup BEHAVIOUR_LOOKUP = new DispenserLookup();
	private static boolean spawneggsRegistered = false;

	public static void gatherMovedDispenseItemBehaviours() {
		IMovedDispenseItemBehaviour.init();
	}

	public static void registerMovedDispenseItemBehaviour(Item item, IMovedDispenseItemBehaviour movedDispenseItemBehaviour) {
		MOVED_DISPENSE_ITEM_BEHAVIOURS.put(item, movedDispenseItemBehaviour);
	}

	@Override
	protected void activate(MovementContext context, BlockPos pos) {
		if (!spawneggsRegistered) {
			spawneggsRegistered = true;
			IMovedDispenseItemBehaviour.initSpawneggs();
		}
		
		DispenseItemLocation location = getDispenseLocation(context);
		if (location.isEmpty()) {
			context.world.levelEvent(1001, pos, 0);
		} else {
			ItemStack itemstack = getItemStackAt(location, context);
			// Special dispense item behaviour for moving contraptions
			if (MOVED_DISPENSE_ITEM_BEHAVIOURS.containsKey(itemstack.getItem())) {
				setItemStackAt(location, MOVED_DISPENSE_ITEM_BEHAVIOURS.get(itemstack.getItem()).dispense(itemstack, context, pos), context);
				return;
			}

			ItemStack backup = itemstack.copy();
			// If none is there, try vanilla registry
			try {
				if (MOVED_PROJECTILE_DISPENSE_BEHAVIOURS.containsKey(itemstack.getItem())) {
					setItemStackAt(location, MOVED_PROJECTILE_DISPENSE_BEHAVIOURS.get(itemstack.getItem()).dispense(itemstack, context, pos), context);
					return;
				}

				IDispenseItemBehavior idispenseitembehavior = BEHAVIOUR_LOOKUP.getDispenseMethod(itemstack);
				if (idispenseitembehavior instanceof ProjectileDispenseBehavior) { // Projectile behaviours can be converted most of the time
					IMovedDispenseItemBehaviour iMovedDispenseItemBehaviour = MovedProjectileDispenserBehaviour.of((ProjectileDispenseBehavior) idispenseitembehavior);
					setItemStackAt(location, iMovedDispenseItemBehaviour.dispense(itemstack, context, pos), context);
					MOVED_PROJECTILE_DISPENSE_BEHAVIOURS.put(itemstack.getItem(), iMovedDispenseItemBehaviour); // buffer conversion if successful
					return;
				}

				Vector3d facingVec = Vector3d.atLowerCornerOf(context.state.getValue(DispenserBlock.FACING).getNormal());
				facingVec = context.rotation.apply(facingVec);
				facingVec.normalize();
				Direction clostestFacing = Direction.getNearest(facingVec.x, facingVec.y, facingVec.z);
				ContraptionBlockSource blockSource = new ContraptionBlockSource(context, pos, clostestFacing);

				if (idispenseitembehavior.getClass() != DefaultDispenseItemBehavior.class) { // There is a dispense item behaviour registered for the vanilla dispenser
					setItemStackAt(location, idispenseitembehavior.dispense(blockSource, itemstack), context);
					return;
				}
			} catch (NullPointerException ignored) {
				itemstack = backup; // Something went wrong with the TE being null in ContraptionBlockSource, reset the stack
			}

			setItemStackAt(location, defaultBehaviour.dispense(itemstack, context, pos), context);  // the default: launch the item
		}
	}

	@ParametersAreNonnullByDefault
	@MethodsReturnNonnullByDefault
	private static class DispenserLookup extends DispenserBlock {
		protected DispenserLookup() {
			super(AbstractBlock.Properties.copy(Blocks.DISPENSER));
		}

		public IDispenseItemBehavior getDispenseMethod(ItemStack itemStack) {
			return super.getDispenseMethod(itemStack);
		}
	}
}
