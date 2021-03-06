package com.github.elenterius.combat_commons.test;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Meant for gameplay testing & example implementations.
 *
 * @note This test mod is only available when you run runTestClient/runTestServer.
 */
@Mod(TestMod.MOD_ID)
public class TestMod {

	public static final String MOD_ID = "test_combat_commons";
	public static final Logger LOGGER = LogManager.getLogger("Combat-Commons/Test");

	public static final List<Supplier<List<ItemStack>>> ITEM_GROUP_ITEM_SUPPLIERS = new ArrayList<>();
	public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab(MOD_ID) {

		public ItemStack makeIcon() {
			return new ItemStack(Items.RED_BANNER);
		}

		@Override
		public void fillItemList(@Nonnull NonNullList<ItemStack> items) {
			super.fillItemList(items);
			for (Supplier<List<ItemStack>> supplier : ITEM_GROUP_ITEM_SUPPLIERS) {
				items.addAll(supplier.get());
			}
			items.addAll(EnchantmentStuff.getItemGroupItems());
		}
	};

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MOD_ID);

	public TestMod() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		ITEMS.register(modEventBus);
		ENCHANTMENTS.register(modEventBus);
		modEventBus.addListener(this::setup);
	}

	private void setup(final FMLCommonSetupEvent event) {

	}

}
