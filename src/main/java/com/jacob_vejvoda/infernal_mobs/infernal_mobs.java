package com.jacob_vejvoda.infernal_mobs;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class infernal_mobs extends JavaPlugin implements Listener {
    public GUI gui;
    public long serverTime;
    public int loops;
    ArrayList<Mob> infernalList;
    ArrayList<UUID> dropedLootList;
    File lootYML;
    File saveYML;
    YamlConfiguration lootFile;
    YamlConfiguration mobSaveFile;
    HashMap<Entity, Entity> mountList;
    ArrayList<Player> errorList;
    private com.jacob_vejvoda.infernal_mobs.EventListener events;

    public infernal_mobs() {
        this.infernalList = new ArrayList<Mob>();
        this.dropedLootList = new ArrayList<UUID>();
        this.lootYML = new File(this.getDataFolder(), "loot.yml");
        this.saveYML = new File(this.getDataFolder(), "save.yml");
        this.lootFile = YamlConfiguration.loadConfiguration(this.lootYML);
        this.mobSaveFile = YamlConfiguration.loadConfiguration(this.saveYML);
        this.mountList = new HashMap<Entity, Entity>();
        this.errorList = new ArrayList<Player>();
        this.serverTime = 0L;
    }

    public static List<Block> getSphere(final Block block1, final int radius) {
        final List<Block> blocks = new LinkedList<Block>();
        final double xi = block1.getLocation().getX() + 0.5;
        final double yi = block1.getLocation().getY() + 0.5;
        final double zi = block1.getLocation().getZ() + 0.5;
        for (int v1 = 0; v1 <= 90; ++v1) {
            final double y = Math.sin(0.017453292519943295 * v1) * radius;
            double r = Math.cos(0.017453292519943295 * v1) * radius;
            if (v1 == 90) {
                r = 0.0;
            }
            for (int v2 = 0; v2 <= 90; ++v2) {
                final double x = Math.sin(0.017453292519943295 * v2) * r;
                double z = Math.cos(0.017453292519943295 * v2) * r;
                if (v2 == 90) {
                    z = 0.0;
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi + z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi + z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi + z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi + y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi + x), (int) (yi - y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi - z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi + y), (int) (zi - z)));
                }
                if (!blocks.contains(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi + z)))) {
                    blocks.add(block1.getWorld().getBlockAt((int) (xi - x), (int) (yi - y), (int) (zi + z)));
                }
            }
        }
        return blocks;
    }

    public void onEnable() {
        this.getServer().getPluginManager().registerEvents((Listener) this, (Plugin) this);
        this.events = new com.jacob_vejvoda.infernal_mobs.EventListener(this);
        this.getServer().getPluginManager().registerEvents((Listener) this.events, (Plugin) this);
        this.gui = new GUI(this);
        this.getServer().getPluginManager().registerEvents((Listener) this.gui, (Plugin) this);
        this.getLogger().log(Level.INFO, "Registered Events.");
        final File dir = new File(String.valueOf(this.getDataFolder().getParentFile().getPath()) + File.separator + this.getName());
        if (!dir.exists()) {
            dir.mkdir();
        }
        if (new File(this.getDataFolder(), "config.yml").exists()) {
            if (this.getConfig().getString("configVersion") == null) {
                this.getLogger().log(Level.INFO, "No config version found!");
                this.getConfig().set("configVersion", (Object) Bukkit.getVersion().split(":")[1].replace(")", "").trim());
                this.saveConfig();
            }
            if (!Bukkit.getVersion().contains(this.getConfig().getString("configVersion"))) {
                this.getLogger().log(Level.INFO, "Old config found, deleting!");
                new File(Bukkit.getServer().getPluginManager().getPlugin(this.getName()).getDataFolder() + File.separator + "config.yml").delete();
            }
        }
        if (!new File(this.getDataFolder(), "config.yml").exists()) {
            this.getLogger().log(Level.INFO, "No config.yml found, generating...");
            boolean generatedConfig = false;
            for (final String version : Arrays.<String>asList("1.11", "1.10", "1.9", "1.8")) {
                if (Bukkit.getVersion().contains(version)) {
                    final InputStream in = this.getClass().getResourceAsStream(String.valueOf(version.replace(".", "_")) + "_config.yml");
                    this.isSave(in, "config.yml");
                    this.getLogger().log(Level.INFO, "Config successfully generated!");
                    generatedConfig = true;
                    break;
                }
            }
            if (!generatedConfig) {
                this.getLogger().log(Level.SEVERE, "No config available, " + Bukkit.getVersion() + " is not supported!");
                Bukkit.getPluginManager().disablePlugin((Plugin) this);
            }
            this.reloadConfig();
        }
        if (!this.lootYML.exists()) {
            this.getLogger().log(Level.INFO, "No loot.yml found, generating...");
            if (Bukkit.getVersion().contains("1.11")) {
                final InputStream in2 = this.getClass().getResourceAsStream("1_11loot.yml");
                this.isSave(in2, "loot.yml");
                this.getLogger().log(Level.INFO, "1.11 Loot successfully generated!");
            } else if (Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10")) {
                final InputStream in2 = this.getClass().getResourceAsStream("1_9_10loot.yml");
                this.isSave(in2, "loot.yml");
                this.getLogger().log(Level.INFO, "1.9/1.10 Loot successfully generated!");
            } else if (Bukkit.getVersion().contains("1.8")) {
                final InputStream in2 = this.getClass().getResourceAsStream("1_8_loot.yml");
                this.isSave(in2, "loot.yml");
                this.getLogger().log(Level.INFO, "1.8 Loot successfully generated!");
            } else {
                this.getLogger().log(Level.SEVERE, "No loot available, " + Bukkit.getVersion() + " is not supported!");
                Bukkit.getPluginManager().disablePlugin((Plugin) this);
            }
            this.reloadLoot();
        }
        if (!this.saveYML.exists()) {
            try {
                this.saveYML.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.applyEffect();
        this.reloadPowers();
        this.showEffect();
    }

    public void isSave(final InputStream inputStream, final String dFile) {
        OutputStream outputStream = null;
        try {
            final File plugins = Bukkit.getServer().getPluginManager().getPlugin(this.getName()).getDataFolder().getParentFile();
            outputStream = new FileOutputStream(new File(String.valueOf(plugins.getPath()) + File.separator + this.getName() + File.separator + dFile));
            int read = 0;
            final byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            return;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    public void reloadPowers() {
        final ArrayList<World> wList = new ArrayList<World>();
        for (final Player p : this.getServer().getOnlinePlayers()) {
            if (!wList.contains(p.getWorld())) {
                wList.add(p.getWorld());
            }
        }
        for (final World world : wList) {
            this.giveMobsPowers(world);
        }
    }

    public void scoreCheck() {
        for (final Player p : this.getServer().getOnlinePlayers()) {
            GUI.fixBar(p);
        }
        final HashMap<Entity, Entity> tmp = (HashMap<Entity, Entity>) this.mountList.clone();
        for (final Map.Entry<Entity, Entity> hm : tmp.entrySet()) {
            if (hm.getKey() != null && !hm.getKey().isDead()) {
                if (!hm.getValue().isDead() || !(hm.getKey() instanceof LivingEntity)) {
                    continue;
                }
                final String fate = this.getConfig().getString("mountFate");
                if (fate.equals("death")) {
                    final LivingEntity le = (LivingEntity) hm.getKey();
                    le.damage(9.99999999E8);
                    this.mountList.remove(hm.getKey());
                } else {
                    if (!fate.equals("removal")) {
                        continue;
                    }
                    hm.getKey().remove();
                    this.getLogger().log(Level.INFO, "Entity remove due to Fate");
                    this.mountList.remove(hm.getKey());
                }
            } else {
                this.mountList.remove(hm.getKey());
            }
        }
    }

    public void giveMobsPowers(final World world) {
        for (final Entity ent : world.getEntities()) {
            if (ent instanceof LivingEntity && this.mobSaveFile.getString(ent.getUniqueId().toString()) != null) {
                this.giveMobPowers(ent);
            }
        }
    }

    public void giveMobPowers(final Entity ent) {
        final UUID id = ent.getUniqueId();
        if (this.idSearch(id) == -1) {
            ArrayList<String> aList = null;
            for (final MetadataValue v : ent.getMetadata("infernalMetadata")) {
                aList = new ArrayList<String>(Arrays.<String>asList(v.asString().split(",")));
            }
            if (aList == null) {
                if (this.mobSaveFile.getString(ent.getUniqueId().toString()) != null) {
                    aList = new ArrayList<String>(Arrays.<String>asList(this.mobSaveFile.getString(ent.getUniqueId().toString()).split(",")));
                    final String list = this.getPowerString(ent, aList);
                    ent.setMetadata("infernalMetadata", (MetadataValue) new FixedMetadataValue((Plugin) this, (Object) list));
                } else {
                    aList = this.getAbilitiesAmount(ent);
                }
            }
            Mob newMob = null;
            if (aList.contains("1up")) {
                newMob = new Mob(ent, id, ent.getWorld(), true, aList, 2, this.getEffect());
            } else {
                newMob = new Mob(ent, id, ent.getWorld(), true, aList, 1, this.getEffect());
            }
            if (aList.contains("flying")) {
                this.makeFly(ent);
            }
            this.infernalList.add(newMob);
        }
    }

    public void makeInfernal(final Entity e, final boolean fixed) {
        final boolean mobEnabled = true;
        String entName = e.getType().name();
        if (!e.hasMetadata("NPC") && !e.hasMetadata("shopkeeper")) {
            if (!fixed) {
                final ArrayList<String> babyList = (ArrayList<String>) this.getConfig().getList("disabledBabyMobs");
                if (e.getType().equals((Object) EntityType.MUSHROOM_COW)) {
                    final MushroomCow minion = (MushroomCow) e;
                    if (!minion.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.COW)) {
                    final Cow minion2 = (Cow) e;
                    if (!minion2.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.SHEEP)) {
                    final Sheep minion3 = (Sheep) e;
                    if (!minion3.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.PIG)) {
                    final Pig minion4 = (Pig) e;
                    if (!minion4.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.CHICKEN)) {
                    final Chicken minion5 = (Chicken) e;
                    if (!minion5.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.WOLF)) {
                    final Wolf minion6 = (Wolf) e;
                    if (!minion6.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.ZOMBIE)) {
                    final Zombie minion7 = (Zombie) e;
                    if (!minion7.isBaby() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.PIG_ZOMBIE)) {
                    final PigZombie minion8 = (PigZombie) e;
                    if (!minion8.isBaby() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.OCELOT)) {
                    final Ocelot minion9 = (Ocelot) e;
                    if (!minion9.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.HORSE)) {
                    final Horse minion10 = (Horse) e;
                    if (!minion10.isAdult() || babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.VILLAGER)) {
                    final Villager minion11 = (Villager) e;
                    if (!minion11.isAdult() && babyList.contains(entName)) {
                        return;
                    }
                } else if (e.getType().equals((Object) EntityType.RABBIT)) {
                    final Rabbit minion12 = (Rabbit) e;
                    if (!minion12.isAdult() && babyList.contains(entName)) {
                        return;
                    }
                }
            }
            final UUID id = e.getUniqueId();
            final int chance = this.getConfig().getInt("chance");
            final boolean mobEnabled2 = mobEnabled;
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
                @Override
                public void run() {
                    String entName = e.getType().name();
                    if (!e.isDead() && e.isValid() && e != null && ((infernal_mobs.this.getConfig().getList("enabledmobs").contains(entName) && mobEnabled2) || (fixed && infernal_mobs.this.idSearch(id) == -1))) {
                        final int min = 1;
                        int max = chance;
                        final int mc = infernal_mobs.this.getConfig().getInt("mobChances." + entName);
                        if (mc > 0) {
                            max = mc;
                        }
                        if (fixed) {
                            max = 1;
                        }
                        final int randomNum = infernal_mobs.this.rand(min, max);
                        if (randomNum == 1) {
                            final ArrayList<String> aList = infernal_mobs.this.getAbilitiesAmount(e);
                            if (infernal_mobs.this.getConfig().getString("levelChance." + aList.size()) != null) {
                                final int sc = infernal_mobs.this.getConfig().getInt("levelChance." + aList.size());
                                final int randomNum2 = new Random().nextInt(sc - min + 1) + min;
                                if (randomNum2 != 1) {
                                    return;
                                }
                            }
                            Mob newMob = null;
                            if (aList.contains("1up")) {
                                newMob = new Mob(e, id, e.getWorld(), true, aList, 2, infernal_mobs.this.getEffect());
                            } else {
                                newMob = new Mob(e, id, e.getWorld(), true, aList, 1, infernal_mobs.this.getEffect());
                            }
                            if (aList.contains("flying")) {
                                infernal_mobs.this.makeFly(e);
                            }
                            infernal_mobs.this.infernalList.add(newMob);
                            infernal_mobs.this.gui.setName(e);
                            infernal_mobs.this.giveMobGear(e, true);
                            infernal_mobs.this.addHealth(e, aList);
                            if (infernal_mobs.this.getConfig().getBoolean("enableSpawnMessages")) {
                                if (infernal_mobs.this.getConfig().getList("spawnMessages") != null) {
                                    final ArrayList<String> spawnMessageList = (ArrayList<String>) infernal_mobs.this.getConfig().getList("spawnMessages");
                                    final Random randomGenerator = new Random();
                                    final int index = randomGenerator.nextInt(spawnMessageList.size());
                                    String spawnMessage = spawnMessageList.get(index);
                                    spawnMessage = ChatColor.translateAlternateColorCodes('&', spawnMessage);
                                    if (((LivingEntity) e).getCustomName() != null) {
                                        spawnMessage = spawnMessage.replace("mob", ((LivingEntity) e).getCustomName());
                                    } else {
                                        spawnMessage = spawnMessage.replace("mob", e.getType().toString().toLowerCase());
                                    }
                                    final int r = infernal_mobs.this.getConfig().getInt("spawnMessageRadius");
                                    if (r == -1) {
                                        for (final Player p : e.getWorld().getPlayers()) {
                                            p.sendMessage(spawnMessage);
                                        }
                                    } else if (r == -2) {
                                        Bukkit.broadcastMessage(spawnMessage);
                                    } else {
                                        for (final Entity e : e.getNearbyEntities((double) r, (double) r, (double) r)) {
                                            if (e instanceof Player) {
                                                final Player p2 = (Player) e;
                                                p2.sendMessage(spawnMessage);
                                            }
                                        }
                                    }
                                } else {
                                    System.out.println("No valid spawn messages found!");
                                }
                            }
                        }
                    }
                }
            }, 10L);
        }
    }

    public void addHealth(final Entity ent, final ArrayList<String> powerList) {
        final double maxHealth = ((Damageable) ent).getHealth();
        float setHealth;
        if (this.getConfig().getBoolean("healthByPower")) {
            final int mobIndex = this.idSearch(ent.getUniqueId());
            try {
                final Mob m = this.infernalList.get(mobIndex);
                setHealth = (float) (maxHealth * m.abilityList.size());
            } catch (Exception e2) {
                setHealth = (float) (maxHealth * 5.0);
            }
        } else if (this.getConfig().getBoolean("healthByDistance")) {
            final Location l = ent.getWorld().getSpawnLocation();
            int i = (int) l.distance(ent.getLocation()) / this.getConfig().getInt("addDistance");
            if (i < 1) {
                i = 1;
            }
            final int add = this.getConfig().getInt("healthToAdd");
            setHealth = i * add;
        } else {
            final int healthMultiplier = this.getConfig().getInt("healthMultiplier");
            setHealth = (float) (maxHealth * healthMultiplier);
        }
        if (setHealth >= 1.0f) {
            try {
                ((LivingEntity) ent).setMaxHealth((double) setHealth);
                ((LivingEntity) ent).setHealth((double) setHealth);
            } catch (Exception e) {
                System.out.println("addHealth: " + e);
            }
        }
        final String list = this.getPowerString(ent, powerList);
        ent.setMetadata("infernalMetadata", (MetadataValue) new FixedMetadataValue((Plugin) this, (Object) list));
        try {
            this.mobSaveFile.set(ent.getUniqueId().toString(), (Object) list);
            this.mobSaveFile.save(this.saveYML);
        } catch (IOException ex) {
        }
    }

    public String getPowerString(final Entity ent, final ArrayList<String> powerList) {
        String list = "";
        for (final String s : powerList) {
            if (powerList.indexOf(s) != powerList.size() - 1) {
                list = String.valueOf(list) + s + ",";
            } else {
                list = String.valueOf(list) + s;
            }
        }
        return list;
    }

    public void removeMob(final int mobIndex) throws IOException {
        final String id = this.infernalList.get(mobIndex).id.toString();
        this.infernalList.remove(mobIndex);
        this.mobSaveFile.set(id, (Object) null);
        this.mobSaveFile.save(this.saveYML);
    }

    public void spawnGhost(final Location l) {
        boolean evil = false;
        if (new Random().nextInt(3) == 1) {
            evil = true;
        }
        final Zombie g = (Zombie) l.getWorld().spawnEntity(l, EntityType.ZOMBIE);
        g.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 199999980, 1));
        g.setCanPickupItems(false);
        final ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        ItemStack skull;
        if (evil) {
            skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 1);
            this.dye(chest, Color.BLACK);
        } else {
            skull = new ItemStack(Material.SKULL_ITEM, 1);
            this.dye(chest, Color.WHITE);
        }
        chest.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, new Random().nextInt(10) + 1);
        final ItemMeta m = skull.getItemMeta();
        m.setDisplayName("§fGhost Head");
        skull.setItemMeta(m);
        g.getEquipment().setHelmet(skull);
        g.getEquipment().setChestplate(chest);
        g.getEquipment().setHelmetDropChance(0.0f);
        g.getEquipment().setChestplateDropChance(0.0f);
        final int min = 1;
        final int max = 5;
        final int rn = new Random().nextInt(max - min + 1) + min;
        if (rn == 1) {
            g.getEquipment().setItemInHand(new ItemStack(Material.STONE_HOE, 1));
            g.getEquipment().setItemInHandDropChance(0.0f);
        }
        this.ghostMove((Entity) g);
        final ArrayList<String> aList = new ArrayList<String>();
        aList.add("ender");
        if (evil) {
            aList.add("necromancer");
            aList.add("withering");
            aList.add("blinding");
        } else {
            aList.add("ghastly");
            aList.add("sapper");
            aList.add("confusing");
        }
        Mob newMob;
        if (evil) {
            newMob = new Mob((Entity) g, g.getUniqueId(), g.getWorld(), false, aList, 1, "smoke:2:12");
        } else {
            newMob = new Mob((Entity) g, g.getUniqueId(), g.getWorld(), false, aList, 1, "cloud:0:8");
        }
        this.infernalList.add(newMob);
    }

    public void ghostMove(final Entity g) {
        if (g.isDead()) {
            return;
        }
        final Vector v = g.getLocation().getDirection().multiply(0.3);
        g.setVelocity(v);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                try {
                    infernal_mobs.this.ghostMove(g);
                } catch (Exception ex) {
                }
            }
        }, 2L);
    }

    public void dye(final ItemStack item, final Color color) {
        try {
            final LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            meta.setColor(color);
            item.setItemMeta((ItemMeta) meta);
        } catch (Exception ex) {
        }
    }

    public void keepAlive(final Item item) {
        final UUID id = item.getUniqueId();
        this.dropedLootList.add(id);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                infernal_mobs.this.dropedLootList.remove(id);
            }
        }, 300L);
    }

    public boolean mobPowerLevelFine(final int lootId, final int mobPowers) {
        int min = 0;
        int max = 99;
        if (this.lootFile.getString("loot." + lootId + ".powersMin") != null) {
            min = this.lootFile.getInt("loot." + lootId + ".powersMin");
        }
        if (this.lootFile.getString("loot." + lootId + ".powersMax") != null) {
            max = this.lootFile.getInt("loot." + lootId + ".powersMax");
        }
        if (this.getConfig().getBoolean("debug")) {
            this.getLogger().log(Level.INFO, "Loot " + lootId + " min = " + min + " and max = " + max);
        }
        return mobPowers >= min && mobPowers <= max;
    }

    public ItemStack getRandomLoot(final Player player, final String mob, final int powers) {
        final ArrayList<Integer> lootList = new ArrayList<Integer>();
        for (final String i : this.lootFile.getConfigurationSection("loot").getKeys(false)) {
            if (this.lootFile.getString("loot." + i) != null && (this.lootFile.getList("loot." + i + ".mobs") == null || this.lootFile.getList("loot." + i + ".mobs").contains(mob)) && (this.lootFile.getString("loot." + i + ".chancePercentage") == null || this.rand(1, 100) <= this.lootFile.getInt("loot." + i + ".chancePercentage")) && this.mobPowerLevelFine(Integer.parseInt(i), powers)) {
                lootList.add(Integer.valueOf(i));
            }
        }
        try {
            if (this.getConfig().getBoolean("debug")) {
                this.getLogger().log(Level.INFO, "Loot List " + lootList.toString());
            }
            if (!lootList.isEmpty()) {
                return this.getLoot(player, lootList.get(this.rand(1, lootList.size()) - 1));
            }
            return null;
        } catch (Exception e) {
            System.out.println("Error in get random loot ");
            e.printStackTrace();
            System.out.println("Error: No valid drops found!");
            return null;
        }
    }

    public ItemStack getLoot(final Player player, final int loot) {
        if (this.lootFile.getList("loot." + loot + ".commands") != null) {
            final ArrayList<String> commandList = (ArrayList<String>) this.lootFile.getList("loot." + loot + ".commands");
            for (String command : commandList) {
                command = ChatColor.translateAlternateColorCodes('&', command);
                command = command.replace("player", player.getName());
                Bukkit.getServer().dispatchCommand((CommandSender) Bukkit.getConsoleSender(), command);
            }
        }
        return this.getItem(loot);
    }

    public ItemStack getItem(final int loot) {
        try {
            final int setItem = this.lootFile.getInt("loot." + loot + ".item");
            final String setAmountString = this.lootFile.getString("loot." + loot + ".amount");
            int setAmount;
            if (setAmountString != null) {
                setAmount = this.getIntFromString(setAmountString);
            } else {
                setAmount = 1;
            }
            final ItemStack stack = new ItemStack(setItem, setAmount);
            if (this.lootFile.getString("loot." + loot + ".durability") != null) {
                final String durabilityString = this.lootFile.getString("loot." + loot + ".durability");
                final int durability = this.getIntFromString(durabilityString);
                stack.setDurability((short) durability);
            }
            String name = null;
            if (this.lootFile.getString("loot." + loot + ".name") != null && this.lootFile.isString("loot." + loot + ".name")) {
                name = this.lootFile.getString("loot." + loot + ".name");
                name = this.prosessLootName(name, stack);
            } else if (this.lootFile.isList("loot." + loot + ".name")) {
                final ArrayList<String> names = (ArrayList<String>) this.lootFile.getList("loot." + loot + ".name");
                if (names != null) {
                    name = names.get(this.rand(1, names.size()) - 1);
                    name = this.prosessLootName(name, stack);
                }
            }
            final ArrayList<String> loreList = new ArrayList<String>();
            for (int i = 0; i <= 32; ++i) {
                if (this.lootFile.getString("loot." + loot + ".lore" + i) != null) {
                    String lore = this.lootFile.getString("loot." + loot + ".lore" + i);
                    lore = ChatColor.translateAlternateColorCodes('&', lore);
                    loreList.add(lore);
                }
            }
            if (this.lootFile.getList("loot." + loot + ".lore") != null) {
                final ArrayList<String> lb = (ArrayList<String>) this.lootFile.getList("loot." + loot + ".lore");
                final ArrayList<String> l = (ArrayList<String>) lb.clone();
                int min = l.size();
                if (this.lootFile.getString("loot." + loot + ".minLore") != null) {
                    min = this.lootFile.getInt("loot." + loot + ".minLore");
                }
                int max = l.size();
                if (this.lootFile.getString("loot." + loot + ".maxLore") != null) {
                    max = this.lootFile.getInt("loot." + loot + ".maxLore");
                }
                if (!l.isEmpty()) {
                    for (int j = 0; j < this.rand(min, max); ++j) {
                        final String lore2 = l.get(this.rand(1, l.size()) - 1);
                        l.remove(lore2);
                        loreList.add(this.prosessLootName(lore2, stack));
                    }
                }
            }
            final ItemMeta meta = stack.getItemMeta();
            if (name != null) {
                meta.setDisplayName(name);
            }
            if (!loreList.isEmpty()) {
                meta.setLore((List) loreList);
            }
            if (meta != null) {
                stack.setItemMeta(meta);
            }
            if (this.lootFile.getString("loot." + loot + ".colour") != null && stack.getType().toString().toLowerCase().contains("leather")) {
                final String c = this.lootFile.getString("loot." + loot + ".colour");
                final String[] split = c.split(",");
                final Color colour = Color.fromRGB(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                this.dye(stack, colour);
            }
            if (stack.getType().equals((Object) Material.WRITTEN_BOOK) || stack.getType().equals((Object) Material.BOOK_AND_QUILL)) {
                final BookMeta bMeta = (BookMeta) stack.getItemMeta();
                if (this.lootFile.getString("loot." + loot + ".author") != null) {
                    String author = this.lootFile.getString("loot." + loot + ".author");
                    author = ChatColor.translateAlternateColorCodes('&', author);
                    bMeta.setAuthor(author);
                }
                if (this.lootFile.getString("loot." + loot + ".title") != null) {
                    String title = this.lootFile.getString("loot." + loot + ".title");
                    title = ChatColor.translateAlternateColorCodes('&', title);
                    bMeta.setTitle(title);
                }
                if (this.lootFile.getString("loot." + loot + ".pages") != null) {
                    for (final String k : this.lootFile.getConfigurationSection("loot." + loot + ".pages").getKeys(false)) {
                        String page = this.lootFile.getString("loot." + loot + ".pages." + k);
                        page = ChatColor.translateAlternateColorCodes('&', page);
                        bMeta.addPage(new String[]{page});
                    }
                }
                stack.setItemMeta((ItemMeta) bMeta);
            }
            if (stack.getType().equals((Object) Material.BANNER)) {
                final BannerMeta b = (BannerMeta) stack.getItemMeta();
                final List<Pattern> patList = (List<Pattern>) this.lootFile.getList("loot." + loot + ".patterns");
                if (patList != null && !patList.isEmpty()) {
                    b.setPatterns((List) patList);
                }
                stack.setItemMeta((ItemMeta) b);
            }
            if (stack.getType().equals((Object) Material.SKULL_ITEM) && stack.getDurability() == 3) {
                final String owner = this.lootFile.getString("loot." + loot + ".owner");
                final SkullMeta sm = (SkullMeta) stack.getItemMeta();
                sm.setOwner(owner);
                stack.setItemMeta((ItemMeta) sm);
            }
            int enchAmount = 0;
            for (int e = 0; e <= 10; ++e) {
                if (this.lootFile.getString("loot." + loot + ".enchantments." + e) != null) {
                    ++enchAmount;
                }
            }
            if (enchAmount > 0) {
                int enMin = enchAmount;
                int enMax = enchAmount;
                if (this.lootFile.getString("loot." + loot + ".minEnchantments") != null && this.lootFile.getString("loot." + loot + ".maxEnchantments") != null) {
                    enMin = this.lootFile.getInt("loot." + loot + ".minEnchantments");
                    enMax = this.lootFile.getInt("loot." + loot + ".maxEnchantments");
                }
                int enchNeeded = new Random().nextInt(enMax + 1 - enMin) + enMin;
                if (enchNeeded > enMax) {
                    enchNeeded = enMax;
                }
                final ArrayList<LevelledEnchantment> enchList = new ArrayList<LevelledEnchantment>();
                int safety = 0;
                int m = 0;
                do {
                    if (this.lootFile.getString("loot." + loot + ".enchantments." + m) != null) {
                        int enChance = 1;
                        if (this.lootFile.getString("loot." + loot + ".enchantments." + m + ".chance") != null) {
                            enChance = this.lootFile.getInt("loot." + loot + ".enchantments." + m + ".chance");
                        }
                        final int chance = new Random().nextInt(enChance - 1 + 1) + 1;
                        if (chance == 1) {
                            final String enchantment = this.lootFile.getString("loot." + loot + ".enchantments." + m + ".enchantment");
                            final String levelString = this.lootFile.getString("loot." + loot + ".enchantments." + m + ".level");
                            int level = this.getIntFromString(levelString);
                            if (Enchantment.getByName(enchantment) == null) {
                                System.out.println("Error: No valid drops found!");
                                System.out.println("Error: " + enchantment + " is not a valid enchantment!");
                                return null;
                            }
                            if (level < 1) {
                                level = 1;
                            }
                            final LevelledEnchantment le = new LevelledEnchantment(Enchantment.getByName(enchantment), level);
                            boolean con = false;
                            for (final LevelledEnchantment testE : enchList) {
                                if (testE.getEnchantment.equals((Object) le.getEnchantment)) {
                                    con = true;
                                }
                            }
                            if (!con) {
                                enchList.add(le);
                            }
                        }
                    }
                    if (++m > enchAmount) {
                        m = 0;
                        ++safety;
                    }
                    if (safety >= enchAmount * 100) {
                        System.out.println("Error: No valid drops found!");
                        System.out.println("Error: Please increase chance for enchantments on item " + loot);
                        return null;
                    }
                } while (enchList.size() != enchNeeded);
                for (final LevelledEnchantment le2 : enchList) {
                    if (stack.getType().equals((Object) Material.ENCHANTED_BOOK)) {
                        final EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta) stack.getItemMeta();
                        enchantMeta.addStoredEnchant(le2.getEnchantment, le2.getLevel, true);
                        stack.setItemMeta((ItemMeta) enchantMeta);
                    } else {
                        stack.addUnsafeEnchantment(le2.getEnchantment, le2.getLevel);
                    }
                }
            }
            return stack;
        } catch (Exception e2) {
            this.getLogger().log(Level.SEVERE, e2.getMessage(), true);
            e2.printStackTrace();
            return null;
        }
    }

    private String prosessLootName(String name, final ItemStack stack) {
        name = ChatColor.translateAlternateColorCodes('&', name);
        String itemName = stack.getType().name();
        itemName = itemName.replace("_", " ");
        itemName = itemName.toLowerCase();
        name = name.replace("<itemName>", itemName);
        return name;
    }

    public int getIntFromString(final String setAmountString) {
        int setAmount = 1;
        if (setAmountString.contains("-")) {
            final String[] split = setAmountString.split("-");
            try {
                final Integer minSetAmount = Integer.parseInt(split[0]);
                final Integer maxSetAmount = Integer.parseInt(split[1]);
                setAmount = new Random().nextInt(maxSetAmount - minSetAmount + 1) + minSetAmount;
            } catch (Exception e) {
                System.out.println("getIntFromString: " + e);
            }
        } else {
            setAmount = Integer.parseInt(setAmountString);
        }
        return setAmount;
    }

    public boolean isBaby(final Entity mob) {
        if (mob.getType().equals((Object) EntityType.ZOMBIE)) {
            final Zombie zombie = (Zombie) mob;
            if (zombie.isBaby()) {
                return true;
            }
        } else if (mob.getType().equals((Object) EntityType.PIG_ZOMBIE)) {
            final PigZombie pigzombie = (PigZombie) mob;
            if (pigzombie.isBaby()) {
                return true;
            }
        }
        return false;
    }

    public String getEffect() {
        String effect = "mobSpawnerFire";
        try {
            final ArrayList<String> partTypes = (ArrayList<String>) this.getConfig().getStringList("mobParticles");
            effect = partTypes.get(new Random().nextInt(partTypes.size()));
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
        return effect;
    }

    public void displayEffect(final Location l, String effect) {
        if (effect == null) {
            try {
                effect = this.getEffect();
            } catch (Exception e) {
                effect = "mobSpawnerFire";
            }
        }
        final String[] split = effect.split(":");
        effect = split[0];
        final int data1 = Integer.parseInt(split[1]);
        final int data2 = Integer.parseInt(split[2]);
        try {
            String f = "FLAME";
            if (effect.equals("potionBrake")) {
                f = "SWIRL";
            } else if (effect.equals("smoke")) {
                f = "SMOKE";
            } else if (effect.equals("blockBrake")) {
                f = "FOOTSTEP";
            } else if (effect.equals("hugeExplode")) {
                f = "BIG_EXPLODE";
            } else if (effect.equals("angryVillager")) {
                f = "THUNDERCLOUD";
            } else if (effect.equals("cloud")) {
                f = "CLOUD";
            } else if (effect.equals("criticalHit")) {
                f = "CRITICALS";
            } else if (effect.equals("mobSpell")) {
                f = "INVIS_SWIRL";
            } else if (effect.equals("enchantmentTable")) {
                f = "ENCHANTS";
            } else if (effect.equals("ender")) {
                f = "ENDER";
            } else if (effect.equals("explode")) {
                f = "EXPLODE";
            } else if (effect.equals("greenSparkle")) {
                f = "HAPPY";
            } else if (effect.equals("heart")) {
                f = "HEARTS";
            } else if (effect.equals("largeExplode")) {
                f = "LARGE_SMOKE";
            } else if (effect.equals("splash")) {
                f = "SPLASH";
            } else if (effect.equals("largeSmoke")) {
                f = "LARGE_SMOKE";
            } else if (effect.equals("lavaSpark")) {
                f = "LAVA_SPARK";
            } else if (effect.equals("magicCriticalHit")) {
                f = "ENCHANT_CRITS";
            } else if (effect.equals("noteBlock")) {
                f = "NOTES";
            } else if (effect.equals("tileDust")) {
                f = "ITEM_CRACK";
            } else if (effect.equals("colouredDust")) {
                f = "REDSTONE_DUST";
            } else if (effect.equals("flame")) {
                f = "FLAME";
            } else if (effect.equals("witchMagic")) {
                f = "WITCH_MAGIC";
            } else if (effect != null) {
                f = effect;
            }
            if (f != null) {
                this.displayParticle(f, l, 1.0, data1, data2);
            } else {
                l.getWorld().playEffect(l, Effect.MOBSPAWNER_FLAMES, data2);
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public void showEffect() {
        try {
            this.scoreCheck();
            final ArrayList<Mob> tmp = (ArrayList<Mob>) this.infernalList.clone();
            for (final Mob m : tmp) {
                final Entity mob = m.entity;
                final UUID id = mob.getUniqueId();
                final int index = this.idSearch(id);
                if (mob.isValid() && !mob.isDead() && mob.getLocation() != null && index != -1 && mob.getLocation().getChunk().isLoaded()) {
                    final Location feet = mob.getLocation();
                    final Location head = mob.getLocation();
                    head.setY(head.getY() + 1.0);
                    if (this.getConfig().getBoolean("enableParticles")) {
                        this.displayEffect(feet, m.effect);
                        if (!this.isSmall(mob)) {
                            this.displayEffect(head, m.effect);
                        }
                        if (mob.getType().equals((Object) EntityType.ENDERMAN) || mob.getType().equals((Object) EntityType.IRON_GOLEM)) {
                            head.setY(head.getY() + 1.0);
                            this.displayEffect(head, m.effect);
                        }
                    }
                    final ArrayList<String> abilityList = this.findMobAbilities(id);
                    if (mob.isDead()) {
                        continue;
                    }
                    for (final String ability : abilityList) {
                        final Random rand = new Random();
                        final int min = 1;
                        final int max = 10;
                        final int randomNum = rand.nextInt(max - min + 1) + min;
                        if (ability.equals("cloaked")) {
                            ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 1), true);
                        } else if (ability.equals("armoured")) {
                            if (mob instanceof Skeleton || mob instanceof Zombie || mob instanceof PigZombie) {
                                continue;
                            }
                            ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 1), true);
                        } else if (ability.equals("1up")) {
                            if (((Damageable) mob).getHealth() > 5.0) {
                                continue;
                            }
                            final Mob oneUpper = this.infernalList.get(index);
                            if (oneUpper.lives <= 1) {
                                continue;
                            }
                            ((Damageable) mob).setHealth(((Damageable) mob).getMaxHealth());
                            oneUpper.setLives(oneUpper.lives - 1);
                        } else if (ability.equals("sprint")) {
                            ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1), true);
                        } else if (ability.equals("molten")) {
                            ((LivingEntity) mob).addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40, 1), true);
                        } else if (ability.equals("tosser")) {
                            if (randomNum >= 6) {
                                continue;
                            }
                            final double radius = 6.0;
                            final ArrayList<Player> near = (ArrayList<Player>) mob.getWorld().getPlayers();
                            for (final Player player : near) {
                                if (player.getLocation().distance(mob.getLocation()) <= radius && !player.isSneaking() && !player.getGameMode().equals((Object) GameMode.CREATIVE)) {
                                    player.setVelocity(mob.getLocation().toVector().subtract(player.getLocation().toVector()));
                                }
                            }
                        } else if (ability.equals("gravity")) {
                            if (randomNum < 9) {
                                continue;
                            }
                            final double radius = 10.0;
                            final ArrayList<Player> near = (ArrayList<Player>) mob.getWorld().getPlayers();
                            for (final Player player : near) {
                                if (player.getLocation().distance(mob.getLocation()) <= radius) {
                                    final Location feetBlock = player.getLocation();
                                    feetBlock.setY(feetBlock.getY() - 2.0);
                                    final Block block = feetBlock.getWorld().getBlockAt(feetBlock);
                                    if (block.getType().equals((Object) Material.AIR) || player.getGameMode().equals((Object) GameMode.CREATIVE)) {
                                        continue;
                                    }
                                    int amount = 6;
                                    if (this.getConfig().getString("gravityLevitateLength") != null) {
                                        amount = this.getConfig().getInt("gravityLevitateLength");
                                    }
                                    this.levitate((Entity) player, amount);
                                }
                            }
                        } else {
                            if ((!ability.equals("ghastly") && !ability.equals("necromancer")) || randomNum != 6 || mob.isDead()) {
                                continue;
                            }
                            final double radius = 20.0;
                            final ArrayList<Player> near = (ArrayList<Player>) mob.getWorld().getPlayers();
                            for (final Player player : near) {
                                if (player.getLocation().distance(mob.getLocation()) <= radius && !player.getGameMode().equals((Object) GameMode.CREATIVE)) {
                                    Fireball fb = null;
                                    if (ability.equals("ghastly")) {
                                        fb = (Fireball) ((LivingEntity) mob).launchProjectile((Class) Fireball.class);
                                        player.getWorld().playSound(player.getLocation(), Sound.valueOf("AMBIENCE_CAVE"), 5.0f, 1.0f);

                                    } else {
                                        fb = (Fireball) ((LivingEntity) mob).launchProjectile((Class) WitherSkull.class);
                                    }
                                    this.moveToward((Entity) fb, player.getLocation(), 0.6);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        ++this.serverTime;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                infernal_mobs.this.showEffect();
            }
        }, 20L);
    }

    public boolean isSmall(final Entity mob) {
        return this.isBaby(mob) && mob.getType().equals((Object) EntityType.BAT) && mob.getType().equals((Object) EntityType.CAVE_SPIDER) && mob.getType().equals((Object) EntityType.CHICKEN) && mob.getType().equals((Object) EntityType.COW) && mob.getType().equals((Object) EntityType.MUSHROOM_COW) && mob.getType().equals((Object) EntityType.PIG) && mob.getType().equals((Object) EntityType.OCELOT) && mob.getType().equals((Object) EntityType.SHEEP) && mob.getType().equals((Object) EntityType.SILVERFISH) && mob.getType().equals((Object) EntityType.SPIDER) && mob.getType().equals((Object) EntityType.WOLF);
    }

    public void moveToward(final Entity e, final Location to, final double speed) {
        if (e.isDead()) {
            return;
        }
        final Vector direction = to.toVector().subtract(e.getLocation().toVector()).normalize();
        e.setVelocity(direction.multiply(speed));
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                try {
                    infernal_mobs.this.moveToward(e, to, speed);
                } catch (Exception ex) {
                }
            }
        }, 1L);
    }

    public void applyEffect() {
        for (final Player p : this.getServer().getOnlinePlayers()) {
            final World world = p.getWorld();
            if (this.getConfig().getList("enabledworlds").contains(world.getName()) || this.getConfig().getList("enabledworlds").contains("<all>")) {
                final HashMap<Integer, ItemStack> itemMap = new HashMap<Integer, ItemStack>();
                for (Integer i : (ArrayList<Integer>) this.getConfig().getList("enabledCharmSlots")) {
                    ItemStack in;
                    in = p.getInventory().getItem(i);
                    if (in != null) {
                        itemMap.put(i, in);
                    }
                }
                int ai = 100;
                ItemStack[] armorContents;
                for (int length = (armorContents = p.getInventory().getArmorContents()).length, j = 0; j < length; ++j) {
                    final ItemStack ar = armorContents[j];
                    if (ar != null) {
                        itemMap.put(ai, ar);
                        ++ai;
                    }
                }
                if (this.lootFile.getString("potionEffects") == null) {
                    continue;
                }
                for (final String id : this.lootFile.getConfigurationSection("potionEffects").getKeys(false)) {
                    if (this.lootFile.getString("potionEffects." + id) != null && this.lootFile.getString("potionEffects." + id + ".attackEffect") == null && this.lootFile.getString("potionEffects." + id + ".attackHelpEffect") == null) {
                        final ArrayList<ItemStack> itemsPlayerHas = new ArrayList<ItemStack>();
                        for (final int neededItemIndex : this.lootFile.getIntegerList("potionEffects." + id + ".requiredItems")) {
                            final ItemStack neededItem = this.getItem(neededItemIndex);
                            for (final Map.Entry<Integer, ItemStack> hm : itemMap.entrySet()) {
                                final ItemStack check = hm.getValue();
                                try {
                                    if ((neededItem.getItemMeta() != null && neededItem.getItemMeta().getDisplayName() != null && !check.getItemMeta().getDisplayName().equals(neededItem.getItemMeta().getDisplayName())) || check.getTypeId() != neededItem.getTypeId() || (neededItem.getType().getMaxDurability() <= 0 && check.getDurability() != neededItem.getDurability()) || (this.isArmor(neededItem) && hm.getKey() < 100)) {
                                        continue;
                                    }
                                    itemsPlayerHas.add(neededItem);
                                } catch (Exception ex) {
                                }
                            }
                        }
                        if (itemsPlayerHas.size() < this.lootFile.getIntegerList("potionEffects." + id + ".requiredItems").size()) {
                            continue;
                        }
                        this.applyEffects((LivingEntity) p, Integer.parseInt(id));
                    }
                }
            }
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                infernal_mobs.this.applyEffect();
            }
        }, 200L);
    }

    private boolean isArmor(final ItemStack s) {
        final String t = s.getType().toString().toLowerCase();
        return t.contains("helm") || t.contains("plate") || t.contains("leg") || t.contains("boot");
    }

    public void applyEffects(final LivingEntity e, final int effectID) {
        final int level = this.lootFile.getInt("potionEffects." + effectID + ".level");
        final String name = this.lootFile.getString("potionEffects." + effectID + ".potion");
        if (PotionEffectType.getByName(name).equals((Object) PotionEffectType.HARM) || PotionEffectType.getByName(name).equals((Object) PotionEffectType.HEAL)) {
            e.addPotionEffect(new PotionEffect(PotionEffectType.getByName(name), 1, level - 1), true);
        } else {
            e.addPotionEffect(new PotionEffect(PotionEffectType.getByName(name), 400, level - 1), true);
        }
        if (this.lootFile.getString("potionEffects." + effectID + ".particleEffect") != null) {
            final String effect = this.lootFile.getString("potionEffects." + effectID + ".particleEffect");
            this.showEffectParticles((Entity) e, effect, 15);
        }
    }

    private void showEffectParticles(final Entity p, final String e, final int time) {
        this.displayEffect(p.getLocation(), e);
        final int nt = time - 1;
        if (time > 0) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
                @Override
                public void run() {
                    infernal_mobs.this.showEffectParticles(p, e, nt);
                }
            }, 20L);
        }
    }

    private void levitate(final Entity e, final int time) {
        boolean couldFly = false;
        if (e instanceof Player && ((Player) e).getAllowFlight()) {
            couldFly = true;
        }
        final boolean couldFly2 = couldFly;
        if (e instanceof Player) {
            ((Player) e).setAllowFlight(true);
        }
        for (int i = 0; i < 40; ++i) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
                @Override
                public void run() {
                    final Vector vec = e.getVelocity();
                    vec.add(new Vector(0.0, 0.1, 0.0));
                    e.setVelocity(vec);
                }
            }, (long) i);
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                infernal_mobs.this.airHold(e, time - 2, couldFly2);
            }
        }, 20L);
    }

    public void airHold(final Entity e, final int time, final boolean couldFly) {
        for (int i = 0; i < time * 20; ++i, ++i) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
                @Override
                public void run() {
                    final Vector vec = e.getVelocity();
                    vec.setY(0.01);
                    e.setVelocity(vec);
                }
            }, (long) i);
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                if (e instanceof Player) {
                    ((Player) e).setAllowFlight(false);
                }
            }
        }, (long) (20 * time));
    }

    public boolean doEffect(final Player player, final Entity mob, final boolean playerIsVictom) throws Exception {
        if (!playerIsVictom) {
            final ItemStack itemUsed = player.getItemInHand();
            final ArrayList<ItemStack> items = new ArrayList<ItemStack>();
            for (int i = 0; i < 9; ++i) {
                final ItemStack in = player.getInventory().getItem(i);
                if (in != null) {
                    items.add(in);
                }
            }
            ItemStack[] armorContents;
            for (int length = (armorContents = player.getInventory().getArmorContents()).length, j = 0; j < length; ++j) {
                final ItemStack ar = armorContents[j];
                if (ar != null) {
                    items.add(ar);
                }
            }
            for (int i = 0; i < 256; ++i) {
                if (this.lootFile.getString("potionEffects." + i) != null) {
                    if (this.lootFile.getString("potionEffects." + i + ".attackEffect") != null) {
                        boolean effectsPlayer = true;
                        if (this.lootFile.getString("potionEffects." + i + ".attackEffect").equals("target")) {
                            effectsPlayer = false;
                        }
                        for (final int neededItemIndex : this.lootFile.getIntegerList("potionEffects." + i + ".requiredItems")) {
                            final ItemStack neededItem = this.getItem(neededItemIndex);
                            try {
                                if ((neededItem.getItemMeta() != null && neededItem.getItemMeta().getDisplayName() != null && !itemUsed.getItemMeta().getDisplayName().equals(neededItem.getItemMeta().getDisplayName())) || itemUsed.getTypeId() != neededItem.getTypeId() || (neededItem.getType().getMaxDurability() <= 0 && itemUsed.getDurability() != neededItem.getDurability())) {
                                    continue;
                                }
                                if (effectsPlayer) {
                                    this.applyEffects((LivingEntity) player, i);
                                } else {
                                    if (!(mob instanceof LivingEntity)) {
                                        continue;
                                    }
                                    this.applyEffects((LivingEntity) mob, i);
                                }
                            } catch (Exception ex) {
                            }
                        }
                    } else if (this.lootFile.getString("potionEffects." + i + ".attackHelpEffect") != null) {
                        boolean effectsPlayer = true;
                        if (this.lootFile.getString("potionEffects." + i + ".attackHelpEffect").equals("target")) {
                            effectsPlayer = false;
                        }
                        final ArrayList<ItemStack> itemsPlayerHas = new ArrayList<ItemStack>();
                        for (final int neededItemIndex2 : this.lootFile.getIntegerList("potionEffects." + i + ".requiredItems")) {
                            final ItemStack neededItem2 = this.getItem(neededItemIndex2);
                            for (final ItemStack check : items) {
                                try {
                                    if ((neededItem2.getItemMeta() != null && neededItem2.getItemMeta().getDisplayName() != null && !check.getItemMeta().getDisplayName().equals(neededItem2.getItemMeta().getDisplayName())) || check.getTypeId() != neededItem2.getTypeId() || (neededItem2.getType().getMaxDurability() <= 0 && check.getDurability() != neededItem2.getDurability()) || itemsPlayerHas.contains(neededItem2)) {
                                        continue;
                                    }
                                    itemsPlayerHas.add(neededItem2);
                                } catch (Exception ex2) {
                                }
                            }
                        }
                        if (itemsPlayerHas.size() >= this.lootFile.getIntegerList("potionEffects." + i + ".requiredItems").size()) {
                            if (effectsPlayer) {
                                this.applyEffects((LivingEntity) player, i);
                            } else if (mob instanceof LivingEntity) {
                                this.applyEffects((LivingEntity) mob, i);
                            }
                        }
                    }
                }
            }
        }
        try {
            final UUID id = mob.getUniqueId();
            if (this.idSearch(id) == -1) {
                return false;
            }
            final ArrayList<String> abilityList = this.findMobAbilities(id);
            if (!player.isDead() && !mob.isDead()) {
                for (final String ability : abilityList) {
                    this.doMagic((Entity) player, mob, playerIsVictom, ability, id);
                }
                return true;
            }
            return false;
        } catch (Exception ex3) {
            return false;
        }
    }

    public void doMagic(final Entity vic, final Entity atc, final boolean playerIsVictom, final String ability, final UUID id) {
        final int min = 1;
        final int max = 10;
        int randomNum = new Random().nextInt(max - min + 1) + min;
        if (atc instanceof Player) {
            randomNum = 1;
        }
        try {
            if (atc instanceof Player) {
                if (ability.equals("tosser")) {
                    if (!(vic instanceof Player) || (!((Player) vic).isSneaking() && !((Player) vic).getGameMode().equals((Object) GameMode.CREATIVE))) {
                        vic.setVelocity(atc.getLocation().toVector().subtract(vic.getLocation().toVector()));
                    }
                } else if (ability.equals("gravity")) {
                    if (!(vic instanceof Player) || (!((Player) vic).isSneaking() && !((Player) vic).getGameMode().equals((Object) GameMode.CREATIVE))) {
                        final Location feetBlock = vic.getLocation();
                        feetBlock.setY(feetBlock.getY() - 2.0);
                        final Block block = feetBlock.getWorld().getBlockAt(feetBlock);
                        if (!block.getType().equals((Object) Material.AIR)) {
                            int amount = 6;
                            if (this.getConfig().getString("gravityLevitateLength") != null) {
                                amount = this.getConfig().getInt("gravityLevitateLength");
                            }
                            this.levitate(vic, amount);
                        }
                    }
                } else if ((ability.equals("ghastly") || ability.equals("necromancer")) && !vic.isDead() && (!(vic instanceof Player) || (!((Player) vic).isSneaking() && !((Player) vic).getGameMode().equals((Object) GameMode.CREATIVE)))) {
                    Fireball fb = null;
                    if (ability.equals("ghastly")) {
                        fb = (Fireball) ((LivingEntity) atc).launchProjectile((Class) Fireball.class);
                    } else {
                        fb = (Fireball) ((LivingEntity) atc).launchProjectile((Class) WitherSkull.class);
                    }
                    this.moveToward((Entity) fb, vic.getLocation(), 0.6);
                }
            }
            if (ability.equals("ender")) {
                atc.teleport(vic.getLocation());
            } else if (ability.equals("poisonous") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 200, 1), true);
            } else if (ability.equals("morph") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                try {
                    final int mc = new Random().nextInt(25) + 1;
                    if (mc != 20) {
                        return;
                    }
                    final Location l = atc.getLocation().clone();
                    final double h = ((Damageable) atc).getHealth();
                    final ArrayList<String> aList = this.infernalList.get(this.idSearch(id)).abilityList;
                    final double dis = 46.0;
                    for (final Entity e : atc.getNearbyEntities(dis, dis, dis)) {
                        if (e instanceof Player) {
                            GUI.fixBar((Player) e);
                        }
                    }
                    atc.teleport(new Location(atc.getWorld(), l.getX(), 0.0, l.getZ()));
                    atc.remove();
                    this.getLogger().log(Level.INFO, "Entity remove due to Morph");
                    final ArrayList<String> mList = (ArrayList<String>) this.getConfig().getList("enabledmobs");
                    final int index = new Random().nextInt(mList.size());
                    final String mobName = mList.get(index);
                    Entity newEnt = null;
                    EntityType[] arrayOfEntityType;
                    for (int j = (arrayOfEntityType = EntityType.values()).length, i = 0; i < j; ++i) {
                        final EntityType e2 = arrayOfEntityType[i];
                        try {
                            if (e2.getName() != null && e2.getName().equalsIgnoreCase(mobName)) {
                                newEnt = vic.getWorld().spawnEntity(l, e2);
                            }
                        } catch (Exception ex2) {
                        }
                    }
                    if (newEnt == null) {
                        System.out.println("Infernal Mobs can't find mob type: " + mobName + "!");
                        return;
                    }
                    Mob newMob = null;
                    if (aList.contains("1up")) {
                        newMob = new Mob(newEnt, newEnt.getUniqueId(), vic.getWorld(), true, aList, 2, this.getEffect());
                    } else {
                        newMob = new Mob(newEnt, newEnt.getUniqueId(), vic.getWorld(), true, aList, 1, this.getEffect());
                    }
                    if (aList.contains("flying")) {
                        this.makeFly(newEnt);
                    }
                    this.infernalList.set(this.idSearch(id), newMob);
                    this.gui.setName(newEnt);
                    this.giveMobGear(newEnt, true);
                    this.addHealth(newEnt, aList);
                    if (h >= ((Damageable) newEnt).getMaxHealth()) {
                        return;
                    }
                    ((Damageable) newEnt).setHealth(h);
                } catch (Exception ex) {
                    System.out.print("Morph Error: ");
                    ex.printStackTrace();
                }
            }
            if (ability.equals("molten") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                int amount2;
                if (this.getConfig().getString("moltenBurnLength") != null) {
                    amount2 = this.getConfig().getInt("moltenBurnLength");
                } else {
                    amount2 = 5;
                }
                vic.setFireTicks(amount2 * 20);
            } else if (ability.equals("blinding") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1), true);
            } else if (ability.equals("confusing") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 80, 2), true);
            } else if (ability.equals("withering") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 180, 1), true);
            } else if (ability.equals("thief") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                if (vic instanceof Player) {
                    if (((Player) vic).getInventory().getItemInHand() != null && !((Player) vic).getInventory().getItemInHand().getType().equals((Object) Material.AIR) && (randomNum <= 1 || randomNum == 1)) {
                        vic.getWorld().dropItemNaturally(atc.getLocation(), ((Player) vic).getInventory().getItemInHand());
                        final int slot = ((Player) vic).getInventory().getHeldItemSlot();
                        ((Player) vic).getInventory().setItem(slot, (ItemStack) null);
                    }
                } else if (vic instanceof PigZombie || vic instanceof Zombie || vic instanceof Skeleton) {
                    final EntityEquipment eq = ((LivingEntity) vic).getEquipment();
                    if (eq.getItemInHand() != null) {
                        vic.getWorld().dropItemNaturally(atc.getLocation(), eq.getItemInHand());
                        eq.setItemInHand((ItemStack) null);
                    }
                }
            } else if (ability.equals("quicksand") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 180, 1), true);
            } else if (ability.equals("bullwark") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                ((LivingEntity) atc).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 500, 2), true);
            } else if (ability.equals("rust") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                if (((Player) vic).getInventory().getItemInHand() != null) {
                    final ItemStack damItem = ((Player) vic).getInventory().getItemInHand();
                    if ((randomNum <= 3 || randomNum == 1) && damItem.getMaxStackSize() == 1) {
                        final int cDur = damItem.getDurability();
                        damItem.setDurability((short) (cDur + 20));
                    }
                }
            } else if (ability.equals("sapper") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 500, 1), true);
            } else if (!ability.equals("1up") || !this.isLegitVictim(atc, playerIsVictom, ability)) {
                if (ability.equals("ender") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                    final Location targetLocation = vic.getLocation();
                    if (randomNum >= 8) {
                        final Random rand2 = new Random();
                        final int min2 = 1;
                        final int max2 = 4;
                        final int randomNum2 = rand2.nextInt(max2 - min2 + 1) + min2;
                        if (randomNum2 == 1) {
                            targetLocation.setZ(targetLocation.getZ() + 6.0);
                        } else if (randomNum == 2) {
                            targetLocation.setZ(targetLocation.getZ() - 5.0);
                        } else if (randomNum == 3) {
                            targetLocation.setX(targetLocation.getX() + 8.0);
                        } else if (randomNum == 4) {
                            targetLocation.setX(targetLocation.getX() - 10.0);
                        }
                        final Location needAir1 = targetLocation;
                        final Location needAir2 = targetLocation;
                        final Location needAir3 = targetLocation;
                        needAir2.setY(needAir2.getY() + 1.0);
                        needAir3.setY(needAir3.getY() + 2.0);
                        if ((needAir1.getBlock().getType().equals((Object) Material.AIR) || needAir1.getBlock().getType().equals((Object) Material.TORCH)) && (needAir2.getBlock().getType().equals((Object) Material.AIR) || needAir2.getBlock().getType().equals((Object) Material.TORCH)) && (needAir3.getBlock().getType().equals((Object) Material.AIR) || needAir3.getBlock().getType().equals((Object) Material.TORCH))) {
                            atc.teleport(targetLocation);
                        }
                    }
                } else if (ability.equals("lifesteal") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                    ((LivingEntity) atc).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 1), true);
                } else if (!ability.equals("cloaked") || !this.isLegitVictim(atc, playerIsVictom, ability)) {
                    if (ability.equals("storm") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                        if ((randomNum <= 2 || randomNum == 1) && !atc.isDead()) {
                            vic.getWorld().strikeLightning(vic.getLocation());
                        }
                    } else if (!ability.equals("sprint") || !this.isLegitVictim(atc, playerIsVictom, ability)) {
                        if (ability.equals("webber") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            if (randomNum >= 8 || randomNum == 1) {
                                final Location feet = vic.getLocation();
                                feet.getBlock().setType(Material.WEB);
                                this.setAir(feet, 60);
                                final int rNum = new Random().nextInt(max - min + 1) + min;
                                if (rNum == 5 && (atc.getType().equals((Object) EntityType.SPIDER) || atc.getType().equals((Object) EntityType.CAVE_SPIDER))) {
                                    final Location k = atc.getLocation();
                                    final Block b = k.getBlock();
                                    final List<Block> blocks = getSphere(b, 4);
                                    for (final Block bl : blocks) {
                                        if (bl.getType().equals((Object) Material.AIR)) {
                                            bl.setType(Material.WEB);
                                            this.setAir(bl.getLocation(), 30);
                                        }
                                    }
                                }
                            }
                        } else if (ability.equals("vengeance") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            if (randomNum >= 5 || randomNum == 1) {
                                int amount3;
                                if (this.getConfig().getString("vengeanceDamage") != null) {
                                    amount3 = this.getConfig().getInt("vengeanceDamage");
                                } else {
                                    amount3 = 6;
                                }
                                if (vic instanceof LivingEntity) {
                                    ((LivingEntity) vic).damage((double) (int) Math.round(2.0 * amount3));
                                }
                            }
                        } else if (ability.equals("weakness") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            ((LivingEntity) vic).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 500, 1), true);
                        } else if (ability.equals("berserk") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            if (randomNum >= 5 && !atc.isDead()) {
                                final double health = ((Damageable) atc).getHealth();
                                ((Damageable) atc).setHealth(health - 1.0);
                                int amount4;
                                if (this.getConfig().getString("berserkDamage") != null) {
                                    amount4 = this.getConfig().getInt("berserkDamage");
                                } else {
                                    amount4 = 3;
                                }
                                if (vic instanceof LivingEntity) {
                                    ((LivingEntity) vic).damage((double) (int) Math.round(2.0 * amount4));
                                }
                            }
                        } else if (ability.equals("potions") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            Potion potion = null;
                            if (randomNum == 5) {
                                potion = new Potion(PotionType.INSTANT_DAMAGE, 2);
                            } else if (randomNum == 6) {
                                potion = new Potion(PotionType.INSTANT_DAMAGE, 1);
                            } else if (randomNum == 7) {
                                potion = new Potion(PotionType.WEAKNESS, 2);
                            } else if (randomNum == 8) {
                                potion = new Potion(PotionType.POISON, 2);
                            } else if (randomNum == 9) {
                                potion = new Potion(PotionType.SLOWNESS, 2);
                            }
                            if (potion != null) {
                                potion.setSplash(true);
                                final ItemStack iStack = new ItemStack(Material.POTION);
                                potion.apply(iStack);
                                final Location sploc = atc.getLocation();
                                sploc.setY(sploc.getY() + 3.0);
                                final ThrownPotion thrownPotion = (ThrownPotion) vic.getWorld().spawnEntity(sploc, EntityType.SPLASH_POTION);
                                thrownPotion.setItem(iStack);
                                final Vector direction = atc.getLocation().getDirection();
                                direction.normalize();
                                direction.add(new Vector(0.0, 0.2, 0.0));
                                double dist = atc.getLocation().distance(vic.getLocation());
                                dist /= 15.0;
                                direction.multiply(dist);
                                thrownPotion.setVelocity(direction);
                            }
                        } else if (ability.equals("mama") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            if (randomNum == 1) {
                                int amount3;
                                if (this.getConfig().getString("mamaSpawnAmount") != null) {
                                    amount3 = this.getConfig().getInt("mamaSpawnAmount");
                                } else {
                                    amount3 = 3;
                                }
                                if (atc.getType().equals((Object) EntityType.MUSHROOM_COW)) {
                                    for (int m = 0; m < amount3; ++m) {
                                        final MushroomCow minion = (MushroomCow) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.MUSHROOM_COW);
                                        minion.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.COW)) {
                                    for (int m = 0; m < amount3; ++m) {
                                        final Cow minion2 = (Cow) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.COW);
                                        minion2.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.SHEEP)) {
                                    for (int m = 0; m < amount3; ++m) {
                                        final Sheep minion3 = (Sheep) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.SHEEP);
                                        minion3.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.PIG)) {
                                    for (int m = 0; m < amount3; ++m) {
                                        final Pig minion4 = (Pig) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.PIG);
                                        minion4.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.CHICKEN)) {
                                    for (int m = 0; m < amount3; ++m) {
                                        final Chicken minion5 = (Chicken) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.CHICKEN);
                                        minion5.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.WOLF)) {
                                    for (int m = 0; m < amount3; ++m) {
                                        final Wolf minion6 = (Wolf) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.WOLF);
                                        minion6.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.ZOMBIE)) {
                                    for (int m = 0; m < amount3; ++m) {
                                        final Zombie minion7 = (Zombie) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.ZOMBIE);
                                        minion7.setBaby(true);
                                    }
                                } else if (atc.getType().equals((Object) EntityType.PIG_ZOMBIE)) {
                                    for (int m = 0; m < amount3; ++m) {
                                        final PigZombie minion8 = (PigZombie) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.PIG_ZOMBIE);
                                        minion8.setBaby(true);
                                    }
                                } else if (atc.getType().equals((Object) EntityType.OCELOT)) {
                                    for (int m = 0; m < amount3; ++m) {
                                        final Ocelot minion9 = (Ocelot) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.OCELOT);
                                        minion9.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.HORSE)) {
                                    for (int m = 0; m < amount3; ++m) {
                                        final Horse minion10 = (Horse) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.HORSE);
                                        minion10.setBaby();
                                    }
                                } else if (atc.getType().equals((Object) EntityType.VILLAGER)) {
                                    for (int m = 0; m < amount3; ++m) {
                                        final Villager minion11 = (Villager) atc.getWorld().spawnEntity(atc.getLocation(), EntityType.VILLAGER);
                                        minion11.setBaby();
                                    }
                                } else {
                                    for (int m = 0; m < amount3; ++m) {
                                        atc.getWorld().spawnEntity(atc.getLocation(), atc.getType());
                                    }
                                }
                            }
                        } else if (ability.equals("archer") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            if (randomNum > 7 || randomNum == 1) {
                                final ArrayList<Arrow> arrowList = new ArrayList<Arrow>();
                                final Location loc1 = vic.getLocation();
                                final Location loc2 = atc.getLocation();
                                if (!this.isSmall(atc)) {
                                    loc2.setY(loc2.getY() + 1.0);
                                }
                                final Arrow a = (Arrow) ((LivingEntity) atc).launchProjectile((Class) Arrow.class);
                                final int arrowSpeed = 1;
                                loc2.setY((double) (loc2.getBlockY() + 2));
                                loc2.setX(loc2.getBlockX() + 0.5);
                                loc2.setZ(loc2.getBlockZ() + 0.5);
                                final Arrow a2 = a.getWorld().spawnArrow(loc2, new Vector(loc1.getX() - loc2.getX(), loc1.getY() - loc2.getY(), loc1.getZ() - loc2.getZ()), (float) arrowSpeed, 12.0f);
                                a2.setShooter((ProjectileSource) atc);
                                loc2.setY((double) (loc2.getBlockY() + 2));
                                loc2.setX((double) (loc2.getBlockX() - 1));
                                loc2.setZ((double) (loc2.getBlockZ() - 1));
                                final Arrow a3 = a.getWorld().spawnArrow(loc2, new Vector(loc1.getX() - loc2.getX(), loc1.getY() - loc2.getY(), loc1.getZ() - loc2.getZ()), (float) arrowSpeed, 12.0f);
                                a3.setShooter((ProjectileSource) atc);
                                arrowList.add(a);
                                arrowList.add(a2);
                                arrowList.add(a3);
                                for (final Arrow ar : arrowList) {
                                    double minAngle = 6.283185307179586;
                                    Entity minEntity = null;
                                    for (final Entity entity : atc.getNearbyEntities(64.0, 64.0, 64.0)) {
                                        if (((LivingEntity) atc).hasLineOfSight(entity) && entity instanceof LivingEntity && !entity.isDead()) {
                                            final Vector toTarget = entity.getLocation().toVector().clone().subtract(atc.getLocation().toVector());
                                            final double angle = ar.getVelocity().angle(toTarget);
                                            if (angle >= minAngle) {
                                                continue;
                                            }
                                            minAngle = angle;
                                            minEntity = entity;
                                        }
                                    }
                                    if (minEntity != null) {
                                        new ArrowHomingTask(ar, (LivingEntity) minEntity, (Plugin) this);
                                    }
                                }
                            }
                        } else if (ability.equals("firework") && this.isLegitVictim(atc, playerIsVictom, ability)) {
                            final int red = this.getConfig().getInt("fireworkColour.red");
                            final int green = this.getConfig().getInt("fireworkColour.green");
                            final int blue = this.getConfig().getInt("fireworkColour.blue");
                            final ItemStack tmpCol = new ItemStack(Material.LEATHER_HELMET, 1);
                            final LeatherArmorMeta tmpCol2 = (LeatherArmorMeta) tmpCol.getItemMeta();
                            tmpCol2.setColor(Color.fromRGB(red, green, blue));
                            final Color col = tmpCol2.getColor();
                            this.launchFirework(atc.getLocation(), col, 1);
                        }
                    }
                }
            }
        } catch (Exception ex3) {
        }
    }

    public void launchFirework(final Location l, final Color c, final int speed) {
        final Firework fw = (Firework) l.getWorld().spawn(l, (Class) Firework.class);
        final FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(c).with(FireworkEffect.Type.BALL_LARGE).build());
        fw.setFireworkMeta(meta);
        fw.setVelocity(l.getDirection().multiply(speed));
        this.detonate(fw);
    }

    public void detonate(final Firework fw) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                try {
                    fw.detonate();
                } catch (Exception ex) {
                }
            }
        }, 2L);
    }

    public boolean isLegitVictim(final Entity e, final boolean playerIsVictom, final String ability) {
        if (e instanceof Player) {
            return true;
        }
        if (this.getConfig().getBoolean("effectAllPlayerAttacks")) {
            return true;
        }
        final ArrayList<String> attackAbilityList = new ArrayList<String>();
        attackAbilityList.add("poisonous");
        attackAbilityList.add("blinding");
        attackAbilityList.add("withering");
        attackAbilityList.add("thief");
        attackAbilityList.add("sapper");
        attackAbilityList.add("lifesteal");
        attackAbilityList.add("storm");
        attackAbilityList.add("webber");
        attackAbilityList.add("weakness");
        attackAbilityList.add("berserk");
        attackAbilityList.add("potions");
        attackAbilityList.add("archer");
        attackAbilityList.add("confusing");
        if (playerIsVictom && attackAbilityList.contains(ability)) {
            return true;
        }
        final ArrayList<String> defendAbilityList = new ArrayList<String>();
        defendAbilityList.add("thief");
        defendAbilityList.add("storm");
        defendAbilityList.add("webber");
        defendAbilityList.add("weakness");
        defendAbilityList.add("potions");
        defendAbilityList.add("archer");
        defendAbilityList.add("quicksand");
        defendAbilityList.add("bullwark");
        defendAbilityList.add("rust");
        defendAbilityList.add("ender");
        defendAbilityList.add("vengeance");
        defendAbilityList.add("mama");
        defendAbilityList.add("firework");
        defendAbilityList.add("morph");
        return !playerIsVictom && defendAbilityList.contains(ability);
    }

    public void setAir(final Location block, final int time) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) this, (Runnable) new Runnable() {
            @Override
            public void run() {
                if (block.getBlock().getType().equals((Object) Material.WEB)) {
                    block.getBlock().setType(Material.AIR);
                }
            }
        }, (long) (time * 20));
    }

    public ArrayList<String> getAbilitiesAmount(final Entity e) {
        int power;
        if (this.getConfig().getBoolean("powerByDistance")) {
            final Location l = e.getWorld().getSpawnLocation();
            int m = (int) l.distance(e.getLocation()) / this.getConfig().getInt("addDistance");
            if (m < 1) {
                m = 1;
            }
            final int add = this.getConfig().getInt("powerToAdd");
            power = m * add;
        } else {
            final int min = this.getConfig().getInt("minpowers");
            final int max = this.getConfig().getInt("maxpowers");
            power = this.rand(min, max);
        }
        return this.getAbilities(power);
    }

    public ArrayList<String> getAbilities(final int amount) {
        final ArrayList<String> abilityList = new ArrayList<String>();
        final ArrayList<String> allAbilitiesList = new ArrayList<String>(Arrays.<String>asList("confusing", "ghost", "morph", "mounted", "flying", "gravity", "firework", "necromancer", "archer", "molten", "mama", "potions", "explode", "berserk", "weakness", "vengeance", "webber", "storm", "sprint", "lifesteal", "ghastly", "ender", "cloaked", "1up", "sapper", "rust", "bullwark", "quicksand", "thief", "tosser", "withering", "blinding", "armoured", "poisonous"));
        final int min = 1;
        for (int i = 0; i < amount; ++i) {
            final int max = allAbilitiesList.size();
            final int randomNum = new Random().nextInt(max - min + 1) + min - 1;
            final String ab = allAbilitiesList.get(randomNum);
            if (this.getConfig().getString(ab) != null) {
                if (this.getConfig().getString(ab).equals("always") || this.getConfig().getBoolean(ab)) {
                    abilityList.add(ab);
                    allAbilitiesList.remove(randomNum);
                } else {
                    allAbilitiesList.remove(randomNum);
                    --i;
                }
            } else {
                this.getLogger().log(Level.WARNING, "Ability: " + ab + " is not set!");
            }
        }
        return abilityList;
    }

    public int idSearch(final UUID id) {
        Mob idMob = null;
        for (final Mob mob : this.infernalList) {
            if (mob.id.equals(id)) {
                idMob = mob;
            }
        }
        if (idMob != null) {
            return this.infernalList.indexOf(idMob);
        }
        return -1;
    }

    public ArrayList<String> findMobAbilities(final UUID id) {
        for (final Mob mob : this.infernalList) {
            if (mob.id.equals(id)) {
                final ArrayList<String> abilityList = mob.abilityList;
                return abilityList;
            }
        }
        return null;
    }

    public Entity getTarget(final Player player) {
        final BlockIterator iterator = new BlockIterator(player.getWorld(), player.getLocation().toVector(), player.getEyeLocation().getDirection(), 0.0, 100);
        Entity target = null;
        while (iterator.hasNext()) {
            final Block item = iterator.next();
            for (final Entity entity : player.getNearbyEntities(100.0, 100.0, 100.0)) {
                for (int acc = 2, x = -acc; x < acc; ++x) {
                    for (int z = -acc; z < acc; ++z) {
                        for (int y = -acc; y < acc; ++y) {
                            if (entity.getLocation().getBlock().getRelative(x, y, z).equals(item)) {
                                return target = entity;
                            }
                        }
                    }
                }
            }
        }
        return target;
    }

    public void makeFly(final Entity ent) {
        final Entity bat = ent.getWorld().spawnEntity(ent.getLocation(), EntityType.BAT);
        bat.setVelocity(new Vector(0, 1, 0));
        bat.setPassenger(ent);
        ((LivingEntity) bat).addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 1), true);
    }

    public void giveMobGear(final Entity mob, final boolean naturalSpawn) {
        final UUID mobId = mob.getUniqueId();
        ArrayList<String> mobAbilityList = null;
        boolean armoured = false;
        if (this.idSearch(mobId) != -1) {
            mobAbilityList = this.findMobAbilities(mobId);
            if (mobAbilityList.contains("armoured")) {
                armoured = true;
                ((LivingEntity) mob).setCanPickupItems(false);
            }
        }
        final ItemStack helm = new ItemStack(Material.DIAMOND_HELMET, 1);
        final ItemStack chest = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
        final ItemStack pants = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
        final ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS, 1);
        final ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
        sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4);
        final EntityEquipment ee = ((LivingEntity) mob).getEquipment();
        if (mob instanceof Skeleton) {
            final Skeleton sk = (Skeleton) mob;
            if (sk.getSkeletonType().equals((Object) Skeleton.SkeletonType.WITHER)) {
                if (armoured) {
                    ee.setHelmetDropChance(0.0f);
                    ee.setChestplateDropChance(0.0f);
                    ee.setLeggingsDropChance(0.0f);
                    ee.setBootsDropChance(0.0f);
                    ee.setItemInHandDropChance(0.0f);
                    ee.setHelmet(helm);
                    ee.setChestplate(chest);
                    ee.setLeggings(pants);
                    ee.setBoots(boots);
                    ee.setItemInHand(sword);
                }
            } else {
                final ItemStack bow = new ItemStack(Material.BOW, 1);
                ee.setItemInHand(bow);
                if (armoured) {
                    ee.setHelmetDropChance(0.0f);
                    ee.setChestplateDropChance(0.0f);
                    ee.setHelmet(helm);
                    ee.setChestplate(chest);
                    if (!mobAbilityList.contains("cloaked")) {
                        ee.setLeggingsDropChance(0.0f);
                        ee.setBootsDropChance(0.0f);
                        ee.setLeggings(pants);
                        ee.setBoots(boots);
                    }
                    ee.setItemInHandDropChance(0.0f);
                    ee.setItemInHand(sword);
                } else if (mobAbilityList.contains("cloaked")) {
                    final ItemStack skull = new ItemStack(Material.GLASS_BOTTLE, 1);
                    ee.setHelmet(skull);
                }
            }
        } else if (mob instanceof Zombie || mob instanceof PigZombie) {
            if (armoured) {
                ee.setHelmetDropChance(0.0f);
                ee.setChestplateDropChance(0.0f);
                ee.setHelmet(helm);
                ee.setChestplate(chest);
                if (!mobAbilityList.contains("cloaked")) {
                    ee.setLeggings(pants);
                    ee.setBoots(boots);
                }
                ee.setLeggingsDropChance(0.0f);
                ee.setBootsDropChance(0.0f);
                ee.setItemInHandDropChance(0.0f);
                ee.setItemInHand(sword);
            } else if (mob instanceof Zombie && mobAbilityList.contains("cloaked")) {
                final ItemStack skull2 = new ItemStack(Material.GLASS_BOTTLE, 1, (short) 2);
                ee.setHelmet(skull2);
            }
        }
        if ((mobAbilityList.contains("mounted") && this.getConfig().getList("enabledRiders").contains(mob.getType().getName())) || (!naturalSpawn && mobAbilityList.contains("mounted"))) {
            ArrayList<String> mounts = new ArrayList<String>();
            mounts = (ArrayList<String>) this.getConfig().getList("enabledMounts");
            final Random randomGenerator = new Random();
            final int index = randomGenerator.nextInt(mounts.size());
            String mount = mounts.get(index);
            String type = null;
            if (mount.contains(":")) {
                final String[] s = mount.split(":");
                mount = s[0];
                type = s[1];
            }
            if (EntityType.fromName(mount) != null) {
                final Entity liveMount = mob.getWorld().spawnEntity(mob.getLocation(), EntityType.fromName(mount));
                this.mountList.put(liveMount, mob);
                liveMount.setPassenger(mob);
                if (liveMount.getType().equals((Object) EntityType.HORSE)) {
                    final Horse hm = (Horse) liveMount;
                    if (type != null) {
                        hm.setVariant(Horse.Variant.valueOf(type));
                    } else {
                        final int randomNum2 = this.rand(1, 7);
                        if (randomNum2 <= 3) {
                            hm.setVariant(Horse.Variant.HORSE);
                        } else if (randomNum2 == 4) {
                            hm.setVariant(Horse.Variant.DONKEY);
                        } else if (randomNum2 == 5) {
                            hm.setVariant(Horse.Variant.MULE);
                        } else if (randomNum2 == 6) {
                            hm.setVariant(Horse.Variant.SKELETON_HORSE);
                        } else {
                            hm.setVariant(Horse.Variant.UNDEAD_HORSE);
                        }
                    }
                    if (this.getConfig().getBoolean("horseMountsHaveSaddles")) {
                        final ItemStack saddle = new ItemStack(Material.SADDLE);
                        hm.getInventory().setSaddle(saddle);
                    }
                    hm.setTamed(true);
                    if (hm.getVariant().equals((Object) Horse.Variant.HORSE)) {
                        final int randomNum3 = this.rand(1, 7);
                        if (randomNum3 == 1) {
                            hm.setColor(Horse.Color.BLACK);
                        } else if (randomNum3 == 2) {
                            hm.setColor(Horse.Color.BROWN);
                        } else if (randomNum3 == 3) {
                            hm.setColor(Horse.Color.CHESTNUT);
                        } else if (randomNum3 == 4) {
                            hm.setColor(Horse.Color.CREAMY);
                        } else if (randomNum3 == 5) {
                            hm.setColor(Horse.Color.DARK_BROWN);
                        } else if (randomNum3 == 6) {
                            hm.setColor(Horse.Color.GRAY);
                        } else {
                            hm.setColor(Horse.Color.WHITE);
                        }
                        if (armoured && this.getConfig().getBoolean("armouredMountsHaveArmour")) {
                            final ItemStack armour = new ItemStack(419);
                            hm.getInventory().setArmor(armour);
                        }
                    }
                } else if (liveMount.getType().equals((Object) EntityType.SHEEP)) {
                    final Sheep sh = (Sheep) liveMount;
                    if (type != null) {
                        sh.setColor(DyeColor.valueOf(type));
                    }
                }
            } else {
                System.out.println("Can't spawn mount!");
                System.out.println(String.valueOf(mount) + " is not a valid Entity!");
            }
        }
    }

    public void changeIntoWither(final Skeleton skeleton) {
        try {
            skeleton.setSkeletonType(Skeleton.SkeletonType.WITHER);
            final EntityEquipment ee = skeleton.getEquipment();
            if (ee.getItemInHand().equals((Object) null)) {
                final ItemStack sword = new ItemStack(Material.STONE_SWORD, 1);
                ee.setItemInHand(sword);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /* remove version stuff
    boolean is9() {
        return Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11");
    }
    
    boolean is10() {
        return Bukkit.getVersion().contains("1.10") || Bukkit.getVersion().contains("1.11");
    }
    
    boolean is11() {
        return Bukkit.getVersion().contains("1.11");
    }
    */
    public void changeIntoElder(final Guardian g) {
        g.setElder(true);
        //final EntityGuardian ent = (EntityGuardian)((CraftGuardian)g).getHandle();
        //((Guardian)ent).setElder(true);
    }

    public void displayParticle(final String effect, final Location l, final double radius, final int speed, final int amount) {
        this.displayParticle(effect, l.getWorld(), l.getX(), l.getY(), l.getZ(), radius, speed, amount);
    }

    private void displayParticle(final String effect, final World w, final double x, final double y, final double z, final double radius, final int speed, final int amount) {
        final Location l = new Location(w, x, y, z);
        try {
            if (radius == 0.0) {
                ParticleEffects.sendToLocation(ParticleEffects.valueOf(effect), l, 0.0f, 0.0f, 0.0f, speed, amount);
            } else {
                final ArrayList<Location> ll = this.getArea(l, radius, 0.2);
                for (int i = 0; i < amount; ++i) {
                    final int index = new Random().nextInt(ll.size());
                    ParticleEffects.sendToLocation(ParticleEffects.valueOf(effect), ll.get(index), 0.0f, 0.0f, 0.0f, speed, 1);
                    ll.remove(index);
                }
            }

        } catch (Exception ex) {
            System.out.println("V: " + this.getServer().getVersion());
            ex.printStackTrace();
        }
    }

    private ArrayList<Location> getArea(final Location l, final double r, final double t) {
        final ArrayList<Location> ll = new ArrayList<Location>();
        for (double x = l.getX() - r; x < l.getX() + r; x += t) {
            for (double y = l.getY() - r; y < l.getY() + r; y += t) {
                for (double z = l.getZ() - r; z < l.getZ() + r; z += t) {
                    ll.add(new Location(l.getWorld(), x, y, z));
                }
            }
        }
        return ll;
    }

    public String getRandomMob() {
        final ArrayList<String> mobList = (ArrayList<String>) this.getConfig().getList("enabledmobs");
        if (mobList.isEmpty()) {
            return "Zombie";
        }
        final String mob = mobList.get(this.rand(1, mobList.size()) - 1);
        if (mob != null) {
            return mob;
        }
        return "Zombie";
    }

    public String generateString(int maxNames, final ArrayList<String> names) {
        String namesString = "";
        if (maxNames > names.size()) {
            maxNames = names.size();
        }
        for (int i = 0; i < maxNames; ++i) {
            namesString = String.valueOf(namesString) + names.get(i) + " ";
        }
        if (names.size() > maxNames) {
            namesString = String.valueOf(namesString) + "... ";
        }
        return namesString;
    }

    public void reloadLoot() {
        if (this.lootYML == null) {
            this.lootYML = new File(this.getDataFolder(), "loot.yml");
        }
        this.lootFile = YamlConfiguration.loadConfiguration(this.lootYML);
        final InputStream defConfigStream = this.getResource("loot.yml");
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            this.lootFile.setDefaults((Configuration) defConfig);
        }
    }

    public String getLocationName(final Location l) {
        return (String.valueOf(l.getX()) + "." + l.getY() + "." + l.getZ() + l.getWorld().getName()).replace(".", "");
    }

    public Block blockNear(final Location l, final Material mat, final int radius) {
        final double xTmp = l.getX();
        final double yTmp = l.getY();
        final double zTmp = l.getZ();
        final int finalX = (int) Math.round(xTmp);
        final int finalY = (int) Math.round(yTmp);
        final int finalZ = (int) Math.round(zTmp);
        for (int x = finalX - radius; x <= finalX + radius; ++x) {
            for (int y = finalY - radius; y <= finalY + radius; ++y) {
                for (int z = finalZ - radius; z <= finalZ + radius; ++z) {
                    final Location loc = new Location(l.getWorld(), (double) x, (double) y, (double) z);
                    final Block block = loc.getBlock();
                    if (block.getType().equals((Object) mat)) {
                        return block;
                    }
                }
            }
        }
        return null;
    }

    private boolean cSpawn(final CommandSender sender, final String mob, final Location l, final ArrayList<String> abList) {
        if (EntityType.fromName(mob) != null) {
            final Entity ent = l.getWorld().spawnEntity(l, EntityType.fromName(mob));
            Mob newMob = null;
            final UUID id = ent.getUniqueId();
            if (abList.contains("1up")) {
                newMob = new Mob(ent, id, l.getWorld(), true, abList, 2, this.getEffect());
            } else {
                newMob = new Mob(ent, id, l.getWorld(), true, abList, 1, this.getEffect());
            }
            if (abList.contains("flying")) {
                this.makeFly(ent);
            }
            this.infernalList.add(newMob);
            this.gui.setName(ent);
            this.giveMobGear(ent, false);
            this.addHealth(ent, abList);
            return true;
        }
        sender.sendMessage("Can't spawn a " + mob + "!");
        return false;
    }

    private int rand(final int min, final int max) {
        final int r = min + (int) (Math.random() * (1 + max - min));
        return r;
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (!cmd.getName().equals("infernalmobs")) {
            if (!cmd.getName().equals("im")) {
                return true;
            }
        }
        try {
            Player player = null;
            boolean plyr = true;
            if (!(sender instanceof Player)) {
                if (!args[0].equals("cspawn") && !args[0].equals("reload") && !args[0].equals("killall")) {
                    sender.sendMessage("This command can only be run by a player!");
                    return true;
                }
                plyr = false;
            } else {
                player = (Player) sender;
            }
            if (!plyr || player.hasPermission("infernal_mobs.commands")) {
                if (args.length == 0) {
                    this.throwError(sender);
                    return true;
                }
                if (args.length == 1 && args[0].equals("reload")) {
                    this.reloadConfig();
                    this.reloadLoot();
                    sender.sendMessage("§eConfig reloaded!");
                } else {
                    if (args[0].equals("mobList")) {
                        sender.sendMessage("§6Mob List:");
                        EntityType[] values;
                        for (int length = (values = EntityType.values()).length, n = 0; n < length; ++n) {
                            final EntityType et = values[n];
                            if (et != null && et.getName() != null) {
                                sender.sendMessage("§e" + et.getName());
                            }
                        }
                        return true;
                    }
                    if (args.length == 1 && args[0].equals("error")) {
                        this.errorList.add(player);
                        sender.sendMessage("§eClick on a mob to send an error report about it.");
                    } else if (args.length == 1 && args[0].equals("info")) {
                        sender.sendMessage("§eMounts: " + this.mountList.size());
                        sender.sendMessage("§eLoops: " + this.loops);
                        sender.sendMessage("§eInfernals: " + this.infernalList.size());
                    } else if (args.length == 1 && args[0].equals("worldInfo")) {
                        final ArrayList<String> enWorldList = (ArrayList<String>) this.getConfig().getList("enabledworlds");
                        final World world = player.getWorld();
                        String enabled = "is not";
                        if (enWorldList.contains(world.getName()) || enWorldList.contains("<all>")) {
                            enabled = "is";
                        }
                        sender.sendMessage("The world you are currently in, " + world + " " + enabled + " enabled.");
                        sender.sendMessage("All the world that are enabled are: " + enWorldList.toString());
                    } else if (args.length == 1 && args[0].equals("help")) {
                        this.throwError(sender);
                    } else if (args.length == 1 && args[0].equals("getloot")) {
                        final int min = this.getConfig().getInt("minpowers");
                        final int max = this.getConfig().getInt("maxpowers");
                        final int powers = this.rand(min, max);
                        final ItemStack gottenLoot = this.getRandomLoot(player, this.getRandomMob(), powers);
                        if (gottenLoot != null) {
                            player.getInventory().addItem(new ItemStack[]{gottenLoot});
                        }
                        sender.sendMessage("§eGave you some random loot!");
                    } else if (args.length == 2 && args[0].equals("getloot")) {
                        try {
                            final int index = Integer.parseInt(args[1]);
                            final ItemStack i = this.getLoot(player, index);
                            if (i != null) {
                                player.getInventory().addItem(new ItemStack[]{i});
                                sender.sendMessage("§eGave you the loot at index §9" + index);
                                return true;
                            }
                        } catch (Exception ex) {
                        }
                        sender.sendMessage("§cUnable to get that loot!");
                    } else if ((args.length == 2 && args[0].equals("spawn")) || (args[0].equals("cspawn") && args.length == 6)) {
                        if (EntityType.fromName(args[1]) == null) {
                            sender.sendMessage("Can't spawn a " + args[1] + "!");
                            return true;
                        }
                        World world;
                        Entity ent;
                        boolean exmsg;
                        if (args[0].equals("cspawn") && args[2] != null && args[3] != null && args[4] != null && args[5] != null) {
                            if (Bukkit.getServer().getWorld(args[2]) == null) {
                                sender.sendMessage(String.valueOf(args[2]) + " dose not exist!");
                                return true;
                            }
                            world = Bukkit.getServer().getWorld(args[2]);
                            final Location spoint = new Location(Bukkit.getServer().getWorld(args[2]), (double) Integer.parseInt(args[3]), (double) Integer.parseInt(args[4]), (double) Integer.parseInt(args[5]));
                            ent = world.spawnEntity(spoint, EntityType.fromName(args[1]));
                            exmsg = true;
                        } else {
                            world = player.getWorld();
                            final Location farSpawnLoc = player.getTargetBlock((Set<Material>) null, 200).getLocation();
                            farSpawnLoc.setY(farSpawnLoc.getY() + 1.0);
                            ent = player.getWorld().spawnEntity(farSpawnLoc, EntityType.fromName(args[1]));
                            exmsg = false;
                        }
                        final ArrayList<String> abList = this.getAbilitiesAmount(ent);
                        Mob newMob = null;
                        final UUID id = ent.getUniqueId();
                        if (abList.contains("1up")) {
                            newMob = new Mob(ent, id, world, true, abList, 2, this.getEffect());
                        } else {
                            newMob = new Mob(ent, id, world, true, abList, 1, this.getEffect());
                        }
                        if (abList.contains("flying")) {
                            this.makeFly(ent);
                        }
                        this.infernalList.add(newMob);
                        this.gui.setName(ent);
                        this.giveMobGear(ent, false);
                        this.addHealth(ent, abList);
                        if (!exmsg) {
                            sender.sendMessage("Spawned a " + args[1]);
                        } else if (exmsg && sender instanceof Player) {
                            sender.sendMessage("Spawned a " + args[1] + " in " + args[2] + " at " + args[3] + ", " + args[4] + ", " + args[5]);
                        }
                    } else if ((args.length >= 3 && args[0].equals("spawn")) || (args[0].equals("cspawn") && args.length >= 6) || (args[0].equals("pspawn") && args.length >= 3)) {
                        if (args[0].equals("spawn")) {
                            final World world2 = player.getWorld();
                            if (EntityType.fromName(args[1]) != null) {
                                final Location farSpawnLoc2 = player.getTargetBlock((Set<Material>) null, 200).getLocation();
                                farSpawnLoc2.setY(farSpawnLoc2.getY() + 1.0);
                                final Entity ent = player.getWorld().spawnEntity(farSpawnLoc2, EntityType.fromName(args[1]));
                                final ArrayList<String> spesificAbList = new ArrayList<String>();
                                for (int j = 0; j <= args.length - 3; ++j) {
                                    if (this.getConfig().getString(args[j + 2]) == null) {
                                        sender.sendMessage(String.valueOf(args[j + 2]) + " is not a valid ability!");
                                        return true;
                                    }
                                    spesificAbList.add(args[j + 2]);
                                }
                                Mob newMob = null;
                                final UUID id = ent.getUniqueId();
                                if (spesificAbList.contains("1up")) {
                                    newMob = new Mob(ent, id, world2, true, spesificAbList, 2, this.getEffect());
                                } else {
                                    newMob = new Mob(ent, id, world2, true, spesificAbList, 1, this.getEffect());
                                }
                                if (spesificAbList.contains("flying")) {
                                    this.makeFly(ent);
                                }
                                this.infernalList.add(newMob);
                                this.gui.setName(ent);
                                this.giveMobGear(ent, false);
                                this.addHealth(ent, spesificAbList);
                                sender.sendMessage("Spawned a " + args[1] + " with the abilities:");
                                sender.sendMessage(spesificAbList.toString());
                            } else {
                                sender.sendMessage("Can't spawn a " + args[1] + "!");
                            }
                        } else if (args[0].equals("cspawn")) {
                            if (Bukkit.getServer().getWorld(args[2]) == null) {
                                sender.sendMessage(String.valueOf(args[2]) + " dose not exist!");
                                return true;
                            }
                            final World world2 = Bukkit.getServer().getWorld(args[2]);
                            final Location spoint2 = new Location(world2, (double) Integer.parseInt(args[3]), (double) Integer.parseInt(args[4]), (double) Integer.parseInt(args[5]));
                            final ArrayList<String> abList2 = new ArrayList<String>();
                            for (int k = 0; k <= args.length - 7; ++k) {
                                abList2.add(args[k + 6]);
                            }
                            if (this.cSpawn(sender, args[1], spoint2, abList2)) {
                                sender.sendMessage("Spawned a " + args[1] + " in " + args[2] + " at " + args[3] + ", " + args[4] + ", " + args[5] + " with the abilities:");
                                sender.sendMessage(abList2.toString());
                            }
                        } else if (args[0].equals("pspawn")) {
                            final Player p = this.getServer().getPlayer(args[2]);
                            if (p == null) {
                                sender.sendMessage(String.valueOf(args[2]) + " is not online!");
                                return true;
                            }
                            final ArrayList<String> abList3 = new ArrayList<String>();
                            for (int l = 3; l < args.length; ++l) {
                                abList3.add(args[l]);
                            }
                            if (this.cSpawn(sender, args[1], p.getLocation(), abList3)) {
                                sender.sendMessage("Spawned a " + args[1] + " at " + p.getName() + " with the abilities:");
                                sender.sendMessage(abList3.toString());
                            }
                        } else {
                            sender.sendMessage("Can't spawn a " + args[1] + "!");
                        }
                    } else if (args.length == 1 && args[0].equals("abilities")) {
                        sender.sendMessage("--Infernal Mobs Abilities--");
                        sender.sendMessage("mama, molten, weakness, vengeance, webber, storm, sprint, lifesteal, ghastly, ender, cloaked, berserk, 1up, sapper, rust, bullwark, quicksand, thief, tosser, withering, blinding, armoured, poisonous, potions, explode, gravity, archer, necromancer, firework, flying, mounted, morph, ghost, confusing");
                    } else if (args.length == 1 && args[0].equals("showAbilities")) {
                        if (this.getTarget(player) != null) {
                            final Entity targeted = this.getTarget(player);
                            final UUID mobId = targeted.getUniqueId();
                            if (this.idSearch(mobId) != -1) {
                                final ArrayList<String> oldMobAbilityList = this.findMobAbilities(mobId);
                                if (!targeted.isDead()) {
                                    sender.sendMessage("--Targeted Mob's Abilities--");
                                    sender.sendMessage(oldMobAbilityList.toString());
                                }
                            } else {
                                sender.sendMessage("§cThis " + targeted.getType().getName() + " §cis not an infernal mob!");
                            }
                        } else {
                            sender.sendMessage("§cUnable to find mob!");
                        }
                    } else if (args[0].equals("setInfernal") && args.length == 2) {
                        if (player.getTargetBlock((Set<Material>) null, 25).getType().equals((Object) Material.MOB_SPAWNER)) {
                            final int delay = Integer.parseInt(args[1]);
                            final String name = this.getLocationName(player.getTargetBlock((Set<Material>) null, 25).getLocation());
                            this.mobSaveFile.set("infernalSpanwers." + name, (Object) delay);
                            this.mobSaveFile.save(this.saveYML);
                            sender.sendMessage("§cSpawner set to infernal with a " + delay + " second delay!");
                        } else {
                            sender.sendMessage("§cYou must be looking a spawner to make it infernal!");
                        }
                    } else if (args[0].equals("kill") && args.length == 2) {
                        final int size = Integer.parseInt(args[1]);
                        for (final Entity e : player.getNearbyEntities((double) size, (double) size, (double) size)) {
                            final int id2 = this.idSearch(e.getUniqueId());
                            if (id2 != -1) {
                                this.removeMob(id2);
                                e.remove();
                                this.getLogger().log(Level.INFO, "Entity remove due to /kill");
                            }
                        }
                        sender.sendMessage("§eKilled all infernal mobs near you!");
                    } else if (args[0].equals("killall") && args.length == 2) {
                        final World w = this.getServer().getWorld(args[1]);
                        if (w != null) {
                            for (final Entity e : w.getEntities()) {
                                final int id2 = this.idSearch(e.getUniqueId());
                                if (id2 != -1) {
                                    this.removeMob(id2);
                                    this.getLogger().log(Level.INFO, "Entity remove due to /killall");
                                    e.remove();
                                }
                            }
                            sender.sendMessage("§eKilled all loaded infernal mobs in that world!");
                        } else {
                            sender.sendMessage("§cWorld not found!");
                        }
                    } else {
                        this.throwError(sender);
                    }
                }
            } else {
                sender.sendMessage("§cYou don't have permission to use this command!");
            }
        } catch (Exception x) {
            this.throwError(sender);
            x.printStackTrace();
        }
        return true;
    }

    public void throwError(final CommandSender sender) {
        sender.sendMessage("--Infernal Mobs v" + Bukkit.getServer().getPluginManager().getPlugin("InfernalMobs").getDescription().getVersion() + "--");
        sender.sendMessage("Usage: /im reload");
        sender.sendMessage("Usage: /im worldInfo");
        sender.sendMessage("Usage: /im error");
        sender.sendMessage("Usage: /im getloot <index>");
        sender.sendMessage("Usage: /im abilities");
        sender.sendMessage("Usage: /im showAbilities");
        sender.sendMessage("Usage: /im setInfernal <time delay>");
        sender.sendMessage("Usage: /im spawn <mob> <ability> <ability>");
        sender.sendMessage("Usage: /im cspawn <mob> <world> <x> <y> <z> <ability> <ability>");
        sender.sendMessage("Usage: /im pspawn <mob> <player> <ability> <ability>");
        sender.sendMessage("Usage: /im kill <size>");
        sender.sendMessage("Usage: /im killall <world>");
    }
}
