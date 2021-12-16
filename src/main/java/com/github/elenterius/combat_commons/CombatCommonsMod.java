package com.github.elenterius.combat_commons;

import com.github.elenterius.combat_commons.compat.pehkui.PehkuiCompat;
import com.github.elenterius.combat_commons.entity.EntityAttributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CombatCommonsMod.MOD_ID)
public class CombatCommonsMod {

	public static final String MOD_ID = "combat_commons";
	public static final Logger LOGGER = LogManager.getLogger("Combat-Commons");

	public CombatCommonsMod() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		EntityAttributes.ATTRIBUTES.register(modEventBus);
	}

	private void setup(final FMLCommonSetupEvent event) {
		event.enqueueWork(PehkuiCompat::init);
	}

}
