package com.therealdp.itemsplitter;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemSplitter implements ModInitializer {
	public static final String MOD_ID = "item-splitter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Item Splitter Loaded!");
	}
}