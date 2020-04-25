package com.bulletjournal.repository;

import com.bulletjournal.repository.models.ContentModel;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchRepository<T extends ContentModel> extends ElasticsearchCrudRepository<T, Long> {
    Iterable<T> search(QueryBuilder queryBuilder);
}
