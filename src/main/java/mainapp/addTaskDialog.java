package mainapp;

import javax.swing.text.MaskFormatter;
import javax.swing.text.DefaultFormatterFactory;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.awt.Color;
import javax.swing.BorderFactory;

public class addTaskDialog extends javax.swing.JFrame {

    private java.awt.Window caller;
    private int subModuleId = -1;
    private int moduleId = -1;
    private String storedDate = null;
    private boolean hideDateField = false;
    private Runnable onClose;

    // Opened from subModuleView — sub-module and date already known.
    public addTaskDialog(java.awt.Window caller, int subModuleId, String date, boolean autoDate) {
        this.caller = caller;
        this.subModuleId = subModuleId;
        this.storedDate = date;
        this.hideDateField = true;
        setup(null);
    }

    // Opened from dayModel — sub-module picker shown, due date pre-filled.
    public addTaskDialog(java.awt.Window caller, int moduleId, String prefilledDate) {
        this(caller, moduleId, prefilledDate, null);
    }

    public addTaskDialog(java.awt.Window caller, int moduleId, String prefilledDate, Runnable onClose) {
        this.caller = caller;
        this.moduleId = moduleId;
        this.onClose = onClose;
        setup(prefilledDate);
    }

    private void setup(String prefilledDate) {
        caller.setEnabled(false);
        setUndecorated(true);
        initComponents();
        setupDateField();

        if (prefilledDate != null) {
            dueDateFTF.setText(prefilledDate);
        }

        if (hideDateField) {
            dueDateFTF.setVisible(false);
            dueDateLabel.setVisible(false);
        }

        // Populate combo box if opened from dayModel
        if (moduleId != -1) {
            subModCBLabel.setVisible(true);
            subModCB.setVisible(true);
            subModCB.addItem("");
            try {
                java.util.List<String> labels = DBMethods.getSubModuleLabels(moduleId);
                for (String label : labels) subModCB.addItem(label);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            subModCB.setSelectedIndex(0);
        } else {
            subModCBLabel.setVisible(false);
            subModCB.setVisible(false);
        }

        setLocationRelativeTo(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                toFront();
                requestFocus();
            }
        });
    }

    @Override
    public void dispose() {
        if (caller != null) caller.setEnabled(true);
        super.dispose();
        if (onClose != null) onClose.run();
    }

    private void setupDateField() {
        try {
            MaskFormatter mask = new MaskFormatter("##/##/####");
            mask.setPlaceholderCharacter('_');
            dueDateFTF.setFormatterFactory(new DefaultFormatterFactory(mask));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private boolean isDateValid() {
        String text = dueDateFTF.getText().replace("_", "").trim();
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            LocalDate entered = LocalDate.parse(text, fmt);
            return !entered.isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        subModCBLabel = new javax.swing.JLabel();
        subModCB = new javax.swing.JComboBox<>();
        dueDateLabel = new javax.swing.JLabel();
        dueDateFTF = new javax.swing.JFormattedTextField();
        jSeparator1 = new javax.swing.JSeparator();
        createButton = new javax.swing.JLabel();
        cancelButton = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        subModCBLabel.setText("Sub-Module:");
        dueDateLabel.setText("Due Date:");

        dueDateFTF.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        dueDateFTF.setText("__/__/____");

        createButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        createButton.setText("Create");
        createButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        createButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                createButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                createButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
                createButton.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                createButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                createButton.setForeground(Color.BLACK);
            }
        });

        cancelButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cancelButton.setText("Cancel");
        cancelButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dispose();
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cancelButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
                cancelButton.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cancelButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cancelButton.setForeground(Color.BLACK);
            }
        });

        // Inner content width: label(80) + gap(10) + field(150) = 240.
        // Button group: 60+30+60 = 150; each side margin = (240-150)/2 = 45.
        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(subModCBLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(subModCB, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(dueDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(dueDateFTF, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addComponent(createButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(subModCBLabel)
                    .addComponent(subModCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dueDateLabel)
                    .addComponent(dueDateFTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void createButtonMouseClicked(java.awt.event.MouseEvent evt) {
        String dueDate = hideDateField ? storedDate : dueDateFTF.getText().trim();

        if (!hideDateField && !isDateValid()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Due date must be valid and not before today.");
            return;
        }

        if (moduleId != -1) {
            String selected = (String) subModCB.getSelectedItem();
            if (selected == null || selected.isBlank()) {
                javax.swing.JOptionPane.showMessageDialog(null, "Please select a sub-module.");
                return;
            }
            try {
                subModuleId = DBMethods.getSubModuleIdByName(selected);
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(null, "Failed to find sub-module: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            DBMethods.addTask(subModuleId, "", dueDate);
            dispose();
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(null, "Failed to create task: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cancelButton;
    private javax.swing.JLabel createButton;
    private javax.swing.JFormattedTextField dueDateFTF;
    private javax.swing.JLabel dueDateLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel subModCBLabel;
    private javax.swing.JComboBox<String> subModCB;
    // End of variables declaration//GEN-END:variables
}
