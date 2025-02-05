package com.github.rfsmassacre.heavenvaults;

import com.github.rfsmassacre.heavenlibrary.paper.HeavenPaperPlugin;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperConfiguration;
import com.github.rfsmassacre.heavenlibrary.paper.configs.PaperLocale;
import lombok.Getter;

@Getter
public final class HeavenVaults extends HeavenPaperPlugin
{
    @Getter
    private static HeavenVaults instance;
    private VaultYaml vaultYaml;

    @Override
    public void onEnable()
    {
        instance = this;
        getDataFolder().mkdir();
        addYamlManager(new PaperConfiguration(this, "", "config.yml", true));
        addYamlManager(new PaperLocale(this, "", "locale.yml", true));
        this.vaultYaml = new VaultYaml();
        getServer().getPluginManager().registerEvents(new VaultListener(), this);
        getCommand("playervault").setExecutor(new VaultCommand());
        getCommand("vaultadmin").setExecutor(new VaultAdminCommand());
    }
}
