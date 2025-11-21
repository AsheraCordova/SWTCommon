//start - license
/*
 * Copyright (c) 2025 Ashera Cordova
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
//end - license
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
