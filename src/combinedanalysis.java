import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class combinedanalysis {
    static class Node
    {
        int participation_count = 0;
        int best_rank = 9999999;
        double total_score = 0;
        double best_score = 0;
        double worst_score = 100;
        ArrayList<Double> scores = new ArrayList<>();
    }
    static HashMap<String, Node> hm = new HashMap<>();
    public static String[] runner(String location) {
        /*
         * Input: filename to the CSV that will be parsed, and the name itself
         * https://www.baeldung.com/java-csv-file-array
         */
        ArrayList<String> filecontents = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(location))) {
            String line;
            while ((line = br.readLine()) != null) {
                filecontents.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] res = new String[filecontents.size()];
        for (int i = 0; i < filecontents.size(); i++)
            res[i] = filecontents.get(i);
        return res;
    }
    public static void analyser(String[] contents)
    {
        // handle first line differently as that is the header which is not pipe-separated
        int number_of_participants = contents.length - 1;
        int max_score = Integer.parseInt(contents[1].split("\\|")[2]);
        for (int i = 1; i < contents.length; i++)
        {
            String[] split = contents[i].split("\\|");
            int score = Integer.parseInt(split[2]);
            double lcmark = 50*((double)score/(double)max_score) + 50*(double)(number_of_participants - (i == 1 ? 0 : i))/(double)number_of_participants;
            // add to hashmap
            Node N = hm.get(split[1]);
            if (N == null)
                N = new Node();
            N.participation_count++;
            N.total_score+=lcmark;
            N.best_rank = Math.min(N.best_rank, i);
            N.best_score = Math.max(N.best_score, lcmark);
            N.worst_score = Math.min(N.worst_score, lcmark);
            N.scores.add(lcmark);
            hm.put(split[1], N);
        }
    }
    public static double round (double num)
    {
        return (double)Math.round(num * 100d) / 100d;
    }
    public static void generatetable()
    {
        System.out.println("User|Mean score|Best rank|Best score|Worst score|Median score|Number of participants");
        for (String s: hm.keySet())
        {
            Node N = hm.get(s);
            Collections.sort(N.scores);
            double median_score = (N.scores.size() % 2 == 1 ? N.scores.get(N.participation_count / 2) : 0.5*N.scores.get(N.participation_count/2 - 1) + 0.5*N.scores.get(N.participation_count/2));
            System.out.println(s + "|" + N.total_score/(double)N.participation_count + "|" + N.best_rank + "|" + N.best_score + "|" + N.worst_score + "|" + median_score + "|" + N.participation_count);
        }
    }
    public static void main(String[] args)
    {
        String loc = Path.of("").toAbsolutePath() + "/../results";
        System.out.println(loc);
        String[] names;
        File f = new File(loc);
        names = f.list();
        assert names != null;
        for (String s : names) {
            // System.out.println(loc + "/" + s);
            analyser(runner(loc + "/" + s));
        }
        generatetable();
    }
}
