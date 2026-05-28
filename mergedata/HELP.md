# 正确的部署做法
## 1、只上传 -encrypted.jar 到服务器，原始包可以删除或保留在本地备份

## 2、启动时必须使用 -javaagent 参数，因为加密后的 JAR 不可直接执行：

bash
# 无密码模式启动
java -javaagent:mergedata-0.0.1-SNAPSHOT-encrypted.jar -jar mergedata-0.0.1-SNAPSHOT-encrypted.jar
安全加固建议
启动时加上这个参数，可以防止从运行中的进程 dump 出解密后的字节码：

bash
java -XX:+DisableAttachMechanism -javaagent:mergedata-0.0.1-SNAPSHOT-encrypted.jar -jar mergedata-0.0.1-SNAPSHOT-encrypted.jar