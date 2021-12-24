package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.components.AbstractSelectionList;

@Mixin(AbstractSelectionList.Entry.class)
public interface AbstractSelectionList$EntryAccessor<E extends AbstractSelectionList.Entry<E>> {
	@Accessor("list")
	AbstractSelectionList<E> create$getList();
}
