package com.campus;

import com.campus.CampusApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 集成测试基类
 * 使用test配置文件，每个测试方法后回滚事务
 */
@SpringBootTest(classes = CampusApplication.class)
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {
}
