package mainapp;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.List;
import javax.swing.*;

public class weeklyViewPanel extends JPanel {

    private int moduleId;
    private LocalDate weekStart;
    private JLabel weekLabel;
    private JPanel daysPanel;
    private java.awt.Window owner;
    private Runnable onNavigate;

    private static final DateTimeFormatter FMT      = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter SHORT_FMT = DateTimeFormatter.ofPattern("M/d");
    private static final DateTimeFormatter LONG_FMT  = DateTimeFormatter.ofPattern("M/d, yyyy");
    private static final String[] DAY_ABBR = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    public weeklyViewPanel(java.awt.Window owner, int moduleId) {
        this(owner, moduleId, null);
    }

    public weeklyViewPanel(java.awt.Window owner, int moduleId, Runnable onNavigate) {
        this.owner = owner;
        this.moduleId = moduleId;
        this.onNavigate = onNavigate;
        this.weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        setLayout(new BorderLayout());
        buildChrome();
        loadWeek();
    }

    // Rebuilds all 7 rows for the current week. Call after navigating.
    public void loadWeek() {
        weekLabel.setText(weekStart.format(SHORT_FMT) + " – " + weekStart.plusDays(6).format(LONG_FMT));
        daysPanel.removeAll();
        for (int i = 0; i < 7; i++)
            daysPanel.add(buildDayRow(weekStart.plusDays(i)));
        daysPanel.revalidate();
        daysPanel.repaint();
    }

    private JPanel buildDayRow(LocalDate date) {
        boolean isToday = date.equals(LocalDate.now());
        String dateStr = date.format(FMT);

        // getMinimumSize prevents BoxLayout from squeezing rows when the scroll pane
        // constrains total panel height. 65px floor ensures the header always shows fully.
        JPanel row = new JPanel(new BorderLayout(0, 0)) {
            @Override public Dimension getMinimumSize() {
                Dimension d = getPreferredSize();
                return new Dimension(d.width, Math.max(d.height, 65));
            }
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(d.width, Math.max(d.height, 65));
            }
        };
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Left header (fixed width) ─────────────────────────────────────────
        JPanel header = new JPanel(new GridBagLayout());
        header.setPreferredSize(new Dimension(115, 0));
        header.setMinimumSize(new Dimension(115, 0));
        header.setBackground(isToday ? new Color(0, 100, 210) : new Color(60, 60, 60));
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel headerInner = new JPanel();
        headerInner.setLayout(new BoxLayout(headerInner, BoxLayout.Y_AXIS));
        headerInner.setOpaque(false);

        // DayOfWeek.getValue(): Mon=1…Sun=7; % 7 gives Sun=0 to match DAY_ABBR index
        JLabel dayName = new JLabel(DAY_ABBR[date.getDayOfWeek().getValue() % 7]);
        dayName.setForeground(Color.WHITE);
        dayName.setFont(dayName.getFont().deriveFont(Font.BOLD, 11f));
        dayName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel dayDate = new JLabel(date.format(SHORT_FMT));
        dayDate.setForeground(Color.WHITE);
        dayDate.setFont(dayDate.getFont().deriveFont(13f));
        dayDate.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerInner.add(dayName);
        headerInner.add(Box.createVerticalStrut(2));
        headerInner.add(dayDate);
        header.add(headerInner);

        header.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                header.setBackground(isToday ? new Color(30, 120, 230) : new Color(90, 90, 90));
            }
            @Override public void mouseExited(MouseEvent e) {
                header.setBackground(isToday ? new Color(0, 100, 210) : new Color(60, 60, 60));
            }
            @Override public void mouseClicked(MouseEvent e) {
                dayModel dm = new dayModel(owner, moduleId, dateStr, () -> loadWeek());
                dm.setVisible(true);
            }
        });

        // ── Task content (expands with content) ───────────────────────────────
        JPanel taskContent = new JPanel();
        taskContent.setLayout(new BoxLayout(taskContent, BoxLayout.Y_AXIS));
        taskContent.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 8));

        try {
            List<String[]> tasks = DBMethods.getAllTasksOnDate(moduleId, dateStr);
            if (tasks.isEmpty()) {
                JLabel empty = new JLabel("No tasks");
                empty.setForeground(new Color(190, 190, 190));
                empty.setFont(empty.getFont().deriveFont(Font.ITALIC, 11f));
                empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                taskContent.add(empty);
            } else {
                String lastLabel = null;
                for (String[] task : tasks) {
                    String smLabel = task[3];
                    boolean done   = "completed".equals(task[2]);

                    if (!smLabel.equals(lastLabel)) {
                        if (lastLabel != null) taskContent.add(Box.createVerticalStrut(3));
                        JLabel smLbl = new JLabel(smLabel);
                        smLbl.setFont(smLbl.getFont().deriveFont(Font.BOLD | Font.ITALIC, 10f));
                        smLbl.setForeground(new Color(90, 90, 90));
                        smLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                        taskContent.add(smLbl);
                        lastLabel = smLabel;
                    }

                    JLabel taskLbl = new JLabel("• " + task[1]);
                    taskLbl.setFont(taskLbl.getFont().deriveFont(11f));
                    if (done) taskLbl.setForeground(new Color(170, 170, 170));
                    taskLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                    taskContent.add(taskLbl);

                    try {
                        for (String[] sub : DBMethods.getSubtasks(Integer.parseInt(task[0]))) {
                            boolean subDone = "completed".equals(sub[2]);
                            JLabel subLbl = new JLabel("    ◦ " + sub[1]);
                            subLbl.setFont(subLbl.getFont().deriveFont(10f));
                            subLbl.setForeground(subDone ? new Color(180, 180, 180) : new Color(80, 80, 80));
                            subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                            taskContent.add(subLbl);
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ex) {
            JLabel err = new JLabel("Error");
            err.setForeground(Color.RED);
            taskContent.add(err);
        }

        row.add(header, BorderLayout.WEST);
        row.add(taskContent, BorderLayout.CENTER);
        return row;
    }

    private void buildChrome() {
        JLabel prev = makeNavBtn("< Prev");
        prev.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                weekStart = weekStart.minusWeeks(1);
                loadWeek();
                if (onNavigate != null) onNavigate.run();
            }
        });

        weekLabel = new JLabel("", SwingConstants.CENTER);
        weekLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JLabel next = makeNavBtn("Next >");
        next.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                weekStart = weekStart.plusWeeks(1);
                loadWeek();
                if (onNavigate != null) onNavigate.run();
            }
        });

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        nav.add(prev);
        nav.add(weekLabel);
        nav.add(next);
        nav.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        add(nav, BorderLayout.NORTH);

        daysPanel = new JPanel();
        daysPanel.setLayout(new BoxLayout(daysPanel, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(daysPanel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scroll, BorderLayout.CENTER);
    }

    private JLabel makeNavBtn(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.WHITE),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)));
                lbl.setForeground(Color.WHITE);
            }
            @Override public void mouseExited(MouseEvent e) {
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)));
                lbl.setForeground(Color.BLACK);
            }
        });
        return lbl;
    }
}
