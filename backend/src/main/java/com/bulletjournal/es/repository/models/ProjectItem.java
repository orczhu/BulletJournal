package com.bulletjournal.es.repository.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "project_item")
public class ProjectItem {

    @Id
    Long id;

    String name;

    String content;

}
