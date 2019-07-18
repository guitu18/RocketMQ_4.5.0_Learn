# RocketMQ 分布式消息中间件-Centos7安装运行

## 一、环境

我们假设你已经设置好如下环境，没有的先自行安装环境，这里不做展开

- Centos7：我这里用的是阿里云服务器，Centos7，内核：3.10.0-693.2.2.el7.x86_64
- JDK1.8：JDK版本：JDK_1.8.0_201_64-Bit
- Maven：Apache Maven 3.6.0
- unzip：解压工具，用来解压安装包，没有该工具可直接使用 ```yum install unzip -y``` 安装

## 二、文档及代码

- Apache 上开源官方地址：https://rocketmq.apache.org/
- GitHub 托管地址：https://github.com/apache/rocketmq
- 阿里官方的介绍文档：http://jm.taobao.org/2017/01/12/rocketmq-quick-start-in-10-minutes/
- Apache 官方提供的 “快速入门” 文档：https://rocketmq.apache.org/docs/quick-start/
- **RocketMQ的运行方式非常简单，这里简单记录一下步骤，最主要的还是记录安装运行过程中踩过的坑**
- **该篇介绍两种运行方式，均参照官方文档操作，以当前最新4.5.0版本为例，所有操作均在 ```/root``` 目录进行**

## 三、安装方式一：二进制直接运行

- 二进制文件：[http://mirrors.shu.edu.cn/apache/rocketmq/4.5.0/rocketmq-all-4.5.0-bin-release.zip](http://mirrors.shu.edu.cn/apache/rocketmq/4.5.0/rocketmq-all-4.5.0-bin-release.zip)
- 下载二进制文件：```wget http://mirrors.shu.edu.cn/apache/rocketmq/4.5.0/rocketmq-all-4.5.0-bin-release.zip```
- 解压：```unzip rocketmq-all-4.5.0-bin-release.zip```
- 进入解压后的二进制文件目录：```cd rocketmq-all-4.5.0-bin-release```
- 看到以下目录结构：

    ```java
    drwxr-xr-x 2 root root  4096 Mar 29 17:37 benchmark
    drwxr-xr-x 3 root root  4096 Mar 29 17:37 bin
    drwxr-xr-x 6 root root  4096 Mar 29 17:37 conf
    drwxr-xr-x 2 root root  4096 Mar 29 17:37 lib
    -rw-r--r-- 1 root root 17336 Mar 28 19:52 LICENSE
    -rw-r--r-- 1 root root  1337 Mar 28 19:53 NOTICE
    -rw-r--r-- 1 root root  2521 Mar 28 19:53 README.md
    ```
### 启动NameServer服务

- 后台启动Name Server服务：```nohup sh bin/mqnamesrv &```
- 查看启动日志：```tail -f ~/logs/rocketmqlogs/namesrv.log```

**这里如果你的系统配置如果不够高那么你可能会遇到如下错误：**

```java
Java HotSpot(TM) 64-Bit Server VM warning: Using the DefNew young collector with the CMS collector is deprecated and will likely be removed in a future release
Java HotSpot(TM) 64-Bit Server VM warning: UseCMSCompactAtFullCollection is deprecated and will likely be removed in a future release.
Java HotSpot(TM) 64-Bit Server VM warning: INFO: os::commit_memory(0x00000006ec800000, 2147483648, 0) failed; error='Cannot allocate memory' (errno=12)
#
# There is insufficient memory for the Java Runtime Environment to continue.
# Native memory allocation (mmap) failed to map 2147483648 bytes for committing reserved memory.
# An error report file with more information is saved as:
# /root/rocketmq-all-4.5.0-bin-release/hs_err_pid16614.log
```

日志已经提示出来了，你也可以打开日志文件查看详细信息：```tail -f ~/logs/rocketmqlogs/broker.log```

这里的报错是因为内存分配失败，解决方法请往下翻到第五节查看。

### 启动Broker服务

- 后台启动Broker服务：```nohup sh bin/mqbroker -n localhost:9876 &```

- 查看启动日志：```tail -f ~/logs/rocketmqlogs/broker.log ```

**这里同样可能会遇到内存分配失败的错误**

### 运行消息生产者测试

```java
 > export NAMESRV_ADDR=localhost:9876
 > sh bin/tools.sh org.apache.rocketmq.example.quickstart.Producer
 SendResult [sendStatus=SEND_OK, msgId= ...
```

### 运行消息消费者测试

```java
 > sh bin/tools.sh org.apache.rocketmq.example.quickstart.Consumer
 ConsumeMessageThread_%d Receive New Messages: [MessageExt...
```

两项测试如果均运行成功那么RocketMQ服务安装没什么问题了

### 关闭服务

```java
> sh bin/mqshutdown broker
The mqbroker(36695) is running...
Send shutdown request to mqbroker(36695) OK

> sh bin/mqshutdown namesrv
The mqnamesrv(36664) is running...
Send shutdown request to mqnamesrv(36664) OK
```

## 四、安装方式二：源码编译运行

### 下载及编译

- 源代码地址：[http://mirrors.shu.edu.cn/apache/rocketmq/4.5.0/rocketmq-all-4.5.0-source-release.zip](http://mirrors.shu.edu.cn/apache/rocketmq/4.5.0/rocketmq-all-4.5.0-source-release.zip)

- 下载源码：```wget http://mirrors.shu.edu.cn/apache/rocketmq/4.5.0/rocketmq-all-4.5.0-source-release.zip```

- 解压：```unzip rocketmq-all-4.5.0-source-release.zip```

- 进入解压后的源码目录：```cd rocketmq-all-4.5.0```

- 可以看到有如下文件：

    ```java
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 acl
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 broker
      -rw-r--r-- 1 root root   997 Mar 29 13:20 BUILDING
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 client
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 common
      -rw-r--r-- 1 root root  1997 Mar 29 13:20 CONTRIBUTING.md
      -rw-r--r-- 1 root root   271 Mar 29 13:20 DEPENDENCIES
      drwxr-xr-x 2 root root  4096 Mar 29 13:20 dev
      drwxr-xr-x 5 root root  4096 Mar 29 13:20 distribution
      drwxr-xr-x 4 root root  4096 Mar 29 13:20 docs
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 example
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 filter
      -rw-r--r-- 1 root root 11365 Mar 29 13:20 LICENSE
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 logappender
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 logging
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 namesrv
      -rw-r--r-- 1 root root   168 Mar 29 13:20 NOTICE
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 openmessaging
      -rw-r--r-- 1 root root 23992 Mar 29 13:20 pom.xml
      -rw-r--r-- 1 root root  2521 Mar 29 13:20 README.md
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 remoting
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 srvutil
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 store
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 style
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 test
      drwxr-xr-x 3 root root  4096 Mar 29 13:20 tools  
    ```

- 执行mvn命令编译：```mvn -Prelease-all -DskipTests clean install -U```，该过程可能会下载若干依赖，耐心等待即可

- 有如下显示表示编译成功

    ```java
    [INFO] ------------------------------------------------------------------------
    [INFO] Reactor Summary for Apache RocketMQ 4.5.0 4.5.0:
    [INFO] 
    [INFO] Apache RocketMQ 4.5.0 .............................. SUCCESS [  4.318 s]
    [INFO] rocketmq-logging 4.5.0 ............................. SUCCESS [  3.083 s]
    [INFO] rocketmq-remoting 4.5.0 ............................ SUCCESS [  2.057 s]
    [INFO] rocketmq-common 4.5.0 .............................. SUCCESS [  3.052 s]
    [INFO] rocketmq-client 4.5.0 .............................. SUCCESS [  3.730 s]
    [INFO] rocketmq-store 4.5.0 ............................... SUCCESS [  4.995 s]
    [INFO] rocketmq-srvutil 4.5.0 ............................. SUCCESS [  0.360 s]
    [INFO] rocketmq-filter 4.5.0 .............................. SUCCESS [  1.033 s]
    [INFO] rocketmq-acl 4.5.0 ................................. SUCCESS [  0.741 s]
    [INFO] rocketmq-broker 4.5.0 .............................. SUCCESS [  2.966 s]
    [INFO] rocketmq-tools 4.5.0 ............................... SUCCESS [  1.758 s]
    [INFO] rocketmq-namesrv 4.5.0 ............................. SUCCESS [  0.745 s]
    [INFO] rocketmq-logappender 4.5.0 ......................... SUCCESS [  0.707 s]
    [INFO] rocketmq-openmessaging 4.5.0 ....................... SUCCESS [  0.594 s]
    [INFO] rocketmq-example 4.5.0 ............................. SUCCESS [  0.780 s]
    [INFO] rocketmq-test 4.5.0 ................................ SUCCESS [  1.218 s]
    [INFO] rocketmq-distribution 4.5.0 ........................ SUCCESS [  5.352 s]
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time:  37.829 s
    [INFO] Finished at: 2019-04-08T17:53:30+08:00
    [INFO] ------------------------------------------------------------------------
    ```

- 进入编译后的目录：```cd distribution/target/apache-rocketmq```

- 看到以下目录结构

    ```java
    drwxr-xr-x 2 root root  4096 Apr  8 17:53 benchmark
    drwxr-xr-x 3 root root  4096 Mar 29 13:20 bin
    drwxr-xr-x 6 root root  4096 Mar 29 13:20 conf
    drwxr-xr-x 2 root root  4096 Apr  8 17:53 lib
    -rw-r--r-- 1 root root 17336 Mar 29 13:20 LICENSE
    -rw-r--r-- 1 root root  1337 Mar 29 13:20 NOTICE
    -rw-r--r-- 1 root root  2521 Mar 29 13:20 README.md0
    ```

- 到这里源码编译完成，下面的步骤跟二进制运行方式一样，请查看二进制运行方式

## 五、可能遇到的坑

### 内存分配失败

```java
Java HotSpot(TM) 64-Bit Server VM warning: INFO: os::commit_memory(0x00000005c0000000, 8589934592, 0) failed; error='Cannot allocate memory' (errno=12)
#
# There is insufficient memory for the Java Runtime Environment to continue.
# Native memory allocation (mmap) failed to map 8589934592 bytes for committing reserved memory.
# An error report file with more information is saved as:
# /root/rocketmq-all-4.5.0-bin-release/hs_err_pid17515.log
```

遇到这个错误，请修改 ```bin``` 目录下的 ```runserver.sh``` 和 ```runbroker.sh``` 这两个文件：

#### runserver.sh

**将默认的4g调整为合适的大小，我这里给了256m**

```java
#===========================================================================================
# JVM Configuration
#===========================================================================================
#JAVA_OPT="${JAVA_OPT} -server -Xms4g -Xmx4g -Xmn2g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m"
JAVA_OPT="${JAVA_OPT} -server -Xms256m -Xmx256m -Xmn128m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m"
JAVA_OPT="${JAVA_OPT} -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:CMSInitiatingOccupancyFraction=70 -XX:+CMSParallelRemarkEnabled -XX:Sof
tRefLRUPolicyMSPerMB=0 -XX:+CMSClassUnloadingEnabled -XX:SurvivorRatio=8  -XX:-UseParNewGC"
JAVA_OPT="${JAVA_OPT} -verbose:gc -Xloggc:/dev/shm/rmq_srv_gc.log -XX:+PrintGCDetails"
JAVA_OPT="${JAVA_OPT} -XX:-OmitStackTraceInFastThrow"
JAVA_OPT="${JAVA_OPT}  -XX:-UseLargePages"
JAVA_OPT="${JAVA_OPT} -Djava.ext.dirs=${JAVA_HOME}/jre/lib/ext:${BASE_DIR}/lib"
#JAVA_OPT="${JAVA_OPT} -Xdebug -Xrunjdwp:transport=dt_socket,address=9555,server=y,suspend=n"
```

#### runbroker.sh

**同理，将默认的8g调整为合适的大小，我这里也给了256m**

```java
#===========================================================================================
# JVM Configuration
#===========================================================================================
#JAVA_OPT="${JAVA_OPT} -server -Xms8g -Xmx8g -Xmn4g"
JAVA_OPT="${JAVA_OPT} -server -Xms256m -Xmx256m -Xmn128m"
JAVA_OPT="${JAVA_OPT} -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:G1ReservePercent=25 -XX:InitiatingHeapOccupancyPercent=30 -XX:SoftRefLRUPolicyMSPerMB=0"
JAVA_OPT="${JAVA_OPT} -verbose:gc -Xloggc:/dev/shm/mq_gc_%p.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintAdap
tiveSizePolicy"
JAVA_OPT="${JAVA_OPT} -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=30m"
JAVA_OPT="${JAVA_OPT} -XX:-OmitStackTraceInFastThrow"
JAVA_OPT="${JAVA_OPT} -XX:+AlwaysPreTouch"
JAVA_OPT="${JAVA_OPT} -XX:MaxDirectMemorySize=15g"
```

**以上只是我个人在安装运行过程中的一些记录和遇到的坑，如有不完善的地方欢迎指出，我也会在文中继续补充完善。**