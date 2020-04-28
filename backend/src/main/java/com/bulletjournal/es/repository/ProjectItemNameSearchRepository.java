package com.bulletjournal.es.repository;

import com.bulletjournal.es.repository.models.ProjectItemNameIndex;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProjectItemNameSearchRepository extends ElasticsearchRepository<ProjectItemNameIndex, Long> {

    List<ProjectItemNameIndex> findByName(String name);

    Iterable<ProjectItemNameIndex> search(QueryBuilder queryBuilder);
}
