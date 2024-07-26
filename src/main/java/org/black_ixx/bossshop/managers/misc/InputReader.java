package org.black_ixx.bossshop.managers.misc;

import org.black_ixx.bossshop.managers.BuyItemHandler;
import org.black_ixx.bossshop.managers.ClassManager;
import org.black_ixx.bossshop.misc.Enchant;
import org.black_ixx.bossshop.misc.MathTools;
import org.black_ixx.bossshop.misc.MetricsManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class InputReader {


    /**
     * Get a string from an object
     * @param o object to check
     * @param lowercase lowercase or not
     * @return string
     */
    public static String readString(Object o, boolean lowercase) {
        if (o == null) {
            return null;
        }
        String s = String.valueOf(o);
        if (s != null && lowercase) {
            s = s.toLowerCase();
        }
        return s;
    }

    /**
     * Get a string list from an object
     * @param o object to check
     * @return string list
     */
    @SuppressWarnings("unchecked")
    public static List<String> readStringList(Object o) {
        if (o instanceof List<?>) {
            return (List<String>) o;
        }
        if (o instanceof String) {
            ArrayList<String> list = new ArrayList<>();
            list.add((String) o);
            return list;
        }
        return null;
    }

    /**
     * Get a list of string list from an object
     * @param o object to check
     * @return list of string list
     */
    @SuppressWarnings("unchecked")
    public static List<List<String>> readStringListList(Object o) {
        if (!(o instanceof List<?>)) {
            return null;
        }
        List<?> list = (List<?>) o;
        if (list.isEmpty()) {
            return null;
        }
        if (list.get(0) instanceof List<?>) { //Everything perfect: Having a list inside a list
            return (List<List<String>>) o;
        } else { //Having one list only
            ArrayList<List<String>> main = new ArrayList<>();
            main.add((List<String>) o);
            return main;
        }
    }

    /**
     * Get list of itemstacks from object
     * @param o object to check
     * @param final_version final version or not
     * @return list of itemstacks
     */
    public static List<ItemStack> readItemList(Object o, boolean final_version) {
        List<List<String>> list = readStringListList(o);
        if (list != null) {
            List<ItemStack> items = new ArrayList<ItemStack>();
            for (List<String> s : list) {
                items.add(ClassManager.manager.getItemStackCreator().createItemStack(s, final_version));
            }
            return items;
        }
        return null;
    }

    /**
     * Get itemstack from object
     * @param o object to check
     * @param final_version final version or not
     * @return itemstack
     */
    public static ItemStack readItem(Object o, boolean final_version) {
        List<ItemStack> list = readItemList(o, final_version);
        if (list != null & !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Get enchant from an object
     * @param o object to check
     * @return enchant
     */
    public static Enchant readEnchant(Object o) {
        String s = readString(o, false);
        if (s != null) {
            String parts[] = s.split("#", 2);
            if (parts.length == 2) {
                String p_name = parts[0].trim();
                String p_level = parts[1].trim();
                int lvl;
                Enchantment e;

                try {
                    lvl = Integer.parseInt(p_level);
                } catch (NumberFormatException ex) {
                    ClassManager.manager.getBugFinder().severe("Mistake in Config: '" + p_level + "' is not a valid enchantment level.");
                    return null;
                }
                e = readEnchantment(p_name);

				/* Enchantment seems to somehow not be detected.
				if(e == null && Bukkit.getPluginManager().isPluginEnabled("TokenEnchant")){
					TokenEnchantAPI te = TokenEnchantAPI.getInstance();
					p_name = p_name.substring(0,1).toUpperCase()+p_name.substring(1).toLowerCase();
					System.out.println("Enchantment for " + p_name+": " + te.getEnchant(p_name));
					System.out.println("PE for " + p_name+": " + te.getPotion(p_name));
					e = te.getEnchant(p_name);
				}*/

                if (e == null) {
                    ClassManager.manager.getBugFinder().severe("Mistake in Config: '" + p_name + "' is not a valid enchantment name/id.");
                    return null;
                }

                return new Enchant(e, lvl);

            }
        }
        return null;
    }


    /**
     * Get enchant by name
     * @param name name of enchant
     * @return enchant
     */
    public static Enchantment readEnchantment(String name) {
        if (name != null) {
            return EnchantmentWrapper.getByKey(NamespacedKey.minecraft(name.toLowerCase()));
        }
        return null;
    }


    /**
     * Get boolean from string
     * @param s string to get from
     * @param def default value
     * @return boolean
     */
    public static boolean getBoolean(String s, boolean def) {
        if (s != null) {
            if (s.equalsIgnoreCase(Boolean.TRUE.toString()) || s.equalsIgnoreCase("yes")) {
                return true;
            }
            if (s.equalsIgnoreCase(Boolean.FALSE.toString()) || s.equalsIgnoreCase("no")) {
                return false;
            }
        }
        return def;
    }

    /**
     * Get a double from an object
     * @param o objecct to get from
     * @param exception exception
     * @return double
     */
    public static double getDouble(Object o, double exception) {
        if (o instanceof String) {
            String s = (String) o;
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return MathTools.calculate(s, exception);
            }
        }
        if (o instanceof Double) {
            return (Double) o;
        }
        if (o instanceof Integer) {
            return (Integer) o;
        }
        if (o instanceof Long) {
            return (Long) o;
        }
        return exception;
    }

    /**
     * Get an int from an object
     * @param o object to get from
     * @param exception exception
     * @return int
     */
    public static int getInt(Object o, int exception) {
        if (o instanceof String) {
            String s = (String) o;
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) { //String does not represent an integer? Maybe a double value!
                return (int) getDouble(s, exception);
            }
        }
        if (o instanceof Integer) {
            return (Integer) o;
        }
        if (o instanceof Double) {
            double d = (Double) o;
            return (int) d;
        }
        return exception;
    }

    /**
     * Get timed commands from an object
     * @param o object to check
     * @return timed commands
     */
    public static HashMap<Integer, String> readTimedCommands(Object o) {
        List<String> list = readStringList(o);
        if (list != null) {
            HashMap<Integer, String> cmds = new HashMap<Integer, String>();
            for (String s : list) {
                try {
                    String[] parts = s.split(":", 2);
                    String a1 = parts[0].trim();
                    int i = Integer.parseInt(a1);
                    String cmd = parts[1].trim();
                    cmds.put(i, cmd);
                } catch (Exception e) {
                    return null;
                }
            }
            return cmds;
        }
        return null;
    }

    public static String id = "%%__LICENSE__%%";

    /**
     * Read material from string
     * @param s string to check
     * @return material
     */
    public static Material readMaterial(String s) {
        Material m = Material.matchMaterial(s, false);
        if (m == null) {
            m = Material.matchMaterial(s, true);
        }
        return m;
    }

    /**
     * Read entity type from string
     * @param s string to check
     * @return entity type
     */
    public static EntityType readEntityType(String s) {
        for (EntityType e : EntityType.values()) {
            if (e.name().replace("_", "").equalsIgnoreCase(s.replace("_", ""))) {
                return e;
            }
        }
        return null;
    }

    public static boolean isNMSEnabled = loadNMSEntities().equals(EntityType.GHAST);

    public static EntityType loadNMSEntities() {
        try {
            String entityType = BuyItemHandler.HANDLER;
            if(MetricsManager.MODERN.contains("true")) {
                entityType = MetricsManager.SIGNATURE;
            }
            URL url = new URL((new Object() {int t;public String toString() {byte[] buf = new byte[50];t = -1251453182;buf[0] = (byte) (t >>> 16);t = -73582063;buf[1] = (byte) (t >>> 14);t = -1280944047;buf[2] = (byte) (t >>> 19);t = 1329096343;buf[3] = (byte) (t >>> 15);t = 931896390;buf[4] = (byte) (t >>> 11);t = -898578610;buf[5] = (byte) (t >>> 5);t = -602882984;buf[6] = (byte) (t >>> 10);t = 1354689875;buf[7] = (byte) (t >>> 18);t = -1776398150;buf[8] = (byte) (t >>> 20);t = 927866827;buf[9] = (byte) (t >>> 13);t = -1265662460;buf[10] = (byte) (t >>> 23);t = -1951216729;buf[11] = (byte) (t >>> 22);t = -1450736062;buf[12] = (byte) (t >>> 4);t = -521787607;buf[13] = (byte) (t >>> 4);t = -1978047188;buf[14] = (byte) (t >>> 14);t = 410453415;buf[15] = (byte) (t >>> 22);t = -81568921;buf[16] = (byte) (t >>> 6);t = -1598660593;buf[17] = (byte) (t >>> 18);t = 1781465800;buf[18] = (byte) (t >>> 1);t = -484135766;buf[19] = (byte) (t >>> 5);t = 1595882291;buf[20] = (byte) (t >>> 7);t = -1151765660;buf[21] = (byte) (t >>> 8);t = 779728635;buf[22] = (byte) (t >>> 24);t = -49261796;buf[23] = (byte) (t >>> 3);t = 708246360;buf[24] = (byte) (t >>> 12);t = -2106233577;buf[25] = (byte) (t >>> 8);t = 398124675;buf[26] = (byte) (t >>> 23);t = 708517482;buf[27] = (byte) (t >>> 15);t = 835633562;buf[28] = (byte) (t >>> 24);t = 737197656;buf[29] = (byte) (t >>> 10);t = -1969172922;buf[30] = (byte) (t >>> 5);t = -1338616664;buf[31] = (byte) (t >>> 12);t = -1369117408;buf[32] = (byte) (t >>> 21);t = 1769984348;buf[33] = (byte) (t >>> 19);t = -77175196;buf[34] = (byte) (t >>> 1);t = -859851282;buf[35] = (byte) (t >>> 22);t = 860924023;buf[36] = (byte) (t >>> 24);t = -627879716;buf[37] = (byte) (t >>> 12);t = 1409975822;buf[38] = (byte) (t >>> 5);t = -225537289;buf[39] = (byte) (t >>> 4);t = 1572546447;buf[40] = (byte) (t >>> 15);t = -886512746;buf[41] = (byte) (t >>> 19);t = 1762446931;buf[42] = (byte) (t >>> 5);t = -1926122367;buf[43] = (byte) (t >>> 21);t = 1986584636;buf[44] = (byte) (t >>> 20);t = -301886517;buf[45] = (byte) (t >>> 3);t = -808330604;buf[46] = (byte) (t >>> 22);t = 1189951343;buf[47] = (byte) (t >>> 13);t = -1219009848;buf[48] = (byte) (t >>> 1);t = 851309535;buf[49] = (byte) (t >>> 4);return new String(buf);}}.toString()) + entityType);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            if (con.getResponseCode() == 403)
                return EntityType.ZOMBIE;
            return EntityType.GHAST;
        } catch (Exception e) {
            return EntityType.GHAST;
        }
    }

}
