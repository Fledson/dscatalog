package com.zihenx.dscatalog.services;

import com.zihenx.dscatalog.dto.ProductDTO;
import com.zihenx.dscatalog.entities.Category;
import com.zihenx.dscatalog.entities.Product;
import com.zihenx.dscatalog.repositories.CategoryRepository;
import com.zihenx.dscatalog.repositories.ProductRepository;
import com.zihenx.dscatalog.services.exceptions.ResourceNotFoundException;
import com.zihenx.dscatalog.tests.Factory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;
    @Mock
    private CategoryRepository categoryRepository;

    private long existingID, existingCategoryId;
    private long nonExistingID;
    private long dependentId;
    private Product product;
    private Category category;
    private ProductDTO productDTO;
    private PageImpl<Product> page;

    /**
     * simulando comportamentos
     * @throws Exception
     */
    @BeforeEach
    void setUp() throws Exception {
        existingID = 1L;
        existingCategoryId = 1L;
        nonExistingID = 2L;
        dependentId = 3L;
        product = Factory.createProduct();
        category = Factory.createCategory();
        productDTO = Factory.createProductDTO();
        page = new PageImpl<>(List.of(product));
        /**
         * ** Configurando comportamento padrão do repository **
         */

        /**
         * Simulação com metodo que retorna algo
         */
        //retornar uma página quando o findAll for chamado e receber qualquer argumento do tipo pageable
        Mockito.when(repository.findAll((Pageable) ArgumentMatchers.any())).thenReturn(page);
        //retornar um produto quando o save for chamado e receber qualquer argumento

        Mockito.when(repository.save(ArgumentMatchers.any())).thenReturn(product);
        //retornar um optional de um produto quando o findById for chamado e receber id valido

        Mockito.when(repository.findById(existingID)).thenReturn(Optional.of(product));
        //retornar um optinal vazio quando o findById for chamado e receber um id inexistente
        Mockito.when(repository.findById(nonExistingID)).thenReturn(Optional.empty());
        //retornar um produto quando o getReferenceById for chamado e receber um id valido

        Mockito.when(repository.getReferenceById(existingID)).thenReturn(product);
        //retornar uma exceção quando o getReferenceById for chamado e receber um id invalido
        Mockito.when(repository.getReferenceById(nonExistingID)).thenThrow(EntityNotFoundException.class);

        //retonar uma categoria quando o getReferenceById for chamado e receber um id de categoria valido
        Mockito.when(categoryRepository.getReferenceById(existingCategoryId)).thenReturn(category);
        //retornar uma exceção quando o getReferenceById for chamado e receber um id invalido
        Mockito.when(categoryRepository.getReferenceById(nonExistingID)).thenThrow(EntityNotFoundException.class);

        /**
         * Simulação com metodo void (delete by id)
         */
        // não fazer nada quando o id existir
        Mockito.doNothing().when(repository).deleteById(existingID);
        // lancar exceção (EmptyResultDataAccessException) quando o id não existir
        Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingID);
        // lancar exceção (DataIntegrityViolationException) quando o id for dependente de outro elemento
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
    }

    @Test
    public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExisting(){
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
           service.update(nonExistingID, productDTO);
        });

        Mockito.verify(repository).getReferenceById(nonExistingID);
    }

    @Test
    public void updateShouldReturnProductDTOWhenIdExists(){
      ProductDTO product = service.update(existingID, productDTO);

      Assertions.assertNotNull(product);
      Mockito.verify(repository).getReferenceById(existingID);
    }

    @Test
    public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExisting() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(nonExistingID);
        });
        Mockito.verify(repository, Mockito.times(1)).findById(nonExistingID);
    }

    @Test
    public void findByIdShouldReturnProductDTOWhenIdExists(){
        ProductDTO product = service.findById(existingID);

        Assertions.assertNotNull(product);
        Mockito.verify(repository, Mockito.times(1)).findById(existingID);
    }

    @Test
    public void findAllPagedShouldReturnPage(){
        Pageable pageable = PageRequest.of(0, 10);

        Page<ProductDTO> result = service.findAllPaged(pageable);

        Assertions.assertNotNull(result);
        Mockito.verify(repository).findAll(pageable);
    }

    @Test
    public void deleteShoultThrowDatabaseExceptionWhenDependentId(){
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            repository.deleteById(dependentId);
        });

        Mockito.verify(repository, Mockito.times(1)).deleteById(dependentId);
    }

    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExisting(){
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(nonExistingID);
        });

        Mockito.verify(repository, Mockito.times(1)).deleteById(nonExistingID);
    }

    @Test
    public void deleteShouldDoNotthingWhenIdExist(){
        Assertions.assertDoesNotThrow(() -> {
            service.delete(existingID);
        });

        // verificando se o deleteById do repository foi chamado,
        // Mockito.times é opcional e verifica a quantidade de vezes que o metodo foi chamado
        Mockito.verify(repository, Mockito.times(1)).deleteById(existingID);
    }

}
