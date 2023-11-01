package org.meudia.commons.helper;

import org.meudia.Main;

import java.io.File;
import java.net.URISyntaxException;

public class AppPathHelper {
    public static String getAppPath() {
        try {
            String pathJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            if (pathJar.contains(".jar")) {
                pathJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath();
            }

            return pathJar;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
