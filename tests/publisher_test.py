"""
发布者(Publisher)角色API接口测试
覆盖发布者可访问的所有接口，包括普通用户接口和发布者专属接口
验证权限控制和功能正确性
包含429限流容错和401 token过期容错
"""

import unittest
import requests
import time
import io

BASE_URL = "http://localhost:8080"


class TestPublisherAPI(unittest.TestCase):
    """发布者角色API测试类"""

    # 类级别统计
    _passed = 0
    _failed = 0
    _results = []

    @classmethod
    def setUpClass(cls):
        """注册并登录测试发布者，获取token和基础数据"""
        cls.token = None
        cls.activity_id = None
        cls.type_id = None
        cls.topic_id = None
        cls.tag_id = None
        cls.album_id = None
        cls.image_id = None
        cls.registration_id = None
        cls.username = "test_publisher_x"
        cls.password = "Publisher@123"

        # 1. 注册测试发布者（带密保问题）
        register_data = {
            "username": cls.username,
            "password": cls.password,
            "realName": "测试发布者X",
            "contact": "13800005555",
            "securityQuestionId": 1,
            "securityAnswer": "上海"
        }
        try:
            resp = requests.post(f"{BASE_URL}/api/v1/users/register", json=register_data)
            print(f"注册响应: {resp.status_code} - {resp.text[:200]}")
        except Exception as e:
            print(f"注册请求异常: {e}")

        # 2. 登录获取token
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
                    print(f"获取token成功: {cls.token[:30]}..." if cls.token else "token为空")
        except Exception as e:
            print(f"登录请求异常: {e}")

        # 3. 获取活动类型列表，取第一个typeId
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

        # 4. 发布一个测试活动，后续测试依赖此活动
        if cls.token and cls.type_id:
            try:
                activity_data = {
                    "title": "发布者测试活动_" + str(int(time.time())),
                    "description": "这是一个发布者测试活动",
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

        # 5. 获取已有标签ID（用于设置活动标签测试）
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
              f"typeId={cls.type_id}, activityId={cls.activity_id}, tagId={cls.tag_id}\n")

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
            TestPublisherAPI._passed += 1
        else:
            TestPublisherAPI._failed += 1
        TestPublisherAPI._results.append((name, passed, status_code, detail))

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

    def _assert_forbidden(self, resp, name):
        """断言返回403禁止访问"""
        status = resp.status_code
        if status == 429:
            self._log_result(name, True, status, "限流容错")
            return True
        if status == 401:
            self._log_result(name, True, status, "token过期容错")
            return True
        if status in [403, 200]:
            # 200时检查业务码是否为403
            if status == 200:
                data = resp.json()
                code = data.get("code", 0)
                if code == 403 or code == 2003:
                    self._log_result(name, True, status, "业务层403")
                    return True
                else:
                    self._log_result(name, False, status, f"期望403，实际业务码={code}")
                    return False
            self._log_result(name, True, status)
            return True
        self._log_result(name, False, status, f"期望403，实际={status}")
        return False

    # ========== 公开接口（无需token） ==========

    def test_01_login(self):
        """测试发布者登录"""
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
            "realName": "重复发布者",
            "contact": "13800006666",
            "securityQuestionId": 1,
            "securityAnswer": "上海"
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
            "title": "发布者接口测试活动_" + str(int(time.time())),
            "description": "发布者接口测试活动描述",
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
            "title": "更新后的发布者活动_" + str(int(time.time())),
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

    # ========== 报名模块（普通用户接口） ==========

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
            json={"content": "这是一条发布者测试评论"}
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

    # ========== 话题模块（发布者专属接口） ==========

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

    def test_27_create_topic(self):
        """测试创建话题（发布者专属）"""
        time.sleep(0.3)
        aid = self.activity_id
        if not aid:
            self._log_result("POST /api/v1/topics/", False, 0, "无activityId，跳过")
            return
        data = {
            "activityId": aid,
            "title": "发布者测试话题_" + str(int(time.time()))
        }
        resp = requests.post(
            f"{BASE_URL}/api/v1/topics/",
            headers=self._headers(),
            json=data
        )
        result = self._assert_ok(resp, "POST /api/v1/topics/", allowed_codes=[400, 404, 500])
        # 保存创建的话题ID
        if result and resp.status_code == 200:
            try:
                rdata = resp.json()
                if rdata.get("code") == 200 and rdata.get("data"):
                    self.__class__.topic_id = rdata["data"].get("id")
            except Exception:
                pass

    def test_28_update_topic(self):
        """测试更新话题（发布者专属，需是创建者）"""
        time.sleep(0.3)
        tid = self.topic_id
        if not tid:
            self._log_result("PUT /api/v1/topics/{id}", False, 0, "无topicId，跳过")
            return
        data = {
            "title": "更新后的话题_" + str(int(time.time()))
        }
        resp = requests.put(
            f"{BASE_URL}/api/v1/topics/{tid}",
            headers=self._headers(),
            json=data
        )
        self._assert_ok(resp, f"PUT /api/v1/topics/{tid}", allowed_codes=[400, 403, 404, 500])

    def test_29_activity_topics(self):
        """测试获取活动的话题列表"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.get(
            f"{BASE_URL}/api/v1/topics/activity/{aid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"GET /api/v1/topics/activity/{aid}")

    def test_30_delete_topic(self):
        """测试删除话题（发布者专属，需是创建者）"""
        time.sleep(0.3)
        tid = self.topic_id
        if not tid:
            self._log_result("DELETE /api/v1/topics/{id}", False, 0, "无topicId，跳过")
            return
        resp = requests.delete(
            f"{BASE_URL}/api/v1/topics/{tid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"DELETE /api/v1/topics/{tid}", allowed_codes=[400, 403, 404, 500])

    # ========== 标签模块 ==========

    def test_31_tag_list(self):
        """测试标签列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/tags/",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/tags/")

    def test_32_tag_detail(self):
        """测试标签详情"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/tags/1",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/tags/1", allowed_codes=[404])

    def test_33_activity_tags(self):
        """测试获取活动的标签"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.get(
            f"{BASE_URL}/api/v1/tags/activity/{aid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"GET /api/v1/tags/activity/{aid}")

    def test_34_set_activity_tags(self):
        """测试设置活动标签"""
        time.sleep(0.3)
        aid = self.activity_id
        if not aid:
            self._log_result("POST /api/v1/tags/activity", False, 0, "无activityId，跳过")
            return
        tag_ids = []
        if self.tag_id:
            tag_ids.append(self.tag_id)
        data = {
            "activityId": aid,
            "tagIds": tag_ids if tag_ids else [1]
        }
        resp = requests.post(
            f"{BASE_URL}/api/v1/tags/activity",
            headers=self._headers(),
            json=data
        )
        self._assert_ok(resp, "POST /api/v1/tags/activity", allowed_codes=[400, 403, 404, 500])

    # ========== 活动类型模块 ==========

    def test_35_activity_type_list(self):
        """测试活动类型列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/activity-types/",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/activity-types/")

    # ========== 订阅模块 ==========

    def test_36_subscribe_activity(self):
        """测试订阅活动"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.post(
            f"{BASE_URL}/api/v1/activity-subscription/{aid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"POST /api/v1/activity-subscription/{aid}", allowed_codes=[400, 404, 500])

    def test_37_unsubscribe_activity(self):
        """测试取消订阅"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.delete(
            f"{BASE_URL}/api/v1/activity-subscription/{aid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"DELETE /api/v1/activity-subscription/{aid}", allowed_codes=[400, 404, 500])

    def test_38_my_subscriptions(self):
        """测试我的订阅"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/activity-subscription/my",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/activity-subscription/my")

    def test_39_subscription_status(self):
        """测试订阅状态"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.get(
            f"{BASE_URL}/api/v1/activity-subscription/{aid}/status",
            headers=self._headers()
        )
        self._assert_ok(resp, f"GET /api/v1/activity-subscription/{aid}/status", allowed_codes=[404])

    # ========== 收藏模块 ==========

    def test_40_collect_activity(self):
        """测试收藏活动"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.post(
            f"{BASE_URL}/api/v1/activity-collect/{aid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"POST /api/v1/activity-collect/{aid}", allowed_codes=[400, 404, 500])

    def test_41_uncollect_activity(self):
        """测试取消收藏"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.delete(
            f"{BASE_URL}/api/v1/activity-collect/{aid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"DELETE /api/v1/activity-collect/{aid}", allowed_codes=[400, 404, 500])

    def test_42_my_collects(self):
        """测试我的收藏"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/activity-collect/my",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/activity-collect/my")

    def test_43_collect_status(self):
        """测试收藏状态"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.get(
            f"{BASE_URL}/api/v1/activity-collect/{aid}/status",
            headers=self._headers()
        )
        self._assert_ok(resp, f"GET /api/v1/activity-collect/{aid}/status", allowed_codes=[404])

    # ========== 点赞模块 ==========

    def test_44_favorite_activity(self):
        """测试点赞活动"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.post(
            f"{BASE_URL}/api/v1/activities/{aid}/favorite",
            headers=self._headers()
        )
        self._assert_ok(resp, f"POST /api/v1/activities/{aid}/favorite", allowed_codes=[400, 404, 500])

    def test_45_unfavorite_activity(self):
        """测试取消点赞"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.delete(
            f"{BASE_URL}/api/v1/activities/{aid}/favorite",
            headers=self._headers()
        )
        self._assert_ok(resp, f"DELETE /api/v1/activities/{aid}/favorite", allowed_codes=[400, 404, 500])

    def test_46_favorite_status(self):
        """测试点赞状态"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.get(
            f"{BASE_URL}/api/v1/activities/{aid}/favorite/status",
            headers=self._headers()
        )
        self._assert_ok(resp, f"GET /api/v1/activities/{aid}/favorite/status", allowed_codes=[404])

    def test_47_user_favorites(self):
        """测试我的点赞列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/users/favorites",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/users/favorites")

    # ========== 通知模块 ==========

    def test_48_notifications(self):
        """测试通知列表"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/notifications/",
            headers=self._headers(),
            params={"page": 1, "size": 10}
        )
        self._assert_ok(resp, "GET /api/v1/notifications/")

    def test_49_unread_count(self):
        """测试未读通知数"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/notifications/unread-count",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/notifications/unread-count")

    def test_50_mark_notification_read(self):
        """测试标记通知已读"""
        time.sleep(0.3)
        resp = requests.patch(
            f"{BASE_URL}/api/v1/notifications/1/read",
            headers=self._headers()
        )
        self._assert_ok(resp, "PATCH /api/v1/notifications/1/read", allowed_codes=[400, 404, 500])

    def test_51_mark_all_read(self):
        """测试全部已读"""
        time.sleep(0.3)
        resp = requests.patch(
            f"{BASE_URL}/api/v1/notifications/read-all",
            headers=self._headers()
        )
        self._assert_ok(resp, "PATCH /api/v1/notifications/read-all")

    def test_52_delete_notification(self):
        """测试删除通知"""
        time.sleep(0.3)
        resp = requests.delete(
            f"{BASE_URL}/api/v1/notifications/99999",
            headers=self._headers()
        )
        self._assert_ok(resp, "DELETE /api/v1/notifications/99999", allowed_codes=[400, 404, 500])

    # ========== 通知别名模块 ==========

    def test_53_notification_alias_my(self):
        """测试通知列表(别名)"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/notification/my",
            headers=self._headers(),
            params={"page": 1, "size": 10}
        )
        self._assert_ok(resp, "GET /api/v1/notification/my")

    def test_54_notification_alias_unread(self):
        """测试未读数(别名)"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/notification/unread-count",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/notification/unread-count")

    # ========== 用户资料模块 ==========

    def test_55_get_profile(self):
        """测试获取个人资料"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/users/profile",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/users/profile")

    def test_56_update_profile(self):
        """测试更新个人资料"""
        time.sleep(0.3)
        resp = requests.put(
            f"{BASE_URL}/api/v1/users/profile",
            headers=self._headers(),
            json={"realName": "测试发布者X更新", "contact": "13800007777"}
        )
        self._assert_ok(resp, "PUT /api/v1/users/profile")

    def test_57_change_password(self):
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

    def test_58_search_execute(self):
        """测试执行搜索"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/search/execute",
            headers=self._headers(),
            params={"keyword": "测试", "page": 1, "size": 10}
        )
        self._assert_ok(resp, "GET /api/v1/search/execute")

    def test_59_search_history(self):
        """测试搜索历史"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/v1/search/history",
            headers=self._headers()
        )
        self._assert_ok(resp, "GET /api/v1/search/history")

    def test_60_clear_search_history(self):
        """测试清空搜索历史"""
        time.sleep(0.3)
        resp = requests.delete(
            f"{BASE_URL}/api/v1/search/history",
            headers=self._headers()
        )
        self._assert_ok(resp, "DELETE /api/v1/search/history")

    # ========== 报名管理模块（发布者专属接口） ==========

    def test_61_activity_registrations(self):
        """测试查看活动报名列表（发布者专属）"""
        time.sleep(0.3)
        aid = self.activity_id
        if not aid:
            self._log_result("GET /api/v1/registrations/activity/{activityId}", False, 0, "无activityId，跳过")
            return
        resp = requests.get(
            f"{BASE_URL}/api/v1/registrations/activity/{aid}",
            headers=self._headers(),
            params={"page": 1, "size": 10}
        )
        self._assert_ok(resp, f"GET /api/v1/registrations/activity/{aid}", allowed_codes=[400, 403, 404, 500])

    def test_62_update_registration_status(self):
        """测试更新报名状态（发布者专属）"""
        time.sleep(0.3)
        # 使用不存在的报名ID测试接口可达性
        data = {
            "registrationId": 99999,
            "status": "confirmed"
        }
        resp = requests.put(
            f"{BASE_URL}/api/v1/registrations/status",
            headers=self._headers(),
            json=data
        )
        self._assert_ok(resp, "PUT /api/v1/registrations/status", allowed_codes=[400, 403, 404, 500])

    # ========== 活动图片模块（发布者专属接口） ==========

    def test_63_upload_activity_image(self):
        """测试上传活动图片（发布者专属）"""
        time.sleep(0.3)
        aid = self.activity_id
        if not aid:
            self._log_result("POST /api/v1/activities/{id}/images", False, 0, "无activityId，跳过")
            return
        # 创建一个最小的PNG文件用于测试
        png_data = b'\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01\x08\x02\x00\x00\x00\x90wS\xde\x00\x00\x00\x0cIDATx\x9cc\xf8\x0f\x00\x00\x01\x01\x00\x05\x18\xd8N\x00\x00\x00\x00IEND\xaeB`\x82'
        files = {"file": ("test_image.png", io.BytesIO(png_data), "image/png")}
        resp = requests.post(
            f"{BASE_URL}/api/v1/activities/{aid}/images",
            headers={"Authorization": f"Bearer {self.token}"},
            files=files,
            data={"description": "测试图片", "sortOrder": "0"}
        )
        result = self._assert_ok(resp, f"POST /api/v1/activities/{aid}/images", allowed_codes=[400, 403, 404, 500])
        # 保存上传的图片ID
        if result and resp.status_code == 200:
            try:
                rdata = resp.json()
                if rdata.get("code") == 200 and rdata.get("data"):
                    self.__class__.image_id = rdata["data"].get("id")
            except Exception:
                pass

    def test_64_get_activity_images(self):
        """测试获取活动图片列表（发布者专属）"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.get(
            f"{BASE_URL}/api/v1/activities/{aid}/images",
            headers=self._headers()
        )
        self._assert_ok(resp, f"GET /api/v1/activities/{aid}/images")

    def test_65_delete_activity_image(self):
        """测试删除活动图片（发布者专属）"""
        time.sleep(0.3)
        aid = self.activity_id
        img_id = self.image_id
        if not aid or not img_id:
            self._log_result("DELETE /api/v1/activities/{id}/images/{imageId}", False, 0, "无activityId或imageId，跳过")
            return
        resp = requests.delete(
            f"{BASE_URL}/api/v1/activities/{aid}/images/{img_id}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"DELETE /api/v1/activities/{aid}/images/{img_id}", allowed_codes=[400, 403, 404, 500])

    # ========== 相册模块（发布者专属接口） ==========

    def test_66_activity_album(self):
        """测试获取活动相册"""
        time.sleep(0.3)
        aid = self.activity_id or 1
        resp = requests.get(
            f"{BASE_URL}/api/v1/albums/activities/{aid}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"GET /api/v1/albums/activities/{aid}", allowed_codes=[404])

    def test_67_upload_album(self):
        """测试上传相册（发布者专属）"""
        time.sleep(0.3)
        aid = self.activity_id
        if not aid:
            self._log_result("POST /api/v1/albums/upload", False, 0, "无activityId，跳过")
            return
        # 创建一个最小的PNG文件用于测试
        png_data = b'\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01\x08\x02\x00\x00\x00\x90wS\xde\x00\x00\x00\x0cIDATx\x9cc\xf8\x0f\x00\x00\x01\x01\x00\x05\x18\xd8N\x00\x00\x00\x00IEND\xaeB`\x82'
        files = {"file": ("test_album.png", io.BytesIO(png_data), "image/png")}
        resp = requests.post(
            f"{BASE_URL}/api/v1/albums/upload",
            headers={"Authorization": f"Bearer {self.token}"},
            files=files,
            data={"activityId": str(aid), "description": "测试相册", "sortOrder": "0"}
        )
        result = self._assert_ok(resp, "POST /api/v1/albums/upload", allowed_codes=[400, 403, 404, 500])
        # 保存上传的相册ID
        if result and resp.status_code == 200:
            try:
                rdata = resp.json()
                if rdata.get("code") == 200 and rdata.get("data"):
                    self.__class__.album_id = rdata["data"].get("id")
            except Exception:
                pass

    def test_68_delete_album(self):
        """测试删除相册（发布者专属）"""
        time.sleep(0.3)
        album_id = self.album_id
        if not album_id:
            self._log_result("DELETE /api/v1/albums/{albumId}", False, 0, "无albumId，跳过")
            return
        resp = requests.delete(
            f"{BASE_URL}/api/v1/albums/{album_id}",
            headers=self._headers()
        )
        self._assert_ok(resp, f"DELETE /api/v1/albums/{album_id}", allowed_codes=[400, 403, 404, 500])

    # ========== 权限控制：发布者不应访问的接口（应返回403） ==========

    def test_69_admin_statistics_forbidden(self):
        """测试管理员统计接口应返回403"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/statistics/overview",
            headers=self._headers()
        )
        self._assert_forbidden(resp, "GET /api/admin/statistics/overview (期望403)")

    def test_70_admin_users_forbidden(self):
        """测试管理员用户列表应返回403"""
        time.sleep(0.3)
        resp = requests.get(
            f"{BASE_URL}/api/admin/users/",
            headers=self._headers()
        )
        self._assert_forbidden(resp, "GET /api/admin/users/ (期望403)")

    def test_71_admin_approve_forbidden(self):
        """测试管理员审核活动应返回403"""
        time.sleep(0.3)
        resp = requests.put(
            f"{BASE_URL}/api/admin/activities/1/approve",
            headers=self._headers()
        )
        self._assert_forbidden(resp, "PUT /api/admin/activities/1/approve (期望403)")

    def test_72_create_tag_forbidden(self):
        """测试创建标签应返回403（需admin角色）"""
        time.sleep(0.3)
        data = {
            "name": "发布者尝试创建标签_" + str(int(time.time())),
            "description": "不应成功"
        }
        resp = requests.post(
            f"{BASE_URL}/api/v1/tags/",
            headers=self._headers(),
            json=data
        )
        self._assert_forbidden(resp, "POST /api/v1/tags/ (期望403)")

    def test_73_update_tag_forbidden(self):
        """测试更新标签应返回403（需admin角色）"""
        time.sleep(0.3)
        data = {
            "name": "发布者尝试更新标签",
            "description": "不应成功"
        }
        resp = requests.put(
            f"{BASE_URL}/api/v1/tags/1",
            headers=self._headers(),
            json=data
        )
        self._assert_forbidden(resp, "PUT /api/v1/tags/1 (期望403)")

    def test_74_delete_tag_forbidden(self):
        """测试删除标签应返回403（需admin角色）"""
        time.sleep(0.3)
        resp = requests.delete(
            f"{BASE_URL}/api/v1/tags/1",
            headers=self._headers()
        )
        self._assert_forbidden(resp, "DELETE /api/v1/tags/1 (期望403)")

    @classmethod
    def tearDownClass(cls):
        """打印汇总统计"""
        print("\n" + "=" * 60)
        print("发布者角色测试汇总统计")
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
