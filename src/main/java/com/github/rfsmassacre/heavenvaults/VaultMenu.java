package com.github.rfsmassacre.heavenvaults;

import com.github.rfsmassacre.heavenlibrary.paper.menu.Icon;
import com.github.rfsmassacre.heavenlibrary.paper.menu.Menu;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class VaultMenu extends Menu
{
    private final int pageLimit;
    @Getter
    private final Vault vault;

    public VaultMenu(Vault vault, int page)
    {
        super("&0" + vault.getName() + "'s Vault", vault.getRows(), page);

        this.vault = vault;
        this.pageLimit = rows * 9;
    }

    @Override
    public void updateIcons(Player player)
    {
        List<List<ItemStack>> pages = Lists.partition(new ArrayList<>(vault.getItems()), pageLimit);
        if (!pages.isEmpty())
        {
            for (ItemStack item : pages.get(page - 1))
            {
                for (int y = 1; y <= rows; y++)
                {
                    boolean placed = false;
                    for (int x = 1; x <= 9; x++)
                    {
                        ItemIcon icon = new ItemIcon(x, y, item, this);
                        if (!slotTaken(icon.getSlot()))
                        {
                            addIcon(icon);
                            placed = true;
                            break;
                        }
                    }

                    if (placed)
                    {
                        break;
                    }
                }
            }
        }
    }

    @Getter
    private class ItemIcon extends Icon
    {
        private final ItemStack realItem;
        private final VaultMenu menu;

        public ItemIcon(int x, int y, ItemStack item, VaultMenu menu)
        {
            super(x, y, 1, false, Material.BARRIER, "", new ArrayList<>());

            this.menu = menu;
            this.realItem = item;
            ItemMeta meta = item.getItemMeta();
            if (meta == null)
            {
                return;
            }

            this.amount = item.getAmount();
            this.material = item.getType();
            this.displayName = meta.getDisplayName();
            this.lore = meta.getLore();
        }

        @Override
        public void onClick(Player player)
        {
            player.getInventory().addItem(realItem);
            vault.getItems().remove(realItem);
            menu.updateIcons(player);
            menu.updateInventory(player);
        }

        @Override
        public ItemStack getItemStack()
        {
            return realItem;
        }
    }
}
