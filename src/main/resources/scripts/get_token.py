import json
import time


import logging
from selenium import webdriver
from selenium.common import TimeoutException
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.edge.service import Service as EdgeService
from selenium.webdriver.edge.options import Options
from threading import Thread

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class TokenFetcher:
    def __init__(self):
        self.driver = self._init_driver()
        self.TASK_URL = "http://ones.inspur.com/project/#/workspace/team/HvBrmPic/filter/view/ft-t-002/task/EiiEaJiCGMMIAftS"
        self.LOGIN_URL_PREFIX = "http://ones.inspur.com/auth/third_party/login/ldap"

    def _init_driver(self):
        edge_options = Options()
        edge_options.add_argument(r"--user-data-dir=C:\Users\Ryker\AppData\Local\Microsoft\Edge\User Data")
        edge_options.add_argument(r"--profile-directory=Default")
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
            if self._needs_login():
                self._perform_login()
                return self._extract_token()
            return ""
        except Exception as e:
            logger.error(f"获取Token时发生错误: {e}")
            return ""
        finally:
            Thread(target=self.driver.quit).start()

    def _navigate_to_task_page(self):
        logger.info("正在导航到任务页面...")
        self.driver.get(self.TASK_URL)
        # time.sleep(3)  # 等待页面加载

    def _needs_login(self):
        try:
            WebDriverWait(self.driver, 10).until(EC.url_changes(self.TASK_URL))
            return self.LOGIN_URL_PREFIX in self.driver.current_url
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

        username_input.send_keys("chenle02")
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
        # 从 localStorage 中提取 token
        localStorage_content = self.driver.execute_script("""
            var items = {};
            for (var i = 0; i < localStorage.length; i++) {
                var key = localStorage.key(i);
                items[key] = localStorage.getItem(key);
            }
            return items;
        """)
        logger.info(f"获取到的localStorage内容: {localStorage_content}")

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
    fetcher = TokenFetcher()
    token = fetcher.fetch_token()
    print(token)    # 关闭浏览器
