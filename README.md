# 🚗 POOLWISE — Smart Carpool Matching & Route Optimization System

> *"We're not building another carpooling app. We're building the algorithm that decides what the best carpool should look like."*

---

## 📁 Project Structure

```
Poolwise/
├── compile.bat              ← Double-click to compile
├── run.bat                  ← Double-click to run
├── README.md                ← This file
├── src/
│   └── poolwise/
│       ├── Location.java            Model: GPS coordinate + Haversine distance
│       ├── User.java                Abstract base: id, name, reliability
│       ├── Driver.java              Extends User: seats, detour, departure time
│       ├── Passenger.java           Extends User: arrival time, preferences
│       ├── OptimizationMode.java    Enum: 5 optimization strategies
│       ├── CompatibilityScore.java  Multi-factor scoring engine
│       ├── CarpoolGroup.java        Driver + passengers + route
│       ├── CarpoolPlan.java         Full plan with environmental impact
│       ├── CompatibilityMatrix.java All driver×passenger scores
│       ├── RouteOptimizer.java      Nearest-neighbor pickup ordering
│       ├── CarpoolOptimizer.java    Main optimization engine (5 modes)
│       ├── ScenarioSimulator.java   Dynamic scenario testing
│       ├── DataManager.java         File I/O (save/load drivers, passengers)
│       └── PoolwiseApp.java         Main application (console menu)
├── bin/                     ← Compiled .class files (created by compile.bat)
└── data/                    ← Saved data files (created at runtime)
```

---

## 🚀 How to Run

### Step 1 — Install Java JDK
Download from: https://www.oracle.com/java/technologies/downloads/
- Choose **JDK 11** or higher
- During install, check "Add to PATH"

### Step 2 — Compile
Double-click **`compile.bat`**

### Step 3 — Run
Double-click **`run.bat`**

---

## 🎮 Main Menu Options

| Option | Description |
|--------|-------------|
| 1 | View all drivers and passengers |
| 2 | Add a new driver (interactive) |
| 3 | Add a new passenger (interactive) |
| 4 | View the full Compatibility Matrix |
| 5 | **Run Optimization** — generates carpool plan |
| 6 | Change optimization mode |
| 7 | **Run 3 Dynamic Scenarios** |
| 8 | **Compare all 5 optimization modes** |
| 9 | Save data to files |
| 10 | Load data from files |
| 0 | Exit |

> The app auto-loads a **sample dataset** (4 drivers, 6 passengers, Delhi area) on startup.

---

## ⚙️ Optimization Modes

| Mode | Goal |
|------|------|
| 🚗 **MIN_CARS** | Use fewest possible vehicles |
| ⛽ **MIN_DISTANCE** | Minimize total extra km travelled |
| ⏱️ **MIN_TIME** | Fastest arrival for all passengers |
| 🌱 **MIN_CO2** | Most environmentally efficient grouping |
| ⚖️ **BALANCED** | Compromise across all factors |

---

## 🧮 How the Algorithm Works

### 1. Compatibility Scoring
Every (driver, passenger) pair gets a score from **0.0 to 1.0**:

| Factor | Weight | How Calculated |
|--------|--------|----------------|
| Route Overlap | 25% | Cosine similarity of travel direction vectors |
| Pickup Distance | 25% | `max(0, 1 - dist/5km)` |
| Detour | 20% | `max(0, 1 - extraKm/maxDetour)` |
| Time Compatibility | 20% | Arrival time vs required arrival at 60 km/h |
| Reliability | 5% | Average of driver + passenger reliability |
| Preferences | 5% | Penalty per passenger preference constraint |

### 2. Greedy Matching
- Builds a `CompatibilityMatrix` of all scores
- Sorts pairs by score (or mode-specific comparator)
- Greedily assigns passengers to drivers, respecting capacity

### 3. Route Optimization
- Uses **nearest-neighbor greedy** algorithm for pickup order
- Driver → closest unvisited passenger → next closest → Destination
- Calculates actual extra distance for each group

### 4. Environmental Impact
- **CO₂**: 0.21 kg per km per car avoided
- **Fuel**: 0.08 L per km per car avoided

---

## 🧪 Dynamic Scenarios (Option 7)

| Scenario | What Changes |
|----------|-------------|
| **Normal Day** | All drivers available → baseline plan |
| **Driver Cancels** | First driver set unavailable → auto-recalculated |
| **Reduced Detour** | Max detour reduced to 2 km → new plan |

---

## 📚 Java Concepts Used

| Concept | Where Used |
|---------|-----------|
| **OOP / Classes** | All 14 classes |
| **Inheritance** | `Driver`, `Passenger` extend `User` |
| **Abstract Class** | `User` is abstract |
| **Enum** | `OptimizationMode` |
| **Polymorphism** | `getRole()` overridden in Driver/Passenger |
| **Collections** | `List<>`, `ArrayList<>`, `Set<>`, `HashSet<>` |
| **File Handling** | `DataManager` — `BufferedReader`, `PrintWriter` |
| **Exception Handling** | try/catch in DataManager, PoolwiseApp |
| **Interfaces** (implicit) | Comparator, streams |
| **Algorithms** | Haversine formula, greedy matching, nearest-neighbor |

---

## 📊 Sample Output

```
╔══════════════════════════════════════════════════╗
║           POOLWISE — CARPOOL PLAN                ║
║  Optimization Mode: Balanced Mode                ║
╚══════════════════════════════════════════════════╝

┌─────────────────────────────────────────┐
│  CARPOOL GROUP: CP-1
│  Driver:    Rahul (3 seats)
│  Passengers (3):
│    • Amit
│    • Vikram
│    • Nisha
│  Extra Distance: 2.40 km
│  CO2 Saved: 4.12 kg
│  Fuel Saved: 1.60 L
│  Route: Rahul Home → P1 Home → P3 Home → P6 Home → College
└─────────────────────────────────────────┘

════════════ ENVIRONMENTAL IMPACT SUMMARY ════════════
  Cars Avoided         : 6
  Estimated CO2 Saved  : 8.24 kg
  Estimated Fuel Saved : 3.20 L
═══════════════════════════════════════════════════════
```
