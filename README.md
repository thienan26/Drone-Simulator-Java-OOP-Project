# Drone Simulation Interface — Java OOP Project

A Java Swing desktop application that connects to the Drone Simulation REST API,
fetches live drone data, and presents it through a three-tab graphical interface
with calculated metrics and paginated flight dynamics.

---

## Prerequisites

| Requirement | Version |
|---|---|
| JDK | 17 or higher |
| University VPN | Required to reach the API server |

No additional build tools (Maven, Gradle) are needed. The only external library is
`lib/json-20240303.jar` (already included).

---

## How to Run

### Option A — Batch script (Windows)

Double-click `run.bat` in the project root, or from a terminal:

```
run.bat
```

This compiles all sources into `bin/` and launches the application.

### Option B — Eclipse

1. File → Import → Existing Projects into Workspace → select this folder.
2. The `.classpath` and `.project` files configure the source root and the JSON library automatically.
3. Run `dronesim.app.DroneSimApp` as a Java Application.

### Option C — Command line (manual)

```bash
mkdir bin
javac --release 17 -cp "lib/json-20240303.jar" -d bin src/dronesim/**/*.java src/dronesim/app/*.java
java -cp "bin;lib/json-20240303.jar" dronesim.app.DroneSimApp
```

---

## First-Time Setup

On first launch, the **Settings** dialog opens automatically.

1. Enter the **Server URL**: `https://dronesim.facets-labs.com`
2. Enter your **Auth Token** (found on the dronesim website after login).
3. Click **Save & Connect**.

Settings are saved to `config.properties` in the working directory and reloaded on
the next startup. You can change them at any time via **File → Settings…**.

---

## Application Tabs

### Dashboard
- Displays all drones in a sortable table.
- Loads four calculated metrics per drone asynchronously.
- Risk level column is colour-coded: red (HIGH), orange (MEDIUM), green (LOW).
- **Refresh Now** button triggers an immediate data reload.
- **Auto-Refresh** toggle starts/stops a background thread that refreshes every 60 s.

### Drone Catalog
- Shows every drone model/type from `/api/dronetypes/` with full specifications:
  manufacturer, type name, weight, max speed, battery capacity, control range.
- Updated automatically whenever the dashboard refreshes.

### Flight Dynamics
- Select a drone from the dropdown, then click **Load / Refresh**.
- Displays 20 records per page: timestamp, speed, coordinates, battery, status, attitude.
- **← Previous** / **Next →** buttons navigate pages without fetching all data at once.
- Only the currently visible page is requested from the API.

---

## Calculated Metrics (Dashboard)

| Metric | Formula | Labels |
|---|---|---|
| **Battery Warning** | `(battery_status / battery_capacity) × 100` | LOW < 20 %, MEDIUM < 50 %, OK |
| **Speed Category** | `(speed / max_speed) × 100` | STANDING = 0, FAST > 80 %, NORMAL |
| **Connection State** | Minutes since `last_seen` | ONLINE ≤ 5 min, STALE otherwise |
| **Risk Level** | Composite of above three | HIGH (low battery or stale), MEDIUM (fast or medium battery), LOW |

---

## Project Structure

```
DroneSimulationProject/
├── src/dronesim/
│   ├── app/
│   │   └── DroneSimApp.java          Entry point, wires all layers
│   ├── api/
│   │   ├── ApiClient.java            Interface (OOP requirement)
│   │   ├── DroneSimApiClient.java    HTTP implementation (HTTPS + token auth)
│   │   └── PaginatedResult.java      Pagination envelope
│   ├── config/
│   │   └── AppConfig.java            Reads/writes config.properties (file I/O)
│   ├── controller/
│   │   └── DashboardController.java  Mediates GUI ↔ services, manages threads
│   ├── exception/
│   │   └── ApiException.java         Custom exception (OOP requirement)
│   ├── gui/
│   │   ├── MainFrame.java            Main window with three tabs
│   │   ├── SettingsDialog.java       Token + URL input dialog
│   │   ├── DashboardPanel.java       Live drone table with metrics
│   │   ├── CatalogPanel.java         Drone type catalog
│   │   ├── FlightDynamicsPanel.java  Paginated dynamics view
│   │   ├── DroneTableModel.java      Table model for dashboard
│   │   └── DynamicsTableModel.java   Table model for dynamics
│   ├── model/
│   │   ├── DroneType.java            Drone model/type data
│   │   ├── Drone.java                Individual drone data
│   │   ├── DroneDynamics.java        Time-varying flight data
│   │   └── DroneMetric.java          Four calculated metrics per drone
│   ├── service/
│   │   ├── DataService.java          Abstract base class with getAll() (OOP requirement)
│   │   ├── DroneTypeService.java     Fetches drone types
│   │   ├── DroneService.java         Fetches drones
│   │   ├── DroneDynamicsService.java Fetches dynamics with pagination support
│   │   └── MetricsService.java       Calculates the four dashboard metrics
│   └── util/
│       ├── AppLogger.java            Configures java.util.logging → dronesim.log
│       └── JsonParser.java           Thin wrapper around org.json
├── lib/
│   └── json-20240303.jar             JSON parsing library (org.json)
├── .classpath                        Eclipse source root + library config
├── .project                          Eclipse project descriptor
├── .vscode/settings.json             VSCode Java source root config
├── run.bat                           Windows build + run script
└── config.properties                 Created on first save (not in repo)
```

---

## OOP Requirements Fulfilled

| Requirement | Class / Location |
|---|---|
| Interface | `api.ApiClient` — `get()`, `fetchPage()` |
| Abstract method | `service.DataService<T>` — `abstract getAll()` |
| Collections | `List<Drone>`, `Map<Integer, DroneType>`, `Map<Integer, DroneMetric>` |
| Custom exception | `exception.ApiException` (with HTTP status code) |
| File I/O (streams) | `config.AppConfig` — `load()` reads, `save()` writes `config.properties` |
| Threads | `DashboardRefreshThread`, `AutoRefreshThread`, `MetricsLoaderThread`, `DynamicsPageThread` |
| Logging | `AppLogger` + all service/controller classes → `dronesim.log` |
| Pagination | `DroneDynamicsService.getPageForDrone()` + `FlightDynamicsPanel` |

---

## API Endpoints Used

| Endpoint | Purpose |
|---|---|
| `/api/dronetypes/?format=json` | Drone catalog (all pages) |
| `/api/drones/?format=json` | Individual drones (all pages) |
| `/api/dronedynamics/?format=json&drone=ID&limit=N&offset=M` | Paginated flight dynamics per drone |

Authentication: `Authorization: Token <your-token>` header on every request.

---

## Log File

All application events are written to `dronesim.log` in the working directory.
`System.out.println` is not used anywhere in the codebase.

---

## AI Usage Declaration

This project was scaffolded with assistance from Claude (Anthropic).
Per course policy, all AI usage is declared here. The prompts and the extent of
AI involvement must be described in the final project documentation.
Every team member is expected to understand and be able to explain all classes,
methods, and design decisions during the milestone and final presentations.
