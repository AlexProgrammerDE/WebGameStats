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
    public static void main(String[] args) throws IOException {
        int port = Main.getPlugin().getConfig().getInt("port");
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new Handler());
        server.setExecutor(null);
        server.start();
    }

    public static Map<String, String> xd(String a) {
        HashMap<String, String> map = new HashMap<>();
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
            String response;

            if (uri != null) {
                Map<String, String> map = HttpHandler.xd(uri);
                String name = map.get("username");
                if (!name.isEmpty()) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(name);
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
                                "advancedtable" +
                                 "</table>";
                        if (Main.getPlugin().data.contains(Objects.requireNonNull(player.getName()))) {
                            String advancedtable =
                                    "  <tr>" +
                                    "    <td style=\"text-align:center;\">Player kills: " + Main.getPlugin().data.getString(player.getName() + ".kills") + " </td>" +
                                    "  </tr>" +
                                    "  <tr>" +
                                    "    <td style=\"text-align:center;\">Deaths: " + Main.getPlugin().data.getString(player.getName() + ".deaths") + " </td>" +
                                    "  </tr>" +
                                    "  <tr>" +
                                    "    <td style=\"text-align:center;\">Times joined: " + Main.getPlugin().data.getString(player.getName() + ".join") + " </td>" +
                                    "  </tr>";
                            table = table.replace("advancedtable", advancedtable);
                        } else {
                            table = table.replace("advancedtable", "");
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

            TimelessServer server = new TimelessServer();
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

            HashMap<String, Integer> kills = sortByValue(playerkills);
            HashMap<String, Integer> joins = sortByValue(playerjoins);
            HashMap<String, Integer> deaths = sortByValue(playerdeaths);

            List<String> killslist = new ArrayList<>();
            List<String> joinslist = new ArrayList<>();
            List<String> deathslist = new ArrayList<>();

            for (String key : kills.keySet()) {
                killslist.add(key);
            }

            for (String key : joins.keySet()) {
                joinslist.add(key);
            }

            for (String key : deaths.keySet()) {
                deathslist.add(key);
            }

            Collections.reverse(killslist);
            Collections.reverse(joinslist);
            Collections.reverse(deathslist);

            if (killslist.size() > 2 && joinslist.size() > 2 && deathslist.size() > 2) {
                String top =
                        "<table style=\"color:white; margin-top:10px; margin-right:auto; margin-left:auto;\">" +
                                "  <tr>" +
                                "    <th>Top kills</th>" +
                                "    <th>Top joins</th>" +
                                "    <th>Top deaths</th>" +
                                "  </tr>" +
                                "first" +
                                "second" +
                                "third" +
                                "</table>";

                String first =
                        "  <tr>" +
                                "    <td style=\"text-align:center;\">" + killslist.get(0) + "</td>" +
                                "    <td style=\"text-align:center;\">" + joinslist.get(0) + "</td>" +
                                "    <td style=\"text-align:center;\">" + deathslist.get(0) + "</td>" +
                                "  </tr>";
                top = top.replace("first", first);

                String second =
                        "  <tr>" +
                                "    <td style=\"text-align:center;\">" + killslist.get(1) + "</td>" +
                                "    <td style=\"text-align:center;\">" + joinslist.get(1) + "</td>" +
                                "    <td style=\"text-align:center;\">" + deathslist.get(1) + "</td>" +
                                "  </tr>";
                top = top.replace("second", second);

                String third =
                        "  <tr>" +
                                "    <td style=\"text-align:center;\">" + killslist.get(2) + "</td>" +
                                "    <td style=\"text-align:center;\">" + joinslist.get(2) + "</td>" +
                                "    <td style=\"text-align:center;\">" + deathslist.get(2) + "</td>" +
                                "  </tr>";
                top = top.replace("third", third);

                response = response.replace("player_stats", top);
            } else {
                response = response.replace("player_stats", "");
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
        List<Map.Entry<String, Integer> > list =
                new LinkedList<Map.Entry<String, Integer> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
}

