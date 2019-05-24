# Swagger动态API页面

## 概述

Swagger根据过滤条件动态生成Swagger-UI界面。可用于权限控制访问RESTful接口。
Swagger-Lobby能够根据预先定义的哪些路径的哪些方法是可以访问的，来输出裁剪后的Swagger的JSON。

## 开发环境

- JDK8
- Gradle5

## 概念: 

Swagger是OpenAPI开源项目的实现，用于描述RESTful接口的元数据。即RESTful接口的出入参数、URL、权限等元数据。

Swagger包含主要的概念:

- Path: 某个路径
- HttpMethod: Http方法包含(GET, POST, PUT, DELETE, PATCH)
- Tag: 一批Path的组，通常对应于某个实体的若干操作
- Model: 实体的模型，通常是相互嵌套的，可以作为RESTful接口的输入和输出

Swagger对象的主要属性: 

![Swagger](https://raw.githubusercontent.com/tlw-ray/swagger-lobby/master/diagram/Swagger.png)

## 示例

1. [MainExample.java](core/src/main/java/com/winning/dcs/swagger/lobby/MainExample.java): 根据条件过滤Swagger中的路径，并生成新的Swagger的json文件。
2. [WebExample.java](core/src/main/java/com/winning/dcs/swagger/lobby/WebExample.java): 根据参数生成条件过滤Swagger中的路径，并将过滤的结果展现在Swagger-UI。