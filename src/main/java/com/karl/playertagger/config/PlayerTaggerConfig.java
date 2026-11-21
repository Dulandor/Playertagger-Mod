package com.karl.playertagger.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.karl.playertagger.data.TagManager;
import com.karl.playertagger.gui.GuiAddPlayerStep1;
import com.karl.playertagger.gui.GuiTagList;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.Button;
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.config.v1.annotations.Text;
import org.polyfrost.oneconfig.api.platform.v1.Platform;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class PlayerTaggerConfig extends Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File settingsFile;
    public static final String DEFAULT_URL = "https://playertagger-db-default-rtdb.europe-west1.firebasedatabase.app/players.json";

    // --- ALLGEMEIN ---
    @Switch(
        title = "Mod Aktivieren",
        category = "Allgemein",
        description = "Aktiviert oder deaktiviert das Markieren von Spielern (Sweats/Cheater)."
    )
    public static boolean enabled = true;

    @Button(
        title = "Spieler-Liste anzeigen",
        text = "Liste öffnen (GUI)",
        category = "Allgemein",
        description = "Öffnet die Ingame-Liste mit allen markierten Spielern, Suchfunktion und Löschbuttons."
    )
    public void openListButton() {
        Platform.screen().display(new GuiTagList(null));
    }

    @Button(
        title = "Neuen Spieler hinzufügen",
        text = "Hinzufügen",
        category = "Allgemein",
        description = "Startet den 4-Schritte-Assistenten (Name, Modus, Zusatzinfo/Cheats, Farbe & Legende)."
    )
    public void addPlayerButton() {
        Platform.screen().display(new GuiAddPlayerStep1(null));
    }

    // --- NAMETAGS ---
    @Switch(
        title = "Nametag Markierung",
        category = "Nametag",
        description = "Färbt die Spielernamen über dem Kopf und blendet Sub-Badges ein."
    )
    public static boolean nametagTagging = true;

    @Switch(
        title = "Multi-Zeilen Sub-Badge anzeigen",
        category = "Nametag",
        description = "Zeigt die Zusatz-Information (z.B. BedWars • Fireball God oder 3.4 Reach) über dem Namen an."
    )
    public static boolean showSubBadge = true;

    @Switch(
        title = "Nametag in Farbe färben",
        category = "Nametag",
        description = "Färbt den Namen des Spielers in der gewählten Kategorie-Farbe."
    )
    public static boolean colorNametag = true;

    @Dropdown(
        title = "Nametag Render-Stil",
        category = "Nametag",
        options = {"Multi-Zeilen (Titel + Name)", "Nur Icon vor Name", "Nur Farbe"},
        description = "Wähle, wie das Nametag über dem Kopf gerendert werden soll."
    )
    public static int nametagStyle = 0;

    // --- TAB-LISTE ---
    @Switch(
        title = "Tab-Liste Markierung",
        category = "Tab-Liste",
        description = "Hebt markierte Spieler in der Tab-Liste farblich und mit Icon hervor."
    )
    public static boolean tabListTagging = true;

    // --- ALERTS & RADAR ---
    @Switch(
        title = "Lobby & Rundenstart Alert",
        category = "Benachrichtigungen",
        description = "Zeigt ein Warnbanner oben auf dem Bildschirm, wenn ein markierter Spieler in deiner Runde ist."
    )
    public static boolean alertsEnabled = true;

    @Switch(
        title = "Warn-Sound abspielen",
        category = "Benachrichtigungen",
        description = "Spielt einen Benachrichtigungston beim Sichten eines markierten Spielers ab."
    )
    public static boolean alertSound = true;

    // --- GOOGLE FIREBASE CLOUD SYNC ---
    @Switch(
        title = "Google Firebase Sync aktivieren",
        category = "Google Firebase",
        description = "Lädt automatisch die gemeinsame Community-Liste aus der Google Firebase Cloud herunter."
    )
    public static boolean firebaseEnabled = true;

    @Switch(
        title = "Auto-Upload in Firebase",
        category = "Google Firebase",
        description = "Lädt neu hinzugefügte Spieler sofort automatisch in die gemeinsame Google Firebase Datenbank hoch."
    )
    public static boolean firebaseAutoUpload = true;

    @Text(
        title = "Firebase Realtime Database URL",
        category = "Google Firebase",
        description = "Deine Google Firebase URL"
    )
    public static String firebaseUrl = DEFAULT_URL;

    @Text(
        title = "Firebase Auth Token (Optional)",
        category = "Google Firebase",
        description = "Optional: Falls deine Firebase Database mit einem Auth Secret / Token geschützt ist."
    )
    public static String firebaseAuth = "";

    @Slider(
        title = "Firebase Auto-Sync (Minuten)",
        category = "Google Firebase",
        min = 1.0F,
        max = 60.0F,
        step = 1.0F,
        description = "Wie oft die Firebase-Liste im Hintergrund automatisch aktualisiert wird."
    )
    public static float syncIntervalMinutes = 10.0F;

    @Button(
        title = "Jetzt mit Firebase abgleichen",
        text = "🔥 Download von Firebase",
        category = "Google Firebase",
        description = "Lädt sofort die neueste Version der Shared Cloud-Liste aus deiner Firebase Datenbank."
    )
    public void syncNowButton() {
        TagManager.getInstance().syncFromFirebase(true);
    }

    @Button(
        title = "Alle lokalen Spieler in Firebase hochladen",
        text = "⬆ In Firebase veröffentlichen",
        category = "Google Firebase",
        description = "Lädt deine lokal markierten Spieler in die gemeinsame Google Firebase Datenbank hoch."
    )
    public void uploadButton() {
        TagManager.getInstance().uploadAllToFirebase(true);
    }

    @Button(
        title = "Lokalen Ordner im Finder öffnen",
        text = "Ordner im Finder öffnen",
        category = "Google Firebase",
        description = "Öffnet den lokalen Speicherordner der JSON-Dateien auf deinem Mac."
    )
    public void openFolderButton() {
        File folder = new File("config/playertagger");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try {
            Runtime.getRuntime().exec(new String[]{"open", folder.getAbsolutePath()});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PlayerTaggerConfig() {
        super("playertagger.json", "Player Tagger", Category.QOL);
        preload();

        // Ensure we don't end up with an empty URL after OneConfig initialization
        if (firebaseUrl == null || firebaseUrl.trim().isEmpty()) {
            firebaseUrl = DEFAULT_URL;
        }
    }

    public static void initPersistence(File configDir) {
        settingsFile = new File(configDir, "settings.json");
        loadCustomSettings();

        // Shutdown hook to guarantee settings are saved on game close
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                saveCustomSettings();
            }
        }));
    }

    public static void loadCustomSettings() {
        if (settingsFile == null) return;
        if (!settingsFile.exists()) {
            saveCustomSettings();
            return;
        }
        try (FileReader reader = new FileReader(settingsFile)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json == null) return;

            if (json.has("enabled")) enabled = json.get("enabled").getAsBoolean();
            if (json.has("nametagTagging")) nametagTagging = json.get("nametagTagging").getAsBoolean();
            if (json.has("showSubBadge")) showSubBadge = json.get("showSubBadge").getAsBoolean();
            if (json.has("colorNametag")) colorNametag = json.get("colorNametag").getAsBoolean();
            if (json.has("nametagStyle")) nametagStyle = json.get("nametagStyle").getAsInt();
            if (json.has("tabListTagging")) tabListTagging = json.get("tabListTagging").getAsBoolean();
            if (json.has("alertsEnabled")) alertsEnabled = json.get("alertsEnabled").getAsBoolean();
            if (json.has("alertSound")) alertSound = json.get("alertSound").getAsBoolean();
            if (json.has("firebaseEnabled")) firebaseEnabled = json.get("firebaseEnabled").getAsBoolean();
            if (json.has("firebaseAutoUpload")) firebaseAutoUpload = json.get("firebaseAutoUpload").getAsBoolean();

            if (json.has("firebaseUrl")) {
                String url = json.get("firebaseUrl").getAsString();
                if (url != null && !url.trim().isEmpty()) {
                    firebaseUrl = url;
                } else {
                    firebaseUrl = DEFAULT_URL;
                }
            } else {
                firebaseUrl = DEFAULT_URL;
            }

            if (json.has("firebaseAuth")) firebaseAuth = json.get("firebaseAuth").getAsString();
            if (json.has("syncIntervalMinutes")) syncIntervalMinutes = json.get("syncIntervalMinutes").getAsFloat();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveCustomSettings() {
        if (settingsFile == null) return;
        try {
            if (!settingsFile.getParentFile().exists()) {
                settingsFile.getParentFile().mkdirs();
            }
            if (firebaseUrl == null || firebaseUrl.trim().isEmpty()) {
                firebaseUrl = DEFAULT_URL;
            }

            JsonObject json = new JsonObject();
            json.addProperty("enabled", enabled);
            json.addProperty("nametagTagging", nametagTagging);
            json.addProperty("showSubBadge", showSubBadge);
            json.addProperty("colorNametag", colorNametag);
            json.addProperty("nametagStyle", nametagStyle);
            json.addProperty("tabListTagging", tabListTagging);
            json.addProperty("alertsEnabled", alertsEnabled);
            json.addProperty("alertSound", alertSound);
            json.addProperty("firebaseEnabled", firebaseEnabled);
            json.addProperty("firebaseAutoUpload", firebaseAutoUpload);
            json.addProperty("firebaseUrl", firebaseUrl);
            json.addProperty("firebaseAuth", firebaseAuth);
            json.addProperty("syncIntervalMinutes", syncIntervalMinutes);

            try (FileWriter writer = new FileWriter(settingsFile)) {
                GSON.toJson(json, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
