package cat.nyaa.mailer.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Utils {
    public static Location parseLocation(String l) {
        String[] split = l.split(":");
        if (split.length != 4) {
            return null;
        }
        try {
            return new Location(
                    Bukkit.getWorld(split[0]),
                    Double.parseDouble(split[1]),
                    Double.parseDouble(split[2]),
                    Double.parseDouble(split[3])
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String serializeLocation(Location l) {
        return l.getWorld().getName() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
    }
}
