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
