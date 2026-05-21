package com.simpletp;

import com.simpletp.commands.*;
import com.simpletp.listeners.*;
import com.simpletp.managers.*;
import com.simpletp.storage.DatabaseManager;
import com.simpletp.utils.MessageUtil;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleTPPlugin extends JavaPlugin {

    private DatabaseManager databaseManager;
    private HomeManager homeManager;
    private TPAManager tpaManager;
    private TeleportManager teleportManager;
    private SpawnManager spawnManager;
    private MessageUtil messageUtil;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);

        messageUtil    = new MessageUtil(this);
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();

        homeManager     = new HomeManager(this, databaseManager);
        teleportManager = new TeleportManager(this);
        tpaManager      = new TPAManager(this, teleportManager);
        spawnManager    = new SpawnManager(this);

        registerCommands();
        registerListeners();

        getLogger().info("SimpleTP enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (teleportManager != null) teleportManager.cancelAll();
        if (databaseManager != null) databaseManager.close();
        getLogger().info("SimpleTP disabled.");
    }

    private void registerCommands() {
        TPACommand tpaCmd = new TPACommand(this, tpaManager);
        register("tpa", tpaCmd, tpaCmd);

        TPAHereCommand tpaHereCmd = new TPAHereCommand(this, tpaManager);
        register("tpahere", tpaHereCmd, tpaHereCmd);

        register("tpaccept",   new TPAcceptCommand(this, tpaManager),  null);
        register("tpdeny",     new TPDenyCommand(this, tpaManager),    null);
        register("tpacancel",  new TPACancelCommand(this, tpaManager), null);
        register("tpatoggle",  new TPAToggleCommand(this, tpaManager), null);

        AdminTPCommand adminTp = new AdminTPCommand(this);
        register("tp", adminTp, adminTp);

        AdminTP2MCommand adminTp2m = new AdminTP2MCommand(this);
        register("tp2m", adminTp2m, adminTp2m);

        register("sethome", new SetHomeCommand(this, homeManager), null);

        HomeCommand homeCmd = new HomeCommand(this, homeManager, teleportManager);
        register("home", homeCmd, homeCmd);

        DelHomeCommand delHomeCmd = new DelHomeCommand(this, homeManager);
        register("delhome", delHomeCmd, delHomeCmd);

        register("homes",     new HomesCommand(this, homeManager),   null);
        register("setnewspawn", new SetSpawnCommand(this, spawnManager), null);
        register("simpletp",  new SimpletpCommand(this),             null);
    }

    private void register(String name, org.bukkit.command.CommandExecutor executor,
                          org.bukkit.command.TabCompleter completer) {
        PluginCommand cmd = getCommand(name);
        if (cmd == null) {
            getLogger().severe("Command '" + name + "' is null! Check plugin.yml.");
            return;
        }
        cmd.setExecutor(executor);
        if (completer != null) cmd.setTabCompleter(completer);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new MoveListener(this, teleportManager),   this);
        getServer().getPluginManager().registerEvents(new DamageListener(this, teleportManager), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this),              this);
        getServer().getPluginManager().registerEvents(new SpawnListener(this, spawnManager),     this);
    }

    public void reload() {
        reloadConfig();
        messageUtil.reload();
        spawnManager = new SpawnManager(this);
    }

    public DatabaseManager getDatabaseManager()   { return databaseManager; }
    public HomeManager getHomeManager()           { return homeManager; }
    public TPAManager getTpaManager()             { return tpaManager; }
    public TeleportManager getTeleportManager()   { return teleportManager; }
    public SpawnManager getSpawnManager()         { return spawnManager; }
    public MessageUtil getMessageUtil()           { return messageUtil; }
}
