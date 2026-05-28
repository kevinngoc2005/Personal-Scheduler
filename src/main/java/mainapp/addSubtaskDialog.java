package mainapp;

import java.awt.Color;
import javax.swing.BorderFactory;

public class addSubtaskDialog extends javax.swing.JFrame {

    private java.awt.Window caller;
    private int taskId;
    private Runnable onClose;

    public addSubtaskDialog(java.awt.Window caller, int taskId) {
        this(caller, taskId, null);
    }

    public addSubtaskDialog(java.awt.Window caller, int taskId, Runnable onClose) {
        this.caller = caller;
        this.taskId = taskId;
        this.onClose = onClose;
        caller.setEnabled(false);
        setUndecorated(true);
        initComponents();
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

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        descLabel = new javax.swing.JLabel();
        descTF = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        createButton = new javax.swing.JLabel();
        cancelButton = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mainPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        descLabel.setText("Subtask:");

        createButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        createButton.setText("Create");
        createButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        createButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                createButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                createButton.setBackground(Color.white);
                createButton.setForeground(Color.white);
                createButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                createButton.setBackground(new Color(153, 153, 153));
                createButton.setForeground(Color.black);
                createButton.setBorder(BorderFactory.createLineBorder(Color.black));
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
                cancelButton.setBackground(Color.white);
                cancelButton.setForeground(Color.white);
                cancelButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cancelButton.setBackground(new Color(153, 153, 153));
                cancelButton.setForeground(Color.black);
                cancelButton.setBorder(BorderFactory.createLineBorder(Color.black));
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(descLabel)
                        .addGap(10, 10, 10)
                        .addComponent(descTF, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(createButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(descLabel)
                    .addComponent(descTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
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
        String desc = descTF.getText().trim();

        if (desc.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Description cannot be empty.");
            return;
        }

        try {
            DBMethods.addSubtask(taskId, desc);
            javax.swing.JOptionPane.showMessageDialog(null, "Subtask created successfully!");
            dispose();
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(null, "Failed to create subtask: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cancelButton;
    private javax.swing.JLabel createButton;
    private javax.swing.JLabel descLabel;
    private javax.swing.JTextField descTF;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables
}
