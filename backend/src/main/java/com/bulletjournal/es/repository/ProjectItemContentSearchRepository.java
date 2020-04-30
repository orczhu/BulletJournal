package com.bulletjournal.es.repository;

import com.bulletjournal.es.repository.models.ProjectItemContentIndex;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProjectItemContentSearchRepository extends ElasticsearchRepository<ProjectItemContentIndex, Long> {

    Iterable<ProjectItemContentIndex> search(QueryBuilder queryBuilder);
}
