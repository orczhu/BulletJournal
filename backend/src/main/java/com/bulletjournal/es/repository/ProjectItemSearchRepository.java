package com.bulletjournal.es.repository;

import com.bulletjournal.es.repository.models.ProjectItem;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProjectItemSearchRepository extends ElasticsearchRepository<ProjectItem, Long> {

    List<ProjectItem> findByName(String name);

    Iterable<ProjectItem> search(QueryBuilder queryBuilder);
}
