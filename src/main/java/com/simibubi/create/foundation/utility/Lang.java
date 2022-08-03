package com.simibubi.create.foundation.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.simibubi.create.Create;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class Lang {

	/**
	 * legacy-ish. Use Lang.translate and other builder methods where possible
	 * 
	 * @param key
	 * @param args
	 * @return
	 */
	public static MutableComponent translateDirect(String key, Object... args) {
		return Components.translatable(Create.ID + "." + key, resolveBuilders(args));
	}

	public static String asId(String name) {
		return name.toLowerCase(Locale.ROOT);
	}

	public static String nonPluralId(String name) {
		String asId = asId(name);
		return asId.endsWith("s") ? asId.substring(0, asId.length() - 1) : asId;
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

	public static LangBuilder builder(String namespace) {
		return new LangBuilder(namespace);
	}

	//

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

	//

	public static Object[] resolveBuilders(Object[] args) {
		for (int i = 0; i < args.length; i++)
			if (args[i]instanceof LangBuilder cb)
				args[i] = cb.component();
		return args;
	}

}
