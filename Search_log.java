package com.mycompany.elasticsearch;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Search_log extends JFrame {

    private JComboBox<String> cbFind;
    private JComboBox<String> cbDir;
    private JButton btnBrowse, btnFindAll;
    private JCheckBox chkWholeWord, chkMatchCase;

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblStatus;

    private JFileChooser chooser;
    private JPopupMenu popupMenu;

    // ================== CONSTRUCTOR ==================
    public Search_log() {
        setTitle("Find in Files");
        setSize(900, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        buildUI();
    }

    // ================== UI BUILD ==================
    private void buildUI() {

        // --- Row: Find what ---
        JPanel panelFind = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbFind = new JComboBox<>();
        cbFind.setEditable(true);
        cbFind.setPreferredSize(new Dimension(400, 25));
        btnFindAll = new JButton("Find All");
        panelFind.add(new JLabel("Find what:"));
        panelFind.add(cbFind);
        panelFind.add(btnFindAll);

        // --- Row: Directory ---
        JPanel panelDir = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbDir = new JComboBox<>();
        cbDir.setEditable(true);
        cbDir.setPreferredSize(new Dimension(400, 25));
        btnBrowse = new JButton("Browse...");
        panelDir.add(new JLabel("Directory :"));
        panelDir.add(cbDir);
        panelDir.add(btnBrowse);

        // --- Row: Options ---
        JPanel panelCheck = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chkWholeWord = new JCheckBox("Match whole word only");
        chkMatchCase = new JCheckBox("Match case");
        panelCheck.add(chkWholeWord);
        panelCheck.add(chkMatchCase);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(panelFind);
        top.add(panelDir);
        top.add(panelCheck);

        // --- Result table ---
        JLabel lbl = new JLabel("Kết quả", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // Model có thêm cột "Path" để chứa full path (sẽ ẩn)
        tableModel = new DefaultTableModel(
                new Object[]{"File", "Line", "Content", "Path"}, 0
        );
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);

        // Ẩn cột Path trên giao diện
        table.getColumnModel().getColumn(3).setMinWidth(0);
        table.getColumnModel().getColumn(3).setMaxWidth(0);
        table.getColumnModel().getColumn(3).setPreferredWidth(0);

        // Popup menu chuột phải
        popupMenu = new JPopupMenu();
        JMenuItem miOpen = new JMenuItem("Open file");
        popupMenu.add(miOpen);
        miOpen.addActionListener(e -> openSelectedFile());

        // Mouse listener: double-click mở file, right-click hiện menu
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openSelectedFile();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopupIfNeeded(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopupIfNeeded(e);
            }

            private void showPopupIfNeeded(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        table.setRowSelectionInterval(row, row);
                    }
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);

        JPanel panelResult = new JPanel(new BorderLayout());
        panelResult.setBorder(new TitledBorder(""));
        panelResult.add(lbl, BorderLayout.NORTH);
        panelResult.add(scroll, BorderLayout.CENTER);

        // Status bar
        lblStatus = new JLabel("Ready.");
        lblStatus.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        add(top, BorderLayout.NORTH);
        add(panelResult, BorderLayout.CENTER);
        add(lblStatus, BorderLayout.SOUTH);

        // Events cho button
        btnBrowse.addActionListener(e -> chooseFolder());
        btnFindAll.addActionListener(e -> startSearch());
    }

    // ================== FILE OPEN ==================
    private void openSelectedFile() {
        try {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) return;

            int modelRow = table.convertRowIndexToModel(viewRow);
            String fullPath = (String) tableModel.getValueAt(modelRow, 3); // cột Path

            if (fullPath == null || fullPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy đường dẫn file.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Desktop.getDesktop().open(new File(fullPath));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể mở file:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================== EVENTS ==================
    private void chooseFolder() {
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            cbDir.setSelectedItem(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void startSearch() {
        String keyword = cbFind.getEditor().getItem().toString().trim();
        String folder  = cbDir.getEditor().getItem().toString().trim();

        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a keyword!");
            return;
        }
        if (folder.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a directory!");
            return;
        }

        cbFind.insertItemAt(keyword, 0);
        cbDir.insertItemAt(folder, 0);

        clearTable();
        setStatus("Searching...");

        Thread worker = new Thread(() -> {
            try {
                runSearch(Paths.get(folder), keyword);
            } catch (Exception ex) {
                setStatus("Error: " + ex.getMessage());
            }
        });
        worker.start();
    }

    // ================== CORE SEARCH ==================
    private void runSearch(Path directory, String keyword) throws IOException {

        // Lấy danh sách file
        List<Path> files = new ArrayList<>();
        try (Stream<Path> st = Files.walk(directory)) {
            for (Path p : (Iterable<Path>) st::iterator) {
                if (Files.isRegularFile(p)) {
                    files.add(p);
                }
            }
        }

        setStatus("Total files: " + files.size());
        if (files.isEmpty()) return;

        int cores   = Runtime.getRuntime().availableProcessors();
        int threadsCount = Math.min(cores, files.size());
        setStatus("Total files: " + files.size() + " | Using " + threadsCount + " threads...");

        ConcurrentLinkedQueue<String> results = new ConcurrentLinkedQueue<>();

        boolean matchCase = chkMatchCase.isSelected();
        boolean wholeWord = chkWholeWord.isSelected();

        final Pattern sharedPattern;
        if (wholeWord) {
            int flags = matchCase ? 0 : Pattern.CASE_INSENSITIVE;
            sharedPattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", flags);
        } else {
            sharedPattern = null;
        }

        String keywordLower = matchCase ? null : keyword.toLowerCase();

        int chunk = (files.size() + threadsCount - 1) / threadsCount;
        Thread[] threads = new Thread[threadsCount];

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadsCount; i++) {
            final int id = i;
            final int s  = i * chunk;
            final int e  = Math.min(s + chunk, files.size());
            if (s >= e) break;

            final List<Path> slice = files.subList(s, e);

            threads[i] = new Thread(() -> {
                for (Path f : slice) {
                    searchFile(f, keyword, keywordLower, matchCase, wholeWord, sharedPattern, results);
                }
                System.out.println("Thread-" + id + " done.");
            });

            threads[i].start();
        }

        for (Thread t : threads) {
            if (t != null) {
                try { t.join(); } catch (InterruptedException ignored) {}
            }
        }

        long endTime = System.currentTimeMillis();
        long ms = endTime - startTime;

        // Ghi ra ketqua.txt + popup kết quả
        Path out = Paths.get("ketqua.txt");
        try (BufferedWriter wr = Files.newBufferedWriter(out)) {
            if (results.isEmpty()) {
                wr.write("No results for keyword: " + keyword);
                setStatus("No result found. Time: " + ms + " ms");

                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(
                                Search_log.this,
                                "Không tìm thấy kết quả nào cho:\n\"" + keyword + "\"\nThời gian: " + ms + " ms",
                                "Search finished",
                                JOptionPane.INFORMATION_MESSAGE
                        )
                );
            } else {
                for (String r : results) {
                    wr.write(r);
                    wr.newLine();
                }
                int count = results.size();
                setStatus("Found " + count + " result(s). Time: " + ms + " ms. Saved to ketqua.txt");

                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(
                                Search_log.this,
                                "Đã tìm thấy " + count + " Result.\n"
                                        + "Contained in: Result.txt\n"
                                        + "Time: " + ms + " ms\n\n"
                                        + "Tip: Double-click or Right-click → Open file to open log.",
                                "Search finished",
                                JOptionPane.INFORMATION_MESSAGE
                        )
                );
            }
        }
    }

    // ================== SEARCH 1 FILE ==================
    private void searchFile(Path file,
                            String key,
                            String keyLower,
                            boolean matchCase,
                            boolean wholeWord,
                            Pattern pattern,
                            ConcurrentLinkedQueue<String> out) {

        try (BufferedReader rd = Files.newBufferedReader(file)) {

            String line;
            int num = 0;

            while ((line = rd.readLine()) != null) {
                num++;

                boolean matched;

                if (wholeWord) {
                    if (pattern != null) {
                        Matcher m = pattern.matcher(line);
                        matched = m.find();
                    } else {
                        matched = false;
                    }
                } else if (matchCase) {
                    matched = line.indexOf(key) >= 0;
                } else {
                    matched = line.toLowerCase().indexOf(keyLower) >= 0;
                }

                if (matched) {
                    String resultLine = file.getFileName() + " | " + num + " | " + line;
                    out.add(resultLine);

                    // thêm vào bảng, kèm full path (cột 4 – ẩn)
                    addResultRow(
                            file.getFileName().toString(),
                            num,
                            line,
                            file.toAbsolutePath().toString()
                    );
                }
            }
        } catch (IOException ignored) {
        }
    }

    // ================== UI HELPERS ==================
    private void clearTable() {
        SwingUtilities.invokeLater(() -> tableModel.setRowCount(0));
    }

    private void addResultRow(String fileName, int line, String content, String path) {
        SwingUtilities.invokeLater(() ->
                tableModel.addRow(new Object[]{fileName, line, content, path})
        );
    }

    private void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> lblStatus.setText(msg));
    }

    // ================== MAIN ==================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Search_log().setVisible(true));
    }
}
