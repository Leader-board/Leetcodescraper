import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
public class scraper extends Thread{
    static String userdirectory = ""; // current working directory
    static int num_threads = 12; // number of threads to run
    static int contest_num = 1;
    // static Node resString[];
    static TreeMap<Integer, Node> resString;
    static Semaphore mutex = new Semaphore(1);
    static int num_q = 0;
    static class Node
    {
        // rank is key
        String name;
        int score;
        String time_taken;
        ArrayList<String> qtime = new ArrayList<>();
        ArrayList<Integer> qpen = new ArrayList<>();
    }
    static class progcore implements Runnable{
        private int page_num;
        private boolean isBiWeekly = true;
        public progcore(int num, Boolean bi)
        {
            page_num = num;
            isBiWeekly = bi;
        }
        @Override
        public void run() {
            // windows
           // System.setProperty("webdriver.chrome.driver", "C:\\chromedriver.exe");
            // linux
            System.setProperty("webdriver.chrome.driver", userdirectory + "/src/chromedriver");
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("window-size=1920x1080");
            //Initiating your chromedriver
            WebDriver driver = new ChromeDriver(options);
            //maximize window
            //    driver.manage().window().maximize();
            //open browser with desired URL
            int ptr = 0;
            if (isBiWeekly)
                driver.get("https://leetcode.com/contest/biweekly-contest-" + contest_num + "/ranking/" + page_num + "/");
            else if (contest_num >= 58)
                driver.get("https://leetcode.com/contest/weekly-contest-" + contest_num + "/ranking/" + page_num + "/");
            else
                driver.get("https://leetcode.com/contest/leetcode-weekly-contest-" + contest_num + "/ranking/" + page_num + "/");
            while (driver.findElements(By.xpath("/html/body/div[2]/div/div/div/div/div[2]/div[2]/table/tbody")).size() == 0) {
                try {
                    sleep(650);
                    ptr++;
                    if (ptr % 20 == 19)
                    {
                  if (isBiWeekly)
                driver.get("https://leetcode.com/contest/biweekly-contest-" + contest_num + "/ranking/" + page_num + "/");
            else if (contest_num >= 58)
                driver.get("https://leetcode.com/contest/weekly-contest-" + contest_num + "/ranking/" + page_num + "/");
            else
                driver.get("https://leetcode.com/contest/leetcode-weekly-contest-" + contest_num + "/ranking/" + page_num + "/");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // grab details of each person
            for (int i = 1; i <= 25; i++)
            {
                // make sure that entry exists
                if (driver.findElements(By.xpath("/html/body/div[2]/div/div/div/div/div[2]/div[2]/table/tbody/tr[" + i + "]")).size() == 0)
                {
                    // invalid rank
                    break;
                }
                int rank = Integer.parseInt(driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div/div[2]/div[2]/table/tbody/tr[" +  i + "]/td[1]")).getText()); // key
                Node det = new Node();
                det.name = driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div/div[2]/div[2]/table/tbody/tr[" +  i + "]/td[2]")).getText().replaceAll("\\s+","");
                det.score = Integer.parseInt(driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div/div[2]/div[2]/table/tbody/tr[" +  i + "]/td[3]")).getText());
                det.time_taken = driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div/div[2]/div[2]/table/tbody/tr[" +  i + "]/td[4]")).getText();
                for (int q = 1; q <= num_q; q++)
                {
                    String str = driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div/div[2]/div[2]/table/tbody/tr[" +  i + "]/td[" + (4 + q) + "]")).getText();
                    if (str.equals(""))
                    {
                        // FAIL, question not attempted
                        det.qtime.add("-1");
                        det.qpen.add(-1);
                    }
                    else
                    {
                        // let's get them
                        det.qtime.add(driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div/div[2]/div[2]/table/tbody/tr[" +  i + "]/td[" + (4 + q) + "]")).getText().split(" ")[0]);
                        try {
                            det.qpen.add(Integer.parseInt(driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div/div[2]/div[2]/table/tbody/tr[" + i + "]/td[" + (4 + q) + "]/span")).getText()));
                        }
                        catch(Exception e)
                        {
                            det.qpen.add(0);
                        }
                    }
                }
                try {
                    mutex.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                resString.put(rank, det);
                mutex.release();
            }
            //   String ranking = driver.findElement(By.xpath("/html/body/div[1]/div[4]/div/div/div/div[2]/div[2]/table/tbody")).getText();

            //closing the browser
            driver.quit();
        }
    }
    public static double round (double num)
    {
        return (double)Math.round(num * 100d) / 100d;
    }
    public static void post_analysis (Boolean isBi) throws FileNotFoundException {
        File file;
        PrintStream stream;
        // only after a full contest result has been generated
        int participant_num = resString.size();
        int index = 1;
        int maxscore = 0;
        double mean = 0;
        for (Node N:resString.values())
        {
            if (index == 1)
                maxscore = N.score;
            if (N.score == 0)
            {
                // do nothing
            }
            else
                index++;
            mean = mean + (double)N.score;
        }
        mean = mean / (double)participant_num;
        int num_nonzero = index - 1; // nonzero score proportion
        index = 1;
        // generate score curves
        double gradecurves[] = new double[12];
        int pointer = 1;
        TreeMap<Integer, Integer> score_counts = new TreeMap<>((a, b) -> (b - a)); // giving number of people with certain score
        // get the grade curve and count of unique scores
        for (Node N:resString.values())
        {
            if (index == pointer * num_nonzero/ 12)
            {
                // got curve
                gradecurves[pointer - 1] = round(50d*((double)N.score/(double)maxscore) + 50d * (double)(participant_num - index)/(double)participant_num);
                pointer++;
                if (pointer > 12)
                    break;
            }
            // count of unique scores
            if (score_counts.get(N.score) == null)
                score_counts.put(N.score, 1);
            else
                score_counts.put(N.score, score_counts.get(N.score) + 1);
            index++;
        }

        // print them off
        file = new File(userdirectory + "/stats/"+(isBi?"biweekly":"weekly") + contest_num + ".txt");
        stream = new PrintStream(file);
        System.setOut(stream);
        System.out.println("A+ " + gradecurves[0]);
        System.out.println("A " + gradecurves[1]);
        System.out.println("A- " + gradecurves[2]);
        System.out.println("B+ " + gradecurves[3]);
        System.out.println("B " + gradecurves[4]);
        System.out.println("B- " + gradecurves[5]);
        System.out.println("C+ " + gradecurves[6]);
        System.out.println("C " + gradecurves[7]);
        System.out.println("C- " + gradecurves[8]);
        System.out.println("D+ " + gradecurves[9]);
        System.out.println("D " + gradecurves[10]);
        System.out.println("D- " + gradecurves[11]);
        System.out.println("F " + 0);
        System.out.println("Number of participants " + participant_num);
        System.out.println("Number of non zero scores " + num_nonzero);
        System.out.println("Maximum score " + maxscore);
        System.out.println("Mean score " + round(mean) + " (" + round(mean * 100d/(double)maxscore) + "%)");
        System.out.println("");
        System.out.println("Count of all possible scores (score frequency):");
        for (int sc: score_counts.keySet())
        {
            System.out.println(sc + " " + score_counts.get(sc) + " (" + round(100d*(double)score_counts.get(sc)/(double)participant_num) + ")");
        }
        System.out.println(0 + " " + (participant_num - num_nonzero) + " (" + round(100d*(double)(participant_num - num_nonzero)/(double)participant_num) + ")");
        stream.close();
    }
    public static void corerunner(Boolean isBi) throws InterruptedException, FileNotFoundException {
        File file;
        PrintStream stream;
        // windows
        // System.setProperty("webdriver.chrome.driver", "C:\\chromedriver.exe");
        // linux
        System.setProperty("webdriver.chrome.driver", userdirectory + "/src/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("window-size=1920x1080");
        WebDriver driver= new ChromeDriver(options);
        if (isBi)
            driver.get("https://leetcode.com/contest/" + "biweekly-contest-" + contest_num + "/ranking/" + 1 + "/");
        else if (contest_num >= 58)
            driver.get("https://leetcode.com/contest/" + "weekly-contest-" + contest_num + "/ranking/" + 1 + "/");
        else
            driver.get("https://leetcode.com/contest/" + "leetcode-weekly-contest-" + contest_num + "/ranking/" + 1 + "/");
        while (driver.findElements(By.xpath("/html/body/div[2]/div/div/div/div/nav/ul/li[6]/a")).size() == 0)
            sleep(500);
        int max_page = Integer.parseInt(driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div/nav/ul/li[6]/a")).getText());
        // find the number of questions
        String str = driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div/div[2]/div[2]/table/thead/tr")).getText();
        if (driver.findElements(By.xpath("/html/body/div[2]/div/div/div/div/div[2]/div[2]/table/thead/tr/th[8]")).size() == 0)
        {
            // Q4 does not exist
            num_q = 3;
        }
        else if ((driver.findElements(By.xpath("/html/body/div[2]/div/div/div/div/div[2]/div[2]/table/thead/tr/th[9]")).size() == 0))
            num_q = 4;
        else if ((driver.findElements(By.xpath("/html/body/div[2]/div/div/div/div/div[2]/div[2]/table/thead/tr/th[10]")).size() == 0))
            num_q = 5;
        else
            num_q = 6;
        resString = new TreeMap<>();
        driver.quit();
        ExecutorService runner = Executors.newFixedThreadPool(num_threads);
        Runnable threads[] = new Runnable[max_page];
        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new progcore((i + 1), isBi);
        }
        for (Runnable thread : threads) {
            runner.execute(thread);
        }
        runner.shutdown();
        while (!runner.awaitTermination(24L, TimeUnit.HOURS))
        {
            // wait
        }
        file = new File(userdirectory + "/results/"+(isBi?"biweekly":"weekly") + contest_num + ".txt");
        stream = new PrintStream(file);
        System.setOut(stream);
        System.out.println(str);
        int rnk = 1;
        for (Node N:resString.values())
        {
            // print each of them
            System.out.print(rnk + "|" + N.name + "|" + N.score + "|" + N.time_taken + "|");
            for (int i = 0; i < num_q; i++)
            {
                System.out.print(N.qtime.get(i) + "|" + N.qpen.get(i) + "|");
            }
            System.out.println();
            rnk++;
        }
        stream.close();
        post_analysis(isBi);
    }
    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        /*
        If start > end, does not run. Set when done.
         */
         // get current working directory
        userdirectory = new File("").getAbsolutePath() + "/..";
        System.out.println("Current working directory is " + userdirectory);
        if (args.length != 5)
        {
            System.out.println("Enter CMD argments: weekly_start, weekly_end, biweekly_start, biweekly_end, number_of_threads. Set (start > end) to skip.");
        }
        else {
            int weekly_start = Integer.parseInt(args[0]);
            int weekly_end = Integer.parseInt(args[1]);
            int biweekly_start = Integer.parseInt(args[2]);
            int biweekly_end = Integer.parseInt(args[3]);
            num_threads = Integer.parseInt(args[4]);
            for (contest_num = weekly_start; contest_num <= weekly_end; contest_num++) {
                if (contest_num < 16 || contest_num > 18)
                    corerunner(false);
            }
            for (contest_num = biweekly_start; contest_num <= biweekly_end; contest_num++)
                corerunner(true);
        }
    }
}
