package com.bulletjournal.es.repository.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.GeneratedValue;
import javax.persistence.SequenceGenerator;

@Document(indexName = "project_name_content_index")
public class ProjectItemContentIndex {

    @Id
    @GeneratedValue(generator = "project_item_content_generator")
    @SequenceGenerator(
            name = "project_item_content_generator",
            sequenceName = "project_item_content_sequence",
            initialValue = 100
    )
    Long id;

    Long project;

    Long group;

    String content;

    public ProjectItemContentIndex(Long project, Long group, String content) {
        this.project = project;
        this.group = group;
        this.content = content;
    }

    public Long getGroup() {
        return group;
    }

    public void setGroup(Long group) {
        this.group = group;
    }

    public Long getProject() {
        return project;
    }

    public void setProject(Long project) {
        this.project = project;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
