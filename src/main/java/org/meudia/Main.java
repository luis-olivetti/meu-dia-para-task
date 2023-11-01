package org.meudia;

import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import org.apache.commons.compress.utils.IOUtils;
import org.meudia.ui.Timekeeping;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

import static javax.swing.WindowConstants.HIDE_ON_CLOSE;

public class Main {

    public static Timekeeping timekeepingForm = null;
    public static void main(String[] args) {
        SystemTray systemTray = SystemTray.get();
        if (systemTray == null) {
            throw new RuntimeException("Não foi possível carregar o SystemTray!");
        }

        systemTray.installShutdownHook();

        loadIconOnSystenTray(systemTray);

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

    private static void loadIconOnSystenTray(SystemTray systemTray) {
        InputStream imageStream = Main.class.getResourceAsStream("/clock.png");
        Image image;
        try {
            if (imageStream == null) {
                throw new Exception("Não foi possível carregar a imagem do ícone!");
            }
            image = Toolkit.getDefaultToolkit().createImage(IOUtils.toByteArray(imageStream));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        systemTray.setImage(image);
    }
}
