package owmii.powah.lib.block;

import owmii.powah.block.Tier;
import owmii.powah.config.v2.types.GeneratorConfig;
import owmii.powah.lib.client.util.Text;
import owmii.powah.lib.client.wiki.page.panel.InfoBox;
import owmii.powah.config.IEnergyConfig;
import owmii.powah.lib.logistics.Transfer;
import owmii.powah.lib.logistics.energy.Energy;
import owmii.powah.lib.registry.IVariant;
import owmii.powah.lib.util.Util;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractGeneratorBlock<B extends AbstractGeneratorBlock<B>> extends AbstractEnergyBlock<GeneratorConfig, B> {
    public AbstractGeneratorBlock(Properties properties) {
        super(properties);
    }

    public AbstractGeneratorBlock(Properties properties, Tier variant) {
        super(properties, variant);
    }

    @Override
    public void additionalEnergyInfo(ItemStack stack, Energy.Item energy, List<Component> tooltip) {
        tooltip.add(new TranslatableComponent("info.lollipop.generates").withStyle(ChatFormatting.GRAY).append(Text.COLON).append(new TextComponent(Util.numFormat(getConfig().getGeneration(this.variant))).append(new TranslatableComponent("info.lollipop.fe.pet.tick")).withStyle(ChatFormatting.DARK_GRAY)));
    }

    @Override
    public Transfer getTransferType() {
        return Transfer.EXTRACT;
    }

    @Override
    public InfoBox getInfoBox(ItemStack stack, InfoBox box) {
        Energy.ifPresent(stack, storage -> {
            if (storage instanceof Energy.Item) {
                Energy.Item energy = (Energy.Item) storage;
                box.set(new TranslatableComponent("info.lollipop.capacity"), new TranslatableComponent("info.lollipop.fe", Util.addCommas(energy.getCapacity())));
                box.set(new TranslatableComponent("info.lollipop.generates"), new TranslatableComponent("info.lollipop.fe.pet.tick", Util.addCommas(getConfig().getGeneration(this.variant))));
                box.set(new TranslatableComponent("info.lollipop.max.extract"), new TranslatableComponent("info.lollipop.fe.pet.tick", Util.addCommas(energy.getMaxExtract())));
            }
        });
        return box;
    }
}
