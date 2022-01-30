package mohammad.adib.mavlinkdashboard;

import android.app.Application;
import android.content.SharedPreferences;

public class MavlinkDashboardApp extends Application {

    private static MavlinkDashboardApp app;
    public MavlinkComm mavlinkComm = new MavlinkComm();
    private SharedPreferences preferences;

    public MavlinkDashboardApp() {
        app = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        new Thread(() -> mavlinkComm.start("0.0.0.0", "0.0.0.0", 14550)).start();
    }

    public void pinItem(String type, String key) {
        preferences.edit().putBoolean(type + "#" + key, true).apply();
    }

    public void unpinItem(String type, String key) {
        preferences.edit().putBoolean(type + "#" + key, false).apply();
    }

    public boolean isPinned(String type, String key) {
        return preferences.getBoolean(type + "#" + key, false);
    }

    public static MavlinkDashboardApp getInstance() {
        return app;
    }
}
