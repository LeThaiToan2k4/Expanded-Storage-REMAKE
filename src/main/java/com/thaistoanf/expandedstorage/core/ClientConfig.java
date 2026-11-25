package com.thaistoanf.expandedstorage.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Lightweight client preferences persisted to {@code config/expandedstorage-client.json}.
 *
 * <p>Uses the Gson library bundled with Minecraft, so no config library mod is required.</p>
 */
public final class ClientConfig {
    public static final String FILE_NAME = "expandedstorage-client.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ClientConfig instance;

    private GuiMode guiMode = GuiMode.ADAPTIVE;
    private boolean darkGui = false;

    private ClientConfig() {
    }

    public static synchronized ClientConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public GuiMode guiMode() {
        return guiMode;
    }

    public boolean darkGui() {
        return darkGui;
    }

    public void setGuiMode(GuiMode mode) {
        this.guiMode = mode == null ? GuiMode.ADAPTIVE : mode;
        save();
    }

    public void setDarkGui(boolean dark) {
        this.darkGui = dark;
        save();
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    private static ClientConfig load() {
        ClientConfig config = new ClientConfig();
        Path file = path();
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                Data data = GSON.fromJson(reader, Data.class);
                if (data != null) {
                    config.guiMode = GuiMode.parse(data.guiMode);
                    config.darkGui = data.darkGui;
                }
            } catch (IOException | JsonParseException e) {
                // A broken file falls back to defaults and is rewritten below.
            }
        }
        config.save();
        return config;
    }

    public synchronized void save() {
        Data data = new Data();
        data.guiMode = guiMode.name();
        data.darkGui = darkGui;
        Path file = path();
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException ignored) {
            // Preferences are cosmetic; failing to persist them must never crash the client.
        }
    }

    /** Plain JSON shape of the file. */
    private static final class Data {
        String guiMode = GuiMode.ADAPTIVE.name();
        boolean darkGui = false;
    }
}
