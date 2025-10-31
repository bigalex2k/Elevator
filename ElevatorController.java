import java.util.*;
public class ElevatorController {
    private static int numElevators;
    private static ArrayList<Elevator> elevators;
    private static int numFloors;

    public static void main(String[] args) {

        Scanner s = new Scanner(System.in);
        int numElevators;
        int numFloor;

        System.out.println("Please enter a number of elevators to have in this simulation");
        numElevators = s.nextInt();

        System.out.println("Please enter the number of floors for these elevators");
        numFloor = s.nextInt();

        while (true) {
            String[] temp;
            System.out.println("Please enter a floor(s) to add to the queue (1 - numFloor)");
            temp = s.nextLine().split(" ");

            //If the first number is -1, exit program
            if (Integer.parseInt(temp[0]) == -1) {
                break;
            }

            //For each input number, assign it to an elevator
            for (String floor: temp) {
                assignFloor(Integer.parseInt(floor));
            }
        }
        System.out.println("Program Complete!");
    }

    public ElevatorController(int numElevators, int numFloors) {
        //Create the building with a certain number of elevators
        ElevatorController.numElevators = numElevators;
        ElevatorController.numFloors = numFloors;
        elevators = new ArrayList<>();

        for (int i = 1; i <= numFloors; i++) {
            elevators.add(new Elevator(i, numFloors));
        }
    }

    /**
     * Find the optimal elevator to assign the floor to and request it.
     *
     * @param floor requested floor.
     */
    private static void assignFloor(int floor) {
        //Keep track of the fastest time and closest Elevator
        Elevator closestElevator = null;

        //Iterate through each elevator to find the one that gets to
        //the assigned floor the fastest.
        for (int i = 0; i < elevators.size(); i++) {
            if (elevators.get(i).estimateTravelTime(floor) < closestElevator.estimateTravelTime(floor)) {
                closestElevator = elevators.get(i);
            }
        }

        //Request the floor and then notify the elevator.
        closestElevator.requestFloor(floor);
        closestElevator.notify();
    }


}
