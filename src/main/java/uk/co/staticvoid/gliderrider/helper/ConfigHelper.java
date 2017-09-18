package uk.co.staticvoid.gliderrider.helper;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigHelper {

    private final String fileName;
    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration fileConfiguration;

    public ConfigHelper(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
    }

    public void reloadConfig() {
        if (this.configFile == null) {
            File dataFolder = this.plugin.getDataFolder();
            if (dataFolder == null) {
                throw new IllegalStateException();
            }

            this.configFile = new File(dataFolder, this.fileName);
        }

        this.fileConfiguration = YamlConfiguration.loadConfiguration(this.configFile);
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new File(this.getClass().getClassLoader().getResource(this.fileName).getPath()));
        this.fileConfiguration.setDefaults(defConfig);
    }

    public FileConfiguration getConfig() {
        if (this.fileConfiguration == null) {
            this.reloadConfig();
        }

        return this.fileConfiguration;
    }

    public void saveConfig() {
        if (this.fileConfiguration != null && this.configFile != null) {
            try {
                this.getConfig().save(this.configFile);
            } catch (IOException ex) {
                this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, ex);
            }

        }
    }

    public void saveDefaultConfig() {
        if (this.configFile == null) {
            this.configFile = new File(this.plugin.getDataFolder(), this.fileName);
        }

        if (!this.configFile.exists()) {
            this.plugin.saveResource(this.fileName, false);
        }
    }

    public File getDataFolder() {
        return this.plugin.getDataFolder();
    }

}
