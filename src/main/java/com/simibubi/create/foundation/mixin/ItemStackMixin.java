package com.simibubi.create.foundation.mixin;

import java.util.function.BiFunction;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

@ScheduledForRemoval(inVersion = "1.21.1+ Port")
@Deprecated(since = "6.0.7", forRemoval = true)
@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;verifyComponentsAfterLoad(Lnet/minecraft/world/item/ItemStack;)V"))
	private void create$migrateOldClipboardComponents(ItemLike item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
		ClipboardContent content = ClipboardContent.EMPTY;

		content = create$migrateComponent(content, components, AllDataComponents.CLIPBOARD_PAGES, ClipboardContent::setPages);
		content = create$migrateComponent(content, components, AllDataComponents.CLIPBOARD_TYPE, ClipboardContent::setType);
		content = create$migrateComponent(content, components, AllDataComponents.CLIPBOARD_READ_ONLY, (c, v) -> c.setReadOnly(true));
		content = create$migrateComponent(content, components, AllDataComponents.CLIPBOARD_COPIED_VALUES, ClipboardContent::setCopiedValues);
		content = create$migrateComponent(content, components, AllDataComponents.CLIPBOARD_PREVIOUSLY_OPENED_PAGE, ClipboardContent::setPreviouslyOpenedPage);

		if (content != ClipboardContent.EMPTY) {
			components.set(AllDataComponents.CLIPBOARD_CONTENT, content);
		}
	}

	@Unique
	private static <T> ClipboardContent create$migrateComponent(ClipboardContent content, PatchedDataComponentMap components, DataComponentType<T> componentType, BiFunction<ClipboardContent, T, ClipboardContent> function) {
		T value = components.get(componentType);
		if (value != null) {
			components.remove(componentType);
			content = function.apply(content, value);
		}

		return content;
	}
}
