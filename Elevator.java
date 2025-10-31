import java.util.*;
import java.util.concurrent.*;
public class Elevator implements Runnable{
    private int currentFloor;
    private Direction direction;
    private BlockingQueue<Integer> upQueue;
    private BlockingQueue<Integer> downQueue;

    public Elevator(int id, int floors) {
        this.currentFloor = 0;
        this.direction = Direction.IDLE;

        //Stores the current queue for the up cycle.
        this.upQueue = new PriorityBlockingQueue<>();

        //Stores the current queue for the down cycle
        this.downQueue = new PriorityBlockingQueue<>(floors, Collections.reverseOrder());
    }

    //Whenever the queues are empty, go to sleep and wait for notification
    //Once notification arrives, go in direction of queue.
    @Override
    public void run() {
        try {
            while (true) {
                synchronized (this) {
                    if (upQueue.isEmpty() && downQueue.isEmpty()) {
                        direction = Direction.IDLE;
                        wait();  //Wait for controller to wake elevator up
                    }
                }
                //Go through all floor requests.
                if (downQueue.isEmpty()) {
                    moveUp();
                } else if (upQueue.isEmpty()) {
                    moveDown();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * When the controller sends a command to the elevator, add floor request to up/down queue.
     * We synchronize it because this method updates the queues. Because we update queues,
     * it's possible that 2 different threads try going through the method at the same time and cause broken data.
     * To fix this, we synchronize the code.
     *
     * @param floorRequest Floor to be added to the queues.
     */
    public synchronized void requestFloor(int... floorRequest) {

        //Add requested floor to corresponding queue
        for (int floor: floorRequest) {
            if (floor == this.currentFloor) {
                return;
            } else if (floor > this.currentFloor) {
                upQueue.add(floor);
            } else {
                downQueue.add(floor);
            }
            //If the elevator was idle, change the direction to start moving.
            if (direction == Direction.IDLE) {
                direction = (floor > this.currentFloor) ? Direction.UP : Direction.DOWN;
            }
        }
    }

    /**
     * Move the elevator up until its corresponding queue is empty (invalid inputs are filtered through controller)
     */
    private void moveUp() throws InterruptedException {
        while (!upQueue.isEmpty()) {
            currentFloor++;
            Thread.sleep(1000);
            System.out.println("Now on floor " + currentFloor + "!");
            if (upQueue.peek().equals(currentFloor)) {
                upQueue.remove();
            }
        }
    }

    private void moveDown() throws InterruptedException {
        while (!downQueue.isEmpty()) {
            currentFloor--;
            Thread.sleep(1000);
            System.out.println("Now on floor " + currentFloor + "!");
        }
    }

    /**
     * Estimates the time in seconds to reach a target floor.
     * This calculation is a "snapshot" and does not account for new
     * requests that may arrive after it's called.
     *
     * @param targetFloor The floor we want to get to.
     * @return Estimated time in seconds.
     */
    public synchronized int estimateTravelTime(int targetFloor) {
        final int TIME_PER_FLOOR = 1;

        if (targetFloor == this.currentFloor) {
            return 0;
        }

        switch (direction) {
            case IDLE:
                // If idle, the time is just the direct travel time.
                return Math.abs(targetFloor - currentFloor) * TIME_PER_FLOOR;

            case UP:
                if (targetFloor > this.currentFloor) {
                    // Target is in the same direction (UP).
                    // Time is the direct travel time from current to target.
                    return (targetFloor - currentFloor) * TIME_PER_FLOOR;
                } else {
                    // Target is in the opposite direction (DOWN).
                    // Must first go to the highest floor in the upQueue, then come down.
                    if (upQueue.isEmpty()) {
                        // Should not happen if direction is UP, but as a fallback:
                        return Math.abs(targetFloor - currentFloor) * TIME_PER_FLOOR;
                    }

                    int highestUpStop = Collections.max(upQueue);
                    int timeToFinishUpCycle = (highestUpStop - currentFloor) * TIME_PER_FLOOR;
                    int timeToTravelBack = (highestUpStop - targetFloor) * TIME_PER_FLOOR;

                    return timeToFinishUpCycle + timeToTravelBack;
                }

            case DOWN:
                if (targetFloor < this.currentFloor) {
                    // Target is in the same direction (DOWN).
                    return (currentFloor - targetFloor) * TIME_PER_FLOOR;
                } else {
                    // Target is in the opposite direction (UP).
                    // Must first go to the lowest floor in the downQueue, then come up.
                    if (downQueue.isEmpty()) {
                        // Fallback:
                        return Math.abs(targetFloor - currentFloor) * TIME_PER_FLOOR;
                    }

                    int lowestDownStop = Collections.min(downQueue);
                    int timeToFinishDownCycle = (currentFloor - lowestDownStop) * TIME_PER_FLOOR;
                    int timeToTravelBack = (targetFloor - lowestDownStop) * TIME_PER_FLOOR;

                    return timeToFinishDownCycle + timeToTravelBack;
                }
        }
        return 0; // Default case
    }

    public int getFloor() {return currentFloor;}
    public Direction getDirection() {return direction;}
    public BlockingQueue<Integer> getUpQueue() {return upQueue;}
    public BlockingQueue<Integer> getDownQueue() {return downQueue;}
}
