# LeetCode scraper

Contains (by "all"/"each" I mean 99% of contests)
- Scraped raw results for all contests (in results directory), which can be easily exported to Excel or any other similar program for statistical analysis.
- Source code of the multithreaded Java scraper and analyser program, scales to an arbitrary number of threads for faster processing
- Cutoffs for each grade for each LeetCode contest (in stats directory). The idea (see readme in the stats directory) is to give you a grade for your performance if the LeetCode contest was a course/module. Readme also gives the steps to calculate the score. 