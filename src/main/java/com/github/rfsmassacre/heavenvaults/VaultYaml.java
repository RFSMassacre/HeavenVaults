package com.github.rfsmassacre.heavenvaults;

import com.github.rfsmassacre.heavenlibrary.paper.managers.PaperYamlStorage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VaultYaml extends PaperYamlStorage<Vault>
{
    public VaultYaml()
    {
        super(HeavenVaults.getInstance(), "vaults");
    }

    @Override
    public Vault load(YamlConfiguration yaml)
    {
        UUID playerId = UUID.fromString(yaml.getString("player-id"));
        String name = yaml.getString("player-name");
        List<ItemStack> items = new ArrayList<>();
        ConfigurationSection section = yaml.getConfigurationSection("items");
        if (section != null)
        {
            for (String key : section.getKeys(false))
            {
                try
                {
                    int slot = Integer.parseInt(key);
                    ItemStack item = yaml.getItemStack("items." + slot);
                    items.add(item);
                }
                catch (NumberFormatException exception)
                {
                    //Do nothing
                }
            }
        }

        return new Vault(playerId, name, items);
    }

    @Override
    public YamlConfiguration save(Vault vault)
    {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("player-id", vault.getPlayerId().toString());
        yaml.set("player-name", vault.getName());
        List<ItemStack> items = vault.getItems();
        for (int index = 0; index < items.size(); index++)
        {
            ItemStack item = items.get(index);
            if (item == null || item.getType().equals(Material.AIR))
            {
                continue;
            }

            yaml.set("items." + index, item);
        }

        return yaml;
    }
}
