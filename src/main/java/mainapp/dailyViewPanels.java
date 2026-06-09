package mainapp;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.JTextArea;

public class dailyViewPanels extends JPanel {

    private int moduleId;
    private LocalDate currentDate;
    private JPanel contentPanel;
    private JLabel dateLabel;
    private java.awt.Window owner;
    private Runnable onNavigate;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public dailyViewPanels(java.awt.Window owner, int moduleId) {
        this(owner, moduleId, null);
    }

    public dailyViewPanels(java.awt.Window owner, int moduleId, Runnable onNavigate) {
        this.owner = owner;
        this.moduleId = moduleId;
        this.onNavigate = onNavigate;
        this.currentDate = LocalDate.now();
        setLayout(new BorderLayout());
        buildChrome();
        loadDay();
    }

    // Rebuilds the task list for currentDate. Call after navigating or adding tasks.
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
                    contentPanel.add(buildSubModuleHeader(sm[1], subModId));

                    List<String[]> tasks = DBMethods.getTasksOnDate(subModId, currentDate.format(FMT));
                    for (String[] task : tasks) {
                        int taskId = Integer.parseInt(task[0]);
                        contentPanel.add(buildTaskRow(task));

                        List<String[]> subtasks = DBMethods.getSubtasks(taskId);
                        for (String[] subtask : subtasks)
                            contentPanel.add(buildSubtaskRow(subtask));
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
                int choice = javax.swing.JOptionPane.showConfirmDialog(owner,
                    "Delete sub-module '" + label + "'?\nThis removes all its tasks and subtasks across all dates.",
                    "Confirm Delete", javax.swing.JOptionPane.YES_NO_OPTION);
                if (choice == javax.swing.JOptionPane.YES_OPTION) {
                    try { DBMethods.deleteSubModule(subModId); }
                    catch (Exception ex) { ex.printStackTrace(); }
                    loadDay();
                }
            }
        });

        java.awt.event.MouseAdapter openView = new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                new subModuleView(owner, subModId, currentDate.format(FMT), () -> loadDay()).setVisible(true);
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

    private void buildChrome() {
        // ── Top bar ──────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));

        final JLabel prevButton = new JLabel("< Prev");
        prevButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        prevButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        prevButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        prevButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                currentDate = currentDate.minusDays(1);
                loadDay();
                if (onNavigate != null) onNavigate.run();
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                prevButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.WHITE), BorderFactory.createEmptyBorder(3, 8, 3, 8)));
                prevButton.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                prevButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(3, 8, 3, 8)));
                prevButton.setForeground(Color.BLACK);
            }
        });

        dateLabel = new JLabel(currentDate.format(FMT));
        dateLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        final JLabel nextButton = new JLabel("Next >");
        nextButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        nextButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        nextButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nextButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                currentDate = currentDate.plusDays(1);
                loadDay();
                if (onNavigate != null) onNavigate.run();
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                nextButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.WHITE), BorderFactory.createEmptyBorder(3, 8, 3, 8)));
                nextButton.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                nextButton.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(3, 8, 3, 8)));
                nextButton.setForeground(Color.BLACK);
            }
        });

        navPanel.add(prevButton);
        navPanel.add(dateLabel);
        navPanel.add(nextButton);

        JPanel navWrapper = new JPanel(new GridBagLayout());
        navWrapper.add(navPanel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));

        JLabel addTaskButton = new JLabel("+ Task");
        addTaskButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        addTaskButton.setPreferredSize(new Dimension(65, 30));
        addTaskButton.setHorizontalAlignment(SwingConstants.CENTER);
        addTaskButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addTaskButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new addTaskDialog(owner, moduleId, currentDate.format(FMT), () -> loadDay()).setVisible(true);
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

        actionPanel.add(addTaskButton);

        JPanel navSpacer = new JPanel();
        navSpacer.setOpaque(false);
        navSpacer.setPreferredSize(new Dimension(actionPanel.getPreferredSize().width, 1));

        topBar.add(navSpacer, BorderLayout.WEST);
        topBar.add(navWrapper, BorderLayout.CENTER);
        topBar.add(actionPanel, BorderLayout.EAST);

        // ── Scrollable content ───────────────────────────────────────────────
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(topBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
}
