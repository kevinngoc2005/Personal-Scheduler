package mainapp;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class monthlyViewPanel extends JPanel {

    private int moduleId;
    private YearMonth currentMonth;
    private JLabel monthLabel;
    private JPanel gridPanel;
    private java.awt.Window owner;
    private Runnable onNavigate;

    private static final DateTimeFormatter FMT   = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TITLE = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final String[] DAY_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    public monthlyViewPanel(java.awt.Window owner, int moduleId) {
        this(owner, moduleId, null);
    }

    public monthlyViewPanel(java.awt.Window owner, int moduleId, Runnable onNavigate) {
        this.owner = owner;
        this.moduleId = moduleId;
        this.onNavigate = onNavigate;
        this.currentMonth = YearMonth.now();
        setLayout(new BorderLayout());
        buildChrome();
        loadMonth();
    }

    // Rebuilds the grid for currentMonth. Call after navigating or after tasks change.
    public void loadMonth() {
        monthLabel.setText(currentMonth.format(TITLE));
        gridPanel.removeAll();

        Map<String, List<String>> dayLabels = fetchDayLabels();

        // DayOfWeek.getValue(): Mon=1…Sun=7.  % 7 maps Sun→0, Mon→1…Sat→6 (Sunday-first grid).
        int startOffset = currentMonth.atDay(1).getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();

        for (int i = 0; i < startOffset; i++)
            gridPanel.add(makeEmptyCell());

        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = currentMonth.atDay(d);
            String dateStr = date.format(FMT);
            gridPanel.add(makeDayCell(date, dayLabels.getOrDefault(dateStr, Collections.emptyList())));
        }

        // Pad to 42 cells (6 rows × 7 cols) so the grid is always the same height
        int filled = startOffset + daysInMonth;
        for (int i = filled; i < 42; i++)
            gridPanel.add(makeEmptyCell());

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private Map<String, List<String>> fetchDayLabels() {
        Map<String, List<String>> map = new LinkedHashMap<>();
        try {
            String start = currentMonth.atDay(1).format(FMT);
            String end   = currentMonth.atEndOfMonth().format(FMT);
            for (String[] row : DBMethods.getSubModuleLabelsInRange(moduleId, start, end))
                map.computeIfAbsent(row[0], k -> new ArrayList<>()).add(row[1]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return map;
    }

    private void buildChrome() {
        // Navigation bar
        JLabel prev = makeNavBtn("< Prev");
        prev.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                currentMonth = currentMonth.minusMonths(1);
                loadMonth();
                if (onNavigate != null) onNavigate.run();
            }
        });

        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel next = makeNavBtn("Next >");
        next.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                currentMonth = currentMonth.plusMonths(1);
                loadMonth();
                if (onNavigate != null) onNavigate.run();
            }
        });

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        nav.add(prev);
        nav.add(monthLabel);
        nav.add(next);

        // Day-of-week header row
        JPanel headers = new JPanel(new GridLayout(1, 7, 0, 0));
        for (String name : DAY_NAMES) {
            JLabel lbl = new JLabel(name, SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
            lbl.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.LIGHT_GRAY));
            lbl.setOpaque(true);
            lbl.setBackground(new Color(240, 240, 240));
            headers.add(lbl);
        }

        JPanel top = new JPanel(new BorderLayout());
        top.add(nav, BorderLayout.NORTH);
        top.add(headers, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // Day grid — fixed 6 rows so height never jumps between months
        gridPanel = new JPanel(new GridLayout(6, 7, 0, 0));
        add(gridPanel, BorderLayout.CENTER);
    }

    private JPanel makeDayCell(LocalDate date, List<String> subModLabels) {
        boolean isToday = date.equals(LocalDate.now());
        String dateStr = date.format(FMT);

        JPanel cell = new JPanel(new BorderLayout(0, 1));
        cell.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Day number — solid blue background badge when today, plain otherwise
        JLabel numLbl = new JLabel(String.valueOf(date.getDayOfMonth()));
        numLbl.setFont(numLbl.getFont().deriveFont(Font.BOLD, 12f));
        if (isToday) {
            numLbl.setOpaque(true);
            numLbl.setBackground(new Color(70, 130, 210));
            numLbl.setForeground(Color.WHITE);
            numLbl.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        } else {
            numLbl.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        }
        JPanel numRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        numRow.setOpaque(false);
        numRow.add(numLbl);
        cell.add(numRow, BorderLayout.NORTH);

        // Sub-module labels in plain black text, capped at 3 with overflow indicator
        if (!subModLabels.isEmpty()) {
            JPanel labelsPanel = new JPanel();
            labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.Y_AXIS));
            labelsPanel.setOpaque(false);
            labelsPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 2, 2));

            boolean overflow = subModLabels.size() > 3;
            int shown = overflow ? 2 : subModLabels.size();
            for (int i = 0; i < shown; i++) {
                String text = subModLabels.get(i);
                if (text.length() > 11) text = text.substring(0, 10) + "…";
                JLabel lbl = new JLabel(text);
                lbl.setFont(lbl.getFont().deriveFont(9f));
                lbl.setForeground(Color.BLACK);
                lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                labelsPanel.add(lbl);
            }
            if (overflow) {
                JLabel dots = new JLabel("...");
                dots.setFont(dots.getFont().deriveFont(9f));
                dots.setForeground(Color.GRAY);
                dots.setAlignmentX(Component.LEFT_ALIGNMENT);
                labelsPanel.add(dots);
            }
            cell.add(labelsPanel, BorderLayout.CENTER);
        }

        cell.addMouseListener(new MouseAdapter() {
            private Color saved;
            @Override public void mouseEntered(MouseEvent e) {
                saved = cell.getBackground();
                cell.setBackground(new Color(220, 232, 255));
            }
            @Override public void mouseExited(MouseEvent e) {
                cell.setBackground(saved);
            }
            @Override public void mouseClicked(MouseEvent e) {
                dayModel dm = new dayModel(owner, moduleId, dateStr, () -> loadMonth());
                dm.setVisible(true);
            }
        });

        return cell;
    }

    private JPanel makeEmptyCell() {
        JPanel cell = new JPanel();
        cell.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
        cell.setBackground(new Color(250, 250, 250));
        return cell;
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
