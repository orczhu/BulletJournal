package com.bulletjournal.es.repository;

import com.bulletjournal.es.repository.models.ProjectItem;
import com.bulletjournal.repository.UserGroupRepository;
import com.bulletjournal.repository.UserRepository;
import com.bulletjournal.repository.models.Group;
import com.bulletjournal.repository.models.User;
import com.bulletjournal.repository.models.UserGroup;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ProjectItemSearchDaoJpa {

    @Autowired
    private ProjectItemSearchRepository projectItemSearchRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ProjectItem> search(String username, String term) {
        List<ProjectItem> results = new ArrayList<>();

        List<User> users = this.userRepository.findByName(username);
        List<Long> userIdList = users.stream().map(User::getId).collect(Collectors.toList());
        List<Long> groupIdList = userGroupRepository.findAllByUserId(userIdList.get(0))
                .stream().map(UserGroup::getGroup).map(Group::getId).collect(Collectors.toList());

        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termsQuery("group_id", groupIdList))
                .must(QueryBuilders.matchQuery("project_item_name", term)
                        .fuzziness(Fuzziness.AUTO)
                        .prefixLength(3)
                        .maxExpansions(10));

        projectItemSearchRepository.search(queryBuilder);

        return results;
    }
}
