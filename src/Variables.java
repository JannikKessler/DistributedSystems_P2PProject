import java.awt.*;
import java.util.HashMap;

public class Variables {

    private static HashMap<String, Object> variables;

    static {
        variables = new HashMap<>();
        variables.put("server_ip", "localhost");
        variables.put("server_port", 3333);
        variables.put("standard_peer_port", 3333);
        variables.put("ippack_length", 6);
        variables.put("time_serverlist_clean", 60000);
        variables.put("time_server_max_without_keep_alive", 55000);
        variables.put("time_send_keep_alive", 25000);
        variables.put("screen_update_time", 1000);
        variables.put("gui_size", new Dimension(250, 250));
        variables.put("font", new Font("Calibri", Font.PLAIN, 16));
        variables.put("font_headline", new Font("Calibri", Font.BOLD, 20));
    }

    public static String getStringValue(String name) {
        return (String) variables.get(name);
    }

    public static int getIntValue(String name) {
        return (int) variables.get(name);
    }

    public static void putObject(String name, Object o) {
        variables.put(name, o);
    }

    public static void putInt(String name, int zahl) {
        variables.put(name, zahl);
    }

    public static Object getObject(String name) {
        return variables.get(name);
    }
}
