package org.black_ixx.bossshop.managers.item;

import org.black_ixx.bossshop.core.BSBuy;
import org.black_ixx.bossshop.managers.ClassManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ItemDataPartCustomSkull extends ItemDataPart {

    public static ItemStack transformSkull(ItemStack i, String input) {
        if (input == null || input.isEmpty()) {
            return i;
        }

        SkullMeta skullMeta = (SkullMeta) i.getItemMeta();
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "bsp_customSkull");
        try {
            profile.getTextures().setSkin(new URL(input));
        } catch(MalformedURLException e) {
            Bukkit.getLogger().warning("[BossShopPro] Invalid URL for custom skull texture: " + input);
            e.printStackTrace();
        }
        i.setItemMeta(skullMeta);
        return i;
    }

    public static String readSkullTexture(ItemStack i) {
        if (i.getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) i.getItemMeta();
            PlayerProfile playerProfile = meta.getOwnerProfile();
            assert playerProfile != null;
            return Objects.requireNonNull(playerProfile.getTextures().getSkin()).toString();
        }
        return null;
    }

    @Override
    public ItemStack transform(ItemStack item, String used_name, String argument) {
        if (!(item.getItemMeta() instanceof SkullMeta)) {
            ClassManager.manager.getBugFinder().warn("Mistake in Config: Itemdata of type '" + used_name + "' with value '" + argument + "' can not be added to an item with material '" + item.getType().name() + "'. Don't worry I'll automatically transform the material into '" + Material.PLAYER_HEAD + ".");
            item.setType(Material.PLAYER_HEAD);
        }
        item = transformSkull(item, argument);
        return item;
    }

    @Override
    public int getPriority() {
        return PRIORITY_EARLY;
    }

    @Override
    public boolean removeSpaces() {
        return true;
    }

    @Override
    public String[] createNames() {
        return new String[]{"customskull", "skull"};
    }

    @Override
    public List<String> read(ItemStack i, List<String> output) {
        String skulltexture = readSkullTexture(i);
        if (skulltexture != null) {
            output.add("customskull:" + skulltexture);
        }
        return output;
    }


    @Override
    public boolean isSimilar(ItemStack shop_item, ItemStack player_item, BSBuy buy, Player p) {
        return true; //Custom skull textures do not matter
    }


}
