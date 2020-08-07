package me.alexprogrammerde.WebGameStats;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class Main
extends JavaPlugin
implements Listener {
    public static File indexhtml;
    public FileConfiguration data;
    public boolean debug;
    public static Main getPlugin() { return Main.getPlugin(Main.class); }
    public Thread thread;
    public List<String> exeludedplayers = new ArrayList<>();

    public void onEnable() {
        this.saveDefaultConfig();
        File dir = getDataFolder();
        Logger logger = getLogger();

        exeludedplayers = getConfig().getStringList("excluded-players");
        getServer().getPluginManager().registerEvents(new DataListener(), this);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (!(indexhtml = new File(dir, "index.html")).exists()) {
            try {
                indexhtml.createNewFile();
            }

            catch (IOException e) {
                e.printStackTrace();
                this.getServer().getPluginManager().disablePlugin((Plugin)this);
                return;
            }

            if (indexhtml.exists()) {
                try {
                    String fileContent =
                    "<html>\n" +
                    "  <head>\n" +
                    "    <title>WebGameStats</title>\n" +
                    "    <link rel=\"stylesheet\" type=\"text/css\" href=\"https://www.pistonmaster.net/WebGameStats/global.css\">\n" +
                    "    <link rel=\"stylesheet\" type=\"text/css\" href=\"https://www.pistonmaster.net/WebGameStats/animate.css\">\n" +
                    "    <style>\n" +
                    "     table, th, td {\n" +
                    "      border: 1px solid black;\n" +
                    "      border-collapse: collapse;\n" +
                    "      border-color: white;\n" +
                    "     }\n" +
                    "    </style>\n" +
                    "  </head>\n" +
                    "  <body>\n" +
                    "    <div class=\"container clearfix\">\n" +
                    "      <div class=\"content\">\n" +
                    "        <article class=\"article animated slideInUp\" style=\"text-align: center; display: block; margin-left: auto; margin-right: auto; width: 50%;\">\n" +
                    "          <div align=\"center\">\n" +
                    "            <form action=\"/\">\n" +
                    "              <label for=\"username\"><b><h1>WebGameStats</h1></b></label>\n" +
                    "              <label for=\"username\"><b><h2>Ver 2.0.0</h2></b></label>\n" +
                    "              player_stats\n" +
                    "              <label for=\"username\"><b><h3>Please enter your username to get your specific stats</h3></b></label>\n" +
                    "              <label for=\"username\"><b><h3>Username</h3></b></label>\n" +
                    "              <input type=\"text\" placeholder=\"Enter Username\" name=\"username\" required>\n" +
                    "              <div id=\"html_element\"></div>\n" +
                    "              <input type=\"submit\" value=\"Submit\" style=\"margin-top:10px;\">\n" +
                    "            </form>\n" +
                    "            playtime_result\n" +
                    "          </div>\n" +
                    "        </article>\n" +
                    "      </div>\n" +
                    "    </div>\n" +
                    "  </body>\n" +
                    "</html>";

                    BufferedWriter writer = new BufferedWriter(new FileWriter("plugins/WebGameStats/index.html"));
                    writer.write(fileContent);
                    writer.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    this.getServer().getPluginManager().disablePlugin((Plugin)this);
                    return;
                }
            }
        }

        // All the loading is finished we can check for debugging now
        data = new ConfigManager(this, "data.yml").getConfig();
        debug = getConfig().getBoolean("debug");

        if (debug) {
            getLogger().info("Starting HttpServer Thread");
        }

        thread = new Thread(() -> {
            try {
                HttpHandler.main(null);
            } catch (IOException e) {
                e.printStackTrace();
                this.getServer().getPluginManager().disablePlugin((Plugin)this);
            }
        });

        thread.start();

        if (debug) {
            getLogger().info("Started HttpServer Thread");
        }

        logger.info("Checking for updates.");
        new UpdateChecker(this, 12345).getVersion(version -> {
            if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                logger.info("There is not a new update available.");
            } else {
                logger.info("There is a new update available.");
            }
        });

        logger.info("Loading metrics.");
        Metrics metrics = new Metrics(this, 8449);

        if (debug) {
            getLogger().info("Enabled WebGameStats. In debugging mode. :)");
        } else {
            getLogger().info("Enabled WebGameStats. :)");
        }
    }

    public void onDisable() {
        getLogger().info("Stopping thread.");

        thread.stop();

        getLogger().info("Disabled WebGameStats. :)");
    }

    public File getDataFile() {
        return new File(getDataFolder(), "data.yml");
    }

    public void reloadData() { this.data = new ConfigManager(this, "data.yml").getConfig(); }
}

