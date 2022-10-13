package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.Create;

import net.createmod.catnip.utility.lang.Components;
import net.createmod.catnip.utility.lang.Lang;
import net.createmod.catnip.utility.lang.LangBuilder;
import net.createmod.catnip.utility.lang.LangNumberFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class CreateLang extends Lang {

	/**
	 * legacy-ish. Use CreateLang.translate and other builder methods where possible
	 *
	 */
	public static MutableComponent translateDirect(String key, Object... args) {
		return Components.translatable(Create.ID + "." + key, LangBuilder.resolveBuilders(args));
	}

	public static List<Component> translatedOptions(String prefix, String... keys) {
		List<Component> result = new ArrayList<>(keys.length);
		for (String key : keys)
			result.add(translate((prefix != null ? prefix + "." : "") + key).component());
		return result;
	}

	//

	public static LangBuilder builder() {
		return new LangBuilder(Create.ID);
	}

	public static LangBuilder blockName(BlockState state) {
		return builder().add(state.getBlock()
				.getName());
	}

	public static LangBuilder itemName(ItemStack stack) {
		return builder().add(stack.getHoverName()
				.copy());
	}

	public static LangBuilder fluidName(FluidStack stack) {
		return builder().add(stack.getDisplayName()
				.copy());
	}

	public static LangBuilder number(double d) {
		return builder().text(LangNumberFormat.format(d));
	}

	public static LangBuilder translate(String langKey, Object... args) {
		return builder().translate(langKey, args);
	}

	public static LangBuilder text(String text) {
		return builder().text(text);
	}

}
