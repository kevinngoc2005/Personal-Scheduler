package mainapp;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.JTextArea;

public class subModuleView extends javax.swing.JFrame {

    private int subModuleId;
    private String date;
    private JPanel contentPanel;
    private java.awt.Window caller;
    private Runnable onClose;

    public subModuleView(java.awt.Window caller, int subModuleId, String date) {
        this(caller, subModuleId, date, null);
    }

    public subModuleView(java.awt.Window caller, int subModuleId, String date, Runnable onClose) {
        this.caller = caller;
        this.subModuleId = subModuleId;
        this.date = date;
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
        loadSubModule();
    }

    @Override
    public void dispose() {
        if (caller != null) caller.setEnabled(true);
        super.dispose();
        if (onClose != null) onClose.run();
    }

    // Clears and rebuilds the task + subtask list for this sub-module on the given date.
    public void loadSubModule() {
        contentPanel.removeAll();

        try {
            List<String[]> tasks = DBMethods.getTasksOnDate(subModuleId, date);

            if (tasks.isEmpty()) {
                JLabel empty = new JLabel("  No tasks for this sub-module on this day.");
                empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(empty);
            } else {
                for (String[] task : tasks) {
                    int taskId = Integer.parseInt(task[0]);
                    contentPanel.add(buildTaskRow(task));

                    List<String[]> subtasks = DBMethods.getSubtasks(taskId);
                    for (String[] subtask : subtasks) {
                        contentPanel.add(buildSubtaskRow(subtask));
                    }

                    contentPanel.add(Box.createVerticalStrut(6));
                }
            }
        } catch (Exception ex) {
            JLabel err = new JLabel("  Failed to load: " + ex.getMessage());
            contentPanel.add(err);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel buildTaskRow(String[] task) {
        int taskId = Integer.parseInt(task[0]);
        boolean completed = task[2].equals("completed");

        JPanel panel = new JPanel(new BorderLayout(6, 4)) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        JTextArea ta = makeTextArea(task[1], false, !completed);
        ta.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    e.consume();
                    try { DBMethods.updateTaskDescription(taskId, ta.getText().trim()); }
                    catch (Exception ex) { ex.printStackTrace(); }
                    ta.transferFocus();
                }
            }
        });

        JCheckBox cb = new JCheckBox("", completed);
        cb.addActionListener(e -> {
            boolean done = cb.isSelected();
            try { DBMethods.updateTaskStatus(taskId, done ? "completed" : "pending"); }
            catch (Exception ex) { ex.printStackTrace(); }
            ta.setEditable(!done);
            ta.setEnabled(!done);
        });

        JButton addSubtaskBtn = new JButton("+");
        addSubtaskBtn.setMargin(new Insets(0, 4, 0, 4));
        addSubtaskBtn.setFont(addSubtaskBtn.getFont().deriveFont(10f));
        addSubtaskBtn.setToolTipText("Add subtask");
        addSubtaskBtn.addActionListener(e ->
            new addSubtaskDialog(subModuleView.this, taskId, () -> loadSubModule()).setVisible(true));


        JLabel delTaskBtn = makeDeleteBtn();
        delTaskBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                try { DBMethods.deleteTask(taskId); }
                catch (Exception ex) { ex.printStackTrace(); }
                loadSubModule();
            }
        });

        JPanel cbWrapper = new JPanel(new GridBagLayout());
        cbWrapper.add(cb);

        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        eastPanel.setOpaque(false);
        eastPanel.add(addSubtaskBtn);
        eastPanel.add(delTaskBtn);

        panel.add(cbWrapper, BorderLayout.WEST);
        panel.add(ta, BorderLayout.CENTER);
        panel.add(eastPanel, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildSubtaskRow(String[] subtask) {
        int subtaskId = Integer.parseInt(subtask[0]);
        boolean completed = subtask[2].equals("completed");

        JPanel panel = new JPanel(new BorderLayout(6, 4)) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 32, 2, 8));

        JTextArea ta = makeTextArea(subtask[1], true, !completed);
        ta.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    e.consume();
                    try { DBMethods.updateSubtaskDescription(subtaskId, ta.getText().trim()); }
                    catch (Exception ex) { ex.printStackTrace(); }
                    ta.transferFocus();
                }
            }
        });

        JCheckBox cb = new JCheckBox("", completed);
        cb.setFont(cb.getFont().deriveFont(11f));
        cb.addActionListener(e -> {
            boolean done = cb.isSelected();
            try { DBMethods.updateSubtaskStatus(subtaskId, done ? "completed" : "pending"); }
            catch (Exception ex) { ex.printStackTrace(); }
            ta.setEditable(!done);
            ta.setEnabled(!done);
        });

        JLabel delBtn = makeDeleteBtn();
        delBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                try { DBMethods.deleteSubtask(subtaskId); }
                catch (Exception ex) { ex.printStackTrace(); }
                loadSubModule();
            }
        });

        JPanel cbWrapper = new JPanel(new GridBagLayout());
        cbWrapper.add(cb);

        panel.add(cbWrapper, BorderLayout.WEST);
        panel.add(ta, BorderLayout.CENTER);
        panel.add(delBtn, BorderLayout.EAST);
        return panel;
    }

    private JLabel makeDeleteBtn() {
        JLabel del = new JLabel("×");
        del.setForeground(new Color(200, 60, 60));
        del.setFont(del.getFont().deriveFont(Font.BOLD, 13f));
        del.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        del.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 4));
        return del;
    }

    private JTextArea makeTextArea(String content, boolean small, boolean editable) {
        JTextArea ta = new JTextArea(content);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setEditable(editable);
        ta.setEnabled(editable);
        ta.setOpaque(false);
        ta.setBorder(null);
        if (small) ta.setFont(ta.getFont().deriveFont(11f));
        return ta;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        addTaskButton = new javax.swing.JLabel();
        closeButton = new javax.swing.JLabel();

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane();
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        scrollPane.setViewportView(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        mainPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        mainPanel.setLayout(new java.awt.BorderLayout());

        // Fetch sub-module label for the title
        String label = "Sub-Module";
        try {
            String fetched = DBMethods.getSubModuleLabelById(subModuleId);
            if (fetched != null) label = fetched;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        titleLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        titleLabel.setText("  " + label + "  —  " + date);

        // "Add Task" button opens addTaskDialog, then refreshes the list
        addTaskButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        addTaskButton.setText("+ Task");
        addTaskButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        addTaskButton.setPreferredSize(new java.awt.Dimension(65, 30));
        addTaskButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addTaskButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try { DBMethods.addTask(subModuleId, "", date); }
                catch (Exception ex) { ex.printStackTrace(); }
                loadSubModule();
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addTaskButton.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.WHITE));
                addTaskButton.setForeground(java.awt.Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addTaskButton.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK));
                addTaskButton.setForeground(java.awt.Color.BLACK);
            }
        });

        closeButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        closeButton.setText("Close");
        closeButton.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        closeButton.setPreferredSize(new java.awt.Dimension(60, 30));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dispose();
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.WHITE));
                closeButton.setForeground(java.awt.Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK));
                closeButton.setForeground(java.awt.Color.BLACK);
            }
        });

        javax.swing.JPanel topBar = new javax.swing.JPanel(new java.awt.BorderLayout());
        topBar.add(titleLabel, java.awt.BorderLayout.WEST);

        javax.swing.JPanel topButtons = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        topButtons.add(addTaskButton);
        topButtons.add(closeButton);
        topBar.add(topButtons, java.awt.BorderLayout.EAST);

        mainPanel.add(topBar, java.awt.BorderLayout.NORTH);
        mainPanel.add(scrollPane, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addTaskButton;
    private javax.swing.JLabel closeButton;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
}
