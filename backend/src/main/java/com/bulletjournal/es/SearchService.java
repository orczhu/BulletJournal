package com.bulletjournal.es;

import com.bulletjournal.controller.models.Content;
import com.bulletjournal.controller.models.ProjectItem;
import com.bulletjournal.controller.models.PublicProjectItem;
import com.bulletjournal.es.repository.ProjectItemSearchDaoJpa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    public static final String PROJECT_ITEM = "project_item";

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private ProjectItemSearchDaoJpa projectItemSearchDaoJpa;

    public void savePublicProjectItemToEs(String username, PublicProjectItem projectItem) {
        Long id = projectItem.getProjectItem().getId();
        String name = projectItem.getProjectItem().getName();
        projectItemSearchDaoJpa.saveProjectItemNameIndex(username, id, name);

        List<Content> contents = projectItem.getContents();
        contents.forEach(c -> projectItemSearchDaoJpa.saveProjectItemContentIndex(username, id, c.getText()));
    }

    public void saveProjectItemToEs(String username, ProjectItem projectItem) {
        Long id = projectItem.getId();
        String name = projectItem.getName();

        projectItemSearchDaoJpa.saveProjectItemNameIndex(username, id, name);
    }
}
