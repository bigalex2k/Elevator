import java.util.*;
public class ElevatorController {
    private static int numElevators;
    private static ArrayList<Elevator> elevators = new ArrayList<>();
    private static ArrayList<Thread> elevatorThreads = new ArrayList<>();
    private static int numFloors;

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            // Ask for counts
            int eCount;
            int fCount;
            while (true) {
                String eStr = javax.swing.JOptionPane.showInputDialog(null, "Number of elevators:", "2");
                if (eStr == null) return; // cancel
                String fStr = javax.swing.JOptionPane.showInputDialog(null, "Number of floors:", "10");
                if (fStr == null) return;
                try {
                    eCount = Integer.parseInt(eStr.trim());
                    fCount = Integer.parseInt(fStr.trim());
                    if (eCount > 0 && fCount > 0) break;
                } catch (NumberFormatException ignore) {}
                javax.swing.JOptionPane.showMessageDialog(null, "Enter positive integers.");
            }

            // Start controller (spawns elevator threads)
            new ElevatorController(eCount, fCount);

            // Build UI
            javax.swing.JFrame frame = new javax.swing.JFrame("Elevator Controller");
            frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new java.awt.BorderLayout(8, 8));

            // Input row
            javax.swing.JPanel input = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
            input.add(new javax.swing.JLabel("Request floor (0.." + (numFloors - 1) + "):"));
            javax.swing.JTextField floorField = new javax.swing.JTextField(8);
            javax.swing.JButton requestBtn = new javax.swing.JButton("Request");
            input.add(floorField);
            input.add(requestBtn);
            frame.add(input, java.awt.BorderLayout.NORTH);

            // Elevator status labels
            java.util.List<javax.swing.JLabel> labels = new java.util.ArrayList<>();
            javax.swing.JPanel status = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 4, 4));
            for (int i = 0; i < eCount; i++) {
                javax.swing.JLabel lbl = new javax.swing.JLabel("Elevator " + (i + 1) + ": starting...");
                labels.add(lbl);
                status.add(lbl);
            }
            frame.add(new javax.swing.JScrollPane(status), java.awt.BorderLayout.CENTER);

            // Submit handler (button and Enter key)
            java.awt.event.ActionListener submit = ev -> {
                String txt = floorField.getText().trim();
                int floor;
                try {
                    floor = Integer.parseInt(txt);
                } catch (NumberFormatException ex) {
                    javax.swing.JOptionPane.showMessageDialog(frame, "Enter an integer floor.");
                    return;
                }
                if (floor < 0 || floor >= numFloors) {
                    javax.swing.JOptionPane.showMessageDialog(frame, "Invalid floor: " + floor);
                    return;
                }
                assignFloor(floor); // uses the existing controller logic
                floorField.setText("");
                floorField.requestFocusInWindow();
            };
            requestBtn.addActionListener(submit);
            floorField.addActionListener(submit);

            // Periodic status refresh
            new javax.swing.Timer(300, ev -> {
                for (int i = 0; i < labels.size() && i < elevators.size(); i++) {
                    Elevator e = elevators.get(i);
                    labels.get(i).setText(
                        String.format("Elevator %d — Floor: %d, Direction: %s",
                            i + 1, e.getFloor(), e.getDirection()));
                }
            }).start();

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public ElevatorController(int numElevators, int numFloors) {
        //Create the building with a certain number of elevators
        ElevatorController.numElevators = numElevators;
        ElevatorController.numFloors = numFloors;

        for (int i = 0; i < numElevators; i++) {
            Elevator e = new Elevator(i, numFloors);
            elevators.add(e);
            Thread t = new Thread(e);
            elevatorThreads.add(t);;
            t.start();
        }
    }

    /**
     * Find the optimal elevator to assign the floor to and request it.
     *
     * @param floor requested floor.
     */
    private static void assignFloor(int floor) {
        if (floor < 0 || floor >= numFloors) {
            System.out.println("Invalid Floor: " + floor);
            return;
        }

        //Keep track of the fastest time and closest Elevator
        Elevator closestElevator = null;
        int bestTime = Integer.MAX_VALUE;

        for (Elevator e: elevators) {
            int eta = e.estimateTravelTime(floor);
            if (eta < bestTime) {
                bestTime = eta;
                closestElevator = e;
            }
        }

        //Request the floor and then notify the elevator.

        if (closestElevator != null) {
            closestElevator.requestFloor(floor);
        }
    }


}
