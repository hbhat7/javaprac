import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


//future.get(); waits for the task to finish the same way as thread.join does
// but future.get is mainly when using the executor service
public class WatchServiceDemo {

    public static void main (String args []) {
        //new WatchServiceExample("/Users/hemanthbhat/Documents/HemanthSoftwares").start();
        System.out.println("WatchThread is running!");

        ExecutorService executorService = Executors.newFixedThreadPool(7);
        for(int i=0;i<7;i++){
            WatchServiceExample watchServiceExample = new WatchServiceExample("/path/n/x/","Thread"+i);
            Future future = executorService.submit(watchServiceExample);
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
    }
}