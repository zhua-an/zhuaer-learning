# elasticsearch-boot

Elasticsearch客户端API:[https://www.elastic.co/guide/en/elasticsearch/client/index.html](https://www.elastic.co/guide/en/elasticsearch/client/index.html "https://www.elastic.co/guide/en/elasticsearch/client/index.html")

具体操作查看：[ElasticsearchApplicationTests.java](./src/test/java/ElasticsearchApplicationTests.java "ElasticsearchApplicationTests.java")

## 添加依赖
> springboot:2.3.3.RELEASE
>
	<dependency>
    	<groupId>org.springframework.boot</groupId>
    	<artifactId>spring-boot-starter-data-elasticsearch</artifactId>
    </dependency>
    
## yml配置

    spring:
      elasticsearch:
        rest:
          uris: http://localhost:9200
    #      username:
    #      password:
          read-timeout: 30s
          connection-timeout: 5s
    
## 应用

    /**
     * @ClassName ElasticsearchApplicationTests
     * @Description TODO
     * @Author zhua
     * @Date 2020/9/15 14:16
     * @Version 1.0
     */
    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = Application.class)
    public class ElasticsearchApplicationTests {
    
        @Autowired
        private RestHighLevelClient restHighLevelClient;
    
        /**
         * 创建索引
         * @throws IOException
         */
        @Test
        public void testCreateIndex() throws IOException {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest("test");
            CreateIndexResponse response = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            System.out.println(JSON.toJSONString(response));
        }
    
        /**
         * 测试索引是否存在
         *
         * @throws IOException
         */
        @Test
        public void testExistIndex() throws IOException {
            GetIndexRequest request = new GetIndexRequest("test");
            boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
            System.out.println(exists);
        }
    
        /**
         * 删除索引
         */
        @Test
        public void deleteIndex() throws IOException {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("test");
            AcknowledgedResponse delete = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            boolean isSuccess = delete.isAcknowledged();
            System.out.println(isSuccess);
        }
    
        /**
         * 测试添加文档
         * 新增文档，如果返回结果为CREATED，新增文档，如果返回结果是UPDATED，更新文档
         * 文档id，指定生成的文档id，如果为空，es会自动生成id
         *
         * @throws IOException
         */
        @Test
        public void createDocument() throws IOException {
            User user = new User("test", 18);
            IndexRequest request = new IndexRequest("test");
            //文档id，指定生成的文档id，如果为空，es会自动生成id
            request.id("1");
            request.timeout(TimeValue.timeValueSeconds(1));
            request.timeout("1s");
            //将我们的数据放入请求，json
            request.source(JSON.toJSONString(user), XContentType.JSON);
            //客服端发送请求
            IndexResponse index = restHighLevelClient.index(request, RequestOptions.DEFAULT);
            DocWriteResponse.Result result = index.getResult();
            System.out.println(result.toString());
            System.out.println(index.toString());
            //对应我们的命令返回状态
            System.out.println(index.status());
        }
    
        /**
         * 判断是否存在文档
         * @throws IOException
         */
        @Test
        public void testIsExist() throws IOException {
            GetRequest getRequest = new GetRequest("test", "1");
            //不获取返回的source的上下文
            getRequest.fetchSourceContext(new FetchSourceContext(false));
            getRequest.storedFields("_none_");
            boolean exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
            System.out.println(exists);
        }
    
        /**
         * 获取文档信息
         * @throws IOException
         */
        @Test
        public void testGetDocument() throws IOException {
            GetRequest getRequest = new GetRequest("test", "1");
            GetResponse response = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
            System.out.println(JSON.toJSONString(response));
            //打印文档信息
            System.out.println(response.getSourceAsString());
            System.out.println(response.getSource());
        }
    
        /**
         * 更新文档信息
         * 根据文档id，更新文档，如果返回结果为UPDATED，更新成功，否则更新失败
         * @throws IOException
         */
        @Test
        public void testUpdateDocument() throws IOException {
            UpdateRequest request = new UpdateRequest("test", "1");
            request.timeout("1s");
            User user = new User("test java", 19);
            request.doc(JSON.toJSONString(user), XContentType.JSON);
            UpdateResponse update = restHighLevelClient.update(request, RequestOptions.DEFAULT);
            DocWriteResponse.Result result = update.getResult();
            System.out.println(result.toString());
            System.out.println(update.status());
        }
    
        /**
         * 删除文档
         * 根据文档id，删除文档，如果返回结果deleted，删除成功，如果返回结果是not_found，文档不存在，删除失败
         * @throws IOException
         */
        @Test
        public void testDeleteDocument() throws IOException {
            DeleteRequest request = new DeleteRequest("test", "1");
            request.timeout("10s");
            User user = new User("test java", 19);
            DeleteResponse update = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
            DocWriteResponse.Result result = update.getResult();
            System.out.println(result.toString());
            System.out.println(update.status());
        }
    
        /**
         * 批量插入数据
         * 批量操作,如果返回结果为SUCCESS，则全部记录操作成功，否则至少一条记录操作失败,并返回失败的日志
         * @throws IOException
         */
        @Test
        public void testBulkRequest() throws IOException {
            BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.timeout("10s");
            ArrayList<User> users = new ArrayList<>();
            users.add(new User("zhangsan", 1));
            users.add(new User("lishi", 12));
            users.add(new User("wangwu", 13));
            users.add(new User("zhaoliu", 14));
            users.add(new User("tianqi", 15));
            for (int i = 0; i < users.size(); i++) {
                bulkRequest.add(
                        new IndexRequest("test")
                                .id("" + i + 1)
                                .source(JSON.toJSONString(users.get(i)), XContentType.JSON)
                );
            }
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            System.out.println(bulkResponse);
            // 如果至少有一个操作失败，此方法返回true
            if (bulkResponse.hasFailures()) {
                StringBuffer sb = new StringBuffer("");
                for (BulkItemResponse bulkItemResponse : bulkResponse) {
                    //指示给定操作是否失败
                    if (bulkItemResponse.isFailed()) {
                        //检索失败操作的失败
                        BulkItemResponse.Failure failure = bulkItemResponse.getFailure();
                        sb.append(failure.toString()).append("\n");
                    }
                }
                System.out.println("=bulk error="+sb.toString());
            } else {
                System.out.println("SUCCESS");
            }
        }
    
    
        /**
         * 分页match查询
         *
         * @return
         * @throws IOException
         */
        @Test
        public void searchMatch() throws IOException {
            /**
             * indexName 索引名称
             * fieldName 查询字段名
             * fileValue 查询字段值
             * startPage 开始页面，从零开始
             * maxSize   每页最大记录数
             */
            String indexName = "test";
            String fieldName = "name";
            String fileValue = "zhangsan";
            int startPage = 0;
            int maxSize = 10;
            SearchRequest searchRequest = new SearchRequest(indexName);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    //        QueryBuilders.matchAllQuery()
            searchSourceBuilder.query(QueryBuilders.matchQuery(fieldName, fileValue));
            searchSourceBuilder.from(startPage);
            searchSourceBuilder.size(maxSize);
            searchRequest.source(searchSourceBuilder);
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    //        System.out.println("=response=" + JSONObject.toJSON(response));
            SearchHit[] hits = response.getHits().getHits();
            List<User> userList = new LinkedList<>();
            for (SearchHit hit : hits) {
                User user = JSONObject.parseObject(hit.getSourceAsString(), User.class);
                userList.add(user);
            }
            System.out.println(userList);
        }
    
    }
    

## Search Request的介绍与使用
SearchRequest用于与搜索文档、聚合、定制查询有关的任何操作，还提供了在查询结果的基于上，对于匹配的关键词进行突出显示的方法。

### 1、先创建搜索请求对象：
    SearchRequest searchRequest = new SearchRequest();

### 2、对搜索请求进行基本参数设置
**1）设置查询指定的某个文档库：**

    SearchRequest searchRequest = new SearchRequest("test");

**2）查询多个文档库，其中多个文档库名之间用逗号隔开**

    SearchRequest searchRequest = new SearchRequest("test","test1", "test2", "test3");

或者这样设置：

    SearchRequest searchRequest = new SearchRequest();
    // 指定只能在哪些文档库中查询：可以添加多个且没有限制，中间用逗号隔开
    searchRequest.indices("test","test1", "test2", "test3");

> 默认是去所有文档库中进行查询

**3）设置指定查询的路由分片**

    searchRequest.routing("routing");

**4）用preference方法去指定优先去某个分片上去查询（默认的是随机先去某个分片）**

    searchRequest.preference("_local");

**5）向主搜索请求中可以添加搜索内容的特征参数**
a.创建  搜索内容参数设置对象:SearchSourceBuilder

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

b. 将SearchSourceBuilder对象添加到搜索请求中

    searchRequest.source(searchSourceBuilder);


### 3、为搜索的文档内容对象SearchSourceBuilder设置参数：
大多控制搜索内容的行为参数都可以在SearchSourceBuilder上进行设置，SearchSourceBuilder包含与Rest API的搜索请求主体中类似的参数选项。 以下是一些常见选项的几个示例：

**1）查询包含指定的内容：**
a.查询所有的内容

    searchSourceBuilder.query(QueryBuilders.matchAllQuery());

b.查询包含关键词字段的文档：如下，表示查询出来所有包含user字段且user字段包含kimchy值的文档

    sourceBuilder.query(QueryBuilders.termQuery("user", "kimchy"));

c.上面是基于QueryBuilders查询选项的，另外还可以使用MatchQueryBuilder配置查询参数

    MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("user", "kimchy");
    // 启动模糊查询
    matchQueryBuilder.fuzziness(Fuzziness.AUTO);
    // 在匹配查询上设置前缀长度选项
    matchQueryBuilder.prefixLength(3);
    // 设置最大扩展选项以控制查询的模糊过程
    matchQueryBuilder.maxExpansions(10);

d.也可以使用QueryBuilders实用程序类创建QueryBuilder对象。此类提供了可用于使用流畅的编程样式创建QueryBuilder对象的辅助方法：

    QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("user", "kimchy")
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .maxExpansions(10);

注：无论用于创建它的方法是什么，都必须将QueryBuilder对象添加到SearchSourceBuilder， searchSourceBuilder.query(matchQueryBuilder);

**mathcQuery与termQuery区别：**

- matchQuery：会将搜索词分词，再与目标查询字段进行匹配，若分词中的任意一个词与目标字段匹配上，则可查询到。
- termQuery：不会对搜索词进行分词处理，而是作为一个整体与目标字段进行匹配，若完全匹配，则可查询到。

**组合查询**

- .must(QueryBuilders): AND
- .mustNot(QueryBUilders):NOT
- .should :OR

**通配符查询（支持* 匹配任何字符序列，包括空 避免*开始，会检索大量内容造成效率缓慢）**

QueryBuilder queryBuilder = QueryBuilders.wildcardQuery("user","he*o");

**分词 模糊查询 fuzzy query**

QueryBuilders.fuzzyQuery("name","hello");

**前缀匹配查询 prefix query**

QueryBuilders.prefixQuery("name","hello");

**3）设置查询的起始索引位置和数量**：如下表示从第1条开始，共返回5条文档数据

    sourceBuilder.from(0);
    sourceBuilder.size(5);

**4）设置查询请求的超时时间**：如下表示60秒没得到返回结果时就认为请求已超时

    sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

**5）默认情况下，搜索请求会返回文档_source的内容**，但与Rest API中的内容一样，您可以覆盖此行为。例如，您可以完全关闭_source检索：

    sourceBuilder.fetchSource(false);

该方法还接受一个或多个通配符模式的数组，以控制以更精细的方式包含或排除哪些字段

    String[] includeFields = new String[] {"title", "user", "innerObject.*"};
    String[] excludeFields = new String[] {"_type"};
    sourceBuilder.fetchSource(includeFields, excludeFields);


