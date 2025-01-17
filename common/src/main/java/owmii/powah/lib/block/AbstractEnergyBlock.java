package owmii.powah.lib.block;

import owmii.powah.EnvHandler;
import owmii.powah.api.energy.IEnergyConnector;
import owmii.powah.block.Tier;
import owmii.powah.config.IConfigHolder;
import owmii.powah.lib.client.util.Text;
import owmii.powah.lib.client.wiki.page.panel.InfoBox;
import owmii.powah.config.IEnergyConfig;
import owmii.powah.lib.item.EnergyBlockItem;
import owmii.powah.lib.item.IEnergyItemProvider;
import owmii.powah.lib.logistics.Transfer;
import owmii.powah.lib.logistics.energy.Energy;
import owmii.powah.lib.registry.IVariant;
import owmii.powah.lib.util.Util;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import java.util.List;

public abstract class AbstractEnergyBlock<C extends IEnergyConfig<Tier>, B extends AbstractEnergyBlock<C, B>> extends AbstractBlock<Tier, B> implements IConfigHolder<Tier, C>, InfoBox.IInfoBoxHolder, IEnergyItemProvider {
    public AbstractEnergyBlock(Properties properties) {
        this(properties, IVariant.getEmpty());
    }

    public AbstractEnergyBlock(Properties properties, Tier variant) {
        super(properties, variant);
    }

    @Override
    @SuppressWarnings("unchecked")
    public EnergyBlockItem getBlockItem(Item.Properties properties, @Nullable CreativeModeTab group) {
        return new EnergyBlockItem(this, properties, group);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof AbstractEnergyStorage) {
            return ((AbstractEnergyStorage) tile).getEnergy().toComparatorPower();
        }
        return super.getAnalogOutputSignal(state, world, pos);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        if (checkValidEnergySide()) {
            Direction side = state.getValue(BlockStateProperties.FACING);
            BlockPos pos1 = pos.relative(side);
            return world.getBlockState(pos1).getBlock() instanceof IEnergyConnector ||
                    world instanceof Level level && EnvHandler.INSTANCE.hasEnergy(level, pos1, side.getOpposite());
        }
        return super.canSurvive(state, world, pos);
    }

    protected boolean checkValidEnergySide() {
        return false;
    }

    @Override
    public boolean isChargeable(ItemStack stack) {
        return getTransferType().canReceive;
    }

    public Transfer getTransferType() {
        return Transfer.ALL;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        Energy.ifPresent(stack, energy -> {
            addEnergyInfo(stack, energy, tooltip);
            addEnergyTransferInfo(stack, energy, tooltip);
            additionalEnergyInfo(stack, energy, tooltip);
            tooltip.add(new TextComponent(""));
        });
    }

    public void addEnergyInfo(ItemStack stack, Energy.Item storage, List<Component> tooltip) {
        if (storage.getCapacity() > 0)
            tooltip.add(new TranslatableComponent("info.lollipop.stored").withStyle(ChatFormatting.GRAY).append(Text.COLON).append(new TranslatableComponent("info.lollipop.fe.stored", Util.addCommas(storage.getStored()), Util.numFormat(storage.getCapacity())).withStyle(ChatFormatting.DARK_GRAY)));
    }

    public void addEnergyTransferInfo(ItemStack stack, Energy.Item storage, List<Component> tooltip) {
        long ext = storage.getMaxExtract();
        long re = storage.getMaxReceive();
        if (ext + re > 0) {
            if (ext == re) {
                tooltip.add(new TranslatableComponent("info.lollipop.max.io").withStyle(ChatFormatting.GRAY).append(Text.COLON).append(new TextComponent(Util.numFormat(ext)).append(new TranslatableComponent("info.lollipop.fe.pet.tick")).withStyle(ChatFormatting.DARK_GRAY)));
            } else {
                if (ext > 0)
                    tooltip.add(new TranslatableComponent("info.lollipop.max.extract").withStyle(ChatFormatting.GRAY).append(Text.COLON).append(new TextComponent(Util.numFormat(ext)).append(new TranslatableComponent("info.lollipop.fe.pet.tick")).withStyle(ChatFormatting.DARK_GRAY)));
                if (re > 0)
                    tooltip.add(new TranslatableComponent("info.lollipop.max.receive").withStyle(ChatFormatting.GRAY).append(Text.COLON).append(new TextComponent(Util.numFormat(re)).append(new TranslatableComponent("info.lollipop.fe.pet.tick")).withStyle(ChatFormatting.DARK_GRAY)));
            }
        }
    }

    public void additionalEnergyInfo(ItemStack stack, Energy.Item energy, List<Component> tooltip) {
    }

    @Override
    public InfoBox getInfoBox(ItemStack stack, InfoBox box) {
        Energy.ifPresent(stack, storage -> {
            if (storage != null) {
                if (storage.getMaxEnergyStored() > 0)
                    box.set(new TranslatableComponent("info.lollipop.capacity"), new TranslatableComponent("info.lollipop.fe", Util.addCommas(storage.getCapacity())));
                box.set(new TranslatableComponent("info.lollipop.max.io"), new TranslatableComponent("info.lollipop.fe.pet.tick", Util.addCommas(storage.getMaxExtract())));
            }
        });
        return box;
    }
}
