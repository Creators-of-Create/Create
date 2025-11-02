
package com.simibubi.create.compat.computercraft.events;

import org.jetbrains.annotations.NotNull;
import net.minecraft.world.item.ItemStack;

public class PackageEvent implements ComputerEvent {

	public @NotNull ItemStack box;
	public String status;

	public PackageEvent(@NotNull ItemStack box, String status) {
		this.box = box;
		this.status = status;
	}

}
