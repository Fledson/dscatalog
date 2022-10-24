package com.zihenx.dscatalog.repositories;

import com.zihenx.dscatalog.entities.Product;
import com.zihenx.dscatalog.tests.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Optional;

@DataJpaTest
public class ProductRepositoryTests {

    @Autowired
    ProductRepository repository;

    long idExist;
    long nonExistingId;
    long countTotalProducts;

    @BeforeEach
    void setUp() throws Exception {
        idExist = 1L;
        nonExistingId = 1000L;
        countTotalProducts = 25L;
    }

    @Test
    public void saveShouldPersistWithAutoIncrementWhenIdIsNull(){
        Product product = Factory.createProduct();
        product.setId(null);

        repository.save(product);

        Assertions.assertNotNull(product.getId());
        Assertions.assertEquals(countTotalProducts + 1, product.getId());
    }

    @Test
    public void deleteShouldDeleteObjectWhenIdExist(){
        repository.deleteById(idExist);

        Optional<Product> result = repository.findById(idExist);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void deleteShouldThrowEmptyResultDataAccessExceptionWhenIdNotExist(){
        Assertions.assertThrows(EmptyResultDataAccessException.class, () -> {
            repository.deleteById(nonExistingId);
        });
    }

    @Test
    public void findByIdShouldReturnNonEmptyOptinalWhenIdExist(){
        Optional<Product> product = repository.findById(idExist);
        Assertions.assertTrue(product.isPresent());
    }

    @Test
    public void findByIdShouldReturnEmptyOptionalWhenIdDoesNotExist(){
        Optional<Product> product = repository.findById(nonExistingId);
        Assertions.assertTrue(product.isEmpty());
    }

}
