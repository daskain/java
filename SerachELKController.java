package rest.conroller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import rest.pojo.XMLResponse;

@RestController
public class SerachELKController {
	public String result;
	@RequestMapping(value = "/searchelk", method = RequestMethod.POST)
	public String search (@RequestPart(name="file", required = true) MultipartFile source) throws IOException {
		//System.out.println(orderId);
		BufferedReader in = new BufferedReader(
				new InputStreamReader(source.getInputStream()));
		String inputLine;
		StringBuilder str = new StringBuilder();
		ArrayList<String> csv = new ArrayList<String>();
		while ((inputLine = in.readLine()) != null) {
			csv.add(inputLine);
		};
		
		csv.stream()
			.forEach(s -> {
				System.out.println(s);
		        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("host", port, "http")));
		        SearchRequest searchRequest = new SearchRequest("someindex-*");
            
		        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		        BoolQueryBuilder query = new BoolQueryBuilder();
            
		        query.must(new MatchQueryBuilder("Order_ID", s));
		        query.must(new MatchPhraseQueryBuilder("Message", "Some message"));
		        sourceBuilder.query(query);
            
		        String[] includeFields = new String[] {"Event_Attributes"}; //фильтр по полям
		        sourceBuilder.fetchSource(includeFields, null); //активируем фильтр
		        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //задаем timeout
		        searchRequest.source(sourceBuilder);
		        
		        try {
						SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
						SearchHits hits = searchResponse.getHits();
				        SearchHit [] searchHits = hits.getHits ();
				        for (SearchHit test : searchHits) {
				            
				            Object findCode = test.getSourceAsMap().get("Event_Attributes");
				            String raw = findCode.toString();
				            System.out.println(raw); //
				            Pattern p = Pattern.compile("raw:(.*)]", Pattern.CASE_INSENSITIVE);
				            Matcher matcher = p.matcher(raw);
				            if(matcher.find()){
				                matcher.group(1).trim(); //строка подается с пробелами на концах
				                XmlMapper mapper = new XmlMapper();
				                XMLResponse value =  new XMLResponse();
				                		value = mapper.readValue(matcher.group(1).trim(), XMLResponse.class);
                            
				                System.out.println(matcher.group(1).trim());
				                System.out.println(value.getResCode());
				                str.append(value.getResCode());
                    }
              }
					}catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	        
			        
			});
	return str.toString();
	}

}
