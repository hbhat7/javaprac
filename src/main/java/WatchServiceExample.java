import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * This class recursively scans the directory and its subdirectory for all the
 * changes like file add delete and modify
 * File IO being a single threaded operation only one thread can operate on a file at a time
 * in case if this program is run by multiple threads need to check the behaviour how to avoid
 * multiple threads reading the same file one after the other may be read write lock on file or synchronized block or something ??
 * this class is stared from WatchServiceDemo.java
 */
public class WatchServiceExample extends Thread {

    Path watchDirPath;
    WatchService watcher;
    private final Map<WatchKey,Path> keys = new HashMap<WatchKey,Path>();


    WatchServiceExample(String path) {
        try {
            watchDirPath = Paths.get(path);
            this.watcher = FileSystems.getDefault().newWatchService();
            registerAll(watchDirPath);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

        keys.put(key, dir);
    }

    private void registerAll(final Path start) throws IOException {

        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }


    public void run() {
        while (true) {
            try {
                WatchKey watchKey = watcher.take();
                List<WatchEvent<?>> events = watchKey.pollEvents();
                for (WatchEvent<?> event : events) {
                    // You can listen for these events too :
                    //     StandardWatchEventKinds.ENTRY_DELETE
                    //     StandardWatchEventKinds.ENTRY_MODIFY

                    if ( (event.kind() == StandardWatchEventKinds.ENTRY_CREATE)) {
                        System.out.println("Created--: " + event.context().toString());

                        try {
                            Path child = watchDirPath.resolve(event.context().toString());
                            if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                                registerAll(child);
                            }
                        } catch (IOException x) {
                            // ignore to keep sample readbale
                        }
                    }

                    if((event.kind() == StandardWatchEventKinds.ENTRY_MODIFY || event.kind() == StandardWatchEventKinds.ENTRY_DELETE)  ){
                        System.out.println("Modified/Deleted--: " + event.context().toString());

                    }

                }
                watchKey.reset();
            }
            catch (Exception e) {
                System.out.println("Error: " + e.toString());
            }
        }
    }
}