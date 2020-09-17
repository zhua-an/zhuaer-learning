import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhuaer.learning.elasticsearch.Application;
import com.zhuaer.learning.elasticsearch.entity.User;
import lombok.SneakyThrows;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.indices.TermsLookup;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;

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
        users.add(new User("kimchy", 10));
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
     * 组合查询
     * must(QueryBuilders) :   AND
     * mustNot(QueryBuilders): NOT
     * should:                  : OR
     */
    @SneakyThrows
    @Test
    public void testQueryBuilder2() {
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("name", "test"))
                .mustNot(QueryBuilders.termQuery("name", "tianqi"))
                .should(QueryBuilders.termQuery("age", 12));
        searchFunction(queryBuilder);
    }

    /**
     * 只查询一个id的
     * 根据给定的idArray查询文档
     * es:
     * GET index/_search
     * {
     *   "query": {
     *    "ids": {"values": [idArray]}
     *   }
     * }
     * QueryBuilders.idsQuery(String...type).ids(Collection<String> ids)
     */
    @SneakyThrows
    @Test
    public void testIdsQuery() {
        QueryBuilder queryBuilder = QueryBuilders.idsQuery().addIds("1");
        searchFunction(queryBuilder);
    }


    /**
     * 包裹查询, 高于设定分数, 不计算相关性
     * 根据给定的字段的值的分词查询分词中包含给定的值的文档
     * 待查询的字段类型为text会分词
     * 待查询的值不会分词
     * es:
     * GET index/_search
     * {
     *   "query": {
     *    "term": {
     *      "待查询的字段": {
     *        "value": "待查询的值"
     *      }
     *    }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void testConstantScoreQuery() {
        QueryBuilder queryBuilder = QueryBuilders.constantScoreQuery(QueryBuilders.termQuery("age", 12)).boost(2.0f);
        searchFunction(queryBuilder);
    }

    /**
     * disMax查询
     * 对子查询的结果做去重合并，score沿用子查询score的最大值
     * 待查询的字段类型为text会分词
     * 待查询的值会分词
     * 广泛用于muti-field查询
     * es:
     * GET index/_search
     * {
     *   "query": {
     *    "dis_max": {
     *      "queries": [
     *          查询条件array
     *      ]
     *    }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void testDisMaxQuery() {
        QueryBuilder queryBuilder = QueryBuilders.disMaxQuery()
                .add(QueryBuilders.matchPhrasePrefixQuery("name", "test"))  // 查询条件
                .add(QueryBuilders.termQuery("name", "zhangsan"))
                .boost(1.3f)
                .tieBreaker(0.7f);
        searchFunction(queryBuilder);
    }

    /**
     * 模糊查询
     * 不能用通配符
     * 根据给定的字段的值的分词查询分词中包含给定的值在纠正指定次数（默认是2）后的文档
     * 待查询的字段不会分词
     * 待查询的值不会分词
     * es:
     * GET index/_search
     * {
     *   "query": {
     *    "fuzzy": {
     *      "待查询的字段": {
     *        "value": "待查询的值，会被纠正",
     *        "fuzziness": 纠正次数
     *
     *      }
     *    }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void testFuzzyQuery() {
        QueryBuilder queryBuilder = QueryBuilders.fuzzyQuery("name", "zhangsan");
        searchFunction(queryBuilder);
    }

    /**
     * 根据给定的字段的值的分词查询分词中包含给定的值的文档
     * 待查询的字段类型为text会分词
     * 待查询的值会分词
     * es：
     *  GET index/_search
     *  {
     *      "query": {
     *          "match": {
     *              "待查询的字段": "待查询的值，多个值用" "隔开"，
     *              "operator": "or"
     *           }
     *      }
     * }
     */
    @SneakyThrows
    @Test
    public void testMatchQuery() {
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("name", "zhang");
        searchFunction(queryBuilder);
    }

    /**
     * 根据给定的字段列表的值的分词查询分词中包含给定的值的文档
     * 待查询的字段类型为text会分词
     * 待查询的值会分词
     * es:
     * GET index/_search
     * {
     *   "query": {
     *    "multi_match": {
     *      "query": "待查询的字段，多个好像无法正确查询出结果",
     *      "fields": [待查询的字段array]
     *    }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void multiMatchQuery(){
        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery("待查询的值", "待查询的字段array");
        searchFunction(queryBuilder);
    }


    /**
     * 根据给定的字段的值的分词查询分词中包含给定的值的文档，匹配的字段分词所在位置必须和待查询的值的分词位置一致
     * 待查询的字段类型为text会分词
     * 待查询的值会分词
     * es:
     * GET index/_search
     * {
     *   "query": {
     *    "match_phrase": {
     *      "待查询的字段": "待查询的值，多个值用" "隔开"
     *    }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void matchPhraseQuery(){
        QueryBuilder queryBuilder = QueryBuilders.matchPhraseQuery("待查询的字段", "待查询的值");
        searchFunction(queryBuilder);
    }

    /**
     * 根据给定的字段的值的分词查询分词中包含给定的值的文档，匹配的字段分词所在位置必须和待查询的值的分词位置一致，
     * 并且会将待查询的值的最后一个词作为前缀去进行查询
     * 待查询的字段类型为text会分词
     * 待查询的值会分词
     * es:
     * GET index/_search
     * {
     *   "query": {
     *    "match_phrase_prefix": {
     *      "待查询的字段": "待查询的值，多个值用" "隔开"
     *    }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void matchPhrasePrefix(){
        QueryBuilder queryBuilder = QueryBuilders.matchPhrasePrefixQuery("待查询的字段", "待查询的值");
        searchFunction(queryBuilder);
    }

    /**
     * 根据给定的字段的值的分词查询分词中符合查询字符串的文档
     * 待查询的字段类型为text会分词
     * 查询字符串会分词
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "query_string": {
     *       "fields": [待查询字段array], 或 "default_field": 待查询的字段,
     *       "query": "查询字符串（支持的通配符。支持通过AND OR NOT ！进行布尔运算。+：代表必须含有  -：代表不能含有）"
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void testQueryString() {
//        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("+tianqi");
        Map<String, Float> fields = new HashMap<>();
        fields.put("待查询的字段q", QueryStringQueryBuilder.DEFAULT_BOOST);
        fields.put("待查询的字段w", QueryStringQueryBuilder.DEFAULT_BOOST);
//        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("查询字符串").defaultField("待查询的字段");
        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("查询字符串").fields(fields);
        searchFunction(queryBuilder);
    }

    /**
     * moreLikeThisQuery: 实现基于内容推荐, 支持实现一句话相似文章查询
     * {
     "more_like_this" : {
     "fields" : ["title", "content"],   // 要匹配的字段, 不填默认_all
     "like_text" : "text like this one",   // 匹配的文本
     }
     }

     percent_terms_to_match：匹配项（term）的百分比，默认是0.3

     min_term_freq：一篇文档中一个词语至少出现次数，小于这个值的词将被忽略，默认是2

     max_query_terms：一条查询语句中允许最多查询词语的个数，默认是25

     stop_words：设置停止词，匹配时会忽略停止词

     min_doc_freq：一个词语最少在多少篇文档中出现，小于这个值的词会将被忽略，默认是无限制

     max_doc_freq：一个词语最多在多少篇文档中出现，大于这个值的词会将被忽略，默认是无限制

     min_word_len：最小的词语长度，默认是0

     max_word_len：最多的词语长度，默认无限制

     boost_terms：设置词语权重，默认是1

     boost：设置查询权重，默认是1

     analyzer：设置使用的分词器，默认是使用该字段指定的分词器
     */
    @SneakyThrows
    @Test
    public void testMoreLikeThisQuery() {
        QueryBuilder queryBuilder = QueryBuilders.moreLikeThisQuery(new String[]{"name"}, new String[]{"zhangsan"}, null);
//                            .minTermFreq(1)         //最少出现的次数
//                            .maxQueryTerms(12);        // 最多允许查询的词语
        searchFunction(queryBuilder);
    }


    /**
     * 根据给定的字段的值的分词查询分词中在指定范围内的文档
     * 待查询的字段类型为text会分词
     * 待查询的值不会分词
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "range": {
     *       "待查询的字段": {
     *         "gte": "下限",
     *         "lte": "上限"
     *       }
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void testRangeQuery() {
        QueryBuilder queryBuilder = QueryBuilders.rangeQuery("name")
                .from("test")
                .to("tianqi")
                .includeLower(true)     // 包含上界
                .includeUpper(true);      // 包含下届
        searchFunction(queryBuilder);
    }


    /**
     * 根据给定的字段的值的分词查询分词中包含待查询的值（包含通配符，*：任意字符；?：任意一个字符）的文档
     * 注意：尽量别用*或?开头
     * 待查询的字段类型为text会分词
     * 待查询的值不会分词
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "wildcard": {
     *       "待查询的字段": {
     *         "value": "待查询的值（包含通配符，*：任意字符；?：任意一个字符）"
     *       }
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void testWildCardQuery() {
        QueryBuilder queryBuilder = QueryBuilders.wildcardQuery("user", "zh*an");
        searchFunction(queryBuilder);
    }

    /**
     * 根据给定的字段的值的分词查询分词中符合正则表达式的文档
     * 注意：最好在使用正则前，加上匹配的前缀
     * 待查询的字段类型为text会分词
     * 待查询的正则表达式不会分词
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "regexp": {
     *       "待查询的字段": "待查询的正则表达式"
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void regexpQuery(){
        QueryBuilder queryBuilder = QueryBuilders.regexpQuery("待查询的字段", "待查询的正则表达式");
        searchFunction(queryBuilder);
    }

    /**
     * 嵌套查询, 内嵌文档查询
     */
    @Test
    public void testNestedQuery() {
        QueryBuilder queryBuilder = QueryBuilders.nestedQuery("location",
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("location.lat", 0.962590433140581))
                        .must(QueryBuilders.rangeQuery("location.lon").lt(36.0000).gt(0.000)), ScoreMode.Total);

    }

    /**
     * 根据给定的字段的值的分词查询分词中符合查询字符串的文档
     * 待查询的字段类型为text会分词
     * 查询字符串会分词
     * es:
     * GET index/_search
     * {
     *   "simple_query_string": {
     *     "query_string": {
     *       "fields": [待查询字段array], 或 "default_field": 待查询的字段,
     *       "query": "查询字符串（支持的通配符。不支持通过AND OR NOT ！进行布尔运算。+：代表必须含有  -：代表不能含有）"
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void simpleQueryStringQuery(){
        Map<String, Float> fields = new HashMap<>();
        fields.put("待查询的字段q", QueryStringQueryBuilder.DEFAULT_BOOST);
        fields.put("待查询的字段w", QueryStringQueryBuilder.DEFAULT_BOOST);
//        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("查询字符串").simpleQueryStringQuery("待查询的字段");
        QueryBuilder queryBuilder = QueryBuilders.simpleQueryStringQuery("查询字符串").fields(fields);
        searchFunction(queryBuilder);
    }

    /**
     * 返回positive query的查询结果，如果positive query的查询结果也满足negative query，则改变其_source的值
     * es:
     * GET index/_search
     * {
     *   "query": {
     *    "boosting": {
     *      "positive": {
     *        指定用于查询的 query，最后返回结果必须满足 positive 对应的条件
     *      },
     *      "negative": {
     *        指定影响相关性算分的 query，
     *        如果positive query查询出来的文档同时满足 negative query，
     *        那么最终得分 = positive query 得分 * negative_boost
     *      },
     *      "negative_boost": 范围是 0 到 1.0
     *    }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void boostingQuery(){
        QueryBuilder positiveQuery = QueryBuilders.wildcardQuery("待查询的字段", "待查询的值");
        QueryBuilder negativeQuery = QueryBuilders.wildcardQuery("待查询的字段", "待查询的值");
        QueryBuilder queryBuilder = QueryBuilders.boostingQuery(positiveQuery, negativeQuery).negativeBoost(0.1f);
        searchFunction(queryBuilder);
    }

    /**
     * 返回满足bool下所有query的结果
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "bool": {
     *       "should": [
     *          query array
     *          返回的文档可能满足should子句的条件。
     *          在一个Bool查询中，如果没有must或者filter，有一个或者多个should子句，那么只要满足一个就可以返回。
     *          minimum_should_match参数定义了至少满足几个子句。
     *       ],
     *       "must": [
     *          query array
     *          返回的文档必须满足must子句的条件，并且参与计算分值
     *       ],
     *       "must_not": [
     *         query array
     *         返回的文档必须不满足must_not定义的条件。
     *       ],
     *       "filter": {query 返回的文档必须满足filter子句的条件。但是不会像Must一样，参与计算分值}
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void boolQuery(){
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.wildcardQuery("待查询的字段", "待查询的值"))
                .must(QueryBuilders.wildcardQuery("待查询的字段", "待查询的值"))
                .should(QueryBuilders.wildcardQuery("待查询的字段", "待查询的值"))
                .mustNot(QueryBuilders.wildcardQuery("待查询的字段", "待查询的值"))
                .filter(QueryBuilders.wildcardQuery("待查询的字段", "待查询的值"));
        searchFunction(queryBuilder);
    }

    /**
     * 等同于 term query ，但与其他Span查询一起使用
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "span_term": {
     *       "待查询的字段": {
     *         "value": "待查询的值"
     *       }
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void spanTermQuery(){
        QueryBuilder queryBuilder = QueryBuilders.spanTermQuery("待查询的字段", "待查询的值");
        searchFunction(queryBuilder);
    }

    /**
     * 查询出待查询的值在待查询的字段的值的分词中前end个位置的文档
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "span_first": {
     *       "match": {
     *         "span_term": {
     *           "待查询的字段": "待查询的值"
     *         }
     *       },
     *       "end": 最大位置值
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void spanFirstQuery(){
        QueryBuilder queryBuilder = QueryBuilders.spanFirstQuery(
                QueryBuilders.spanTermQuery("待查询的字段", "待查询的值"),
                3);
        searchFunction(queryBuilder);

    }

    /**
     * 几个span query匹配的值的跨度必须在0-slop范围内，匹配的值的顺序必须和span query顺序一样
     * 注意：所有span query的待查询的字段必须为同一个，不然会报异常
     * es:
     * GET index/_search
     * {
     *   "query": {
     *    "span_near": {
     *      "clauses": [
     *          span query array
     *      ],
     *      "slop": 最大的跨度,
     *      "in_order": false
     *    }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void spanNearQuery(){
        QueryBuilder queryBuilder = QueryBuilders.spanNearQuery(
                QueryBuilders.spanTermQuery("待查询的字段", "待查询的值"),
                12).addClause(QueryBuilders.spanTermQuery("待查询的字段", "待查询的值"));
        searchFunction(queryBuilder);
    }

    /**
     * 把include query查询结果中符合exclude query的文档排除后返回结果
     * 注意：include query和exclude query的待查询的字段必须为同一个，不然会报异常
     * 注意：include query和exclude query的span query都为span_term好像不会被拦截
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "span_not": {
     *       "include": {
     *         "span_term": {
     *           "待查询的字段": {
     *             "value": "待查询的值"
     *           }
     *         }
     *       },
     *       "exclude": {
     *         "span_term": {
     *           "待查询的字段": {
     *             "value": "待查询的值"
     *           }
     *         }
     *       }
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void spanNotQuery(){
        QueryBuilder queryBuilder = QueryBuilders.spanNotQuery(
                QueryBuilders.spanTermQuery("待查询的字段", "待查询的值"),
                QueryBuilders.spanTermQuery("待查询的字段", "待查询的值"));
        searchFunction(queryBuilder);
    }

    /**
     * 返回与任何span query匹配的文档
     * 注意：所有span query的待查询的字段必须为同一个，不然会报异常
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "span_or": {
     *       "clauses": [
     *         span query array
     *       ]
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void spanOrQuery(){
        QueryBuilder queryBuilder = QueryBuilders.spanOrQuery(QueryBuilders.spanTermQuery("待查询的字段", "待查询的值"))
                .addClause(QueryBuilders.spanTermQuery("待查询的字段", "待查询的值"));
        searchFunction(queryBuilder);
    }

    /**
     * 查找符合big query条件且包含little query的文档
     * 注意：所有span query的待查询的字段必须为同一个，不然会报异常
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "span_within": {
     *       "little": {
     *         "span_term": {
     *           "待查询的字段": {
     *             "value": "待查询的值"
     *           }
     *         }
     *       },
     *       "big": {
     *         "span_near": {
     *           "clauses": [
     *             {
     *               "span_term": {
     *                 "待查询的字段": {
     *                   "value": "待查询的值"
     *                 }
     *               }
     *             },
     *             {
     *               "span_term": {
     *                 "待查询的字段": {
     *                   "value": "待查询的值"
     *                 }
     *               }
     *             }
     *           ],
     *           "slop": 1,
     *           "in_order": false
     *         }
     *       }
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void spanWithinQuery(){
        QueryBuilder queryBuilder = QueryBuilders.spanWithinQuery(
                QueryBuilders.spanNearQuery(
                        QueryBuilders.spanTermQuery("待查询的字段", "待查询的值"),
                        12).addClause(QueryBuilders.spanTermQuery("待查询的字段", "待查询的值")),
                QueryBuilders.spanTermQuery("待查询的字段", "待查询的值"));
        searchFunction(queryBuilder);
    }

    /**
     * 查找符合big query条件的文档，之后筛选出包含little query的文档
     * 注意：所有span query的待查询的字段必须为同一个，不然会报异常
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "span_containing": {
     *       "little": {
     *         "待查询的字段": {
     *           "message": {
     *             "value": "待查询的值"
     *           }
     *         }
     *       },
     *       "big": {
     *         "span_near": {
     *           "clauses": [
     *             {
     *               "待查询的字段": {
     *                 "message": {
     *                   "value": "待查询的值"
     *                 }
     *               }
     *             },
     *             {
     *               "待查询的字段": {
     *                 "message": {
     *                   "value": "待查询的值"
     *                 }
     *               }
     *             }
     *           ],
     *           "slop": 1,
     *           "in_order": false
     *         }
     *       }
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void spanContainingQuery(){
        QueryBuilder queryBuilder = QueryBuilders.spanWithinQuery(
                QueryBuilders.spanNearQuery(
                        QueryBuilders.spanTermQuery("待查询的字段", "待查询的值"),
                        12).addClause(QueryBuilders.spanTermQuery("待查询的字段", "待查询的值")),
                QueryBuilders.spanTermQuery("待查询的字段", "待查询的值"));
        searchFunction(queryBuilder);
    }

    /**
     * 查找满足条件的文档
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "span_multi": {
     *       "match": {
     *         可以是term, range, prefix, wildcard, regexp 或者 fuzzy 查询
     *       }
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void spanMultiTermQueryBuilder(){
        QueryBuilder queryBuilder = QueryBuilders.spanMultiTermQueryBuilder(QueryBuilders.wildcardQuery("待查询的字段", "待查询的值"));
        searchFunction(queryBuilder);
    }

    /**
     * 具体不清楚是干嘛的，好像只能用于span query下，猜测是从已查询到的文档中过滤出符合该query的文档
     * 注意：所有span query的待查询的字段必须为同一个，不然会报异常
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "span_near": {
     *       "clauses": [
     *         {
     *           "span_term": {
     *             "待查询的字段": {
     *               "value": "待查询的值"
     *             }
     *           }
     *         },
     *         {
     *           "field_masking_span": {
     *             "query": {
     *               "span_term": {
     *                 "待查询的字段": {
     *                   "value": "待查询的值"
     *                 }
     *               }
     *             },
     *             "field": "待查询的字段"
     *           }
     *         }
     *       ],
     *       "slop": 12,
     *       "in_order": false
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void fieldMaskingSpanQuery(){
        QueryBuilder queryBuilder = QueryBuilders.fieldMaskingSpanQuery(
                QueryBuilders.spanTermQuery("待查询的字段", "待查询的值"),
                "待查询的字段");
        searchFunction(queryBuilder);
    }

    /**
     * 查询满足条件的文档并返回指定的_source
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "constant_score": {
     *       "filter": {
     *         query
     *       },
     *       "boost": 1.2
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void constantScoreQuery(){
        QueryBuilder queryBuilder = QueryBuilders.constantScoreQuery(
                QueryBuilders.termQuery("待查询的字段", "待查询的值")).boost(1.2f);
        searchFunction(queryBuilder);
    }

    /**
     * 查询满足条件的文档但不对_source进行计算
     * es:
     * GET index/_search
     * {
     *   "query": {
     *    "function_score": {
     *      "query": {
     *        query
     *      }
     *    }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void functionScoreQueryNoFunction(){
        QueryBuilder queryBuilder = QueryBuilders.functionScoreQuery(
                QueryBuilders.termQuery("待查询的字段", "待查询的值"));
        searchFunction(queryBuilder);
    }

    /**
     * 查询满足条件的文档并对_source进行计算
     * 这个太过复杂，详情请看https://www.jianshu.com/p/f164f127bf33
     */
    @SneakyThrows
    @Test
    public void functionScoreQuery(){
        FunctionScoreQueryBuilder.FilterFunctionBuilder[] filterFunctionBuilders =
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[2];
        filterFunctionBuilders[0] = new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                QueryBuilders.termQuery("待查询的字段", "待查询的值"),
                ScoreFunctionBuilders.fieldValueFactorFunction("待查询的字段"));
        filterFunctionBuilders[1] = new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                ScoreFunctionBuilders.randomFunction().seed(1).setField("待查询的字段"));
        QueryBuilder queryBuilder = QueryBuilders.functionScoreQuery(
                QueryBuilders.termQuery("待查询的字段", "待查询的值"),filterFunctionBuilders);
        searchFunction(queryBuilder);
    }

    /**
     * 根据待查询字段array查询包含待查询的值array的文档
     * 相关参数解释地址：https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-mlt-query.html
     * es7.6测试查不到数据，不知道是什么原因
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "more_like_this": {
     *       "fields": [
     *         待查询字段array，es7.6必须为非空，所用的springboot-data-elasticsearch不兼容es7.6
     *       ],
     *       "like": ["待查询的值array"],
     *       "min_term_freq": 1,
     *       "max_query_terms": 12
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void moreLikeThisQuery(){
        String[] likes = new String[1];
        likes[0] = "待查询的值";
        QueryBuilder queryBuilder = QueryBuilders.moreLikeThisQuery(likes);
        searchFunction(queryBuilder);
    }

    /**
     * 用于nested嵌套类型的查询
     * nested嵌套类型详细说明请看：https://blog.csdn.net/laoyang360/article/details/82950393
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "nested": {
     *       "path": "类型为nested的字段名",
     *       "query": {
     *          query，例：
     *              "match": {
     *                  "类型为nested的字段名.类型为nested的字段里的字段名": "待查询的值"
     *              }
     *       }
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void nestedQuery(){
        QueryBuilder query = QueryBuilders.matchQuery("类型为nested的字段名.类型为nested的字段里的字段名", "待查询的值");
        QueryBuilder queryBuilder = QueryBuilders.nestedQuery("类型为nested的字段名", query, ScoreMode.None);
        searchFunction(queryBuilder);
    }

    /**
     * 根据给定的字段的值的分词查询分词中包含给定的值array的文档
     * 待查询的字段类型为text会分词
     * 待查询的值不会分词
     * es:
     * GET index/_search
     * {
     *   "query": {
     *    "terms": {
     *      "待查询的字段": [
     *        待查询的值array
     *      ]
     *    }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void termsQuery(){
        String[] values = new String[2];
        values[0] = "待查询的值1";
        values[1] = "待查询的值2";
        QueryBuilder queryBuilder = QueryBuilders.termsQuery("待查询的字段", values);
        searchFunction(queryBuilder);
    }

    /**
     * 根据查询语句的base编码格式进行查询，例如：
     * eyJ0ZXJtIiA6IHsgIuW+heafpeivoueahOWtl+autSI6ICLlvoXmn6Xor6LnmoTlgLwiIH19 对应 {"term" : { "待查询的字段": "待查询的值" }}
     * es:
     * GET index/_search
     * {
     *   "query": {
     *      "wrapper": {
     *          "query": "base64编码格式的查询语句"
     *      }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void wrapperQuery(){
        QueryBuilder queryBuilder = QueryBuilders.wrapperQuery("eyJ0ZXJtIiA6IHsgIuW+heafpeivoueahOWtl+autSI6ICLlvoXmn6Xor6LnmoTlgLwiIH19");
        searchFunction(queryBuilder);
    }

    /**
     * 根据文档类型查询，在7.0中已经被弃用
     * es:
     * GET /_search
     * {
     *     "query": {
     *         "type" : {
     *             "value" : "待查询类型"
     *         }
     *     }
     * }
     */
    @SneakyThrows
    @Test
    public void typeQuery(){
        QueryBuilder queryBuilder = QueryBuilders.typeQuery("待查询类型");
        searchFunction(queryBuilder);
    }

    /**
     * 根据给定的字段的值的分词查询分词中包含指定索引下指定id的文档的指定字段的值
     * 待查询的字段类型为text会分词
     * 待查询的值不会分词
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "terms": {
     *       "待查询的字段": {
     *         "index": "作为查询条件的索引",
     *         "id": "作为查询条件的文档的id",
     *         "path": "作为查询条件的字段"
     *       }
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void termsLookupQuery(){
        TermsLookup termsLookup = new TermsLookup("作为查询条件的索引", "作为查询条件的文档类型", "作为查询条件的文档的id", "作为查询条件的字段");
        QueryBuilder queryBuilder = QueryBuilders.termsLookupQuery("待查询的字段", termsLookup);
        searchFunction(queryBuilder);
    }

    /**
     *
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "script": {
     *       "script": {
     *         作为查询运行的脚本，具体使用方法见：
     *         https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-scripting-using.html
     *         例子，查询指定字段的值大于指定值的数据：
     *         "source": "doc['自定字段'].value > params.param1",
     *         "lang": "painless",
     *         "params": {
     *           "param1": 2
     *         }
     *       }
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void scriptQuery(){
        Map<String, Object> params = new HashMap<>();
        params.put("param1", 2);
        Script script = new Script(ScriptType.INLINE, Script.DEFAULT_SCRIPT_LANG,
                "doc['自定字段'].value > params.param1", params);
        QueryBuilder queryBuilder = QueryBuilders.scriptQuery(script);
        searchFunction(queryBuilder);
    }

    /**
     * 查询文档中包含待查询字段的数据
     * es:
     * GET index/_search
     * {
     *   "query": {
     *     "exists": {
     *       "field": "待查询字段"
     *     }
     *   }
     * }
     */
    @SneakyThrows
    @Test
    public void existsQuery(){
        QueryBuilder queryBuilder = QueryBuilders.existsQuery("待查询字段");
        searchFunction(queryBuilder);
    }
    
    /**
     * 分页match查询
     * 使用QueryBuilder
     * termQuery("key", obj) 完全匹配
     * termsQuery("key", obj1, obj2..)   一次匹配多个值
     * matchQuery("key", Obj) 单个匹配, field不支持通配符, 前缀具高级特性
     * multiMatchQuery("text", "field1", "field2"..);  匹配多个字段, field有通配符忒行
     * matchAllQuery();         匹配所有文件
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
        String fileValue = "zh";
        int startPage = 0;
        int maxSize = 10;
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        QueryBuilders.matchAllQuery()
//        searchSourceBuilder.query(QueryBuilders.matchQuery(fieldName, fileValue));
//        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(fieldName, fileValue, "test"));
//        searchSourceBuilder.query(QueryBuilders.termQuery(fieldName, fileValue));
        //模糊匹配
        searchSourceBuilder.query(QueryBuilders.fuzzyQuery(fieldName, fileValue).fuzziness(Fuzziness.AUTO));

        //分页
        searchSourceBuilder.from(startPage);
        searchSourceBuilder.size(maxSize);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("=response=" + JSONObject.toJSON(response.getHits().getHits()));
        SearchHit[] hits = response.getHits().getHits();
        List<User> userList = new LinkedList<>();
        for (SearchHit hit : hits) {
            User user = JSONObject.parseObject(hit.getSourceAsString(), User.class);
            userList.add(user);
        }
        System.out.println(userList);
    }

    /**
     * 查询遍历抽取
     * @param queryBuilder
     */
    private void searchFunction(QueryBuilder queryBuilder) throws IOException {
        String indexName = "test";
        int startPage = 0;
        int maxSize = 10;
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        //分页
        searchSourceBuilder.from(startPage);
        searchSourceBuilder.size(maxSize);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("=response=" + JSONObject.toJSON(response.getHits().getHits()));
        SearchHit[] hits = response.getHits().getHits();
        List<User> userList = new LinkedList<>();
        for (SearchHit hit : hits) {
            User user = JSONObject.parseObject(hit.getSourceAsString(), User.class);
            userList.add(user);
        }
        System.out.println(userList);
    }


}

