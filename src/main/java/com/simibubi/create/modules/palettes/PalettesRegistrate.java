package com.simibubi.create.modules.palettes;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.registrate.CreateRegistrateBase;
import com.simibubi.create.modules.Sections;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.NonNullLazyValue;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.block.Block;
import net.minecraft.block.Block.Properties;

public class PalettesRegistrate extends CreateRegistrateBase<PalettesRegistrate> {

	protected PalettesRegistrate(String modid) {
		super(modid, () -> Create.palettesCreativeTab);
	}

	public static NonNullLazyValue<PalettesRegistrate> lazy(String modid) {
		return new NonNullLazyValue<>(() -> new PalettesRegistrate(modid));
	}

	public <T extends Block> BlockBuilder<T, PalettesRegistrate> baseBlock(String name,
		NonNullFunction<Properties, T> factory, NonNullSupplier<Block> propertiesFrom) {
		return super.block(name, factory).initialProperties(propertiesFrom)
			.blockstate((c, p) -> {
				final String location = "block/palettes/" + c.getName() + "/plain";
				p.simpleBlock(c.get(), p.models()
					.cubeAll(c.getName(), p.modLoc(location)));
			})
			.simpleItem();
	}
	
	@Override
	public Sections currentSection() {
		return Sections.PALETTES;
	}

}
