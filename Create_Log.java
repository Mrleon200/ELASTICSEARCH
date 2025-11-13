package com.mycompany.elasticsearch;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Create_Log {

    private static final int NUM_FILES = 3000;
    private static final int LINES_PER_FILE = 20000;
    private static final Path LOG_DIR = Paths.get("logs");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd_MM_yy");

    private static class WriterThread extends Thread {
        private final int startIndex;
        private final int endIndex;
        private final LocalDate startDate;

        WriterThread(int startIndex, int endIndex, LocalDate date, String name) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.startDate = date;
            setName(name);
        }

        @Override
        public void run() {
            Random rand = new Random(System.nanoTime() ^ getId());

            for (int i = startIndex; i < endIndex; i++) {
                LocalDate d = startDate.plusDays(i);
                String fileName = "log_" + d.format(FMT) + ".txt";
                Path file = LOG_DIR.resolve(fileName);

                try (BufferedWriter w = Files.newBufferedWriter(file)) {
                    for (int line = 1; line <= LINES_PER_FILE; line++) {
                        String log = generateLogLine(d, line, rand);
                        w.write(log);
                        w.newLine();
                    }
                } catch (IOException e) {
                    System.err.println("Error writing " + file + ": " + e.getMessage());
                }
            }
        }
    }

    private static String generateLogLine(LocalDate date, int line, Random rand) {
        int x = rand.nextInt(5000); 

        if (x == 0) {
            return "[%s] INFO user=99 action=login by 99 line=%d"
                    .formatted(date, line);
        }
        return "[%s] DEBUG event at line=%d val=%d"
                .formatted(date, line, x);
    }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(LOG_DIR);

        LocalDate start = LocalDate.of(2024, 1, 1);
        int CORES = Runtime.getRuntime().availableProcessors();
        int THREADS = Math.min(CORES, NUM_FILES);

        List<WriterThread> list = new ArrayList<>();
        int chunk = (NUM_FILES + THREADS - 1) / THREADS;

        for (int i = 0; i < THREADS; i++) {
            int s = i * chunk;
            int e = Math.min(s + chunk, NUM_FILES);
            if (s >= e) break;

            WriterThread t = new WriterThread(s, e, start, "Writer-" + i);
            list.add(t);
            t.start();
        }

        for (Thread t : list) t.join();

        System.out.println("Created 3000 log files in folder: " + LOG_DIR.toAbsolutePath());
    }
}

