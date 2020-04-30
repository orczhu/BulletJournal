package com.bulletjournal.es;

import com.bulletjournal.config.SpringESConfig;
import com.bulletjournal.controller.models.Content;
import com.bulletjournal.controller.models.PublicProjectItem;
import com.bulletjournal.es.repository.ProjectItemSearchDaoJpa;
import com.bulletjournal.repository.GroupDaoJpa;
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
    private SpringESConfig springESConfig;

    @Autowired
    private GroupDaoJpa groupDaoJpa;

    @Autowired
    private ProjectItemSearchDaoJpa projectItemSearchDaoJpa;

    public void saveToES(PublicProjectItem projectItem) {
        Long id = projectItem.getProjectItem().getId();
        String name = projectItem.getProjectItem().getName();
        projectItemSearchDaoJpa.saveProjectItemNameIndex(id, name);

        List<Content> contents = projectItem.getContents();
        contents.forEach(c -> projectItemSearchDaoJpa.saveProjectItemContentIndex(id, c.getText()));
    }
}
