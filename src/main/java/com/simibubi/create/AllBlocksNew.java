package com.simibubi.create;

import static com.simibubi.create.modules.Sections.SCHEMATICS;

import java.util.function.Supplier;

import com.simibubi.create.foundation.utility.data.BlockStateGen;
import com.simibubi.create.modules.Sections;
import com.simibubi.create.modules.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.modules.contraptions.relays.elementary.CogwheelBlockItem;
import com.simibubi.create.modules.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.modules.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.create.modules.schematics.block.SchematicTableBlock;
import com.simibubi.create.modules.schematics.block.SchematicannonBlock;
import com.tterrag.registrate.util.RegistryEntry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.item.ItemStack;

public class AllBlocksNew {

	private static final CreateRegistrate REGISTRATE = Create.registrate();

	static {
		REGISTRATE.startSection(SCHEMATICS);
	}

	public static final BlockEntry<SchematicannonBlock> SCHEMATICANNON =
		REGISTRATE.block("schematicannon", SchematicannonBlock::new, b -> b
				.initialProperties(() -> Blocks.DISPENSER)
				.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), BlockStateGen.partialBaseModel(ctx, prov)))
				.item()
					.model(BlockStateGen::customItemModel)
					.build()
				.register());

	public static final BlockEntry<SchematicTableBlock> SCHEMATIC_TABLE =
		REGISTRATE.block("schematic_table", SchematicTableBlock::new, b -> b
				.initialProperties(() -> Blocks.LECTERN)
				.blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), prov.models()
						.getExistingFile(ctx.getId()), 0))
				.simpleItem()
				.register());

	static {
		REGISTRATE.startSection(Sections.KINETICS);
	}

	public static final BlockEntry<ShaftBlock> SHAFT = REGISTRATE.block("shaft", ShaftBlock::new, b -> b
			.initialProperties(SharedProperties::kinetic)
			.blockstate((c, p) -> BlockStateGen.axisKineticBlock(c, p, BlockStateGen.standardModel(c, p)))
			.simpleItem()
			.register());

	public static final BlockEntry<CogWheelBlock> COGWHEEL = REGISTRATE.block("cogwheel", CogWheelBlock::small, b -> b
			.initialProperties(SharedProperties::kinetic)
			.properties(p -> p.sound(SoundType.WOOD))
			.blockstate((c, p) -> BlockStateGen.axisKineticBlock(c, p, BlockStateGen.standardModel(c, p)))
			.item(CogwheelBlockItem::new)
				.build()
			.register());

	public static final BlockEntry<CogWheelBlock> LARGE_COGWHEEL =
		REGISTRATE.block("large_cogwheel", CogWheelBlock::large, b -> b
				.initialProperties(SharedProperties::kinetic)
				.properties(p -> p.sound(SoundType.WOOD))
				.blockstate((c, p) -> BlockStateGen.axisKineticBlock(c, p, BlockStateGen.standardModel(c, p)))
				.item(CogwheelBlockItem::new)
					.build()
				.register());

	public static final BlockEntry<EncasedShaftBlock> ENCASED_SHAFT =
		REGISTRATE.block("encased_shaft", EncasedShaftBlock::new, b -> b
				.initialProperties(SharedProperties::kinetic)
				.blockstate((c, p) -> BlockStateGen.axisKineticBlock(c, p, BlockStateGen.partialBaseModel(c, p)))
				.item()
					.model(BlockStateGen::customItemModel)
					.build()
				.register());

	public static void register() {
	}

	public static class BlockEntry<B extends Block> implements Supplier<B> {

		private RegistryEntry<B> delegate;

		public BlockEntry(RegistryEntry<B> entry) {
			this.delegate = entry;
		}

		public boolean typeOf(BlockState state) {
			return get() == state.getBlock();
		}

		public ItemStack asStack() {
			return new ItemStack(get());
		}

		public BlockState getDefault() {
			return get().getDefaultState();
		}

		@Override
		public B get() {
			return delegate.get();
		}

	}

}
