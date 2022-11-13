package org.example;

import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URISyntaxException;

import static javax.swing.WindowConstants.HIDE_ON_CLOSE;

public class Main {

    public static Window3 ww = null;
    public static void main(String[] args) throws URISyntaxException {
        SystemTray systemTray = SystemTray.get();
        if (systemTray == null) {
            throw new RuntimeException("Unable to load SystemTray!");
        }

        systemTray.installShutdownHook();
        systemTray.setImage(getAppPath() + "/clock.png");

        systemTray.getMenu().add(new MenuItem("Apontar o que vou fazer", new ActionListener() {
            @Override
            public
            void actionPerformed(final ActionEvent e) {
                if (ww == null) {
                    ww = new Window3();
                    ww.setDefaultCloseOperation(HIDE_ON_CLOSE);
                    ww.setTitle("O que vou fazer?");
                    ww.setSize(600, 200);
                    ww.setLocationRelativeTo(null);
                }

                ww.setVisible(true);
            }
        }));

        systemTray.getMenu().add(new MenuItem("Quit", new ActionListener() {
            @Override
            public
            void actionPerformed(final ActionEvent e) {
                systemTray.shutdown();
                //System.exit(0);  not necessary if all non-daemon threads have stopped.
            }
        }));
    }

    private static String getAppPath() throws URISyntaxException {
        String pathJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        if (pathJar.contains(".jar")) {
            pathJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath();
        }
        return pathJar;
    }
}