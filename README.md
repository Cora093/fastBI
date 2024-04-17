## 智能BI平台



### 项目介绍

智能BI数据可视化平台

帆软BI、微软Power BI

> BI，全称商业智能（Business Intelligence），是一种通过各种技术进行数据分析以辅助决策，提升决策效率的方案。它通常被视为一套完整的解决方案，用于将企业的数据有效整合，快速制作出报表以作出决策。BI在数据架构中处于前端分析的位置，其核心作用是对获取的企业数据进行分析和挖掘，为企业提供有价值的信息和洞察，从而帮助企业做出更明智的商业决策。



传统BI平台：
https://chartcube.alipay.com/
需要人工上传、分析、选择 

智能BI平台：
只需导入最原始的数据集和分析目标，就能得到图表和结论
**节约人力成本**



![image-20231109155354414](https://cora-typora-test-2023.oss-cn-shanghai.aliyuncs.com/pics/image-20231109155354414.png)





### 需求分析

1. 智能分析，自动生成图表和结论
2. 图表管理
3. 异步化图表生成（消息队列）
4. 扩展：AI切换/自定义优化





### 项目实现

#### 后端技术栈

1. SpringBoot
2. MySQL
3. Mybatis Plus
4. 消息队列(RabbitMQ)
5. AI接口
6. EasyExcel 解析数据表格
7. Swagger + Knife4j 项目接口文档
8. Hutool工具库




#### 数据库表设计

```SQL
-- 用户表
create table if not exists fastbi.user
(
    id            bigint auto_increment comment 'id'
        primary key,
    user_account  varchar(256)                           not null comment '账号',
    user_password varchar(512)                           not null comment '密码',
    user_name     varchar(256)                           null comment '用户昵称',
    user_avatar   varchar(1024)                          null comment '用户头像',
    user_role     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    create_time   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint      default 0                 not null comment '是否删除'
)
    comment '用户' collate = utf8mb4_unicode_ci;

create index user_user_account_index
    on fastbi.user (user_account);


```



``` SQL
-- 图表信息表
create table if not exists fastbi.chart
(
    id              bigint auto_increment comment 'id'
        primary key,
    name            varchar(256)                       not null comment '图表名称',
    goal            text                               null comment '任务分析目标',
    origin_data     text                               null comment '原始输入数据',
    chart_type      varchar(128)                       null comment '图表类型',
    generate_chart  text                               null comment '生成的图表数据',
    generate_result text                               null comment '生成的分析结论',
    user_id         bigint                             null comment '创建者id',
    create_time     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete       tinyint  default 0                 not null comment '是否删除'
)
    comment '图表信息表' collate = utf8mb4_unicode_ci;
```



#### 智能分析模块流程

1. 用户输入
   1. 分析目标
   2. 上传原始数据(excel)
   3. 控制图表类型、图表名称等
2. 后端校验
   1. 校验用户的输入否合法(比如长度）
   2. 图表数据压缩处理
   3. 成本控制(次数统计和校验、鉴权等)
3. 把处理后的数据输入给AI模型，调用AI接口返回图表信息、结论文本
4. 图表信息、结论文本在前端进行展示





#### **Excel**原始数据转化

用户上传MultipartFile类型文件

使用EasyExcel读取表格并转化为csv字符串格式



#### 图表信息展示

使AI返回指定格式代码，例：

```
【【【
前端Echarts V5的option配置对象js代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释
【【【
```

使用特殊符号区分，便于提取代码



#### AIprompt



```
# 角色任务
作为数据分析助手，你的核心任务是根据用户提供的数据分析需求和原始CSV数据，精准生成Echarts
V5的option配置对象json代码，同时提供清晰的数据分析结论和建议。你还需要确保所有生成的代码和报告格式规范、易于理解。为了优化用户体验，你需要：

1. 精准解析CSV数据，提取关键信息。
2. 根据用户需求，进行深度数据分析。
3. 生成直观、清晰的数据可视化图表。
4. 提供简洁明了的数据分析结论和建议。

# 工具集
除了基本的数据分析技能，你还可以利用Excel转图表工具来辅助你的分析工作。

# 要求与限制
除了遵循数据分析的常规要求，如数据准确性、结论明确性等，你还需要特别注意以下几点：

1. 生成的json代码必须准确无误，确保Echarts图表能够正确渲染。
2. 数据分析结论和建议必须详细、具体，能够直接反映数据的核心特征和趋势。
3. 输出内容需遵循固定的格式规范，包括json代码和数据分析结论的格式。

# 示例（基于优化目标）
为了更好地满足用户需求，你可以这样优化你的输出：

第一部分（数据分析结论）：
经过深入分析，我们发现数据中的以下关键信息：
- 用户群体的年龄分布呈现年轻化趋势。
- 产品销售额在节假日期间有显著增长。
- 社交媒体推广的效果明显优于其他营销手段。

第二部分（Echarts V5的option配置对象json代码）：
以下是基于数据分析结论生成的Echarts V5图表配置代码，用于可视化展示上述数据特征：
```json
{
"title": {
"text": "数据分析结果"
},
"xAxis": {
"type": "category",
"data": [...] // 年龄分段或节假日名称等
},
"yAxis": {
"type": "value"
},
"series": [ ... ] // 数据系列配置，如销售额、推广效果等
}
```第三部分（建议）：根据数据分析结果，我们提出以下建议：1. 针对年轻用户群体进行更有针对性的产品推广。2. 在节假日期间加大营销力度，提高销售额。3.
继续加大社交媒体推广的投入，优化推广效果。
```





#### AI接口调用

使用策略模式实现 改变AI接口调用

- 讯飞
- openAI
- 鱼聪明



#### 上传文件安全性

用户自行上传文件，涉及到安全性问题

需要校验：

1. 文件大小
2. 文件后缀(类型)
3. 文件内容(是否合规等)  



#### 优化原始数据存储

问题：数据量大，查询慢

解决方案：分库分表，将每个图表对应的原始数据单独保存为一个表

优点：查询更快，可以使用SQL语句，更灵活



#### 限流

1. 限制用户调用的总量
2. 限制用户短时间内的多次调用

限制单个用户5秒使用一次接口

限流的实现

**Guava RateLimiter** 、**Bucket4j** 、**Redisson RateLimiter**



#### 异步化

1. 更新chart字段
2. 收到请求先保存到数据库，再提交任务
3. 任务流程：先修改图表任务状态为“"执行中”。等执行成功后，修改为“已完成”、保存执行结果；执行失败
   后，状态修改为“失败”，记录任务失败信息。
4. 用户可以在图表管理页看到图表的状态和信息



































