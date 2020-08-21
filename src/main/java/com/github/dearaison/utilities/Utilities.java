package com.github.dearaison.utilities;

/**
 * Created by IntelliJ on Wednesday, 19 August, 2020 at 13:50.
 *
 * @author Joseph Maria
 */
public class Utilities {
    public static String getAbsolutePathOfLocalResource(String resourceFileName) {
        return Utilities.class.getResource(resourceFileName).getPath().substring(1);
    }
}
