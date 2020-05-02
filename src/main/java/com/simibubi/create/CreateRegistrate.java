package com.simibubi.create;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.simibubi.create.modules.Sections;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.util.NonNullLazyValue;
import com.tterrag.registrate.util.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.block.Block;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class CreateRegistrate extends AbstractRegistrate<CreateRegistrate> {

    /**
     * Create a new {@link CreateRegistrate} and register event listeners for registration and data generation. Used in lieu of adding side-effects to constructor, so that alternate initialization
     * strategies can be done in subclasses.
     * 
     * @param modid
     *            The mod ID for which objects will be registered
     * @return The {@link CreateRegistrate} instance
     */
    public static CreateRegistrate create(String modid) {
        return new CreateRegistrate(modid)
        		.registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus())
        		.itemGroup(() -> Create.creativeTab);
    }
    
    public static NonNullLazyValue<CreateRegistrate> lazy(String modid) {
    	return new NonNullLazyValue<>(() -> create(modid));
    }

    protected CreateRegistrate(String modid) {
        super(modid);
    }
    
    private Map<RegistryEntry<?>, Sections> sectionLookup = new IdentityHashMap<>();
    
    private Sections section;

	public CreateRegistrate startSection(Sections section) {
		this.section = section;
		return self();
	}
	
	public Sections currentSection() {
		return section;
	}
	
	@Deprecated
	public <T extends Block> BlockBuilder<T, CreateRegistrate> block(String name, NonNullSupplier<T> factory) {
		return block(name, $ -> factory.get());
	}
	
	@Override
	protected <R extends IForgeRegistryEntry<R>, T extends R> RegistryEntry<T> accept(String name,
			Class<? super R> type, Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> creator) {
		RegistryEntry<T> ret = super.accept(name, type, builder, creator);
		sectionLookup.put(ret, currentSection());
		return ret;
	}
	
	public Sections getSection(RegistryEntry<?> entry) {
		return sectionLookup.getOrDefault(entry, Sections.UNASSIGNED);
	}

	public Sections getSection(IForgeRegistryEntry<?> entry) {
		return sectionLookup.entrySet().stream()
				.filter(e -> e.getKey().get() == entry)
				.map(Entry::getValue)
				.findFirst()
				.orElse(Sections.UNASSIGNED);
	}
}
