package com.zihenx.dscatalog.services;

import com.zihenx.dscatalog.dto.ProductDTO;
import com.zihenx.dscatalog.entities.Category;
import com.zihenx.dscatalog.entities.Product;
import com.zihenx.dscatalog.repositories.CategoryRepository;
import com.zihenx.dscatalog.repositories.ProductRepository;
import com.zihenx.dscatalog.services.exceptions.DatabaseException;
import com.zihenx.dscatalog.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository repository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAllPaged(Pageable pageable) {
        Page<Product> list = repository.findAll(pageable);

        Page<ProductDTO> listDto = list.map(product -> new ProductDTO(product, product.getCategories()));

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
        copyDtoToEntity(dto, entity);

        entity = repository.save(entity);

        return new ProductDTO(entity, entity.getCategories());
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO dto) {
        try {

            Product entity = repository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity = repository.save(entity);

            return new ProductDTO(entity, entity.getCategories());

        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Id not found " + id);
        }
    }

    // n??o usar @Transactional para pegar a exce????o DataIntegrityViolationException
    // do banco
    public void delete(Long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Id not found " + id);
        } catch (DataIntegrityViolationException ex) { // exce????o para erro na integridade do banco
            throw new DatabaseException("Integrity violation");
        }
    }

    private void copyDtoToEntity(ProductDTO dto, Product entity) {

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setImgUrl(dto.getImgUrl());
        entity.setDate(dto.getDate());

        entity.getCategories().clear();
        dto.getCategories().forEach(catDto -> {
            // ver alternativa par getOne (que instancia sem consultar o bd)
            Category category = categoryRepository.getReferenceById(catDto.getId());
            entity.getCategories().add(category);
        });

    }
}
