package com.bulletjournal.es.repository;

import com.bulletjournal.es.repository.models.ProjectItemContentIndex;
import com.bulletjournal.es.repository.models.ProjectItemNameIndex;
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
    private ProjectItemNameSearchRepository projectItemNameSearchRepository;

    @Autowired
    private ProjectItemContentSearchRepository projectItemContentSearchRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private UserRepository userRepository;

    public void saveProjectItemNameIndex(Long id, String name) {
        ProjectItemNameIndex projectItemNameIndex = new ProjectItemNameIndex(id, name);
        projectItemNameSearchRepository.save(projectItemNameIndex);
    }

    public void saveProjectItemContentIndex(Long id, String content) {
        ProjectItemContentIndex projectItemContentIndex = new ProjectItemContentIndex(id, content);
        projectItemContentSearchRepository.save(projectItemContentIndex);
    }

    public List<ProjectItemNameIndex> search(String username, String term) {
        List<ProjectItemNameIndex> results = new ArrayList<>();

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

        projectItemNameSearchRepository.search(queryBuilder);
        return results;
    }
}
