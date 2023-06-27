package com.simibubi.create.content.equipment.clipboard;

import com.simibubi.create.Create;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;

public class ClipboardOverrides {

	public enum ClipboardType {
		EMPTY("empty_clipboard"), WRITTEN("clipboard"), EDITING("clipboard_and_quill");

		public String file;
		public static ResourceLocation ID = Create.asResource("clipboard_type");

		private ClipboardType(String file) {
			this.file = file;
		}
	}

	public static void switchTo(ClipboardType type, ItemStack clipboardItem) {
		CompoundTag tag = clipboardItem.getOrCreateTag();
		tag.putInt("Type", type.ordinal());
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerModelOverridesClient(ClipboardBlockItem item) {
		ItemProperties.register(item, ClipboardType.ID, (pStack, pLevel, pEntity, pSeed) -> {
			CompoundTag tag = pStack.getTag();
			return tag == null ? 0 : tag.getInt("Type");
		});
	}

	public static ItemModelBuilder addOverrideModels(DataGenContext<Item, ClipboardBlockItem> c,
		RegistrateItemModelProvider p) {
		ItemModelBuilder builder = p.generated(() -> c.get());
		for (int i = 0; i < ClipboardType.values().length; i++) {
			builder.override()
				.predicate(ClipboardType.ID, i)
				.model(p.getBuilder(c.getName() + "_" + i)
					.parent(new UncheckedModelFile("item/generated"))
					.texture("layer0", Create.asResource("item/" + ClipboardType.values()[i].file)))
				.end();
		}
		return builder;
	}

}
