package com.therealdp.itemsplitter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SplitterConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("item-splitter.json");

    public List<SplitBind> binds = new ArrayList<>();

    public static class SplitBind {
        public int keyCode;
        public String keyDisplay;
        public int amount;

        public SplitBind(int keyCode, String keyDisplay, int amount) {
            this.keyCode = keyCode;
            this.keyDisplay = keyDisplay;
            this.amount = amount;
        }
    }

    private static SplitterConfig instance;

    public static SplitterConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        try (Reader reader = new FileReader(CONFIG_PATH.toFile())) {
            instance = GSON.fromJson(reader, SplitterConfig.class);
            if (instance == null) instance = new SplitterConfig();
        } catch (Exception e) {
            instance = new SplitterConfig();
            save();
        }
    }

    public static void save() {
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(instance, writer);
        } catch (Exception e) {
            ItemSplitter.LOGGER.error("Failed to save config: " + e.getMessage());
        }
    }
}