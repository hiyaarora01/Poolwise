# POOLWISE — Smart Carpool Matching & Route Optimization

POOLWISE is a Java Swing desktop application that demonstrates carpool matching, pickup-route optimization, file-based persistence, and estimated environmental impact.

## Requirements

- **JDK 21** (required by the current Maven compiler configuration and source code)
- Windows for the supplied `.bat` launch scripts
- Optional: Apache Maven 3.9+ if you prefer Maven commands

## Run on Windows

1. Install a JDK 21 distribution.
2. Make sure `java` and `javac` are available on PATH, or set `JAVA_HOME` to the JDK installation folder.
3. From the project root, double-click `compile.bat`.
4. After a successful build, double-click `run.bat`.

You can also run from PowerShell in the project root:

```powershell
.compile.bat
.un.bat
```

If Maven is installed:

```powershell
mvn clean package
java -jar target/poolwise-1.0.0.jar
```

The Maven build and batch scripts compile the active application in `src/main/java`. Do not compile the legacy source folders as part of the current application.

## Project structure

```
Poolwise/
├── pom.xml
├── compile.bat
├── run.bat
├── src/
│   ├── main/java/com/poolwise/
│   │   ├── Main.java
│   │   ├── algorithm/     Graph, DijkstraAlgorithm, GreedyMatcher
│   │   ├── data/          FileManager
│   │   ├── exception/     Application-specific exceptions
│   │   ├── model/         User, Driver, Passenger, Vehicle, Ride, Carpool
│   │   ├── service/       User, matching, route, and impact services
│   │   ├── ui/            Swing screens and panels
│   │   └── util/          Validation and sample-data loading
│   ├── poolwise/          Legacy source; not used by current Maven build
│   └── poolwise_backup/  Legacy backup; not used by current Maven build
└── data/                  CSV data created/used at runtime
```

## Current application modules

- Login / registration-style welcome screen
- Dashboard
- Driver and passenger management
- Carpool matching with a weighted compatibility score
- Map and route display based on a predefined sample road network
- Route calculation using Dijkstra's shortest-path algorithm
- Pickup ordering using exhaustive permutations for small groups and a nearest-neighbour heuristic for larger groups
- Environmental impact estimates
- Scenario simulation
- CSV-based saving and loading of drivers and passengers

## Matching and route algorithms

The matching engine uses a **greedy heuristic**. It assigns passengers in required-arrival-time order to the best currently compatible driver while respecting available seats. A greedy heuristic is efficient, but it does **not** guarantee a globally optimal assignment.

The route module uses Dijkstra's algorithm on a small, predefined Delhi-area graph. This is a teaching/demo network, not live map data or a navigation service. Locations entered by users that are not graph nodes cannot receive a true graph route; the application may use an estimate in that case.

## Environmental estimates

The impact calculator uses documented simplifying assumptions in code:

- Average fuel efficiency: 12 km/L
- Petrol combustion factor: 2.31 kg CO₂/L
- Fuel price assumption: ₹102/L

These figures are illustrative estimates, not measured savings. Actual fuel use and emissions depend on vehicle, route, traffic, occupancy, and driving conditions.

## Java concepts demonstrated

- Classes, objects, constructors, encapsulation
- Abstract class and inheritance (`User`, `Driver`, `Passenger`)
- Method overriding and polymorphism
- Interfaces and the Strategy pattern
- Association, aggregation, and composition
- Collections including `ArrayList`, `HashMap`, `HashSet`, and `PriorityQueue`
- Custom exceptions and input validation
- File handling with buffered readers/writers and CSV data
- Swing GUI and event handling
- Graphs, Dijkstra's algorithm, greedy matching, and route-order heuristics

## Important current limitations

- The welcome screen is currently a **demo entry screen**: the sign-in and registration buttons open the application but do not authenticate or persist user accounts.
- The route graph is predefined and does not query Google Maps or live traffic.
- Matching and environmental calculations are educational approximations and should be described as such in a project demonstration.
- The active source of truth is `src/main/java`; the older `src/poolwise` folders are retained for reference and are not part of the current Maven build.
