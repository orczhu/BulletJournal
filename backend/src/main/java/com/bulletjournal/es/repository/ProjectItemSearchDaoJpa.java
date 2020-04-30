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

    public void saveProjectItemNameIndex(String username, Long id, String name) {

        List<User> userList = this.userRepository.findByName(username);
        List<Long> userIdList = userList.stream().map(User::getId).collect(Collectors.toList());
        List<Long> groupIdList = userGroupRepository.findAllByUserId(userIdList.get(0))
                .stream().map(UserGroup::getGroup).map(Group::getId).collect(Collectors.toList());

        List<ProjectItemNameIndex> projectItemNameIndices = new ArrayList<>();
        for (Long group : groupIdList) {
            projectItemNameIndices.add(new ProjectItemNameIndex(id, group, name));
        }
        projectItemNameSearchRepository.saveAll(projectItemNameIndices);
    }

    public void saveProjectItemContentIndex(String username, Long id, String content) {
        List<User> userList = this.userRepository.findByName(username);
        List<Long> userIdList = userList.stream().map(User::getId).collect(Collectors.toList());
        List<Long> groupIdList = userGroupRepository.findAllByUserId(userIdList.get(0))
                .stream().map(UserGroup::getGroup).map(Group::getId).collect(Collectors.toList());

        List<ProjectItemContentIndex> projectItemContentIndices = new ArrayList<>();
        for (Long group : groupIdList) {
            projectItemContentIndices.add(new ProjectItemContentIndex(id, group, content));
        }
        projectItemContentSearchRepository.saveAll(projectItemContentIndices);
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
