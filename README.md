# 简介
#        **godeye**

监控开始
![](photo/cicada_architecture.jpg)
- ![image](http://images2015.cnblogs.com/blog/524341/201607/524341-20160727211005747-332815071.png)


![image](https://t.hao0.me/images/trace/trace-model.png)

对于一个基本的分布式系统调用跟踪系统，大致需要满足基本的特性：低侵入性，高性能，高可用容错，低丢失率等，下文绍这几个特性的简单实现。


# 特点

## 低侵入性

对于业务系统而言，低侵入性是一个比较基础的特性，保证对业务开放人员的透明性，
尽量减少代码级的侵入。对于分布式调用跟踪，跟踪事件发生在调用前后，因此可借助
类似拦截器，过滤器，AOP等机制，利用配置代替编码。

## 高可用容错

在跟踪分布式调用过程中，应当保证跟踪系统具有高可用容错，首先保证跟踪服务器具
有集群特性，不能出现单点，即便跟踪服务器均不可用，也不应影响到业务系统的稳定
性，保持自身的轻量性。**建议以日志文件的方式记录跟踪行为，再通过Collector来收集这些记录，输出到跟踪服务器**。这样即便跟踪服务器不可用时，也不会影响到业务系统的运行。同时，使用了记录文件后，有便于以后针对高流量时，可适当做一些缓冲或流控方面的优化。

## 高性能 & 低丢失率

对于分布式调用跟踪，应该保证良好的性能，如上文所述**，采用文件记录的方式肯定比
直接通过网络传输更高效，结合一些高效的I/O手段，如使用高效的队列库Disruptor，
在结合Java自身的MappedByteBuffer和RandomAccessFile的顺序写，随机读的方式，将
达到一个可观的性能。除此外，MappedByteBuffer也保证，在应用将跟踪记录写入到内
存后，若此时应用意外崩溃，但跟踪记录并不会丢失，而是由操作系统完成从内存到文
件的同步工作，这进一步降低了记录丢失的发生。**

![image](https://t.hao0.me/images/trace/trace-arch.png)


假设现在前端有一个创建订单的请求，需要先调用到web应用的API，再分别调用订单服务和用户服务，订单服务还会间接调用用户服务，如下图所示

![image](https://t.hao0.me/images/trace/trace-demo.png)


####     Trace: 
    

---
## 
    一次服务调用追踪链路。
    一组代表一次用户请求所包含的spans，其中根span只有一个。
####     Span: 
    
    追踪服务调基本结构，多span形成树形结构组合成一次Trace追踪记录。
    一组代表一次RPC请求所包含的annotations，基本工作单元，一次链路调用(可以是
    RPC，DB等没有特定的限制)创建一个span，通过一个64位ID标识它， 
    span通过还有其他的数据，例如描述信息，时间戳，key-value对的
    (Annotation)tag信息，parent-id等,其中parent-id 
    可以表示span调用链路来源，通俗的理解span就是一次请求信息
    
    
埋点日志通常要包含以下内容：
TraceId、RPCId、调用的开始时间，调用类型，协议类型，调用方ip和端口，请求的服
务名等信息；调用耗时，调用结果，异常信息，消息报文等；

####     Annotation:
    
    在span中的标注点，记录整个span时间段内发生的事件。
    
    保留类型

        Cs CLIENT_SEND，客户端发起请求
        Cr CLIENT_RECIEVE，客户端收到响应
        Sr SERVER_RECIEVE，服务端收到请求
        Ss SERVER_SEND，服务端发送结果
        
        cs：客户端发起请求，标志Span的开始；
        sr：服务端接收到请求，并开始处理内部事务，其中sr - cs则为网络延迟和时钟抖动；
        ss：服务端处理完请求，返回响应内容，其中ss-sr则为服务端处理请求耗时；
        cr：客户端接收到服务端响应内容，标志着Span的结束，其中cr-ss则为网络延迟和时钟抖动

    用户自定义类型

        Event 记录普通事件
        Exception 记录异常事件


####     BinaryAnnotation:
    
    属于Annotation一种类型和普通Annotation区别，这键值对形式标注在span中发生的事件，
    和一些其他相关的信息。用户自定义事件。

# 收集
#   1.日志收集
  Cat是直接将日志发往消费集群；hydra是发给日志收集器，日志收集器推到消息队列；Zipkin的client将统计日志发往消息队列，日志收集器读取后落地存储；Dapper和Eagle eye是记录本地文件，后台进程定期扫描。
  
##  跟踪
 Trace Id - 全局的 id
Span Id - 每个方法调用的id
Optional Parent Span Id - 当前方法调用的父方法的 span id
Sampled boolean - 是否需要采样

一条链路通过Trace Id唯一标识，Span标识发起的请求信息，各span通过parent id 关联起来，如
 
 
  
   **Pinpoint**
特点如下:
分布式事务跟踪，跟踪跨分布式应用的消息
自动检测应用拓扑，帮助你搞清楚应用的架构
水平扩展以便支持大规模服务器集群
提供代码级别的可见性以便轻松定位失败点和瓶颈
使用字节码增强技术，添加新功能而无需修改代码
  
  
  **Pinpoint中的数据结构**

Pinpoint中，核心数据结构由Span, Trace, 和 TraceId组成。

**Span**: RPC (远程过程调用/remote procedure call)跟踪的基本单元; 当一个RPC调用到达时指示工作已经处理完成并包含跟踪数据。为了确保代码级别的可见性，Span拥有带SpanEvent标签的子结构作为数据结构。每个Span包含一个TraceId。

**Trace**: 多个Span的集合; 由关联的RPC (Spans)组成. 在同一个trace中的span共享相同的TransactionId。Trace通过SpanId和ParentSpanId整理为继承树结构.

**TraceId**: 由 TransactionId, SpanId, 和 ParentSpanId 组成的key的集合. 

**TransactionId** 指明消息ID，而SpanId 和 ParentSpanId 表示RPC的父-子关系。
TransactionId (TxId): 在分布式系统间单个事务发送/接收的消息的ID; 必须跨整个服务器集群做到全局唯一.
SpanId: 当收到RPC消息时处理的工作的ID; 在RPC请求到达节点时生成。
ParentSpanId (pSpanId): 发起RPC调用的父span的SpanId. 如果节点是事务的起点，这里将没有父span - 对于这种情况， 使用值-1来表示这个span是事务的根span。

Google Dapper 和 NAVER Pinpoint在术语上的不同
Pinpoint中的术语"TransactionId"和googledapper中的术语"TraceId"有相同的含义。而Pinpoint中的术语"TraceId"引用到多个key的集合。


**手工跟踪**	

优点1. 要求更少开发资源 2. API可以更简单并最终减少bug的数量

缺点1. 开发人员必须修改代码 2. 跟踪级别低

**自动跟踪**

优点 1. 开发人员不需要修改代码 2. 可以收集到更多精确的数据因为有字节码中的更多信息

缺点 1. 在开发pinpoint时，和实现一个手工方法相比，需要10倍开销来实现一个自动方法 2. 需要更高能力的开发人员，可以立即识别需要跟踪的类库代码并决定跟踪点 3. 增加bug发生的可能性，因为使用了如字节码增强这样的高级开发技巧
  
  
####  **字节码增强的价值**

我们选择字节码增强的理由，除了前面描述的那些外，还有下面的强有力的观点：
##### 隐藏API

一旦API被暴露给开发人员使用，我们作为API的提供者，就不能随意的修改API。这样的限会给我们增加压力。
我们可能修改API来纠正错误设计或者添加新的功能。但是，如果做这些受到限制，对我们来说很难改进API。解决这个问题的最好的答案是一个可升级的系统设计，而每个人都知道这不是一个容易的选择。如果我们不能掌控未来，就不可能创建完美的API设计。
而使用字节码增强技术，我们就不必担心暴露跟踪API而可以持续改进设计，不用考虑依赖关系。对于那些计划使用pinpoint开发应用的人，换一句话说，这代表对于pinpoint开发人员，API是可变的。现在，我们将保留隐藏API的想法，因为改进性能和设计是我们的第一优先级。
##### 容易启用或者禁用

使用字节码增强的缺点是当Pinpoint自身类库的采样代码出现问题时可能影响应用。
不过，可以通过启用或者禁用pinpoint来解决问题，很简单，因为不需要修改代码。
通过增加下面三行到JVM启动脚本中就可以轻易的为应用启用pinpoint：

- [ ] -javaagent:$AGENT_PATH/pinpoint-bootstrap-$VERSION.jar
- [ ] -Dpinpoint.agentId=<Agent's UniqueId>
- [ ] -Dpinpoint.applicationName=<The name indicating a same service (AgentId collection)>

如果因为pinpoint发生问题，只需要在JVM启动脚本中删除这些配置数据 
  
#### 字节码如何工作

由于字节码增强技术处理java字节码，有增加开发风险的趋势，同时会降低效率。另外，开
人员更容易犯错。在pinpoint，我们通过抽象出拦截器(interceptor)来改进效率和可达性(ac
cessibility)。pinpoint在类装载时通过介入应用代码为分布式事务和性能信息注入必要的跟
踪代码。这会提升性能，因为代码注入是在应用代码中直接实施的。  
  
  ![image](https://github.com/naver/pinpoint/raw/master/doc/img/td_figure3.png)
  在pinpoint中，拦截器API在性能数据被记录的地方分开(separated)。为了跟踪，我们添加
  拦截器到目标方法使得before()方法和after()方法被调用，并在before()方法和after()方
  法中实现了部分性能数据的记录。使用字节码增强，pinpoint 
  agent可以记录需要方法的数据，只有这样采样数据的大小才能变小
  
  
  
 
  
  
  
#   二.框架简介

## 1.代码架构
收集、传输、分析、存储和展示。
 
###    1.godEye-client 
   收集trace与发送，提供埋点API,scirbe来把所有的跟踪数据传输到zipkin的后端和hadoop文件系统。scribe是facebook开发的，运行在每台机器上面。
### 2.godEye-collector

一旦搜集的数据到达zipkin的后端，服务器端会对数据进行校验、存储，索引。---收集器，•  Collector接收各service传输的数据

### 3.godEye-Storage 

存储
### 4.godEye-web 

展示UI

### 5.godEye-query

负责查询Storage中存储的数据,提供简单的JSON API获取数据，主要提供给web UI使用

### 收集阶段：
负责业务应用日志的埋点和采集。CAT目前提供多种语言的客户端供业务应用程序调用，埋点结果以消息树的形式存入传输队列。如果队列满，则会自动丢弃当前消息。

### 传输阶段：
CAT客户端负责将客户端消息传输到后端，CAT消费机负责接收消息。传输前CAT客户端会与CAT消费机建立TCP长连接，不断地从客户端队列中取出消息树，序列化后写入网络；CAT消费机则不断地从网络中取出数据，反序列化后放入消费队列。
日志的采集和存储有许多开源的工具可以选择，
一般来说，会使用离线+实时的方式去存储日志，主要是分布式日志采集的方式。
典型的解决方案如Flume结合Kafka等MQ。

### 分析阶段：
负责报表生成。实时消费调度器会将消费队列消息取出，分发给每个消费者内部队列；
报表分析器只会从自己的报表队列中取出消息树，逐个消费，更新报表模型。CAT以小时
为单位形成报表，原始日志转储(raw log dump)是一个特殊的分析器，它不生产报表，而是将消息存入本地文件系统。

一条调用链的日志散落在调用经过的各个服务器上，
首先需要按 TraceId 汇总日志，然后按照RpcId 对调用链进行顺序整理。
调用链数据不要求百分之百准确，可以允许中间的部分日志丢失。

### 存储阶段：
负责报表和原始日志的存储，目前报表会存在MySQL中，原始日志压缩后存在HDFS中长久保存。保留时长取决于存储容量的大小，一般报表会保存3个月以上，原始日志保存一个月。

### 展示阶段：
负责数据的可视化。作为用户服务入口，负责报表和原始日志的输出显示。对于实时报表请求，会向各个消费机分发请求，并将结果聚合后输出HTML，在浏览器展示；历史报表会直接取自数据库。XML数据输出是另一种内置的数据展示方式，方便基于CAT开放外围工具。










# 末尾

## 参考
### 1.CAT
  
业务应用目前使用CAT API进行埋点，后台异步线程采用TCP长连接方式，将消息源源不
断地传输到后台服务器；CAT具有fail-over机制，在后台服务器不可用时会自动切换到
另一台可用服务器。CAT目前使用native协议做序列化和反序列化，将来会考虑支持更多
协议，比如thrift。

消息被送到后台，经反序列化后会被放入队列，实时消费调度器会将消息分发到所有消
费者内部队列，每个消费者只需处理自己的消费队列，各消费者之间彼此相对独立，如
果消费速度太慢，导致消费队列满，则新来的消息会被丢弃。典型消费者都采用实时增
量计算的方式，流式处理消息，产生的报表会在当前小时结束后保存到中央数据库中。

日报表采用后台作业的形式，由24个小时报表合并得到。周报表则由7个日报表合并得到
，以此类推。

CAT控制台，即UI层，负责接收用户请求，从后台存储中将报表信息取出显示。对于实时
报表，直接通过HTTP请求分发到相应消费机，待结果返回后聚合展示；历史报表则直接
取数据库并展示。

所有原始消息会先存储在本地文件系统，然后上传到HDFS中保存；而对于报表，因其远
比原始日志小，则以K/V的方式保存在MySQL中


**日志埋点**是监控活动的最重要环节之一，日志质量决定着监控质量和效率。当前CAT的埋点目标是以问题为中心，像程序抛出exception就是典型问题。我个人对问题的定义是：不符合预期的就可以算问题。比如请求未完成，响应时间快了慢了，请求TPS多了少了，时间分布不均匀等等。

在互联网环境中，最突出的问题场景，我的理解是，**跨越边界的行为。包括但不限于，HTTP/REST、RPC/SOA、MQ、Job、Cache、DAL;搜索/查询引擎、业务应用、外包系统、遗留系统; 母/子公司,第三方网关/银行,合作伙伴/供应商之间；还有各类业务指标，如PV、用户登录、订单数、支付状态、销售额。**

3.CAT在分布式实时方面，主要归结于以下几点因素：

去中心化，数据分区处理；

基于日志只读特性，以一个小时为时间窗口，实时报表基于内存建模和分析，历史报表通过聚合完成；

基于内存队列，全面异步化，单线程化，无锁设计；

全局消息ID，数据本地化生产，集中式存储；

组件化、服务化理念，致力于工具间互通互联。


## 2.鹰眼

### 1）埋点


在前端请求到达服务器时，应用容器在执行实际业务处理之前，会先执行 EagleEye 的埋点逻辑（类似Filter的机制），埋点逻辑为这个前端请求分配一个全局唯一的调用链ID。这个ID在EagleEye里面被称为 TraceId，埋点逻辑把TraceId放在一个调用上下文对象里面，而调用上下文对象会存储在ThreadLocal里面。调用上下文里还有一个ID非常重要，在 EagleEye里面被称作RpcId。RpcId用于区分同一个调用链下的多个网络调用的发生顺序和嵌套层次关系。对于前端收到请求，生成的 RpcId 固定都是0。

当这个前端执行业务处理需要发起RPC调用时，淘宝的RPC调用客户端 HSF 会首先从当前线程 ThreadLocal 上面获取之前 EagleEye 设置的调用上下文。然后，把 RpcId 递增一个序号。在 EagleEye 里使用多级序号来表示RpcId，比如前端刚接到请求之后的 RpcId 是0，那么 它第一次调用RPC服务A时，会把RpcId改成0.1。之后，调用上下文会作为附件随这次请求一起发送到远程的 HSF 服务器。

HSF 服务端收到这个请求之后，会从请求附件里取出调用上下文,并放到当前线程ThreadLocal上面。如果服务A在处理时，需要调用另一个服务，这个时候它会重复之前提到的操作，唯一的差别就是RpcId 会先改成0.1.1再传过去。服务A的逻辑全部处理完毕之后，HSF在返回响应对象之前，会把这次调用情况以及TraceId、RpcId都打印到它的访问日志之中，同时，会从ThreadLocal清理掉调用上下文。

### 2）ThreadLocal

上述图描述了EagleEye在一个非常简单的分布式调用场景里做的事情，就是为每次调用分配TraceId、RpcId，放在ThreadLocal的调用上下文上面，调用结束的时候，把TraceId、RpcId打印到访问日志。类似的其他网络调用中间件的调用过程也都比较类似，这里不再赘述了。访问日志里面，一般会记录调用时间、远端IP地址、结果状态码、调用耗时之类，也会记录与这次调用类型相关的一些信息，如URL、服 务名、消息topic等。很多调用场景会比上面说的完全同步的调用更为复杂，比如会遇到异步、单向、广播、并发、批处理等等，这时候需要妥善处理好ThreadLocal上的调用上下文，避免调用上下文混乱和无法正确释放。另外，采用多级序号的RpcId设计方案会比单级序号递增更容易准确还原当时的调用情况。

最后，EagleEye 分析系统把调用链相关的所有访问日志都收集上来，按 TraceId 汇总在一起之后，就可以准确还原调用当时的情况了。

![image](http://images2015.cnblogs.com/blog/524341/201607/524341-20160728111215294-1511204813.png)
--参考
https://github.com/liaokailin/zipkin
http://www.cnblogs.com/java-zhao/p/5858138.html
https://github.com/Yirendai/cicada/blob/master/cicada-docs/cicada_design.md
[http://outofmemory.cn/java/flume-kafka-docker-log-collect-practise](http://note.youdao.com/)

http://tech.meituan.com/mt-log-system-arch.html
