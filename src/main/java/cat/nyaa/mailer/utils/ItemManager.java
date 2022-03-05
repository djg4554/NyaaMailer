package cat.nyaa.mailer.utils;

import org.bukkit.inventory.ItemStack;

import java.util.Base64;

public class ItemManager {

    public static boolean isNotValid(ItemStack itemStack) {
        return itemStack == null || itemStack.getType().isAir();
    }


    public static String encodeItem(ItemStack item) {
        Base64.Encoder encoder = Base64.getEncoder();
         return encoder.encodeToString(item.serializeAsBytes());
    }

    public static ItemStack decodeItem(String encoded) {
        Base64.Decoder decoder = Base64.getDecoder();
        return ItemStack.deserializeBytes(decoder.decode(encoded));
    }
}
