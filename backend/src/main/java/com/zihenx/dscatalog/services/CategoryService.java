package com.zihenx.dscatalog.services;

import com.zihenx.dscatalog.dto.CategoryDTO;
import com.zihenx.dscatalog.entities.Category;
import com.zihenx.dscatalog.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository repository;

    @Transactional
    public List<CategoryDTO> findAll() {
        List<Category> list = repository.findAll();

        List<CategoryDTO> listDto = list.stream().map( category -> new CategoryDTO(category) ).collect(Collectors.toList());

        return listDto;
    }

}
