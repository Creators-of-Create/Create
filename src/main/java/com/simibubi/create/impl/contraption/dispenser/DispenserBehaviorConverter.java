package com.simibubi.create.impl.contraption.dispenser;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.Create;
import com.simibubi.create.api.contraption.dispenser.DefaultMountedDispenseBehavior;
import com.simibubi.create.api.contraption.dispenser.MountedDispenseBehavior;
import com.simibubi.create.api.contraption.dispenser.MountedProjectileDispenseBehavior;
import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.mixin.accessor.DispenserBlockAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public enum DispenserBehaviorConverter implements SimpleRegistry.Provider<Item, MountedDispenseBehavior> {
	INSTANCE;

	@Override
	@Nullable
	public MountedDispenseBehavior get(Item item) {
		DispenseItemBehavior vanilla = getDispenseMethod(new ItemStack(item));
		if (vanilla == null)
			return null;

		// when the default, return null. The default will be used anyway, avoid caching it for no reason.
		if (vanilla.getClass() == DefaultDispenseItemBehavior.class)
			return null;

		// if the item is explicitly blocked from having its behavior wrapped, ignore it
		if (AllItemTags.DISPENSE_BEHAVIOR_WRAP_BLACKLIST.matches(item))
			return null;

		if (vanilla instanceof ProjectileDispenseBehavior projectile) {
			return MountedProjectileDispenseBehavior.of(projectile);
		}

		// other behaviors are more dangerous due to BlockSource providing a BlockEntity, which contraptions can't do.
		// wrap in a fallback that will watch for errors.
		return new FallbackBehavior(item, vanilla);
	}

	@Override
	public void onRegister(Runnable invalidate) {
		// invalidate if the blacklist tag might've changed
		NeoForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> {
			if (event.shouldUpdateStaticData()) {
				invalidate.run();
			}
		});
	}

	@Nullable
	private static DispenseItemBehavior getDispenseMethod(ItemStack stack) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null)
			return null;

		return ((DispenserBlockAccessor) Blocks.DISPENSER).create$callGetDispenseMethod(server.getLevel(Level.OVERWORLD), stack);
	}

	private static final class FallbackBehavior extends DefaultMountedDispenseBehavior {
		private final Item item;
		private final DispenseItemBehavior wrapped;
		private boolean hasErrored;

		private FallbackBehavior(Item item, DispenseItemBehavior wrapped) {
			this.item = item;
			this.wrapped = wrapped;
		}

		@Override
		protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3 facing) {
			if (this.hasErrored)
				return stack;

			MinecraftServer server = context.world.getServer();
			ServerLevel serverLevel = server != null ? server.getLevel(context.world.dimension()) : null;

			Direction nearestFacing = MountedDispenseBehavior.getClosestFacingDirection(facing);
			BlockState state = context.state;
			if (state.hasProperty(BlockStateProperties.FACING))
				state = state.setValue(BlockStateProperties.FACING, nearestFacing);

			BlockSource source = new BlockSource(serverLevel, pos, state, null);

			try {
				// use a copy in case of implosion after modifying it
				return this.wrapped.dispense(source, stack.copy());
			} catch (NullPointerException e) {
				// likely due to the lack of a BlockEntity
				ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(this.item);
				String message = "Error dispensing item '" + itemId + "' from contraption, not doing that anymore";
				Create.LOGGER.error(message, e);
				this.hasErrored = true;
				return stack;
			}
		}
	}
}
