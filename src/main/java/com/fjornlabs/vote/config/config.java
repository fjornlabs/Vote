package com.fjornlabs.vote.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class config {

    public static final class VoteLink {
        public String label;
        public String url;

        public VoteLink(String label, String url) {
            this.label = label;
            this.url = url;
        }

        /** A link is usable if it has a label and a valid http(s) URL. */
        boolean isValid() {
            if (label == null || url == null) return false;
            try {
                String scheme = URI.create(url).getScheme();
                return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

    public String header = "=== Vote for fjornlabs ===";
    public String footer = "Thanks for supporting me!";
    public List<VoteLink> links = new ArrayList<>(List.of(
            new VoteLink("Github", "https://www.github.com/fjornlabs")
    ));
    public String headerColor = "GOLD";
    public String footerColor = "YELLOW";
    public String labelColor = "AQUA";
    public String clickColor = "GREEN";

    // ----------------------------------------------------------------

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("vote.json");

    private static volatile config instance;

    /** Returns the current config, loading it on first access. */
    public static config get() {
        config current = instance;
        if (current == null) {
            synchronized (config.class) {
                if (instance == null) {
                    instance = load();
                }
                current = instance;
            }
        }
        return current;
    }

    /** Re-reads the config from disk. */
    public static synchronized void reload() {
        instance = load();
    }

    private static config load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                config loaded = GSON.fromJson(reader, config.class);
                if (loaded != null) {
                    loaded.normalize();
                    return loaded;
                }
                LOGGER.warn("[Vøte] Config file is empty, using defaults.");
            } catch (Exception e) {
                // Don't overwrite the file here, so the user can fix their typo
                LOGGER.error("[Vøte] Failed to read config, using defaults: {}", e.getMessage());
            }
            return new config();
        }

        config defaults = new config();
        defaults.save();
        return defaults;
    }

    /** Fills in missing fields with defaults and drops invalid links. */
    private void normalize() {
        config defaults = new config();

        if (header == null) header = defaults.header;
        if (footer == null) footer = defaults.footer;
        if (headerColor == null) headerColor = defaults.headerColor;
        if (footerColor == null) footerColor = defaults.footerColor;
        if (labelColor == null) labelColor = defaults.labelColor;
        if (clickColor == null) clickColor = defaults.clickColor;

        if (links == null) {
            links = defaults.links;
            return;
        }

        List<VoteLink> valid = new ArrayList<>();
        for (VoteLink link : links) {
            if (link != null && link.isValid()) {
                valid.add(link);
            } else {
                LOGGER.warn("[Vøte] Ignoring invalid link in config: {}",
                        link == null ? "null" : link.label + " -> " + link.url);
            }
        }
        links = valid;
    }

    private void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            LOGGER.error("[Vøte] Failed to save config: {}", e.getMessage());
        }
    }
}