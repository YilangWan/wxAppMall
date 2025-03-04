# 微信商城小程序后端

基于Spring Boot的微信商城小程序后端项目，实现了商城首页查询所有商品的功能（分页查询）。

## 技术栈

- Spring Boot 2.7.14
- MyBatis Plus 3.5.3.1
- Redis
- MySQL
- Swagger 2.9.2
- Lombok
- Hutool
- Fastjson

## 项目结构

```
wxmall
├── src/main/java/com/wxmall
│   ├── common                 # 通用类
│   │   ├── api                # API相关类
│   │   └── exception          # 异常处理
│   ├── config                 # 配置类
│   ├── controller             # 控制器
│   ├── dto                    # 数据传输对象
│   ├── mapper                 # MyBatis映射接口
│   ├── model                  # 实体类
│   ├── service                # 服务接口
│   │   └── impl               # 服务实现类
│   ├── vo                     # 视图对象
│   └── WxMallApplication.java # 应用程序入口
├── src/main/resources
│   ├── db                     # 数据库脚本
│   ├── mapper                 # MyBatis XML映射文件
│   ├── application.yml        # 应用配置文件
│   └── logback-spring.xml     # 日志配置文件
└── pom.xml                    # Maven配置文件
```

## 功能特点

- 统一的业务异常处理
- 统一的API返回格式
- 基于Redis的缓存实现
- 基于MyBatis Plus的分页查询
- 完善的日志配置
- Swagger API文档

## 运行步骤

### 1. 准备环境

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+
- Redis

### 2. 创建数据库

执行`src/main/resources/db/schema.sql`创建数据库和表结构。

### 3. 插入测试数据

执行`src/main/resources/db/data.sql`插入测试数据。

### 4. 修改配置

根据实际环境修改`src/main/resources/application.yml`中的数据库和Redis配置。

### 5. 编译运行

```bash
# 编译
mvn clean package

# 运行
java -jar target/wxmall-0.0.1-SNAPSHOT.jar
```

### 6. 访问API文档

启动应用后，访问 http://localhost:8080/swagger-ui.html 查看API文档。

## API接口

### 商品接口

- `GET /api/products` - 分页查询商品列表
  - 参数：
    - pageNum - 页码（默认1）
    - pageSize - 每页数量（默认10）
    - keyword - 搜索关键字（可选）
    - categoryId - 商品分类ID（可选）
    - orderBy - 排序方式（可选，price_asc-价格升序；price_desc-价格降序；sale_desc-销量降序）

- `GET /api/products/{id}` - 获取商品详情
  - 参数：
    - id - 商品ID
