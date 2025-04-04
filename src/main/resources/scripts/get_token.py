import json
import time
import logging
from logging.handlers import TimedRotatingFileHandler
from selenium import webdriver
from selenium.common import TimeoutException
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.edge.service import Service as EdgeService
from selenium.webdriver.edge.options import Options
from threading import Thread
import sys
import os

# 配置日志
log_file = "token_fetcher.log"
if os.path.exists(log_file):
    os.remove(log_file)

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)

# 创建TimedRotatingFileHandler，每天午夜滚动日志，保留7天
file_handler = TimedRotatingFileHandler(
    log_file,
    when="midnight",
    interval=1,
    backupCount=7
)

# 添加控制台日志
console_handler = logging.StreamHandler()
console_handler.setFormatter(logging.Formatter('%(asctime)s - %(levelname)s - %(message)s'))
logger.addHandler(console_handler)


class TokenFetcher:
    def __init__(self):
        self.driver = self._init_driver()
        self.TASK_URL = "http://ones.inspur.com/project/#/workspace/team/HvBrmPic/filter/view/ft-t-002/task/EiiEaJiCGMMIAftS"
        self.LOGIN_URL_PREFIX = "http://ones.inspur.com/auth/third_party/login/ldap"

    def _init_driver(self):
        edge_options = Options()
        # edge_options.add_argument(r"--user-data-dir=C:\Users\chenle02\AppData\Local\Microsoft\Edge\User Data")
        # edge_options.add_argument(r"--profile-directory=Default")
        service = EdgeService(executable_path="edgedriver_win64/msedgedriver.exe")
        return webdriver.Edge(service=service, options=edge_options)

    def fetch_token(self):
        try:
            self._navigate_to_task_page()
            token = self._extract_token()
            if token and token != "":
                logger.info(f"成功获取Token: {token}")
                return token

            # 如果获取不到 token 或 URL 发生变化，则进行登录
            e = self._needs_login()
            logger.info(f"是否需要登录: {e}")
            if e:
                self._perform_login()
                time.sleep(3)
                return self._extract_token()
            return ""
        except Exception as e:
            logger.error(f"获取Token时发生错误: {e}")
            return ""
        # finally:
        #     Thread(target=self.driver.quit).start()

    def _navigate_to_task_page(self):
        logger.info("正在导航到任务页面...")
        self.driver.get(self.TASK_URL)
        # 使用 any_of 方法同时等待多个元素中的任意一个变得可见

        try:
            logger.info("开始等待元素可见...")
            WebDriverWait(self.driver, 60).until(
                EC.any_of(
                    EC.visibility_of_element_located(
                        (By.XPATH, '//*[@id="root"]/div/div/div/img')),
                    EC.visibility_of_element_located((By.XPATH, '//*[@id="sidebar-nav"]/div[1]/a/img'))
                )
            )
            logger.info("元素已可见")
        except Exception as e:
            logger.error(f"等待元素可见时发生错误", e)

    def _needs_login(self):
        try:
            # 等待 URL 发生变化
            WebDriverWait(self.driver, 10).until(EC.url_changes(self.TASK_URL))

            # 设置最大重试次数和超时时间
            max_retries = 10
            retry_count = 0
            timeout = time.time() + 5  # 5秒超时

            # 如果当前 URL 是中间页面（B 网址），继续等待跳转到登录页面
            while retry_count < max_retries and time.time() < timeout:
                current_url = self.driver.current_url
                if self.LOGIN_URL_PREFIX in current_url:
                    return True
                time.sleep(0.5)  # 短暂等待，避免 CPU 占用过高
                retry_count += 1
            return False
        except TimeoutException:
            return False

    def _perform_login(self):
        logger.info("检测到需要登录，正在执行登录操作...")
        username_input = WebDriverWait(self.driver, 100).until(
            EC.presence_of_element_located((By.XPATH, '//*[@id="username"]'))
        )
        password_input = WebDriverWait(self.driver, 100).until(
            EC.presence_of_element_located((By.XPATH, '//*[@id="password"]'))
        )
        login_button = WebDriverWait(self.driver, 10).until(
            EC.element_to_be_clickable(
                (By.XPATH, '//*[@id="root"]/div/div/div/div/div/div/form/div[3]/div/div/div/div/button'))
        )

        username_input.clear()
        username_input.send_keys("chenle02")
        password_input.clear()
        password_input.send_keys("Nevergiveup123456-")
        login_button.click()

        self._handle_mfa_verification()

    def _handle_mfa_verification(self):
        while True:
            current_url = self.driver.current_url
            if "http://ones.inspur.com/identity/mfa/verify" in current_url:
                logger.info("检测到需要进行MFA验证...")
                WebDriverWait(self.driver, 30).until(EC.url_changes(current_url))
                break
            time.sleep(0.1)

    def _extract_token(self):
        try:
            # 添加10秒超时
            localStorage_content = self.driver.execute_script("""return window.localStorage""")
            # print("失败")
            logger.info(f"获取到的localStorage内容: {localStorage_content}")



            # 优先从localStorage中获取token
            token = localStorage_content.get('_ones_json_ones_org_jwt')
            if token:
                token = json.loads(token)['access_token']
                logger.info(f"从localStorage中获取到的Token: {token}")
                return token

            cookies = self.driver.get_cookies()
            logger.info(f"获取到的Cookies: {cookies}")
            # 如果localStorage中没有，则从cookies中获取
            token = next((cookie.get('value') for cookie in cookies if cookie.get('name') == 'ones-lt'), None)
            return token or ""
        except TimeoutException:
            logger.error("获取localStorage超时")
            return ""
        except Exception as e:
            logger.error(f"执行脚本时发生错误: {e}")
            return ""

        # 从 cookies 中提取 token
        cookies = self.driver.get_cookies()
        logger.info(f"获取到的Cookies: {cookies}")



        # 如果 localStorage 中没有，则从 cookies 中获取
        token = next((cookie.get('value') for cookie in cookies if cookie.get('name') == 'ones-lt'), None)
        if token:
            return token

        # 优先从 localStorage 中获取 token
        token = localStorage_content.get('_ones_json_ones_org_jwt')
        if token:
            token = json.loads(token)['access_token']
            logger.info(f"从localStorage中获取到的Token: {token}")
            return token
        return token or ""

if __name__ == "__main__":
    # 获取传入的参数
    args = sys.argv[1:]  # 忽略第一个参数（脚本路径）
    # 示例：处理参数
    if "--no_log" in args:
        logger.disabled = True
    fetcher = TokenFetcher()
    token = fetcher.fetch_token()
    print(token)    # 关闭浏览器
