
package com.simibubi.create.compat.computercraft.events;

import org.jetbrains.annotations.NotNull;
import net.minecraft.world.item.ItemStack;

public class RepackageEvent implements ComputerEvent {

	public @NotNull ItemStack box;
	public int count;

	public RepackageEvent(@NotNull ItemStack box, int count) {
		this.box = box;
		this.count = count;
	}

}
