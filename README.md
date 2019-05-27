# tools

#### 介绍
wiseloong-基础工具

#### 软件架构
`[wiseloong/tools "0.1.0-SNAPSHOT"]`

#### 使用说明

1. config.clj

用于获取后端配置文件信息，基于`mount`，比如数据库配置

2. ajax.cljs

前端ajax封装，默认获取携带token，可支持配置多后端地址，可自动跳转

3. utils.clj，utils.cljs

常用工具

4. cors.clj

后端跨域访问中间件

5. warn.clj

统一管理错误提醒中间件，比如修改用户时，前端没有传id，可提示信息错误。

> 具体可参考代码示例