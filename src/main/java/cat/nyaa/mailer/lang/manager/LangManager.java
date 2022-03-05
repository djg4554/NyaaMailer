package cat.nyaa.mailer.lang.manager;

import cat.nyaa.mailer.NyaaMailer;
import cat.nyaa.mailer.file.FileHandler;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class LangManager {

    private final NyaaMailer plugin;
    private final String fileName;
    private String lang;
    private FileHandler fileHandler;
    private File langFolder;

    public LangManager(NyaaMailer plugin) {
        this.plugin = plugin;
        lang = plugin.getConfig().getString("lang", "en");
        fileName = lang + ".yml";
        langFolder = new File(plugin.getDataFolder(), "lang");
    }

    public void loadLanguageConfig() {
        String internalPath = "lang"+File.separator+ fileName;
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        File langFile = new File(langFolder, fileName);
        if (!langFile.exists()) {
            if (plugin.getResource("lang/"+fileName) != null) {
                plugin.saveResource(internalPath, false);
            } else {
                plugin.getLogger().severe("Cannot find language file: "+fileName);
                plugin.getLogger().severe("Using default language file: en.yml");
            }
        }

        fileHandler = new FileHandler(langFolder, lang);

    }

    public FileConfiguration getLanguageConfig() {
        return fileHandler.getConfig();
    }


}
