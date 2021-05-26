# seata参数配置 1.3.0版本

## 参数同步到配置中心使用

### Nacos

**shell:**

```shell
sh ${SEATAPATH}/script/config-center/nacos/nacos-config.sh -h localhost -p 8848 -g SEATA_GROUP -t 5a3c7d6c-f497-4d68-a71a-2e5e3340b3ca
```

参数说明：

-h: host，默认值 localhost

-p: port，默认值 8848

-g: 配置分组，默认值为 'SEATA_GROUP'

-t: 租户信息，对应 Nacos 的命名空间ID字段, 默认值为空 ''


**Apollo:**

```shell
sh ${SEATAPATH}/script/config-center/apollo/apollo-config.sh -h localhost -p 8070 -e DEV -a seata-server -c default -n application -d apollo -r apollo -t 3aa026fc8435d0fc4505b345b8fa4578fb646a2c
```

参数说明：

-h: host，默认值 localhost

-p: port，默认值 8070

-e: 所管理的配置环境，默认值 DEV

-a: Namespace 所属的 AppId，默认值 seata-server

-c: 所管理的配置集群名， 一般情况下传入 default 即可。如果是特殊集群，传入相应集群的名称即可，默认值 default

-n: 所管理的 Namespace 的名称，如果是非 properties 格式，需要加上后缀名，如 sample.yml，默认值 application

-d: item 的创建人，格式为域账号，也就是 sso 系统的 User ID

-r: 发布人，域账号，注意：如果 ApolloConfigDB.ServerConfig 中的 namespace.lock.switch 设置为 true 的话（默认是 false），那么该环境不允许发布人和编辑人为同一人。所以如果编辑人是 zhangsan，发布人就不能再是 zhangsan。

-t: Apollo 管理员在 http://{portal_address}/open/manage.html 创建第三方应用，创建之前最好先查询此AppId是否已经创建。创建成功之后会生成一个 token

**Consul:**

```shell
sh ${SEATAPATH}/script/config-center/consul/consul-config.sh -h localhost -p 8500
```

参数说明：

-h: host，默认值 localhost

-p: port，默认值 8500

**Etcd3:**

```shell
sh ${SEATAPATH}/script/config-center/etcd3/etcd3-config.sh -h localhost -p 2379
```

参数说明：

-h: host，默认值 localhost

-p: port，默认值 2379

**python:**

```shell
python ${SEATAPATH}/script/config-center/nacos/nacos-config.py localhost:8848
```

**ZK:**

```shell
sh ${SEATAPATH}/script/config-center/zk/zk-config.sh -h localhost -p 2181 -z "/Users/zhangchenghui/zookeeper-3.4.14"
```

参数说明：

-h: host，默认值 localhost

-p: port，默认值 2181

-z: zk所属路径

