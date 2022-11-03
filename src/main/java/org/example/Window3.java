package org.example;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Window3 extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JCheckBox cbSaida;

    private static final short CD_PROJETO = 0;
    private static final short CD_RESPONSAVEL = 2;
    private static final short DH_INICIO = 3;
    private static final short DH_TERMINO = 4;
    private static final short CD_EQUIPE = 8;
    private static final short COMMENT = 10;

    public Window3() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    onOK();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() throws IOException, URISyntaxException {

        Config config = new ConfigFacade().getConfiguration(getAppPath() + "/config.json");
        if (config == null) {
            return;
        }

        FileInputStream file = new FileInputStream(getAppPath() + "/apontamentos.xls");
        HSSFWorkbook workbook = new HSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        Row lastRow = sheet.getRow(sheet.getLastRowNum());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date finalDate = new Date();

        if (cbSaida.isSelected()) {
            if (lastRow.getRowNum() > 0) {
                lastRow.createCell(DH_TERMINO).setCellValue(dateFormat.format(finalDate));
            }
        } else {
            int newRow = sheet.getLastRowNum() + 1;

            sheet.createRow(newRow);
            Row row = sheet.getRow(newRow);

            row.createCell(CD_PROJETO).setCellValue(config.projectCode);
            row.createCell(CD_RESPONSAVEL).setCellValue(config.username);

            int milleseconds = 0;

            if (lastRow.getRowNum() > 0) {
                boolean isExit = lastRow.getCell(DH_TERMINO) == null || lastRow.getCell(DH_TERMINO).getStringCellValue().isEmpty();
                if (isExit) {
                    lastRow.createCell(DH_TERMINO).setCellValue(dateFormat.format(finalDate));
                    milleseconds += 1000;
                }
            }

            finalDate.setTime(finalDate.getTime() + milleseconds);
            row.createCell(DH_INICIO).setCellValue(dateFormat.format(finalDate));

            row.createCell((short) 6).setCellValue(1);
            row.createCell(CD_EQUIPE).setCellValue(config.teamCode);
            row.createCell(COMMENT).setCellValue(textField1.getText());
        }

        FileOutputStream fileOut = new FileOutputStream(getAppPath() + "/apontamentos.xls", false);
        workbook.write(fileOut);

        resetForm();

        dispose();
    }

    private void resetForm() {
        textField1.setText("");
        cbSaida.setSelected(false);
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        Window3 dialog = new Window3();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    private static String getAppPath() throws URISyntaxException {
        String pathJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        if (pathJar.contains(".jar")) {
            pathJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath();
        }
        return pathJar;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(panel1, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        buttonOK = new JButton();
        buttonOK.setText("OK");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(buttonOK, gbc);
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(buttonCancel, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(panel3, gbc);
        textField1 = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(textField1, gbc);
        cbSaida = new JCheckBox();
        cbSaida.setText("Pausa/Encerramento");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(cbSaida, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
