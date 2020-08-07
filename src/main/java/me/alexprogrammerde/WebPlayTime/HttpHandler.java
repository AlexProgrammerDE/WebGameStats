package me.alexprogrammerde.WebPlayTime;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import me.codedred.playtimes.api.TimelessPlayer;
import me.codedred.playtimes.api.TimelessServer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

public class HttpHandler {
    static boolean debug;

    public static void main(String[] args) throws IOException {
        int port = Main.getPlugin().getConfig().getInt("port");
        debug = Main.getPlugin().getConfig().getBoolean("debug");

        if (debug) {
            Main.getPlugin().getLogger().info("HttpServer Thread: Creating server and starting it.");
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new Handler());
        server.setExecutor(null);
        server.start();

        if (debug) {
            Main.getPlugin().getLogger().info("HttpServer Thread: Started HttpServer.");
        }
    }

    public static Map<String, String> xd(String a) {
        HashMap<String, String> map = new HashMap<>();

        for (String s : a.split("&")) {
            map.put(s.split("=")[0], s.split("=")[1]);
        }

        if (debug) {
            Main.getPlugin().getLogger().info("HttpServer Thread: A URL is getting mapped. The result is: " + map.toString());
        }

        return map;
    }

    static class Handler implements com.sun.net.httpserver.HttpHandler {
        Handler() {}

        @Override
        public void handle(HttpExchange t) throws IOException {
            if (debug) {
                Main.getPlugin().getLogger().info("HttpServer Thread: A connection is getting handled. More logs should follow.");
            }

            StringBuilder contentBuilder = new StringBuilder();

            try {
                String str;
                BufferedReader in = new BufferedReader(new FileReader("plugins/WebPlayTime/index.html"));

                while ((str = in.readLine()) != null) {
                    contentBuilder.append(str);
                }

                in.close();

                if (debug) {
                    Main.getPlugin().getLogger().info("HttpServer Thread: Loaded a index file.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String page = contentBuilder.toString();
            String uri = t.getRequestURI().getRawQuery();
            String response;

            if (debug) {
                Main.getPlugin().getLogger().info("HttpServer Thread: Retrieved Query and initialized stuff.");
            }

            if (uri != null) {
                Map<String, String> map = HttpHandler.xd(uri);
                String name = map.get("username");

                if (!name.isEmpty()) {
                    if (debug) {
                        Main.getPlugin().getLogger().info("HttpServer Thread: Query isn't null and the username: " + name);
                    }

                    OfflinePlayer player = Bukkit.getOfflinePlayer(name);
                    String div = "<div style=\"color:white;\"> placeholder </div>";
                    TimelessPlayer timelessplayer = new TimelessPlayer(player.getUniqueId());

                    if (player.hasPlayedBefore()) {
                        if (debug) {
                            Main.getPlugin().getLogger().info("HttpServer Thread: That player has played before.");
                        }

                        String hour;
                        String min;

                        if (Instant.ofEpochMilli(player.getFirstPlayed()).atZone(ZoneOffset.UTC).getHour() < 10) {
                          hour = "0" + Instant.ofEpochMilli(player.getFirstPlayed()).atZone(ZoneOffset.UTC).getHour();
                        } else {
                          hour = "" + Instant.ofEpochMilli(player.getFirstPlayed()).atZone(ZoneOffset.UTC).getHour();
                        }

                        if (Instant.ofEpochMilli(player.getFirstPlayed()).atZone(ZoneOffset.UTC).getMinute() < 10) {
                          min = "0" + Instant.ofEpochMilli(player.getFirstPlayed()).atZone(ZoneOffset.UTC).getMinute();
                        } else {
                          min = "" + Instant.ofEpochMilli(player.getFirstPlayed()).atZone(ZoneOffset.UTC).getMinute();
                        }

                        String table =
                                "<table style=\"color:white; margin-top:10px; margin-right:auto; margin-left:auto;\">" +
                                "first" +
                                "second" +
                                "third" +
                                "fourth" +
                                "fifth" +
                                 "</table>";

                        String playerfirst =
                                "  <tr>" +
                                "    <td style=\"text-align:center;\">First time joined: " + hour + ":" + min + " " + Instant.ofEpochMilli(player.getFirstPlayed()).atZone(ZoneOffset.UTC).getDayOfMonth() + "." + Instant.ofEpochMilli(player.getFirstPlayed()).atZone(ZoneOffset.UTC).getMonth().name() + "." + Instant.ofEpochMilli(player.getFirstPlayed()).atZone(ZoneOffset.UTC).getYear() + "</td>" +
                                "  </tr>";

                        table = table.replaceAll("first", playerfirst);

                        String playersecound =
                                "  <tr>" +
                                "    <td style=\"text-align:center;\">Time played: " + timelessplayer.getPlayTime() + " </td>" +
                                "  </tr>";

                        table = table.replaceAll("second", playersecound);

                        if (Main.getPlugin().data.isSet(Objects.requireNonNull(player.getName()) + ".kills")) {
                            String playerthird =
                                    "  <tr>" +
                                    "    <td style=\"text-align:center;\">Player kills: " + Main.getPlugin().data.getString(player.getName() + ".kills") + " </td>" +
                                    "  </tr>";

                            table = table.replaceAll("third", playerthird);
                        } else {
                            table = table.replaceAll("third", "");
                        }

                        if (Main.getPlugin().data.isSet(Objects.requireNonNull(player.getName()) + ".deaths")) {
                            String playerfourth =
                                    "  <tr>" +
                                    "    <td style=\"text-align:center;\">Deaths: " + Main.getPlugin().data.getString(player.getName() + ".deaths") + " </td>" +
                                    "  </tr>";

                            table = table.replaceAll("fourth", playerfourth);
                        } else {
                            table = table.replaceAll("fourth", "");
                        }

                        if (Main.getPlugin().data.isSet(Objects.requireNonNull(player.getName()) + ".join")) {
                            String playerfith =
                                    "  <tr>" +
                                    "    <td style=\"text-align:center;\">Times joined: " + Main.getPlugin().data.getString(player.getName() + ".join") + " </td>" +
                                    "  </tr>";

                            table = table.replaceAll("fifth", playerfith);
                        } else {
                            table = table.replaceAll("fifth", "");
                        }

                        if (debug) {
                            Main.getPlugin().getLogger().info("HttpServer Thread: Prepared player table. Result: " + table);
                        }

                        div = div.replace("placeholder", table);
                    } else {
                        String warning = "<p style=\"color:red; margin-top:10px;\">This player never joined this server.";

                        if (debug) {
                            Main.getPlugin().getLogger().info("HttpServer Thread: That player seems to never have played before.");
                        }

                        div = div.replace("placeholder", warning);
                    }

                    response = page.replace("playtime_result", div);
                } else {
                    if (debug) {
                        Main.getPlugin().getLogger().info("HttpServer Thread: No player specified. Displaying default site.");
                    }

                    response = page.replace("playtime_result", "");
                }
            } else {
                if (debug) {
                    Main.getPlugin().getLogger().info("HttpServer Thread: No player specified. Displaying default site.");
                }

                response = page.replace("playtime_result", "");
            }

            if (debug) {
                Main.getPlugin().getLogger().info("HttpServer Thread: Calculating top players.");
            }

            HashMap<String, Integer> playerkills = new HashMap<>();
            HashMap<String, Integer> playerjoins = new HashMap<>();
            HashMap<String, Integer> playerdeaths = new HashMap<>();
            FileConfiguration data = Main.getPlugin(Main.class).data;

            for (String key : data.getKeys(false)) {
                playerkills.put(key, data.getInt(key + ".kills"));
            }

            for (String key : data.getKeys(false)) {
                playerjoins.put(key, data.getInt(key + ".join"));
            }

            for (String key : data.getKeys(false)) {
                playerdeaths.put(key, data.getInt(key + ".deaths"));
            }

            TimelessServer server = new TimelessServer();
            List<UUID> ids = server.getTop10Players();

            HashMap<String, Integer> kills = sortByValue(playerkills);
            HashMap<String, Integer> joins = sortByValue(playerjoins);
            HashMap<String, Integer> deaths = sortByValue(playerdeaths);

            List<String> killslist = new ArrayList<>(kills.keySet());
            List<String> joinslist = new ArrayList<>(joins.keySet());
            List<String> deathslist = new ArrayList<>(deaths.keySet());

            Collections.reverse(killslist);
            Collections.reverse(joinslist);
            Collections.reverse(deathslist);

            if (debug) {
                Main.getPlugin().getLogger().info("HttpServer Thread: Finished calculation.");
            }

            if (killslist.size() > 2 && joinslist.size() > 2 && deathslist.size() > 2 && ids.size() > 2) {
                if (debug) {
                    Main.getPlugin().getLogger().info("HttpServer Thread: Enough players. Preparing table.");
                }

                String top =
                        "<table style=\"color:white; margin-top:10px; margin-right:auto; margin-left:auto;\">" +
                                "  <tr>" +
                                "    <th>Top kills</th>" +
                                "    <th>Top joins</th>" +
                                "    <th>Top deaths</th>" +
                                "    <th>Top Play Time</th>" +
                                "  </tr>" +
                                "first" +
                                "second" +
                                "third" +
                                "</table>";

                if (ids.get(0) != null) {
                    String first =
                            "  <tr>" +
                                    "    <td style=\"text-align:center;\">" + killslist.get(0) + "</td>" +
                                    "    <td style=\"text-align:center;\">" + joinslist.get(0) + "</td>" +
                                    "    <td style=\"text-align:center;\">" + deathslist.get(0) + "</td>" +
                                    "    <td style=\"text-align:center;\">" + Bukkit.getOfflinePlayer(ids.get(0)).getName() + "</td>" +
                                    "  </tr>";
                    top = top.replace("first", first);
                } else {
                    top = top.replace("first", "");
                }

                if (ids.get(1) != null) {
                String second =
                        "  <tr>" +
                                "    <td style=\"text-align:center;\">" + killslist.get(1) + "</td>" +
                                "    <td style=\"text-align:center;\">" + joinslist.get(1) + "</td>" +
                                "    <td style=\"text-align:center;\">" + deathslist.get(1) + "</td>" +
                                "    <td style=\"text-align:center;\">" + Bukkit.getOfflinePlayer(ids.get(1)).getName() + "</td>" +
                                "  </tr>";
                top = top.replace("second", second);
                } else {
                    top = top.replace("first", "");
                }

                if (ids.get(2) != null) {
                    String third =
                        "  <tr>" +
                                "    <td style=\"text-align:center;\">" + killslist.get(2) + "</td>" +
                                "    <td style=\"text-align:center;\">" + joinslist.get(2) + "</td>" +
                                "    <td style=\"text-align:center;\">" + deathslist.get(2) + "</td>" +
                                "    <td style=\"text-align:center;\">" + Bukkit.getOfflinePlayer(ids.get(2)).getName() + "</td>" +
                                "  </tr>";
                    top = top.replace("third", third);
                } else {
                    top = top.replace("first", "");
                }

                if (debug) {
                    Main.getPlugin().getLogger().info("HttpServer Thread: Finished creating table. Result: " + top);
                }

                response = response.replace("player_stats", top);
            } else {
                if (debug) {
                    Main.getPlugin().getLogger().info("HttpServer Thread: Not enough players in database to display it. (If more than 3 players are in the data this is a issue.)");
                }

                response = response.replace("player_stats", "");
            }

            if (debug) {
                Main.getPlugin().getLogger().info("HttpServer Thread: Finished calculating ALL things. Not displaying result cuz of the size for console.");
            }

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    // function to sort hashmap by values
    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list = new LinkedList<>(hm.entrySet());

        // Sort the list
        list.sort(Map.Entry.comparingByValue());

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }

        if (debug) {
            Main.getPlugin().getLogger().info("HttpServer Thread: Sorting player stats by value finished. result: " + temp.toString());
        }

        return temp;
    }
}

