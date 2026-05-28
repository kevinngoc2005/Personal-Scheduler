package mainapp;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.JTextArea;

public class dayModel extends javax.swing.JFrame {

    private int moduleId;
    private LocalDate currentDate;
    private JPanel contentPanel;
    private JLabel dateLabel;
    private java.awt.Window caller;
    private Runnable onClose;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public dayModel(java.awt.Window caller, int moduleId, String date) {
        this(caller, moduleId, date, null);
    }

    public dayModel(java.awt.Window caller, int moduleId, String date, Runnable onClose) {
        this.caller = caller;
        this.moduleId = moduleId;
        this.currentDate = LocalDate.parse(date, FMT);
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
        loadDay();
    }

    @Override
    public void dispose() {
        if (caller != null) caller.setEnabled(true);
        super.dispose();
        if (onClose != null) onClose.run();
    }

    // Clears and rebuilds the task list for the current date.
    public void loadDay() {
        dateLabel.setText(currentDate.format(FMT));
        contentPanel.removeAll();

        try {
            List<String[]> subModules = DBMethods.getSubModulesWithTasksOnDate(moduleId, currentDate.format(FMT));

            if (subModules.isEmpty()) {
                JLabel empty = new JLabel("  No tasks scheduled for this day.");
                empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(empty);
            } else {
                for (String[] sm : subModules) {
                    int subModId = Integer.parseInt(sm[0]);
                    String label = sm[1];

                    contentPanel.add(buildSubModuleHeader(label, subModId));

                    List<String[]> tasks = DBMethods.getTasksOnDate(subModId, currentDate.format(FMT));
                    for (String[] task : tasks) {
                        int taskId = Integer.parseInt(task[0]);
                        contentPanel.add(buildTaskRow(task));

                        List<String[]> subtasks = DBMethods.getSubtasks(taskId);
                        for (String[] subtask : subtasks) {
                            contentPanel.add(buildSubtaskRow(subtask));
                        }
                    }
                    contentPanel.add(Box.createVerticalStrut(8));
                }
            }
        } catch (Exception ex) {
            JLabel err = new JLabel("  Failed to load: " + ex.getMessage());
            contentPanel.add(err);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Sub-module header — left side opens subModuleView, right side is a delete button.
    private JPanel buildSubModuleHeader(String label, int subModId) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        panel.setBackground(new Color(60, 60, 60));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 4));

        JLabel lbl = new JLabel(label);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13f));
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel delBtn = makeDeleteBtn();
        delBtn.setForeground(new Color(255, 150, 150));
        delBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                int choice = JOptionPane.showConfirmDialog(dayModel.this,
                    "Delete sub-module '" + label + "'?\nThis removes all its tasks and subtasks across all dates.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    try { DBMethods.deleteSubModule(subModId); }
                    catch (Exception ex) { ex.printStackTrace(); }
                    loadDay();
                }
            }
        });

        java.awt.event.MouseAdapter openView = new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                new subModuleView(dayModel.this, subModId, currentDate.format(FMT), () -> loadDay()).setVisible(true);
            }
        };
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(openView);
        lbl.addMouseListener(openView);

        panel.add(lbl, BorderLayout.WEST);
        panel.add(delBtn, BorderLayout.EAST);
        return panel;
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

        JLabel delBtn = makeDeleteBtn();
        delBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                try { DBMethods.deleteTask(taskId); }
                catch (Exception ex) { ex.printStackTrace(); }
                loadDay();
            }
        });

        JPanel cbWrapper = new JPanel(new GridBagLayout());
        cbWrapper.add(cb);

        panel.add(cbWrapper, BorderLayout.WEST);
        panel.add(ta, BorderLayout.CENTER);
        panel.add(delBtn, BorderLayout.EAST);
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
                loadDay();
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

        // ── Top bar ──────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());

        // Prev / date label / Next — centered in the top bar
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

        JLabel prevButton = new JLabel("< Prev");
        prevButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        prevButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        prevButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                currentDate = currentDate.minusDays(1);
                loadDay();
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                prevButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
                prevButton.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                prevButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                prevButton.setForeground(Color.BLACK);
            }
        });

        dateLabel = new JLabel(currentDate.format(FMT));
        dateLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel nextButton = new JLabel("Next >");
        nextButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        nextButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                currentDate = currentDate.plusDays(1);
                loadDay();
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                nextButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
                nextButton.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                nextButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                nextButton.setForeground(Color.BLACK);
            }
        });

        navPanel.add(prevButton);
        navPanel.add(dateLabel);
        navPanel.add(nextButton);

        // Add Task / Close on the right side
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));

        JLabel addTaskButton = new JLabel("+ Task");
        addTaskButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        addTaskButton.setPreferredSize(new Dimension(65, 30));
        addTaskButton.setHorizontalAlignment(SwingConstants.CENTER);
        addTaskButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addTaskButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new addTaskDialog(dayModel.this, moduleId, currentDate.format(FMT), () -> loadDay()).setVisible(true);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                addTaskButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
                addTaskButton.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                addTaskButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                addTaskButton.setForeground(Color.BLACK);
            }
        });

        JLabel closeButton = new JLabel("Close");
        closeButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        closeButton.setPreferredSize(new Dimension(60, 30));
        closeButton.setHorizontalAlignment(SwingConstants.CENTER);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dispose();
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setBorder(BorderFactory.createLineBorder(Color.WHITE));
                closeButton.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                closeButton.setForeground(Color.BLACK);
            }
        });

        actionPanel.add(addTaskButton);
        actionPanel.add(closeButton);

        // Mirror the action panel's width on the left so navPanel (CENTER) is truly centered.
        JPanel navSpacer = new JPanel();
        navSpacer.setOpaque(false);
        navSpacer.setPreferredSize(new java.awt.Dimension(actionPanel.getPreferredSize().width, 1));

        topBar.add(navSpacer, BorderLayout.WEST);
        topBar.add(navPanel, BorderLayout.CENTER);
        topBar.add(actionPanel, BorderLayout.EAST);

        // ── Scrollable content ───────────────────────────────────────────────
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        mainPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables
}
