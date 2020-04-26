package com.bulletjournal.es;

import com.bulletjournal.config.SpringESConfig;
import com.bulletjournal.controller.models.ProjectItem;
import com.bulletjournal.repository.GroupDaoJpa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    public static final String PROJECT_ITEM = "project_item";

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

//    @Qualifier("client")
//    @Autowired(required = false)
//    private RestHighLevelClient highLevelClient;

    @Autowired
    private SpringESConfig springESConfig;

    @Autowired
    private GroupDaoJpa groupDaoJpa;

    public void saveToES(ProjectItem projectItem) {
//        if (highLevelClient == null) {
//            return;
//        }
//
//        List<Long> relatedGroupIds = groupDaoJpa.getProjectItemGroups(projectItem.getOwner())
//                .stream()
//                .map(Group::getId)
//                .collect(Collectors.toList());
//        BulkRequest bulkRequest = new BulkRequest(PROJECT_ITEM, "default");
//        for (Long groupId : relatedGroupIds) {
//            Map<String, Object> json = new HashMap<>();
//            json.put("project_item_name", projectItem.getName());
//            json.put("group_id", groupId);
//            String index = projectItem.getClass().getSimpleName() + "@" + projectItem.getId();
//            IndexRequest request = new IndexRequest(PROJECT_ITEM, "default", index)
//                    .source(json)
//                    .setRefreshPolicy(WriteRequest.RefreshPolicy.NONE);
//            bulkRequest.add(request);
//        }
//        try {
//            highLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
//        } catch (IOException e) {
//            LOGGER.error("SaveToES fail", e);
//        }
    }
}
