package com.simibubi.create;

import java.util.function.Supplier;

import com.simibubi.create.modules.schematics.block.SchematicTableTileEntity;
import com.simibubi.create.modules.schematics.block.SchematicannonRenderer;
import com.simibubi.create.modules.schematics.block.SchematicannonTileEntity;
import com.simibubi.create.modules.shopping.ShopShelfTileEntity;

import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.MOD)
public enum AllTileEntities {

	ShopShelfTileEntity(ShopShelfTileEntity::new, AllBlocks.SHOP_SHELF),
	
	Schematicannon(SchematicannonTileEntity::new, AllBlocks.SCHEMATICANNON),
	SchematicTable(SchematicTableTileEntity::new, AllBlocks.SCHEMATIC_TABLE);

	private Supplier<? extends TileEntity> supplier;
	public TileEntityType<?> type;
	private AllBlocks block;

	private AllTileEntities(Supplier<? extends TileEntity> supplier, AllBlocks block) {
		this.supplier = supplier;
		this.block = block;
	}

	@SubscribeEvent
	public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event) {

		for (AllTileEntities tileEntity : values()) {
			ResourceLocation resourceLocation = new ResourceLocation(Create.ID, tileEntity.name().toLowerCase());
			tileEntity.type = TileEntityType.Builder.create(tileEntity.supplier, tileEntity.block.get()).build(null)
					.setRegistryName(resourceLocation);
			event.getRegistry().register(tileEntity.type);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerRenderers() {
		bind(SchematicannonTileEntity.class, new SchematicannonRenderer());
	}

	@OnlyIn(Dist.CLIENT)
	private static <T extends TileEntity> void bind(Class<T> clazz, TileEntityRenderer<? super T> renderer) {
		ClientRegistry.bindTileEntitySpecialRenderer(clazz, renderer);
	}

}
