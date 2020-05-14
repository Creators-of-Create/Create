package com.simibubi.create.modules.palettes;

import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.connected.CTModel;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.registrate.CreateRegistrateBase;
import com.simibubi.create.modules.Sections;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.NonNullLazyValue;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import net.minecraft.block.Block;
import net.minecraft.block.Block.Properties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

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

	public <T extends Block> NonNullUnaryOperator<BlockBuilder<T, PalettesRegistrate>> connectedTextures(
		ConnectedTextureBehaviour behavior) {
		return b -> b.onRegister(entry -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> registerModel(entry, behavior)));
	}

	@OnlyIn(Dist.CLIENT)
	private void registerModel(Block entry, ConnectedTextureBehaviour behavior) {
		CreateClient.getCustomBlockModels()
			.register(entry.delegate, model -> new CTModel(model, behavior));
	}
}
