import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class word_counter {
    private Map<String, Integer> wc = new HashMap<>();
    private final Object lock = new Object();

    public void counting_words(String[] words) {
        synchronized (lock) {
            for (String word : words) {
                wc.put(word, wc.getOrDefault(word, 0) + 1);
            }
        }
    }

    public Map<String, Integer> getWc() {
        return wc;
    }
}

class FileProcessor implements Runnable {
    private final word_counter wordCounter;
    private final String file_name;

    public FileProcessor(word_counter wordCounter, String[] filePath) {
        this.wordCounter = wordCounter;
        this.file_name = Arrays.toString(filePath);
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file_name))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");
                wordCounter.counting_words(words);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class MultithreadingCountTask {
    public static void main(String[] args) {
        word_counter wordCounter = new word_counter();
        ExecutorService executorService = Executors.newFixedThreadPool(4); // Adjust the thread pool size as needed

        // Replace "data.txt" with the actual path to your text file
        String filePath = "data.txt";


       // System.out.println("Absolute Path: " + new File(filePath).getAbsolutePath());

        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(filePath));
            String line;
            int chunk_size = 100; 

            while ((line = fileReader.readLine()) != null) {
                String[] words = line.split("\\s+");
                for (int i = 0; i < words.length; i += chunk_size) {
                    int end = Math.min(i + chunk_size, words.length);
                    String[] chunk = Arrays.copyOfRange(words, i, end);
                    executorService.execute(new FileProcessor(wordCounter, chunk));
                }
            }

            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            // Print the final word count
            Map<String, Integer> finalWordCount = wordCounter.getWc();
            for (Map.Entry<String, Integer> entry : finalWordCount.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
