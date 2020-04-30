package com.bulletjournal.es.repository.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.GeneratedValue;
import javax.persistence.SequenceGenerator;

@Document(indexName = "project_name_item_index")
public class ProjectItemNameIndex {

    @Id
    @GeneratedValue(generator = "project_item_name_generator")
    @SequenceGenerator(
            name = "project_item_name_generator",
            sequenceName = "project_item_name_sequence",
            initialValue = 100
    )
    Long id;

    Long project;

    Long group;

    String name;

    public ProjectItemNameIndex(Long project, Long group, String name) {
        this.project = project;
        this.group = group;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
