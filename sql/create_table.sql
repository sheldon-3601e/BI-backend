# 数据库初始化

-- 创建库
create database if not exists my_bi;

-- 切换库
use my_bi;

-- 如果存在用户表，删除
drop table if exists user;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    genNum       integer                                not null comment '调用次数',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_userAccount (userAccount)
) comment '用户' collate = utf8mb4_unicode_ci;

drop table if exists chart;
-- 图表信息
create table if not exists chart
(
    id          bigint auto_increment comment 'id' primary key,
    userId      bigint                             not null comment '创建用户 id',
    name        varchar(128)                       null comment '图标名称',
    goal        text                               null comment '分析目标',
    chartData   text                               null comment '图表数据',
    chartType   varchar(256)                       null comment '图表类型',
    status      int                                not null default '0' comment '图表状态 -1:分析失败 0:等待中， 1:分析中 2:分析成功',
    execMessage text                               null comment '执行信息',
    genChart    text                               null comment '生成的图表数据',
    genResult   text                               null comment '生成的分析结论',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除'
) comment '图表' collate = utf8mb4_unicode_ci;

