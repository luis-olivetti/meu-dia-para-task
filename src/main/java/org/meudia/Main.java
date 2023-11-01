package org.meudia;

import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import org.meudia.ui.Timekeeping;

import javax.swing.*;

import static javax.swing.WindowConstants.HIDE_ON_CLOSE;
import static org.meudia.commons.helper.AppPathHelper.getAppPath;

public class Main {

    public static Timekeeping timekeepingForm = null;
    public static void main(String[] args) {
        SystemTray systemTray = SystemTray.get();
        if (systemTray == null) {
            throw new RuntimeException("Não foi possível carregar o SystemTray!");
        }

        systemTray.installShutdownHook();
        systemTray.setImage(getAppPath() + "/clock.png");

        systemTray.getMenu().add(new MenuItem("Apontar o que vou fazer", actionEvent -> {
            if (timekeepingForm == null) {
                timekeepingForm = new Timekeeping();
                timekeepingForm.setDefaultCloseOperation(HIDE_ON_CLOSE);
                timekeepingForm.setTitle("Apontamento");
                timekeepingForm.setSize(800, 500);
                timekeepingForm.setResizable(false);
                timekeepingForm.setLocationRelativeTo(null);
            }

            timekeepingForm.setVisible(true);
        }));

        systemTray.getMenu().add(new MenuItem("Encerrar", actionEvent -> systemTray.shutdown()));

        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.themes.FlatMacLightLaf");
        } catch (Exception ex) {
            throw new RuntimeException("Falha ao definir o tema FlatLaf!");
        }
    }
}
