package com.fluxer.srv.utils;

public class VersionUtils {
    public static boolean isOutdated(String current, String latest) {
        String[] currentParts = current.replaceFirst("^v", "").split("\\.");
        String[] latestParts = latest.replaceFirst("^v", "").split("\\.");

        int length = Math.max(currentParts.length, latestParts.length);

        for (int i = 0; i < length; i++) {
            int currentNum = i < currentParts.length ? parsePart(currentParts[i]) : 0;
            int latestNum = i < latestParts.length ? parsePart(latestParts[i]) : 0;

            if (currentNum < latestNum) return true;
            if (currentNum > latestNum) return false;
        }

        return false;
    }

    private static int parsePart(String part) {
        try {
            return Integer.parseInt(part.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}