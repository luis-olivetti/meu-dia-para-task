package org.meudia.ui;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.meudia.config.JsonConfigLoader;
import org.meudia.domain.Config;
import org.meudia.domain.MapTask;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Function;

import static org.meudia.commons.constants.SpreadsheetConstants.*;
import static org.meudia.commons.helper.AppPathHelper.getAppPath;

public class Timekeeping extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField txfDescription;
    private JTextField txfInitialDate;
    private JTextArea txfLastData;
    private JTextField txfJiraCode;
    private JCheckBox cbSaida;
    private JLabel lblTip;
    private static String appPath;

    public static void main(String[] args) {
        displayFormDialog();

        System.exit(0);
    }

    public Timekeeping() {
        appPath = getAppPath();

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent componentEvent) {
                super.componentShown(componentEvent);
                initializeInitialDate();

                try {
                    Config config = loadConfiguration();
                    ensureConfigIsLoaded(config);

                    defineDefaultCode(config.getDefaultCode());
                    defineTip(config.getTip());

                    try (HSSFWorkbook workbook = loadWorkbook()) {
                        displayLatestEntries(workbook);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        buttonOK.addActionListener(e -> {
            try {
                onOK();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private static void displayFormDialog() {
        Timekeeping userFormDialog = new Timekeeping();
        userFormDialog.pack();
        userFormDialog.setVisible(true);
    }

    private void initializeInitialDate() {
        txfInitialDate.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date().getTime()));
    }

    private void defineDefaultCode(String value) {
        txfJiraCode.setText(value);
    }

    private void defineTip(String value) {
        lblTip.setText(value);
    }

    private Config loadConfiguration() {
        return new JsonConfigLoader().getConfiguration(appPath + "/config.json");
    }

    private void ensureConfigIsLoaded(Config config) {
        if (config == null) {
            throw new RuntimeException("O sistema não foi inicializado, pois não encontrou o arquivo 'config.json'. Verifique!");
        }
    }

    private HSSFWorkbook loadWorkbook() throws IOException {
        FileInputStream file = new FileInputStream(appPath + "/apontamentos.xls");
        return new HSSFWorkbook(file);
    }

    private void displayLatestEntries(HSSFWorkbook workbook) {
        Sheet sheet = workbook.getSheetAt(0);
        Row lastRow = sheet.getRow(getLastRowNumber(sheet));
        Row penultimateRow = null;

        if (lastRow != null && lastRow.getRowNum() > 0) {
            penultimateRow = sheet.getRow(lastRow.getRowNum() - 1);
        }

        clearLastData();
        updateLastDataWithRowInfo(lastRow, txfLastData);
        updateLastDataWithRowInfo(penultimateRow, txfLastData);
    }

    private void updateLastDataWithRowInfo(Row row, JTextArea lastData) {
        if (row == null) {
            return;
        }

        if (row.getCell(COMMENT) != null && row.getCell(DH_INICIO) != null) {
            String text = lastData.getText()
                    + "\n"
                    + row.getCell(DH_INICIO).getStringCellValue()
                    + " | "
                    + row.getCell(COMMENT).getStringCellValue();

            if (text.startsWith("\n")) {
                text = text.substring(1);
            }

            lastData.setText(text);
        }
    }

    private void clearLastData() {
        txfLastData.setText("");
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

    private void onOK() throws IOException, ParseException {

        Config config = new JsonConfigLoader().getConfiguration(appPath + "/config.json");
        if (config == null) {
            return;
        }

        String originalFilePath = appPath + "/apontamentos.xls";
        String backupFilePath = appPath + "/apontamentos_backup.xls";
        Files.copy(Paths.get(originalFilePath), Paths.get(backupFilePath), StandardCopyOption.REPLACE_EXISTING);

        try (
                FileInputStream file = new FileInputStream(originalFilePath);
                HSSFWorkbook workbook = new HSSFWorkbook(file)
        ) {
            Sheet sheet = workbook.getSheetAt(0);

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

                row.createCell(CD_PROJETO).setCellValue(config.getProjectCode());

                String searchKey = txfJiraCode.getText().trim().toUpperCase();

                Long taskCode = getTaskAttribute(searchKey, MapTask::getTaskCode, config);
                if (taskCode != null) {
                    row.createCell(CD_TAREFA).setCellValue(taskCode);
                }

                Long taskTypeCode = getTaskAttribute(searchKey, MapTask::getTaskTypeCode, config);
                if (taskTypeCode != null) {
                    row.createCell(CD_TIPOTAREFA).setCellValue(taskTypeCode);
                }

                row.createCell(CD_RESPONSAVEL).setCellValue(config.getUsername());

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

                row.createCell(EMPTY_COLUMN).setCellValue(1);
                row.createCell(CD_EQUIPE).setCellValue(config.getTeamCode());

                String jiraCode = txfJiraCode.getText().trim().toUpperCase();
                String comment = formatComment(jiraCode, txfDescription.getText().trim().toUpperCase());

                row.createCell(COMMENT).setCellValue(comment);
                row.createCell(CD_JIRA).setCellValue(jiraCode);
            }

            try (FileOutputStream fileOut = new FileOutputStream(originalFilePath, false)) {
                workbook.write(fileOut);
            }

            resetForm();
        } finally {
            dispose();
        }
    }

    private Long getTaskAttribute(String key, Function<MapTask, Object> attributeGetter, Config config) {
        return config.getTasksMap().stream()
            .filter(task -> key.equals(task.getKey()))
            .findFirst()
            .map(task -> {
                Object attributeValue = attributeGetter.apply(task);
                if (attributeValue != null) {
                    return Long.valueOf(attributeValue.toString());
                }
                return null;
            })
            .orElse(null);
    }

    private void resetForm() {
        txfDescription.setText("");
        cbSaida.setSelected(false);
    }

    private void onCancel() {
        dispose();
    }

    private String formatComment(String jiraCode, String description) {
        return "[" +
                jiraCode.trim().toUpperCase() +
                "] - " +
                description.toUpperCase();
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
        contentPane.setMaximumSize(new Dimension(800, 500));
        contentPane.setMinimumSize(new Dimension(800, 500));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        panel1.setBackground(new Color(-855310));
        panel1.setEnabled(false);
        panel1.setOpaque(false);
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
        panel2.setOpaque(false);
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
        buttonOK.setText("Salvar");
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
        buttonCancel.setText("Cancelar");
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
        panel3.setOpaque(false);
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
        gbc.gridwidth = 2;
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
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 0, 0);
        panel3.add(txfInitialDate, gbc);
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setText("Data/hora início");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
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
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label2, gbc);
        cbSaida = new JCheckBox();
        cbSaida.setActionCommand("Pausa/Encerramento  ");
        cbSaida.setBackground(new Color(-855310));
        cbSaida.setContentAreaFilled(false);
        Font cbSaidaFont = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, cbSaida.getFont());
        if (cbSaidaFont != null) cbSaida.setFont(cbSaidaFont);
        cbSaida.setOpaque(false);
        cbSaida.setText("Pausa/Encerramento");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 0, 0);
        panel3.add(cbSaida, gbc);
        final JLabel label3 = new JLabel();
        label3.setEnabled(true);
        Font label3Font = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, label3.getFont());
        if (label3Font != null) label3.setFont(label3Font);
        label3.setText("Os dois últimos lançamentos");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(30, 0, 0, 0);
        panel3.add(label3, gbc);
        txfLastData = new JTextArea();
        txfLastData.setBackground(new Color(-778));
        txfLastData.setColumns(0);
        txfLastData.setEditable(false);
        txfLastData.setEnabled(true);
        Font txfLastDataFont = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, txfLastData.getFont());
        if (txfLastDataFont != null) txfLastData.setFont(txfLastDataFont);
        txfLastData.setOpaque(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 0, 0);
        panel3.add(txfLastData, gbc);
        final JLabel label4 = new JLabel();
        Font label4Font = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, label4.getFont());
        if (label4Font != null) label4.setFont(label4Font);
        label4.setText("Código (Jira)");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(label4, gbc);
        txfJiraCode = new JTextField();
        txfJiraCode.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(txfJiraCode, gbc);
        lblTip = new JLabel();
        lblTip.setEnabled(true);
        Font lblTipFont = this.$$$getFont$$$("DejaVu Sans Mono", -1, -1, lblTip.getFont());
        if (lblTipFont != null) lblTip.setFont(lblTipFont);
        lblTip.setForeground(new Color(-16777216));
        lblTip.setText("Defina um dica no arquivo de configurações");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 5, 0, 0);
        panel3.add(lblTip, gbc);
        label1.setLabelFor(txfInitialDate);
        label2.setLabelFor(txfDescription);
        label3.setLabelFor(txfLastData);
        label4.setLabelFor(txfJiraCode);
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
