package com.bulletjournal.controller;

import com.bulletjournal.clients.UserClient;
import com.bulletjournal.es.repository.ProjectItemSearchDaoJpa;
import com.bulletjournal.es.repository.models.ProjectItemNameIndex;
import com.bulletjournal.repository.UserGroupRepository;
import com.bulletjournal.repository.UserRepository;
import org.elasticsearch.action.search.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;


@RestController
public class QueryController {

    protected static final String SEARCH_ROUTE = "/api/query";
    protected static final String SEARCH_ROUTE_JPA = "/api/query_jpa/";
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryController.class);

//    @Qualifier("client")
//    @Autowired(required = false)
//    private RestHighLevelClient highLevelClient;

    @Autowired
    UserGroupRepository userGroupRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectItemSearchDaoJpa projectItemSearchDaoJpa;


    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(SEARCH_ROUTE)
    @ResponseStatus(HttpStatus.OK)
    public SearchResponse searchItems(@Valid @RequestParam @NotBlank String term) throws IOException {
        return null;
//        if (highLevelClient == null) {
//            LOGGER.info("ES is not enabled.");
//            return null;
//        }
//
//        // TODO return List<ProjectItems>
//        String username = MDC.get(UserClient.USER_NAME_KEY);
//        List<User> userList = this.userRepository.findByName(username);
//        List<Long> userIdList = userList.stream().map(User::getId).collect(Collectors.toList());
//        List<Long> groupIdList = userGroupRepository.findAllByUserId(userIdList.get(0))
//                .stream().map(UserGroup::getGroup).map(Group::getId).collect(Collectors.toList());
//
//        SearchRequest searchRequest = new SearchRequest(SearchService.PROJECT_ITEM);
//        searchRequest.source(new SearchSourceBuilder().query(
//                QueryBuilders.boolQuery()
//                        .filter(QueryBuilders.termsQuery("group_id", groupIdList))
//                        .must(QueryBuilders.matchQuery("project_item_name", term)
//                                .fuzziness(Fuzziness.AUTO)
//                                .prefixLength(3)
//                                .maxExpansions(10))
//
//        ));
//
//        SearchResponse response = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//        LOGGER.info(response.toString());
//        return response;
    }

    @GetMapping(SEARCH_ROUTE_JPA)
    @ResponseStatus(HttpStatus.OK)
    public List<ProjectItemNameIndex> search(@Valid @RequestParam @NotBlank String term) {
        String username = MDC.get(UserClient.USER_NAME_KEY);
        return projectItemSearchDaoJpa.search(username, term);
    }
}
