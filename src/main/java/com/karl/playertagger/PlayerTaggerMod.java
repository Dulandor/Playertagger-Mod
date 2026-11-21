package com.karl.playertagger;

import com.karl.playertagger.command.TagCommand;
import com.karl.playertagger.config.PlayerTaggerConfig;
import com.karl.playertagger.data.TagManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public class PlayerTaggerMod implements ClientModInitializer {

    public static final String MODID = "playertagger";
    public static final String NAME = "Player Tagger";
    public static final String VERSION = "1.0.0";

    public static PlayerTaggerMod instance;
    public static PlayerTaggerConfig config;

    @Override
    public void onInitializeClient() {
        instance = this;

        File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), "playertagger");
        PlayerTaggerConfig.initPersistence(configDir);
        TagManager.getInstance().init(configDir);

        // Instantiate OneConfig v1 (Registers the mod into OneConfig UI automatically)
        config = new PlayerTaggerConfig();

        // Register Client Commands (/tag, /playertag, /sweatradar, /ptag)
        TagCommand.register();
    }
}
