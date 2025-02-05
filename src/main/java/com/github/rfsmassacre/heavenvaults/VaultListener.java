package com.github.rfsmassacre.heavenvaults;

import com.github.rfsmassacre.heavenlibrary.interfaces.LocaleData;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import com.github.rfsmassacre.heavenlibrary.paper.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class VaultListener implements Listener
{
    private final PaperLocale locale;

    public VaultListener()
    {
        this.locale = HeavenVaults.getInstance().getLocale();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        Vault.getVault(player.getUniqueId(), (vault) ->
        {
            if (vault == null)
            {
                vault = new Vault(player);
            }

            Vault.addVault(vault);
            Vault.saveVault(vault);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();
        Vault.getVault(player.getUniqueId(), (vault) ->
        {
            if (vault == null)
            {
                return;
            }

            Vault.saveVault(vault);
            Vault.removeVault(player.getUniqueId());
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onVaultAdd(InventoryClickEvent event)
    {
        Player player = (Player) event.getWhoClicked();
        if (!(Menu.getView(player.getUniqueId()) instanceof VaultMenu menu))
        {
            return;
        }

        Inventory inventory = event.getView().getBottomInventory();
        Inventory clickedInventory = event.getClickedInventory();
        if (!inventory.equals(clickedInventory))
        {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().equals(Material.AIR))
        {
            return;
        }

        Vault vault = menu.getVault();
        if (vault.getItems().size() >= vault.getRows() * 9)
        {
            return;
        }

        if (!Vault.isValid(item))
        {
            locale.sendLocale(player, "blacklisted-item", "{items}",
                    String.join(", ", Vault.getInvalid(item)
                            .stream()
                            .map((invalidItem) -> LocaleData.capitalize(invalidItem.getType().name()))
                            .toList()));
            return;
        }

        vault.getItems().add(item);
        event.setCurrentItem(null);
        menu.updateIcons(player);
        menu.updateInventory(player);
        Vault.saveVault(vault);
    }
}
