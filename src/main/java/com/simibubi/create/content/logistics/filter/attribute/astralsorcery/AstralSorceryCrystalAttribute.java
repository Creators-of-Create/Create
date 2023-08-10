package com.simibubi.create.content.logistics.filter.attribute.astralsorcery;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.logistics.filter.ItemAttribute;

import net.createmod.catnip.utility.lang.Components;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class AstralSorceryCrystalAttribute implements ItemAttribute {
    String traitName;

    public AstralSorceryCrystalAttribute(String traitName) {
        this.traitName = traitName;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        for (Tag trait : extractTraitList(itemStack)) {
            if(((CompoundTag) trait).getString("property").equals(this.traitName))
                return true;
        }
        return false;
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        ListTag traits = extractTraitList(itemStack);
        List<ItemAttribute> atts = new ArrayList<>();
        for (int i = 0; i < traits.size(); i++) {
            atts.add(new AstralSorceryCrystalAttribute(traits.getCompound(i).getString("property")));
        }
        return atts;
    }

    @Override
    public String getTranslationKey() {
        return "astralsorcery_crystal";
    }

    @Override
    public Object[] getTranslationParameters() {
        ResourceLocation traitResource = new ResourceLocation(traitName);
        String something = Components.translatable(String.format("crystal.property.%s.%s.name", traitResource.getNamespace(), traitResource.getPath())).getString();
        return new Object[] { something };
    }

    @Override
    public void writeNBT(CompoundTag nbt) {
        nbt.putString("property", this.traitName);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag nbt) {
        return new AstralSorceryCrystalAttribute(nbt.getString("property"));
    }

    private ListTag extractTraitList(ItemStack stack) {
        return stack.getTag() != null ? stack.getTag().getCompound("astralsorcery").getCompound("crystalProperties").getList("attributes", 10) : new ListTag();
    }
}
