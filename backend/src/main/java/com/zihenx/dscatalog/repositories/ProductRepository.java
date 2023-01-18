package com.zihenx.dscatalog.repositories;

import com.zihenx.dscatalog.entities.Category;
import com.zihenx.dscatalog.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * A paginação é feita pelo pageable,
     * Consultas com relacionamentos sem INNER JOIN funcionam para outras classes mas só com relacionamento 1/1
     * Consultas como essa, com relacionamento *\* faz-se o uso do INNER JOIN, sempre usando aliases para as tabelas
     * Uso do DISTINCT para evitar repetição causada pelo INNER JOIN
     * Uso da Forma Normal Conjuntiva para aninhar o conjunto de condições por AND e com seus OR dentro e fazer 2 consultas
     * COALESCE -> tratamento de valor nulo em coleção
     **/
    @Query(
        "SELECT DISTINCT obj FROM Product obj " +
            "INNER JOIN obj.categories cats " +
        "WHERE " +
            "(COALESCE(:categories) IS NULL OR cats IN :categories) " +
        "AND " +
            "(UPPER(obj.name) LIKE UPPER(CONCAT('%',:name,'%')) )"
    )
    Page<Product> find(String name, List<Category> categories, Pageable pageable);

}
