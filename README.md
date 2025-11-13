# 🌟 Parallel Log Generator & Log Search System

### **High-performance Java Multithreading + Swing GUI Application**

<p align="center">
  <img src="https://img.shields.io/badge/Java-22-orange?style=flat-square">
  <img src="https://img.shields.io/badge/Multithreading-Enabled-green?style=flat-square">
  <img src="https://img.shields.io/badge/Swing-GUI-blue?style=flat-square">
  <img src="https://img.shields.io/badge/License-MIT-purple?style=flat-square">
</p>

A full-featured Java application for generating massive log datasets and performing fast parallel log searching with a modern desktop GUI.
Originally built for a **Parallel Algorithms** university assignment — now expanded into a professional tool.

---

# 📖 Table of Contents

* [About the Project](#about-the-project)
* [Key Features](#key-features)
* [Architecture](#architecture)
* [Screenshots](#screenshots)
* [Installation](#installation)
* [How to Use](#how-to-use)
* [Folder Structure](#folder-structure)
* [Technologies Used](#technologies-used)
* [Performance](#performance)
* [License](#license)
* [Tags / Keywords](#tags--keywords)

---

# 📌 About the Project

Modern systems generate millions of log entries daily. Searching through thousands of log files can be extremely slow if done sequentially.
This project solves the problem using:

* **Multithreaded log generation**
* **Parallel log searching engine**
* **Optimized pattern matching**
* **Desktop-level GUI application with table-based results**
* **Right-click → Open file**, **double-click to open**, **export to file**, etc.

This makes it not only suitable as a school assignment — but also a powerful tool for real-world daily use.

---

# 🚀 Key Features

### 📝 **1. Log Generator (3000 logs × 20,000 lines each)**

* Creates massive log datasets instantly
* Thread-optimized file creation
* Realistic synthetic log entries
* Timestamp-based file names:

  ```
  log_dd_MM_yy.txt
  ```

---

### 🔍 **2. Parallel Log Search Engine**

* Fully uses all CPU cores
* Efficient load balancing
* Thread-safe data collection
* Whole-word search (regex)
* Case-sensitive search mode
* Millions of lines processed in seconds
* Auto-export to `ketqua.txt`

---

### 🖥️ **3. Clean & Modern Swing GUI**

* Directory picker
* Keyword history (combobox)
* Options:

  * Match whole word only
  * Match case
* Results displayed in a table:

| File | Line | Content |
| ---- | ---- | ------- |

With:
✔ Hidden full path column
✔ Sortable columns
✔ Right-click → Open file
✔ Double-click to open file
✔ Status bar
✔ Pop-up summary when search finishes

---

# 🧩 Architecture

```text
Project Root
│
├── log-generator
│   └── Create_log.java     # Program 1 — Generate 3000 log files
│
├── log-search
│   ├── Search_log.java # Program 2 — Parallel search GUI
│   ├── Table renderer + popup menu
│   └── Utilities
│
├── logs/               # Auto-generated log files
└── ketqua.txt          # Output of the search engine
```

### High-Level System Diagram

```text
               ┌──────────────┐
               │  Create_log.java │
               │ (Generator)   │
               └──────┬───────┘
                      │
              Generates 3000 logs
                      │
                      ▼
        ┌────────────────────────────┐
        │        Search_log GUI      │
        └──────┬──────────┬─────────┘
               │          │
               │     User Input
               │          │
               ▼          ▼
       ┌────────────┐  ┌─────────────┐
       │ File Walker │  │ Keyword     │
       │ (Files.walk)│  │ Options     │
       └──────┬──────┘  └─────┬──────┘
              │               │
      Split file list across threads
              │
              ▼
  ┌───────────────────────────┐
  │ Parallel Search Workers   │  (N = CPU cores)
  └───────┬─────────┬────────┘
          │         │
    Each worker scans slice of files
          │
          ▼
  Thread-safe queue collects results
          │
          ▼
 ┌────────────────────────────────┐
 │ GUI Table + Export → ketqua.txt│
 └────────────────────────────────┘
```

---

# 📸 Screenshots

*(You can upload your images to `/images/` folder on GitHub and update paths below.)*

### 🔍 Search Interface

```
<img width="1106" height="639" alt="image" src="https://github.com/user-attachments/assets/91bdc2af-bb78-4e24-8f8d-bdd8a9ac21a0" />

```

### 📁 Log File Example

```
<img width="1166" height="643" alt="image" src="https://github.com/user-attachments/assets/d7b04217-8169-40fb-a7b9-83440a302fc3" />

```

### 📊 Parallel Result Table

```
<img width="1107" height="637" alt="image" src="https://github.com/user-attachments/assets/3f71727e-ac65-4013-92fc-ecdc2f4a7c6a" />

```
```
<img width="1658" height="783" alt="image" src="https://github.com/user-attachments/assets/87e2d758-939c-4271-8642-8ee24a09f2ac" />

```
---

# 📥 Installation

### **1. Clone the repository**

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
cd YOUR_REPO_NAME
```

### **2. Open project in NetBeans (or IntelliJ / Eclipse)**

* Use JDK 17 / JDK 22 (tested OK)
* Maven auto-imports dependencies

### **3. Build & Run**

* Program 1 (generate logs) → run `Create_log.java`
* Program 2 (GUI search) → run `Search_log.java`

---

# 🧭 How to Use

## **Step 1 — Generate logs**

Run:

```
Create_log.java
```

It creates:

```
logs/log_dd_MM_yy.txt (3000 files)
each containing 20,000 random entries
```

---

## **Step 2 — Search logs**

1. Open `Search_log.java`
2. Select directory (`logs/`)
3. Enter keyword (example: `login by 99`)
4. Choose options:

   * Match whole word
   * Match case
5. Click **Find All**

### Result Table:

* File name
* Line number
* Full log content line
* Hidden column: full file path

### Extra:

✔ Double-click row → open file
✔ Right-click → Open file
✔ Results exported to `ketqua.txt` automatically
✔ Summary dialog appears when search completes

---

# 🛠 Technologies Used

| Technology                       | Purpose                    |
| -------------------------------- | -------------------------- |
| **Java (JDK 22)**                | Core language              |
| **Java Swing**                   | GUI                        |
| **Multithreading / Concurrency** | Parallel searching         |
| **ConcurrentLinkedQueue**        | Thread-safe result storage |
| **Pattern / Matcher**            | Regex whole-word search    |
| **Files.walk**                   | Directory traversal        |
| **BufferedReader**               | Fast file scanning         |

---

# ⚡ Performance (Benchmark)

| Test Size                          | CPU       | Time         | Results         |
| ---------------------------------- | --------- | ------------ | --------------- |
| 3000 files × 20k lines (60M lines) | Ryzen 7   | 8–12 seconds | ~12,000 matches |
| 1200 files × 10k lines             | i5-12400F | ~4 seconds   | ~5,000 matches  |

Optimizations included:

* Balanced chunk splitting
* Zero-copy string matching (`indexOf` instead of `contains`)
* Shared compiled regex
* Minimal memory footprint

---

# 📂 Folder Structure

```text
YOUR_REPO/
│
├── README.md
├── pom.xml
│
├── logs/                # generated by Create_log.java
├── ketqua.txt           # output file
│
└── src/
    └── main/
        ├── java/
        │   └── com/mycompany/elasticsearch/
        │       ├── Create_log.java
        │       ├── Search_log.java
        │       └── ...
        └── resources/
```

---

# 📄 License

This project is licensed under the **MIT License** — feel free to use, modify, contribute, or integrate into your own systems.

---

# 🏷 Tags / Keywords

```
Java, Multithreading, Parallel Search, Log Analyzer, Log Parser, Swing GUI,
Concurrent Programming, Pattern Matching, Regex Search, Desktop Application,
Thread-safe, Java Concurrency, File Search Tool, Performance Optimization,
Parallel Algorithms Assignment, NetBeans Project
```

---

