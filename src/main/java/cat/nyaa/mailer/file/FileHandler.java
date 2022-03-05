package cat.nyaa.mailer.file;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class FileHandler {

    private File file;
    private final File folder;

    private final String fileName;
    private FileConfiguration config;

    public FileHandler(File folder, String name) {
        this.folder = folder;
        this.fileName = name+".yml";
    }


    /**
     * Create the  tracks folder if doesnt exists
     */
    private void setup() {
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    /**
     * Create the data file if doesnt exists
     */
    public void setupFile() {
        file = new File(folder, fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
                Bukkit.getConsoleSender().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&aFile "+ fileName + " created correctly!"));
            } catch (IOException e) {
                Bukkit.getConsoleSender().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cCould not create "+ fileName + "!"));
            }
        }
    }

    /**
     * this return everytime the configuration of the file
     * @return
     */
    public FileConfiguration getConfig() {
        if (file == null || !file.exists()) {
            setupFile();
        }
        if (config == null) {
            config = YamlConfiguration.loadConfiguration(file);
        }
        return config;
    }

    /**
     * save the config into the file, if the file exists and the config is not null
     * @return false if the file is not saved
     */
    public boolean save() {

        if (!file.exists() || config == null) {
            return false;
        }
        try {
            config.save(file);
            return true;
        } catch (IOException e) {

            return false;
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(file);
    }


    public void deleteFile() {
        file.delete();
    }
}

