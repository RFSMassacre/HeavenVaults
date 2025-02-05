package com.github.rfsmassacre.heavenvaults;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.*;
import java.util.function.Consumer;

@Getter
@Setter
public class Vault
{
    private static final Map<UUID, Vault> VAULTS = new HashMap<>();

    public static void addVault(Vault vault)
    {
        VAULTS.put(vault.playerId, vault);
    }

    public static void removeVault(UUID playerId)
    {
        VAULTS.remove(playerId);
    }

    public static void getVault(UUID playerId, Consumer<Vault> callback)
    {
        Vault vault = VAULTS.get(playerId);
        if (vault != null)
        {
            callback.accept(vault);
            return;
        }

        VaultYaml yaml = HeavenVaults.getInstance().getVaultYaml();
        yaml.readAsync(playerId.toString(), callback);
    }

    public static void getVault(String playerName, Consumer<Vault> callback)
    {
        for (Vault vault : VAULTS.values())
        {
            if (vault.getName().equals(playerName))
            {
                callback.accept(vault);
                return;
            }
        }

        VaultYaml yaml = HeavenVaults.getInstance().getVaultYaml();
        yaml.allAsync((vaults) ->
        {
            for (Vault vault : vaults)
            {
                if (vault.getName().equals(playerName))
                {
                    callback.accept(vault);
                    return;
                }
            }

            callback.accept(null);
        });
    }

    public static void saveVault(Vault vault)
    {
        VaultYaml yaml = HeavenVaults.getInstance().getVaultYaml();
        yaml.writeAsync(vault.playerId.toString(), vault);
    }

    public static void getAllVaults(Consumer<Set<Vault>> callback)
    {
        Set<Vault> vaults = new HashSet<>(VAULTS.values());
        VaultYaml yaml = HeavenVaults.getInstance().getVaultYaml();
        yaml.allAsync((offlineVaults) ->
        {
            for (Vault vault : offlineVaults)
            {
                if (!VAULTS.containsKey(vault.playerId))
                {
                    vaults.add(vault);
                }
            }

            callback.accept(vaults);
        });
    }

    public static boolean isValid(ItemStack item)
    {
        List<String> blackList = HeavenVaults.getInstance().getConfiguration().getStringList("blacklist");
        for (String value : blackList)
        {
            if (item.getItemMeta() instanceof BlockStateMeta meta && meta.getBlockState() instanceof ShulkerBox shulker)
            {
                for (ItemStack shulkerItem : shulker.getInventory().getContents())
                {
                    if (shulkerItem == null || shulkerItem.getType().equals(Material.AIR))
                    {
                        continue;
                    }

                    if (!isValid(shulkerItem))
                    {
                        return false;
                    }
                }
            }
            else if (item.getItemMeta() instanceof BundleMeta meta)
            {
                for (ItemStack bundleItem : meta.getItems())
                {
                    if (bundleItem == null || bundleItem.getType().equals(Material.AIR))
                    {
                        continue;
                    }

                    if (!isValid(bundleItem))
                    {
                        return false;
                    }
                }
            }
            else if (item.getType().name().contains(value.toUpperCase()))
            {
                return false;
            }
        }

        return true;
    }

    public static List<ItemStack> getInvalid(ItemStack item)
    {
        List<ItemStack> invalidItems = new ArrayList<>();
        for (String value : HeavenVaults.getInstance().getConfiguration().getStringList("blacklist"))
        {
            if (item.getType().name().contains(value.toUpperCase()))
            {
                invalidItems.add(item);
                break;
            }
            else if (item.getItemMeta() instanceof BlockStateMeta meta && meta.getBlockState() instanceof
                    ShulkerBox shulker)
            {
                for (ItemStack shulkerItem : shulker.getInventory().getContents())
                {
                    if (shulkerItem == null || shulkerItem.getType().equals(Material.AIR))
                    {
                        continue;
                    }

                    List<ItemStack> invalidShulkerItems = getInvalid(shulkerItem);
                    if (!invalidShulkerItems.isEmpty())
                    {
                        invalidItems.addAll(invalidShulkerItems);
                        break;
                    }
                }
            }
            else if (item.getItemMeta() instanceof BundleMeta meta)
            {
                for (ItemStack bundleItem : meta.getItems())
                {
                    if (bundleItem == null || bundleItem.getType().equals(Material.AIR))
                    {
                        continue;
                    }

                    List<ItemStack> invalidBundleItems = getInvalid(bundleItem);
                    if (!invalidBundleItems.isEmpty())
                    {
                        invalidItems.addAll(invalidBundleItems);
                        break;
                    }
                }
            }
        }

        return invalidItems;
    }

    private final UUID playerId;
    private final List<ItemStack> items;
    private String name;

    public Vault(OfflinePlayer player)
    {
        this(player.getUniqueId(), player.getName(), new ArrayList<>());
    }

    public Vault(UUID playerId, String name, List<ItemStack> items)
    {
        this.playerId = playerId;
        this.items = items;
        this.name = name;
    }

    public int getRows()
    {
        return HeavenVaults.getInstance().getConfiguration().getInt("rows");
    }

    public Set<String> validate()
    {
        Set<String> removedItems = new HashSet<>();
        for (ItemStack item : new ArrayList<>(items))
        {
            removedItems.addAll(getInvalid(item).stream()
                    .filter(Objects::nonNull)
                    .map((invalidItem) -> item.getType().name())
                    .toList());
        }

        return removedItems;
    }
}
