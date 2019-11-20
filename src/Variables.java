import java.util.HashMap;

public class Variables {

    private static HashMap<String, Object> variables;

    static {
        variables = new HashMap<>();
        variables.put("server_ip", "localhost");
        variables.put("server_port", 3334);
        variables.put("peer_port", 3333);
        variables.put("ippack_length", 6);
        variables.put("time_serverlist_clean", 6000);
        variables.put("time_server_max_without_keep_alive", 5000);
        variables.put("time_send_keep_alive", 2500);
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
