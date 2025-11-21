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

public class OperatingSystem {
    private static String OS = System.getProperty("os.name").toLowerCase();
    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }
    
    public static boolean isOSX() {
        return (OS.indexOf("mac os x") >= 0);
    }
    
    public static boolean isLinux() {
        return (OS.indexOf("linux") >= 0);
    }
    
    public static String getOs() {
    	if (isWindows()) {
    		return "windows";
    	}
    	
    	if (isOSX()) {
    		return "mac";
    	}
    	
    	return "linux";
    }
}
