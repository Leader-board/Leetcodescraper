import time
from selenium import webdriver
from selenium.webdriver.common.keys import Keys
import sys
import multiprocessing
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from functools import partial
contest_num = 1`
def main_thread(result_text, page_num):
    driver = webdriver.Chrome('C:/chromedriver.exe') 
    driver.get('https://leetcode.com/contest/biweekly-contest-' + str(contest_num) + '/ranking/' + str(page_num) + '/')
    # get main table
    while (len(driver.find_elements_by_xpath('/html/body/div[1]/div[4]/div/div/div/div[2]/div[2]/table/tbody')) == 0):
        time.sleep(2)
    ranking = driver.find_element_by_xpath('/html/body/div[1]/div[4]/div/div/div/div[2]/div[2]/table/tbody').text
    result_text[page_num] = ranking # may need sorting
   # f = open(page_num + ".txt", "w", encoding="utf-8")
   # f.write(ranking)
   # f.close()
    print(result_text)
    driver.quit()
def post_processing():
    # append the header
    driver = webdriver.Chrome('C:/chromedriver.exe') 
    driver.get('https://leetcode.com/contest/weekly-contest-' + str(contest_num) + '/ranking/' + str(page_num) + '/')
    # get main table
    ranking = driver.find_element_by_xpath('/html/body/div[1]/div[4]/div/div/div/div[2]/div[2]/table/thead').text
    f = open("0" + ".txt", "w", encoding="utf-8")
    f.write(ranking)
    f.close()
    driver.quit()
if __name__ == '__main__':
    NUMBER_OF_THREADS = multiprocessing.cpu_count()
    pool = multiprocessing.Pool(processes=8)
    manager = multiprocessing.Manager()
    result = manager.list()
    # get the number of contestants
    driver = webdriver.Chrome('C:/chromedriver.exe') 
    driver.get('https://leetcode.com/contest/biweekly-contest-' + str(contest_num) + '/ranking/' + '1' + '/')
    while (len(driver.find_elements_by_xpath('/html/body/div[1]/div[4]/div/div/div/nav/ul/li[6]/a')) == 0):
        time.sleep(1)
    max_page = int(driver.find_element_by_xpath('/html/body/div[1]/div[4]/div/div/div/nav/ul/li[6]/a').text)
    driver.quit()
    result = [None] * (max_page + 1)
    func = partial(main_thread, result)
    pool.map(func, range(1, max_page + 1))
    result.pop(0)
    print("______________________________")
    print(result)