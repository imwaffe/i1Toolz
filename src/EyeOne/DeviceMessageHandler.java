package EyeOne;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The DeviceMessageHandler allows for a list of Runnables or Consumers to be executed
 * whenever the predicate turns true.
 *
 * Predicate predicate:
 *      input: Integer message generated by the instrument
 *      output: TRUE when the message corresponds to the message this handler should handle
 *              and false otherwise.
 *
 * Runnables:
 *      Callbacks to be executed whenever the instrument generates the message
 *      truing the predicate.
 *
 * Consumers:
 *      Callbacks to be executed whenever the instrument generates the message
 *      truing the predicate.
 *      input: Integer message generated by the instrument.
 * */

public class DeviceMessageHandler {
    private List<Runnable> runnables = new ArrayList<>();
    private List<Consumer<Integer>> consumers = new ArrayList<>();
    private Predicate<Integer> predicate;

    public DeviceMessageHandler(Predicate<Integer> predicate){
        this.predicate = predicate;
    }

    public void add(Runnable callback){
        runnables.add(callback);
    }

    public void remove(Runnable callback){
        runnables.remove(callback);
    }

    public void add(Consumer<Integer> callback){
        consumers.add(callback);
    }

    public void remove(Consumer<Integer> callback){
        consumers.remove(callback);
    }

    public void clearAll(){
        runnables.clear();
        consumers.clear();
    }

    public void run(int msg){
        if(predicate.test(msg)) {
            runnables.forEach(Runnable::run);
            consumers.forEach(c->c.accept(msg));
        }
    }
}
