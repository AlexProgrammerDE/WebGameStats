package me.alexprogrammerde.WebPlayTime;

import com.google.common.collect.Lists;
import com.google.common.math.Stats;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import me.Cmaaxx.PlayTime.PlayTimeAPI;
import me.codedred.playtimes.api.TimelessPlayer;
import me.codedred.playtimes.api.TimelessServer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

public class HttpHandler {
    public static void main(String[] args) throws IOException {
        int port = Main.getPlugin().getConfig().getInt("port");
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new Handler());
        server.setExecutor(null);
        server.start();
    }

    public static Map<String, String> xd(String a) {
        HashMap<String, String> map = new HashMap<String, String>();
        for (String s : a.split("&")) {
            map.put(s.split("=")[0], s.split("=")[1]);
        }
        return map;
    }

    static class Handler
    implements com.sun.net.httpserver.HttpHandler {
        Handler() {
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            StringBuilder contentBuilder = new StringBuilder();

            try {
                String str;
                BufferedReader in = new BufferedReader(new FileReader("plugins/WebPlayTime/index.html"));
                while ((str = in.readLine()) != null) {
                    contentBuilder.append(str);
                }
                in.close();
            }

            catch (IOException e) {
                e.printStackTrace();
            }

            String page = contentBuilder.toString();
            String uri = t.getRequestURI().getRawQuery();
            String response = "";

            if (uri != null) {
                Map<String, String> map = HttpHandler.xd(uri);
                String name = map.get("username");
                if (!name.isEmpty()) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer((String)name);
                    String div = "<div style=\"color:white;\"> placeholder </div>";
                    TimelessPlayer timelessplayer = new TimelessPlayer(player.getUniqueId());
                    if (player.hasPlayedBefore()) {
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
                                "  <tr>" +
                                "    <td style=\"text-align:center;\">First time joined: " + hour + ":" + min + " " + Instant.ofEpochMilli(player.getFirstPlayed()).atZone(ZoneOffset.UTC).getDayOfMonth() + "." + Instant.ofEpochMilli(player.getFirstPlayed()).atZone(ZoneOffset.UTC).getMonth().name() + "." + Instant.ofEpochMilli(player.getFirstPlayed()).atZone(ZoneOffset.UTC).getYear() + "</td>" +
                                "  </tr>" +
                                "  <tr>" +
                                "    <td style=\"text-align:center;\">Time played: " + timelessplayer.getPlayTime() + " </td>" +
                                "  </tr>" +
                                "  <tr>" +
                                "    <td style=\"text-align:center;\">Player kills: " + Main.getPlugin().data.getString(player.getName() + ".kills") + " </td>" +
                                "  </tr>" +
                                "advancedtable" +
                                 "</table>";
                        if (Main.getPlugin().data.contains(player.getName())) {
                            String advancedtable =
                                    "  <tr>" +
                                    "    <td style=\"text-align:center;\">Deaths: " + Main.getPlugin().data.getString(player.getName() + ".deaths") + " </td>" +
                                    "  </tr>" +
                                    "  <tr>" +
                                    "    <td style=\"text-align:center;\">Times joined: " + Main.getPlugin().data.getString(player.getName() + ".join") + " </td>" +
                                    "  </tr>";
                            table.replace("advancedtable", advancedtable);
                        } else {
                            table.replace("advancedtable", "");
                        }

                        div = div.replace("placeholder", table);
                    } else {
                        String warning = "<p style=\"color:red; margin-top:10px;\">This player never joined this server.";
                        div = div.replace("placeholder", warning);
                    }

                    response = page.replace("playtime_result", div);
                } else {
                    response = page.replace("playtime_result", "");
                }
            } else {
                response = page.replace("playtime_result", "");
            }

            /*TimelessServer server = new TimelessServer();
            HashMap<Integer, String> playerkills = new HashMap<>();

            for (String key : Main.getPlugin().data.getKeys(false)) {
                playerkills.put(Main.getPlugin().data.getInt(key + ".kills"), key);
            }

            Object[] keys = playerkills.keySet().toArray();

            Arrays.sort(keys);

            String top =
                    "<table style=\"color:white; margin-top:10px; margin-right:auto; margin-left:auto;\">" +
                    "  <tr>" +
                    "    <th>Top time played</th>" +
                    "    <th>Top player kills</th>" +
                    "  </tr>" +
                    "first" +
                    "secound" +
                    "third" +
                    "</table>";

            /*if () {
                String first =
                        "  <tr>" +
                        "    <td style=\"text-align:center;\">" + players.get(0).getName() + "</td>" +
                        "    <td style=\"text-align:center;\">" + playerkills.get(keys[0]) + "</td>" +
                        "  </tr>";
                top.replace("first", first);
            } else {
                top.replace("first", "");
            }



            if () {
                String secound =
                        "  <tr>" +
                        "    <td style=\"text-align:center;\">Player kills: " + players.get(1).getName() + " </td>" +
                        "    <td style=\"text-align:center;\">Player kills: " + playerkills.get(keys[1]) + " </td>" +
                        "  </tr>";
                top.replace("first", secound);
            } else {
                top.replace("first", "");
            }



            if () {
                String third =
                        "  <tr>" +
                        "    <td style=\"text-align:center;\">Player kills: " + players.get(2).getName() + " </td>" +
                        "    <td style=\"text-align:center;\">Player kills: " + playerkills.get(keys[2]) + " </td>" +
                        "  </tr>";
                top.replace("first", third);
            } else {
                top.replace("first", "");
            }

            Main.getPlugin().getLogger().info(server.getNumberOne().getName());
            Main.getPlugin().getLogger().info(server.getNumberTwo().getName());
            Main.getPlugin().getLogger().info(server.getNumberThree().getName());
            Main.getPlugin().getLogger().info(playerkills.get(keys[0]));
            Main.getPlugin().getLogger().info(playerkills.get(keys[1]));
            Main.getPlugin().getLogger().info(playerkills.get(keys[2]));

            response = response.replace("top_stats", top);*/

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}

