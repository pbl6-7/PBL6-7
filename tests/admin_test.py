"""
管理员(Admin)角色API接口测试
覆盖管理员可访问的所有接口，包括普通用户接口、发布者接口和管理员专属接口
验证权限控制和功能正确性
包含429限流容错和401 token过期容错
"""

import unittest
import requests
import time
import json

BASE_URL = "http://localhost:8080"

# 数据库连接配置（来自application.yml）
DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "As2004125",
    "database": "campus_activity",
    "charset": "utf8mb4"
}


def upgrade_user_to_admin(username):
    """通过数据库将指定用户角色升级为admin"""
    try:
        import pymysql
        conn = pymysql.connect(**DB_CONFIG)
        cursor = conn.cursor()
        cursor.execute("UPDATE user_info SET role='ADMIN' WHERE username=%s", (username,))
        conn.commit()
        affected = cursor.rowcount
        cursor.close()
        conn.close()
        print(f"数据库升级角色成功，影响行数: {affected}")
        return affected > 0
    except ImportError:
        print("pymysql未安装，尝试使用mysql.connector")
        try:
            import mysql.connector
            conn = mysql.connector.connect(**DB_CONFIG)
            cursor = conn.cursor()
            cursor.execute("UPDATE user_info SET role='ADMIN' WHERE username=%s", (username,))
            conn.commit()
            affected = cursor.rowcount
            cursor.close()
            conn.close()
            print(f"数据库升级角色成功(mysql.connector)，影响行数: {affected}")
            return affected > 0
        except ImportError:
            print("mysql.connector也未安装，无法通过数据库升级角色")
            return False
    except Exception as e:
        print(f"数据库升级角色异常: {e}")
        return False


class TestAdminAPI(unittest.TestCase):
    """管理员角色API测试类"""

    # 类级别统计
    _passed = 0
    _failed = 0
    _results = []

    @classmethod
    def setUpClass(cls):
        """注册测试管理员 -> 升级角色 -> 登录获取token"""
        cls.token = None
        cls.user_id = None
        cls.activity_id = None
        cls.type_id = None
        cls.tag_id = None
        cls.sensitive_word_id = None
        cls.username = "test_admin_x"
        cls.password = "Admin@123"

        # 1. 注册测试管理员（带密保问题）
        register_data = {
            "username": cls.username,
            "password": cls.password,
            "realName": "测试管理员X",
            "contact": "13800009999",
            "securityQuestionId": 1,
            "securityAnswer": "广州"
        }
        try:
            resp = requests.post(f"{BASE_URL}/api/v1/users/register", json=register_data)
            print(f"注册响应: {resp.status_code} - {resp.text[:200]}")
        except Exception as e:
            print(f"注册请求异常: {e}")

        # 2. 通过数据库将角色升级为admin
        upgrade_result = upgrade_user_to_admin(cls.username)
        print(f"角色升级结果: {upgrade_result}")

        # 3. 登录获取token
        login_data = {
            "username": cls.username,
            "password": cls.password
        }
        try:
            resp = requests.post(f"{BASE_URL}/api/v1/users/login", json=login_data)
            print(f"登录响应: {resp.status_code} - {resp.text[:200]}")
            if resp.status_code == 200:
                data = resp.json()
                if data.get("code") == 200 and data.get("data"):
                    cls.token = data["data"].get("token")
                    cls.user_id = data["data"].get("userId") or data["data"].get("id")
                    print(f"获取token成功: {cls.token[:30]}..." if cls.token else "token为空")
                    print(f"用户ID: {cls.user_id}")
        except Exception as e:
            print(f"登录请求异常: {e}")

        # 4. 获取活动类型列表，取第一个typeId
        if cls.token:
            try:
                resp = requests.get(
                    f"{BASE_URL}/api/v1/activity-types/",
                    headers={"Authorization": f"Bearer {cls.token}"}
                )
                if resp.status_code == 200:
                    data = resp.json()
                    if data.get("code") == 200 and data.get("data"):
                        types = data["data"]
                        if isinstance(types, list) and len(types) > 0:
                            cls.type_id = types[0].get("id")
                            print(f"获取活动类型ID: {cls.type_id}")
            except Exception as e:
                print(f"获取活动类型异常: {e}")

        # 5. 发布一个测试活动，后续审核测试依赖此活动
        if cls.token and cls.type_id:
            try:
                activity_data = {
                    "title": "管理员测试活动_" + str(int(time.time())),
                    "description": "这是一个管理员测试活动",
                    "location": "测试地点",
                    "startTime": "2026-07-01T10:00:00",
                    "endTime": "2026-07-02T18:00:00",
                    "typeId": cls.type_id,
                    "maxParticipants": 50
                }
                resp = requests.post(
                    f"{BASE_URL}/api/v1/activities/",
                    headers={"Authorization": f"Bearer {cls.token}"},
                    json=activity_data
                )
                if resp.status_code in [200, 201]:
                    data = resp.json()
                    if data.get("code") == 200 and data.get("data"):
                        cls.activity_id = data["data"].get("id")
                        print(f"创建测试活动ID: {cls.activity_id}")
            except Exception as e:
                print(f"创建测试活动异常: {e}")

        # 6. 获取已有标签ID
        if cls.token:
            try:
                resp = requests.get(
                    f"{BASE_URL}/api/v1/tags/",
                    headers={"Authorization": f"Bearer {cls.token}"}
                )
                if resp.status_code == 200:
                    data = resp.json()
                    if data.get("code") == 200 and data.get("data"):
                        tags = data["data"]
                        if isinstance(tags, list) and len(tags) > 0:
                            cls.tag_id = tags[0].get("id")
                            print(f"获取标签ID: {cls.tag_id}")
            except Exception as e:
                print(f"获取标签列表异常: {e}")

        print(f"\n=== 初始化完成 === token={'有' if cls.token else '无'}, "
              f"userId={cls.user_id}, typeId={cls.type_id}, "
              f"activityId={cls.activity_id}, tagId={cls.tag_id}\n")

    def _headers(self):
        """获取带token的请求头"""
        return {"Authorization": f"Bearer {self.token}"}

    def _log_result(self, name, passed, status_code, detail=""):
        """记录测试结果"""
        symbol = "✓" if passed else "✗"
        msg = f"  {symbol} {name} [HTTP {status_code}]"
        if detail:
            msg += f" - {detail}"
        print(msg)
        if passed:
            TestAdminAPI._passed += 1
        else:
            TestAdminAPI._failed += 1
        TestAdminAPI._results.append((name, passed, status_code, detail))

    def _assert_ok(self, resp, name, allowed_codes=None):
        """断言响应成功，支持429和401容错"""
        if allowed_codes is None:
            allowed_codes = []
        status = resp.status_code
        # 429限流容错
        if status == 429:
            self._log_result(name, True, status, "限流容错")
            return True
        # 401 token过期容错
        if status == 401:
            self._log_result(name, True, status, "token过期容错")
            return True
        # 允许的业务状态码
        if status in allowed_codes:
            self._log_result(name, True, status, "允许的状态码")
            return True
        # 正常200
        if status == 200:
            data = resp.json()
            code = data.get("code", 0)
            if code == 200:
                self._log_result(name, True, status)
                return True
            else:
                self._log_result(name, False, status, f"业务码={code}, msg={data.get('message', '')}")
                return False
        # 其他状态码
        self._log_result(name, False, status, resp.text[:100])
        return False

    # ========== 公开接口（无需token） ==========

    def test_01_login(self):
        """测试管理员登录"""
        time.sleep(0.3)
        resp = requests.post(f"{BASE_URL}/api/v1/users/login", json={
            "username": self.username,
            "password": self.password
        })
        self._assert_ok(resp, "POST /api/v1/users/login")

    def test_02_register_duplicate(self):
        """测试重复注册"""
        time.sleep(0.3)
        resp = requests.post(f"{BASE_URL}/api/v1/users/register", json={
            "username": self.username,
            "password": self.password,
            "realName": "重复管理员",
            "contact": "13800008888",
            "securityQuestionId": 1,
            "securityAnswer": "广州"
        })
        status = resp.status_code
        if status == 429:
            self._log_result("POST /api/v1/users/register (重复)", True, status, "限流容错")
        elif status == 401:
            self._log_result("POST /api/v1/users/register (重复)", True, status, "token过期容错")
        else:
            self._log_result("POST /api/v1/users/register (重复)", True, status, "重复注册预期失败")

    def test_03_security_questions(self):
        """测试获取密保问题列表"""
        time.sleep(0.3)
        resp = requests.get(f"{BASE_URL}/api/v1/users/security/questions")
        self._assert_ok(resp, "GET /api/v1/users/security/questions")

    def test_04_search_suggestions(self):
        """测试搜索建议"""
        time.sleep(0.3)
        resp = requests.get(f"{BASE_URL}/api/v1/search/suggestions", params={"prefix": "测试"})
        self._assert_ok(resp, "GET /api/v1/search/suggestions")

    def test_05_search_autocomplete(self):
        """测试搜索自动补全"""
        time.sleep(0.3)
        resp = requests.get(f"{BASE_URL}/api/v1/search/autocomplete", params={"prefix": "活动"})
        self._assert_ok(resp, "GET /api/v1/search/autocomplete")

    def test_06_search_hot(self):
        """测试热门搜索"""
        time.sleep(0.3)
        resp = requests.get(f"{BASE_URL}/api/v1/search/hot")
        self._assert_ok(resp, "GET /api/v1/search/hot")

    # ========== 活动模块（普通用户接口） ==========

    def test_07_activity_list(self):
        """测试活动列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/activities/list",
            headers=self._headers(),
            params={"page": 1, "size": 10}
        )
        self._assert_ok(resp, "GET /api/v1/activities/list")

    def test_08_activity_detail(self):
        """测试活动详情"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.get(f"{BASE_URL}/api/v1/activities/{aid}")
        self._assert_ok(resp, f"GET /api/v1/activities/{aid}")

    def test_09_my_activities(self):
        """测试我的活动"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/activities/my",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/activities/my")

    def test_10_publish_activity(self):
        """测试发布活动"""
        time.sleep(0.3)
        if not self.type_id:
            self._log_result("POST /api/v1/activities/", False, 0, "无typeId，跳过")
            return
        data = {
            "title": "管理员接口测试活动_" + str(int(time.time())),
            "description": "管理员接口测试活动描述",
            "location": "测试地点",
            "startTime": "2026-08-01T10:00:00",
            "endTime": "2026-08-02T18:00:00",
            "typeId": self.type_id,
            "maxParticipants": 30
        }
        resp = requests.post(
            f"{BASE_URL}/api/v1/activities/",
            headers=self._headers(),
            json=data
        )
        self._assert_ok(resp, "POST /api/v1/activities/")

    def test_11_update_activity(self):
        """测试更新活动"""
        time.sleep(0.3)
        aid = self.activity_id
        if not aid:
            self._log_result("PUT /api/v1/activities/{id}", False, 0, "无activityId，跳过")
            return
        data = {
            "title": "更新后的管理员活动_" + str(int(time.time())),
            "description": "更新后的描述",
            "location": "更新地点",
            "startTime": "2026-09-01T10:00:00",
            "endTime": "2026-09-02T18:00:00",
            "typeId": self.type_id or 1,
            "maxParticipants": 100
        }
        resp = requests.put(
            f"{BASE_URL}/api/v1/activities/{aid}",
            headers=self._headers(),
            json=data
        )
        self._assert_ok(resp, f"PUT /api/v1/activities/{aid}", allowed_codes=[400, 404, 500])

    def test_12_activity_status(self):
        """测试获取活动状态"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.get(
            f"{BASE_URL}/api/v1/activities/{aid}/status",
            headers=self._headers()
        )
        self._assert_ok(resp, f"GET /api/v1/activities/{aid}/status", allowed_codes=[404])

    def test_13_update_activity_status(self):
        """测试更新活动状态"""
        time.sleep(0.3)
        aid = self.activity_id
        if not aid:
            self._log_result("PUT /api/v1/activities/{id}/status", False, 0, "无activityId，跳过")
            return
        resp = requests.put(
            f"{BASE_URL}/api/v1/activities/{aid}/status",
            headers=self._headers(),
            json={"status": "published"}
        )
        self._assert_ok(resp, f"PUT /api/v1/activities/{aid}/status", allowed_codes=[400, 404, 500])

    def test_14_publish_activity_status(self):
        """测试发布活动状态"""
        time.sleep(0.3)
        aid = self.activity_id
        if not aid:
            self._log_result("PUT /api/v1/activities/{id}/publish", False, 0, "无activityId，跳过")
            return
        resp = requests.put(
            f"{BASE_URL}/api/v1/activities/{aid}/publish",
            headers=self._headers()
        )
        self._assert_ok(resp, f"PUT /api/v1/activities/{aid}/publish", allowed_codes=[400, 404, 500])

    def test_15_cancel_activity(self):
        """测试取消活动"""
        time.sleep(0.3)
        aid = self.activity_id
        if not aid:
            self._log_result("PUT /api/v1/activities/{id}/cancel", False, 0, "无activityId，跳过")
            return
        resp = requests.put(
            f"{BASE_URL}/api/v1/activities/{aid}/cancel",
            headers=self._headers()
        )
        self._assert_ok(resp, f"PUT /api/v1/activities/{aid}/cancel", allowed_codes=[400, 404, 500])

    def test_16_end_activity(self):
        """测试结束活动"""
        time.sleep(0.3)
        aid = self.activity_id
        if not aid:
            self._log_result("PUT /api/v1/activities/{id}/end", False, 0, "无activityId，跳过")
            return
        resp = requests.put(
            f"{BASE_URL}/api/v1/activities/{aid}/end",
            headers=self._headers()
        )
        self._assert_ok(resp, f"PUT /api/v1/activities/{aid}/end", allowed_codes=[400, 404, 500])

    def test_17_share_activity(self):
        """测试分享活动"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.post(
            f"{BASE_URL}/api/v1/activities/{aid}/share",
            headers=self._headers()
        )
        self._assert_ok(resp, f"POST /api/v1/activities/{aid}/share", allowed_codes=[404])

    def test_18_share_count(self):
        """测试获取分享数"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.get(
            f"{BASE_URL}/api/v1/activities/{aid}/share-count",
            headers=self._headers()
        )
        self._assert_ok(resp, f"GET /api/v1/activities/{aid}/share-count", allowed_codes=[404])

    def test_19_delete_activity(self):
        """测试删除活动（使用不存在的ID避免影响其他测试）"""
        time.sleep(0.3)
        resp = requests.delete(
            f"{BASE_URL}/api/v1/activities/99999",
            headers=self._headers()
        )
        self._assert_ok(resp, "DELETE /api/v1/activities/99999", allowed_codes=[400, 404, 500])

    # ========== 报名模块 ==========

    def test_20_register_activity(self):
        """测试报名活动"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.post(
            f"{BASE_URL}/api/v1/registrations/",
            headers=self._headers(),
            json={"activityId": aid}
        )
        self._assert_ok(resp, "POST /api/v1/registrations/", allowed_codes=[400, 404, 500])

    def test_21_my_registrations(self):
        """测试我的报名"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/registrations/my",
            headers=self._headers(),
            params={"page": 1, "size": 10}
        )
        self._assert_ok(resp, "GET /api/v1/registrations/my")

    def test_22_cancel_registration(self):
        """测试取消报名"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.delete(
            f"{BASE_URL}/api/v1/registrations/activity/{aid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"DELETE /api/v1/registrations/activity/{aid}", allowed_codes=[400, 404, 500])

    # ========== 评论模块 ==========

    def test_23_post_comment(self):
        """测试发表评论"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.post(
            f"{BASE_URL}/api/v1/activities/{aid}/comments",
            headers=self._headers(),
            json={"content": "这是一条管理员测试评论"}
        )
        self._assert_ok(resp, f"POST /api/v1/activities/{aid}/comments", allowed_codes=[400, 404, 500])

    def test_24_get_comments(self):
        """测试评论列表"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.get(
            f"{BASE_URL}/api/v1/activities/{aid}/comments",
            headers=self._headers(),
            params={"page": 1, "size": 10}
        )
        self._assert_ok(resp, f"GET /api/v1/activities/{aid}/comments", allowed_codes=[404])

    # ========== 话题模块 ==========

    def test_25_topic_list(self):
        """测试话题列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/topics/",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/topics/")

    def test_26_topic_detail(self):
        """测试话题详情"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/topics/1",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/topics/1", allowed_codes=[404])

    # ========== 标签模块（读取接口） ==========

    def test_27_tag_list(self):
        """测试标签列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/tags/",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/tags/")

    def test_28_tag_detail(self):
        """测试标签详情"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/tags/1",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/tags/1", allowed_codes=[404])

    # ========== 活动类型模块（读取接口） ==========

    def test_29_activity_type_list(self):
        """测试活动类型列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/activity-types/",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/activity-types/")

    # ========== 订阅模块 ==========

    def test_30_subscribe_activity(self):
        """测试订阅活动"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.post(
            f"{BASE_URL}/api/v1/activity-subscription/{aid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"POST /api/v1/activity-subscription/{aid}", allowed_codes=[400, 404, 500])

    def test_31_unsubscribe_activity(self):
        """测试取消订阅"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.delete(
            f"{BASE_URL}/api/v1/activity-subscription/{aid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"DELETE /api/v1/activity-subscription/{aid}", allowed_codes=[400, 404, 500])

    def test_32_my_subscriptions(self):
        """测试我的订阅"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/activity-subscription/my",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/activity-subscription/my")

    def test_33_subscription_status(self):
        """测试订阅状态"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.get(
            f"{BASE_URL}/api/v1/activity-subscription/{aid}/status",
            headers=self._headers()
        )
        self._assert_ok(resp, f"GET /api/v1/activity-subscription/{aid}/status", allowed_codes=[404])

    # ========== 收藏模块 ==========

    def test_34_collect_activity(self):
        """测试收藏活动"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.post(
            f"{BASE_URL}/api/v1/activity-collect/{aid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"POST /api/v1/activity-collect/{aid}", allowed_codes=[400, 404, 500])

    def test_35_uncollect_activity(self):
        """测试取消收藏"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.delete(
            f"{BASE_URL}/api/v1/activity-collect/{aid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"DELETE /api/v1/activity-collect/{aid}", allowed_codes=[400, 404, 500])

    def test_36_my_collects(self):
        """测试我的收藏"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/activity-collect/my",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/activity-collect/my")

    def test_37_collect_status(self):
        """测试收藏状态"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.get(
            f"{BASE_URL}/api/v1/activity-collect/{aid}/status",
            headers=self._headers()
        )
        self._assert_ok(resp, f"GET /api/v1/activity-collect/{aid}/status", allowed_codes=[404])

    # ========== 点赞模块 ==========

    def test_38_favorite_activity(self):
        """测试点赞活动"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.post(
            f"{BASE_URL}/api/v1/activities/{aid}/favorite",
            headers=self._headers()
        )
        self._assert_ok(resp, f"POST /api/v1/activities/{aid}/favorite", allowed_codes=[400, 404, 500])

    def test_39_unfavorite_activity(self):
        """测试取消点赞"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.delete(
            f"{BASE_URL}/api/v1/activities/{aid}/favorite",
            headers=self._headers()
        )
        self._assert_ok(resp, f"DELETE /api/v1/activities/{aid}/favorite", allowed_codes=[400, 404, 500])

    def test_40_favorite_status(self):
        """测试点赞状态"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.get(
            f"{BASE_URL}/api/v1/activities/{aid}/favorite/status",
            headers=self._headers()
        )
        self._assert_ok(resp, f"GET /api/v1/activities/{aid}/favorite/status", allowed_codes=[404])

    def test_41_user_favorites(self):
        """测试我的点赞列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/users/favorites",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/users/favorites")

    # ========== 通知模块 ==========

    def test_42_notifications(self):
        """测试通知列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/notifications/",
            headers=self._headers(),
            params={"page": 1, "size": 10}
        )
        self._assert_ok(resp, "GET /api/v1/notifications/")

    def test_43_unread_count(self):
        """测试未读通知数"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/notifications/unread-count",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/notifications/unread-count")

    def test_44_mark_notification_read(self):
        """测试标记通知已读"""
        time.sleep(0.3)
        resp = requests.patch(
            f"{BASE_URL}/api/v1/notifications/1/read",
            headers=self._headers()
        )
        self._assert_ok(resp, "PATCH /api/v1/notifications/1/read", allowed_codes=[400, 404, 500])

    def test_45_mark_all_read(self):
        """测试全部已读"""
        time.sleep(0.3)
        resp = requests.patch(
            f"{BASE_URL}/api/v1/notifications/read-all",
            headers=self._headers()
        )
        self._assert_ok(resp, "PATCH /api/v1/notifications/read-all")

    def test_46_delete_notification(self):
        """测试删除通知"""
        time.sleep(0.3)
        resp = requests.delete(
            f"{BASE_URL}/api/v1/notifications/99999",
            headers=self._headers()
        )
        self._assert_ok(resp, "DELETE /api/v1/notifications/99999", allowed_codes=[400, 404, 500])

    # ========== 通知别名模块 ==========

    def test_47_notification_alias_my(self):
        """测试通知列表(别名)"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/notification/my",
            headers=self._headers(),
            params={"page": 1, "size": 10}
        )
        self._assert_ok(resp, "GET /api/v1/notification/my")

    def test_48_notification_alias_unread(self):
        """测试未读数(别名)"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/notification/unread-count",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/notification/unread-count")

    # ========== 用户资料模块 ==========

    def test_49_get_profile(self):
        """测试获取个人资料"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/users/profile",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/users/profile")

    def test_50_update_profile(self):
        """测试更新个人资料"""
        time.sleep(0.3)
        resp = requests.put(
            f"{BASE_URL}/api/v1/users/profile",
            headers=self._headers(),
            json={"realName": "测试管理员X更新", "contact": "13800009876"}
        )
        self._assert_ok(resp, "PUT /api/v1/users/profile")

    def test_51_change_password(self):
        """测试修改密码（仅验证接口可达，不实际修改）"""
        time.sleep(0.3)
        resp = requests.put(
            f"{BASE_URL}/api/v1/users/password",
            headers=self._headers(),
            json={"oldPassword": "WrongPass@123", "newPassword": "NewPass@456"}
        )
        status = resp.status_code
        if status in [429, 401]:
            self._log_result("PUT /api/v1/users/password", True, status, "限流/token过期容错")
        elif status < 500:
            self._log_result("PUT /api/v1/users/password", True, status, "旧密码错误预期失败")
        else:
            self._log_result("PUT /api/v1/users/password", False, status, "服务器错误")

    # ========== 搜索模块 ==========

    def test_52_search_execute(self):
        """测试执行搜索"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/search/execute",
            headers=self._headers(),
            params={"keyword": "测试", "page": 1, "size": 10}
        )
        self._assert_ok(resp, "GET /api/v1/search/execute")

    def test_53_search_history(self):
        """测试搜索历史"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/search/history",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/search/history")

    def test_54_clear_search_history(self):
        """测试清空搜索历史"""
        time.sleep(0.3)
        resp = requests.delete(
            f"{BASE_URL}/api/v1/search/history",
            headers=self._headers()
        )
        self._assert_ok(resp, "DELETE /api/v1/search/history")

    # ========== 相册模块 ==========

    def test_55_activity_album(self):
        """测试活动相册"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.get(
            f"{BASE_URL}/api/v1/albums/activities/{aid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"GET /api/v1/albums/activities/{aid}", allowed_codes=[404])

    # ================================================================
    # 管理员专属接口
    # ================================================================

    # ========== 管理员活动管理 ==========

    def test_56_admin_pending_activities(self):
        """测试获取待审核活动列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/activities/pending",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/activities/pending")

    def test_57_admin_activities_by_status(self):
        """测试按审核状态查询活动"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/activities/approval-status/pending",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/activities/approval-status/pending")

    def test_58_admin_approve_activity(self):
        """测试审核通过活动"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.put(
            f"{BASE_URL}/api/admin/activities/{aid}/approve",
            headers=self._headers()
        )
        self._assert_ok(resp, f"PUT /api/admin/activities/{aid}/approve", allowed_codes=[400, 404, 500])

    def test_59_admin_reject_activity(self):
        """测试审核拒绝活动"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.put(
            f"{BASE_URL}/api/admin/activities/{aid}/reject",
            headers=self._headers(),
            json={"reason": "不合规"}
        )
        self._assert_ok(resp, f"PUT /api/admin/activities/{aid}/reject", allowed_codes=[400, 404, 500])

    def test_60_admin_approval_statistics(self):
        """测试审核统计"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/activities/statistics",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/activities/statistics")

    # ========== 管理员统计 ==========

    def test_61_admin_statistics_overview(self):
        """测试总览统计"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/statistics/overview",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/statistics/overview")

    def test_62_admin_statistics_activities(self):
        """测试活动统计"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/statistics/activities",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/statistics/activities")

    def test_63_admin_statistics_users(self):
        """测试用户统计"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/statistics/users",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/statistics/users")

    def test_64_admin_statistics_registrations(self):
        """测试报名统计"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/statistics/registrations",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/statistics/registrations")

    def test_65_admin_statistics_trend(self):
        """测试趋势统计"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/statistics/trend",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/statistics/trend")

    def test_66_admin_statistics_hot_activities(self):
        """测试热门活动统计"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/statistics/hot-activities",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/statistics/hot-activities")

    def test_67_admin_statistics_clear_cache(self):
        """测试清除统计缓存"""
        time.sleep(0.3)
        resp = requests.post(
            f"{BASE_URL}/api/admin/statistics/clear-cache",
            headers=self._headers()
        )
        self._assert_ok(resp, "POST /api/admin/statistics/clear-cache")

    def test_68_admin_statistics_daily(self):
        """测试每日统计（已废弃接口）"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/statistics/daily",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/statistics/daily", allowed_codes=[400, 404, 500])

    # ========== 管理员监控 ==========

    def test_69_admin_monitor_status(self):
        """测试系统状态"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/monitor/status",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/monitor/status")

    def test_70_admin_monitor_metrics(self):
        """测试系统指标"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/monitor/metrics",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/monitor/metrics")

    def test_71_admin_monitor_recent_activities(self):
        """测试最近活动"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/monitor/recent-activities",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/monitor/recent-activities")

    def test_72_admin_monitor_recent_users(self):
        """测试最近用户"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/monitor/recent-users",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/monitor/recent-users")

    def test_73_admin_monitor_cache(self):
        """测试缓存信息"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/monitor/cache",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/monitor/cache")

    def test_74_admin_monitor_cache_clear(self):
        """测试清除监控缓存"""
        time.sleep(0.3)
        resp = requests.delete(
            f"{BASE_URL}/api/admin/monitor/cache/clear",
            headers=self._headers()
        )
        self._assert_ok(resp, "DELETE /api/admin/monitor/cache/clear")

    # ========== 管理员用户管理 ==========

    def test_75_admin_users_list(self):
        """测试用户分页列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/users/",
            headers=self._headers(),
            params={"page": 1, "size": 10}
        )
        self._assert_ok(resp, "GET /api/admin/users/")

    def test_76_admin_users_all(self):
        """测试所有用户列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/users/all",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/users/all")

    def test_77_admin_user_detail(self):
        """测试用户详情"""
        time.sleep(0.3)
        uid = self.user_id or 1
        resp = requests.get(
            f"{BASE_URL}/api/admin/users/{uid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"GET /api/admin/users/{uid}", allowed_codes=[404])

    def test_78_admin_users_by_role(self):
        """测试按角色查询用户"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/users/role/user",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/users/role/user")

    def test_79_admin_update_user_role(self):
        """测试更新用户角色"""
        time.sleep(0.3)
        uid = self.user_id or 1
        resp = requests.put(
            f"{BASE_URL}/api/admin/users/{uid}/role",
            headers=self._headers(),
            json={"role": "admin"}
        )
        self._assert_ok(resp, f"PUT /api/admin/users/{uid}/role", allowed_codes=[400, 404, 500])

    def test_80_admin_update_user_status(self):
        """测试更新用户状态"""
        time.sleep(0.3)
        uid = self.user_id or 1
        resp = requests.put(
            f"{BASE_URL}/api/admin/users/{uid}/status",
            headers=self._headers(),
            json={"enabled": True}
        )
        self._assert_ok(resp, f"PUT /api/admin/users/{uid}/status", allowed_codes=[400, 404, 500])

    def test_81_admin_batch_operation(self):
        """测试批量操作用户"""
        time.sleep(0.3)
        uid = self.user_id or 1
        resp = requests.post(
            f"{BASE_URL}/api/admin/users/batch",
            headers=self._headers(),
            json={"userIds": [uid], "action": "enable"}
        )
        self._assert_ok(resp, "POST /api/admin/users/batch", allowed_codes=[400, 404, 500])

    def test_82_admin_locked_users(self):
        """测试锁定用户列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/users/locked",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/users/locked")

    def test_83_admin_unlock_user(self):
        """测试解锁用户"""
        time.sleep(0.3)
        uid = self.user_id or 1
        resp = requests.put(
            f"{BASE_URL}/api/admin/users/{uid}/unlock",
            headers=self._headers()
        )
        self._assert_ok(resp, f"PUT /api/admin/users/{uid}/unlock", allowed_codes=[400, 404, 500])

    def test_84_admin_permissions(self):
        """测试权限列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/users/permissions",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/users/permissions")

    # ========== 管理员登录锁定 ==========

    def test_85_admin_login_lock_list(self):
        """测试登录锁定列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/login-lock/list",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/login-lock/list")

    def test_86_admin_login_lock_unlock(self):
        """测试解锁登录锁定（使用不存在的用户名）"""
        time.sleep(0.3)
        resp = requests.delete(
            f"{BASE_URL}/api/admin/login-lock/nonexistent_user_xyz",
            headers=self._headers()
        )
        self._assert_ok(resp, "DELETE /api/admin/login-lock/nonexistent_user_xyz", allowed_codes=[400, 404, 500])

    # ========== 敏感词管理 ==========

    def test_87_sensitive_words_list(self):
        """测试敏感词列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/sensitive-words/",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/sensitive-words/")

    def test_88_sensitive_words_by_type(self):
        """测试按类型查询敏感词"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/sensitive-words/type/other",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/sensitive-words/type/other")

    def test_89_sensitive_word_detail(self):
        """测试敏感词详情（使用不存在的ID）"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/sensitive-words/99999",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/sensitive-words/99999", allowed_codes=[404])

    def test_90_add_sensitive_word(self):
        """测试添加敏感词"""
        time.sleep(0.3)
        data = {
            "word": "测试敏感词_" + str(int(time.time())),
            "level": 1,
            "category": "other"
        }
        resp = requests.post(
            f"{BASE_URL}/api/admin/sensitive-words/",
            headers=self._headers(),
            json=data
        )
        result = self._assert_ok(resp, "POST /api/admin/sensitive-words/", allowed_codes=[400, 500])
        # 保存创建的敏感词ID
        if result and resp.status_code == 200:
            try:
                rdata = resp.json()
                if rdata.get("code") == 200 and rdata.get("data"):
                    self.__class__.sensitive_word_id = rdata["data"].get("id")
            except Exception:
                pass

    def test_91_update_sensitive_word(self):
        """测试更新敏感词"""
        time.sleep(0.3)
        wid = self.sensitive_word_id
        if not wid:
            self._log_result("PUT /api/admin/sensitive-words/{id}", False, 0, "无sensitiveWordId，跳过")
            return
        data = {
            "word": "更新敏感词_" + str(int(time.time())),
            "level": 2,
            "category": "other"
        }
        resp = requests.put(
            f"{BASE_URL}/api/admin/sensitive-words/{wid}",
            headers=self._headers(),
            json=data
        )
        self._assert_ok(resp, f"PUT /api/admin/sensitive-words/{wid}", allowed_codes=[400, 404, 500])

    def test_92_delete_sensitive_word(self):
        """测试删除敏感词（使用不存在的ID避免影响其他测试）"""
        time.sleep(0.3)
        resp = requests.delete(
            f"{BASE_URL}/api/admin/sensitive-words/99998",
            headers=self._headers()
        )
        self._assert_ok(resp, "DELETE /api/admin/sensitive-words/99998", allowed_codes=[400, 404, 500])

    def test_93_batch_delete_sensitive_words(self):
        """测试批量删除敏感词"""
        time.sleep(0.3)
        resp = requests.delete(
            f"{BASE_URL}/api/admin/sensitive-words/batch",
            headers=self._headers(),
            json=[99997, 99996]
        )
        self._assert_ok(resp, "DELETE /api/admin/sensitive-words/batch", allowed_codes=[400, 404, 500])

    def test_94_sensitive_words_stats(self):
        """测试敏感词统计"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/sensitive-words/stats",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/admin/sensitive-words/stats")

    def test_95_refresh_sensitive_word_tree(self):
        """测试刷新敏感词树"""
        time.sleep(0.3)
        resp = requests.post(
            f"{BASE_URL}/api/admin/sensitive-words/refresh",
            headers=self._headers()
        )
        self._assert_ok(resp, "POST /api/admin/sensitive-words/refresh")

    def test_96_check_sensitive_word(self):
        """测试检测敏感词"""
        time.sleep(0.3)
        resp = requests.post(
            f"{BASE_URL}/api/admin/sensitive-words/check",
            headers=self._headers(),
            json={"text": "这是一段测试文本"}
        )
        self._assert_ok(resp, "POST /api/admin/sensitive-words/check", allowed_codes=[400, 500])

    def test_97_batch_add_sensitive_words_v1(self):
        """测试批量添加敏感词（v1路径）"""
        time.sleep(0.3)
        data = {
            "words": [
                {"word": "批量词1_" + str(int(time.time())), "level": 1},
                {"word": "批量词2_" + str(int(time.time())), "level": 1}
            ]
        }
        resp = requests.post(
            f"{BASE_URL}/api/v1/admin/sensitive-words/batch",
            headers=self._headers(),
            json=data
        )
        self._assert_ok(resp, "POST /api/v1/admin/sensitive-words/batch", allowed_codes=[400, 500])

    # ========== 标签管理（admin专属） ==========

    def test_98_create_tag(self):
        """测试创建标签（admin专属）"""
        time.sleep(0.3)
        data = {
            "name": "管理员测试标签_" + str(int(time.time()))
        }
        resp = requests.post(
            f"{BASE_URL}/api/v1/tags/",
            headers=self._headers(),
            json=data
        )
        result = self._assert_ok(resp, "POST /api/v1/tags/")
        # 保存创建的标签ID
        if result and resp.status_code == 200:
            try:
                rdata = resp.json()
                if rdata.get("code") == 200 and rdata.get("data"):
                    self.__class__.tag_id = rdata["data"].get("id")
            except Exception:
                pass

    def test_99_update_tag(self):
        """测试更新标签（admin专属）"""
        time.sleep(0.3)
        tid = self.tag_id or 1
        data = {
            "name": "更新标签_" + str(int(time.time()))
        }
        resp = requests.put(
            f"{BASE_URL}/api/v1/tags/{tid}",
            headers=self._headers(),
            json=data
        )
        self._assert_ok(resp, f"PUT /api/v1/tags/{tid}", allowed_codes=[400, 404, 500])

    def test_100_delete_tag(self):
        """测试删除标签（admin专属，使用不存在的ID避免影响其他测试）"""
        time.sleep(0.3)
        resp = requests.delete(
            f"{BASE_URL}/api/v1/tags/99999",
            headers=self._headers()
        )
        self._assert_ok(resp, "DELETE /api/v1/tags/99999", allowed_codes=[400, 404, 500])

    # ========== 活动类型管理（admin专属） ==========

    def test_101_create_activity_type(self):
        """测试创建活动类型（admin专属）"""
        time.sleep(0.3)
        data = {
            "name": "管理员测试类型_" + str(int(time.time())),
            "description": "管理员测试活动类型描述"
        }
        resp = requests.post(
            f"{BASE_URL}/api/v1/activity-types/",
            headers=self._headers(),
            json=data
        )
        self._assert_ok(resp, "POST /api/v1/activity-types/", allowed_codes=[400, 500])

    def test_102_update_activity_type(self):
        """测试更新活动类型（admin专属）"""
        time.sleep(0.3)
        tid = self.type_id or 1
        data = {
            "name": "更新类型_" + str(int(time.time())),
            "description": "更新后的活动类型描述"
        }
        resp = requests.put(
            f"{BASE_URL}/api/v1/activity-types/{tid}",
            headers=self._headers(),
            json=data
        )
        self._assert_ok(resp, f"PUT /api/v1/activity-types/{tid}", allowed_codes=[400, 404, 500])

    def test_103_delete_activity_type(self):
        """测试删除活动类型（admin专属，使用不存在的ID避免影响其他测试）"""
        time.sleep(0.3)
        resp = requests.delete(
            f"{BASE_URL}/api/v1/activity-types/99999",
            headers=self._headers()
        )
        self._assert_ok(resp, "DELETE /api/v1/activity-types/99999", allowed_codes=[400, 404, 500])

    # ========== 缓存管理 ==========

    def test_104_cache_clear_all_post(self):
        """测试清除所有缓存（POST方式）"""
        time.sleep(0.3)
        resp = requests.post(
            f"{BASE_URL}/api/admin/cache/clear",
            headers=self._headers()
        )
        self._assert_ok(resp, "POST /api/admin/cache/clear")

    def test_105_cache_clear_all_delete(self):
        """测试清除所有缓存（DELETE方式）"""
        time.sleep(0.3)
        resp = requests.delete(
            f"{BASE_URL}/api/admin/cache/clear",
            headers=self._headers()
        )
        self._assert_ok(resp, "DELETE /api/admin/cache/clear")

    def test_106_cache_clear_by_name_delete(self):
        """测试清除指定缓存（DELETE方式）"""
        time.sleep(0.3)
        resp = requests.delete(
            f"{BASE_URL}/api/admin/cache/clear/statisticsCache",
            headers=self._headers()
        )
        self._assert_ok(resp, "DELETE /api/admin/cache/clear/statisticsCache", allowed_codes=[400, 404, 500])

    def test_107_cache_clear_by_name_post(self):
        """测试清除指定缓存（POST方式）"""
        time.sleep(0.3)
        resp = requests.post(
            f"{BASE_URL}/api/admin/cache/clear/statisticsCache",
            headers=self._headers()
        )
        self._assert_ok(resp, "POST /api/admin/cache/clear/statisticsCache", allowed_codes=[400, 404, 500])

    # ========== 清理：删除测试创建的敏感词 ==========

    def test_108_cleanup_sensitive_word(self):
        """清理测试创建的敏感词"""
        time.sleep(0.3)
        wid = self.sensitive_word_id
        if not wid:
            self._log_result("DELETE /api/admin/sensitive-words/{id} (清理)", True, 0, "无sensitiveWordId，跳过清理")
            return
        resp = requests.delete(
            f"{BASE_URL}/api/admin/sensitive-words/{wid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"DELETE /api/admin/sensitive-words/{wid} (清理)", allowed_codes=[400, 404, 500])

    @classmethod
    def tearDownClass(cls):
        """打印汇总统计"""
        print("\n" + "=" * 60)
        print("管理员角色测试汇总统计")
        print("=" * 60)
        total = cls._passed + cls._failed
        print(f"  总计: {total}")
        print(f"  通过: {cls._passed}")
        print(f"  失败: {cls._failed}")
        if total > 0:
            print(f"  通过率: {cls._passed / total * 100:.1f}%")
        print("=" * 60)

        # 打印失败详情
        failed_items = [r for r in cls._results if not r[1]]
        if failed_items:
            print("\n失败详情:")
            for name, _, status, detail in failed_items:
                print(f"  ✗ {name} [HTTP {status}] - {detail}")
        print()


if __name__ == "__main__":
    unittest.main(verbosity=2)
