package org.example.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
@NoRepositoryBean
public interface BaseRepository<T> extends JpaRepository<T, Long> {
    @Query("SELECT t FROM #{#entityName} t WHERE t.id IN :ids")
    Stream<T> getByIds(@Param("ids") Collection<Long> ids);

}
