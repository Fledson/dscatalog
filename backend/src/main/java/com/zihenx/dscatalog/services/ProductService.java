package com.zihenx.dscatalog.services;

import com.zihenx.dscatalog.dto.ProductDTO;
import com.zihenx.dscatalog.entities.Product;
import com.zihenx.dscatalog.repositories.ProductRepository;
import com.zihenx.dscatalog.services.exceptions.DatabaseException;
import com.zihenx.dscatalog.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(PageRequest pageRequest) {
        Page<Product> list = repository.findAll(pageRequest);

        Page<ProductDTO> listDto = list.map( category -> new ProductDTO(category) );

        return listDto;
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Optional<Product> obj = repository.findById(id);
        Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
        return new ProductDTO(entity, entity.getCategories());
    }

    @Transactional
    public ProductDTO insert(ProductDTO dto) {
        Product entity = new Product();
//        entity.setName(dto.getName());

        entity = repository.save(entity);

        return new ProductDTO(entity);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        try {

            Product entity = repository.getReferenceById(id);
//            entity.setName(dto.getName());
            entity = repository.save(entity);

            return new ProductDTO(entity);

        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Id not found " + id);
        }
    }

    // não usar @Transactional para pegar a exceção DataIntegrityViolationException do banco
    public void delete(Long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Id not found " + id);
        } catch (DataIntegrityViolationException ex){ // exceção para erro na integridade do banco
            throw new DatabaseException("Integrity violation");
        }
    }
}
