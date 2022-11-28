package com.zihenx.dscatalog.services;

import com.zihenx.dscatalog.dto.UserDTO;
import com.zihenx.dscatalog.dto.UserInsertDTO;
import com.zihenx.dscatalog.entities.Category;
import com.zihenx.dscatalog.entities.Role;
import com.zihenx.dscatalog.entities.User;
import com.zihenx.dscatalog.repositories.CategoryRepository;
import com.zihenx.dscatalog.repositories.RoleRepository;
import com.zihenx.dscatalog.repositories.UserRepository;
import com.zihenx.dscatalog.services.exceptions.DatabaseException;
import com.zihenx.dscatalog.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public Page<UserDTO> findAllPaged(Pageable pageable) {
        Page<User> list = repository.findAll(pageable);

        Page<UserDTO> listDto = list.map(product -> new UserDTO(product));

        return listDto;
    }

    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        Optional<User> obj = repository.findById(id);
        User entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
        return new UserDTO(entity);
    }

    @Transactional
    public UserDTO insert(UserInsertDTO dto) {
        User entity = new User();
        copyDtoToEntity(dto, entity);
        entity.setPassword( passwordEncoder.encode(dto.getPassword()) ); // mascarando a senha com o encoder
        entity = repository.save(entity);

        return new UserDTO(entity);
    }

    @Transactional
    public UserDTO update(Long id, UserDTO dto) {
        try {

            User entity = repository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity = repository.save(entity);

            return new UserDTO(entity);

        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Id not found " + id);
        }
    }

    // não usar @Transactional para pegar a exceção DataIntegrityViolationException
    // do banco
    public void delete(Long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("Id not found " + id);
        } catch (DataIntegrityViolationException ex) { // exceção para erro na integridade do banco
            throw new DatabaseException("Integrity violation");
        }
    }

    private void copyDtoToEntity(UserDTO dto, User entity) {

       entity.setFirstName(dto.getFirstName());
       entity.setLastName(dto.getLastName());
       entity.setEmail(dto.getEmail());

        entity.getRoles().clear();
        dto.getRoles().forEach(catDto -> {
            // ver alternativa par getOne (que instancia sem consultar o bd)
            Role role = roleRepository.getReferenceById(catDto.getId());
            entity.getRoles().add(role);
        });

    }
}
