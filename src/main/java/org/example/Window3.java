package org.example;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Window3 extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField txfDescription;
    private JCheckBox cbSaida;
    private JTextField txfInitialDate;
    private JTextField textField1;

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

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent componentEvent) {
                super.componentShown(componentEvent);
                txfInitialDate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date().getTime()));

                Config config = null;
                try {
                    config = new ConfigFacade().getConfiguration(getAppPath() + "/config.json");
                    if (config == null) {
                        throw new RuntimeException("O sistema não foi inicializado, pois não encontrou o arquivo 'config.json'. Verifique!");
                    }

                    FileInputStream file = new FileInputStream(getAppPath() + "/apontamentos.xls");
                    HSSFWorkbook workbook = new HSSFWorkbook(file);
                    try {
                        Sheet sheet = workbook.getSheetAt(0);
                        Row lastRow = sheet.getRow(getLastRowNumber(sheet));

                        if (lastRow.getRowNum() > 0) {
                            if (lastRow.getCell(COMMENT) != null && lastRow.getCell(DH_INICIO) != null) {
                                textField1.setText(lastRow.getCell(COMMENT).getStringCellValue() + " | " + lastRow.getCell(DH_INICIO).getStringCellValue());
                            }
                        }
                    }
                    finally {
                        workbook.close();
                        file.close();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    onOK();
                } catch (Exception ex) {
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

    private static int getLastRowNumber(Sheet sheet) {
        int lastRowNumber = 0;
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                continue;
            }

            if (row.getCell(DH_INICIO) == null || row.getCell(DH_INICIO).getStringCellValue().isEmpty()) {
                break;
            }
            lastRowNumber++;
        }
        return lastRowNumber;
    }

    private void onOK() throws IOException, URISyntaxException, ParseException {

        Config config = new ConfigFacade().getConfiguration(getAppPath() + "/config.json");
        if (config == null) {
            return;
        }

        FileInputStream file = new FileInputStream(getAppPath() + "/apontamentos.xls");
        HSSFWorkbook workbook = new HSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        try {
            Row lastRow = sheet.getRow(getLastRowNumber(sheet));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date finalDate = txfInitialDate.getText().isEmpty() ? new Date() : new SimpleDateFormat("dd/MM/yyyy HH:mm").parse(txfInitialDate.getText());

            if (cbSaida.isSelected()) {
                if (lastRow.getRowNum() > 0) {
                    lastRow.createCell(DH_TERMINO).setCellValue(dateFormat.format(finalDate));
                }
            } else {
                int newRow = getLastRowNumber(sheet) + 1;
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
                row.createCell(COMMENT).setCellValue(txfDescription.getText());
            }

            FileOutputStream fileOut = new FileOutputStream(getAppPath() + "/apontamentos.xls", false);
            workbook.write(fileOut);

            resetForm();
        } finally {
            workbook.close();
            file.close();
            dispose();
        }
    }

    private void resetForm() {
        txfDescription.setText("");
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
        Font contentPaneFont = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, contentPane.getFont());
        if (contentPaneFont != null) contentPane.setFont(contentPaneFont);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        panel1.setBackground(new Color(-855310));
        panel1.setEnabled(false);
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
        panel2.setBackground(new Color(-855310));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        buttonOK = new JButton();
        buttonOK.setEnabled(true);
        Font buttonOKFont = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, buttonOK.getFont());
        if (buttonOKFont != null) buttonOK.setFont(buttonOKFont);
        buttonOK.setText("OK");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(buttonOK, gbc);
        buttonCancel = new JButton();
        Font buttonCancelFont = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, buttonCancel.getFont());
        if (buttonCancelFont != null) buttonCancel.setFont(buttonCancelFont);
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
        panel3.setBackground(new Color(-855310));
        panel3.setEnabled(true);
        Font panel3Font = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, panel3.getFont());
        if (panel3Font != null) panel3.setFont(panel3Font);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipadx = 50;
        gbc.insets = new Insets(10, 10, 10, 10);
        contentPane.add(panel3, gbc);
        txfDescription = new JTextField();
        Font txfDescriptionFont = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, txfDescription.getFont());
        if (txfDescriptionFont != null) txfDescription.setFont(txfDescriptionFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 0, 0);
        panel3.add(txfDescription, gbc);
        txfInitialDate = new JTextField();
        Font txfInitialDateFont = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, txfInitialDate.getFont());
        if (txfInitialDateFont != null) txfInitialDate.setFont(txfInitialDateFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 0, 0);
        panel3.add(txfInitialDate, gbc);
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Data/hora início (Dica: Use quando esqueceu de iniciar no horário certo)");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 0, 0);
        panel3.add(label1, gbc);
        final JLabel label2 = new JLabel();
        Font label2Font = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, label2.getFont());
        if (label2Font != null) label2.setFont(label2Font);
        label2.setText("Descrição");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label2, gbc);
        cbSaida = new JCheckBox();
        cbSaida.setBackground(new Color(-855310));
        Font cbSaidaFont = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, cbSaida.getFont());
        if (cbSaidaFont != null) cbSaida.setFont(cbSaidaFont);
        cbSaida.setText("Pausa/Encerramento");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 0, 0);
        panel3.add(cbSaida, gbc);
        final JLabel label3 = new JLabel();
        label3.setEnabled(false);
        Font label3Font = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setText("Último lançamento (Descrição e Data/Hora início)");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(30, 0, 0, 0);
        panel3.add(label3, gbc);
        textField1 = new JTextField();
        textField1.setEditable(false);
        textField1.setEnabled(true);
        Font textField1Font = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, textField1.getFont());
        if (textField1Font != null) textField1.setFont(textField1Font);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 0, 0);
        panel3.add(textField1, gbc);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
