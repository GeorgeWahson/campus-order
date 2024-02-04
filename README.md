# Dependency

- spring-boot-starter-parent 2.7.6
- mybatis-plus-boot-starter 3.5.3.1
- druid-spring-boot-starter 1.2.8
- aliyun-sdk-oss
- spring-boot-starter-mail
- sharding-jdbc-spring-boot-starter 4.0.0-RC1

# Reconfiguration

- [x] （1）分模块重构
- [x] （2）引入远程服务器redis缓存
- [x] （3）引入两台云服务器mysql，使用shardingsphere实现读写分离
- [x] （4）使用阿里云oss实现图片上传及回显
- [x] （5）使用spring mail发送验证码短信
- [x] （6）完善操作界面，优化部分逻辑，追加实现部分功能

# TODO

组件：
- [ ] shardingsphere升级版本
- [ ] 引入JWT令牌
- [ ] 引入AOP插入日志信息
- [ ] 打包前端资源，分开部署

逻辑：
- [ ] 后台管理端，新增套餐时搜索菜品名称功能
- [ ] 用户端，下单时没有地址新增地址后可以直接下单

