package com.ashera.common;

import java.util.prefs.Preferences;

public class PreferenceWrapper {
    public static int DEFAULT_WIDTH = 320;
    public static int DEFAULT_HEIGHT = 568;

    private static Preferences prefs = Preferences.userRoot().node("SystemPreferences");
    
    public static int getCurrentWidth() {
        return prefs.getInt("width", DEFAULT_WIDTH);
    }
    
    public static int getDefaultHeight() {
        return prefs.getInt("height", DEFAULT_HEIGHT);

    }

    public static void saveSize(int width, int height) {
        prefs.putInt("width", width);
        prefs.putInt("height", height);
        
    }
}
