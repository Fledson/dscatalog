package com.zihenx.dscatalog.services;

import com.zihenx.dscatalog.dto.ProductDTO;
import com.zihenx.dscatalog.repositories.ProductRepository;
import com.zihenx.dscatalog.services.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Teste de Integração
 * teste que vai até a camada do banco de dados
 */
@SpringBootTest // anotação de teste de integração
@Transactional // ao fim de cada teste é feito um rollback no banco
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductService service;
    @Autowired
    private ProductRepository repository;

    private Long existingId, nonExistingId, countTotalProducts;


    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        countTotalProducts = 25L;
    }

    @Test
    public void deleteShouldDeleteResourceWhenIdExists() {
        service.delete(existingId);

        Assertions.assertEquals(countTotalProducts - 1, repository.count());
    }

    @Test
    public void deleteShouldReturnNotFoundWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(nonExistingId);
        });
    }

    @Test
    public void findAllPagedShouldReturnPageWhenPage0Size10() {
        // instanciando manualmente um page request
        PageRequest pageRequest = PageRequest.of(0, 10);

        // consultando e armazenando a listagem de produtos
        Page<ProductDTO> page = service.findAllPaged(pageRequest);

        Assertions.assertFalse(page.isEmpty()); // testando se a lista é vazia
        Assertions.assertEquals(0, page.getNumber()); // verificando se o numero da pagina é 0
        Assertions.assertEquals(countTotalProducts, page.getTotalElements()); // verificando se o valor total de itens esperado é o mesmo valor de itens retornado
    }

    @Test
    public void findAllPagedShouldReturnEmptyPageWhenPageDoesNotExist() {
        PageRequest pageRequest = PageRequest.of(50, 10);

        Page<ProductDTO> page = service.findAllPaged(pageRequest);

        Assertions.assertTrue(page.isEmpty());
    }

    @Test
    public void findAllPagedShouldReturnSortedPageWhenSortedByName() {
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name"));

        Page<ProductDTO> page = service.findAllPaged(pageRequest);

        Assertions.assertFalse(page.isEmpty());
        Assertions.assertEquals("Macbook Pro", page.getContent().get(0).getName());
        Assertions.assertEquals("PC Gamer", page.getContent().get(1).getName());
        Assertions.assertEquals("PC Gamer Alfa", page.getContent().get(2).getName());
    }

}
