# Elevator Simulation (Java)

A simple multi-elevator simulation with a Swing UI. It models multiple elevators serving floor requests and assigns each request to the “best” elevator based on an estimated time-to-arrival.

## What it does
- Spawns N elevators as independent threads.
- Each elevator:
  - Keeps two priority queues: one for upward stops (ascending order) and one for downward stops (descending order).
  - Moves one floor per second and prints/logs its current floor.
  - De-duplicates floor requests and ignores “already here” requests.
  - Sleeps when idle and wakes on new requests (wait/notifyAll).
- The controller:
  - Starts elevator threads and tracks them.
  - Chooses an elevator for a requested floor using a simple ETA heuristic.
  - Exposes a minimal Swing UI to enter floor requests and view elevator status (current floor and direction).

## Components
- Elevator.java
  - Runnable elevator thread with up/down queues, current floor, and direction.
  - Synchronized requestFloor(...) adds requests and notifies the thread.
  - estimateTravelTime(int) provides a snapshot ETA used by the controller.
- Direction.java
  - Enum: UP, DOWN, IDLE.
- ElevatorController.java
  - Owns elevator instances and their threads.
  - Public assignFloor(int) selects the elevator and forwards the request.
  - Main method builds a small Swing UI (text field + button + status labels).

## How to run (Windows, no build tools)
1) Open a terminal in the project folder:
   - cd C:\Programs\Java\Elevator\Elevator
2) Compile (Java 21+ recommended):
   - javac --release 21 *.java
3) Run the Swing UI:
   - java ElevatorController
4) Enter a floor number in the UI and click “Request”.

Notes:
- Close the window to exit. Threads are stopped by process termination.
- If you prefer terminal input instead of UI, you can adapt main() to loop on Scanner and call assignFloor(...) (already supported by the controller logic).

## Design notes
- Concurrency
  - requestFloor(...) is synchronized and calls notifyAll() to wake a sleeping elevator.
  - The elevator thread uses a while loop around wait() to guard against spurious wakeups.
  - currentFloor and direction are volatile for cross-thread visibility.
- Scheduling
  - Simple ETA heuristic; not globally optimal but fast and predictable.
- Robustness
  - Input validation rejects out-of-range floors.
  - Duplicate requests for the same floor are filtered.

## Known limitations / ideas
- No door/open/close timing or capacity modeling.
- No batching/express optimization; only a basic ETA heuristic.
- UI is minimal; consider per-elevator request buttons or visual car movement if desired.
