package com.zihenx.dscatalog.services;

import com.zihenx.dscatalog.dto.UserDTO;
import com.zihenx.dscatalog.dto.UserInsertDTO;
import com.zihenx.dscatalog.dto.UserUpdateDTO;
import com.zihenx.dscatalog.entities.Role;
import com.zihenx.dscatalog.entities.User;
import com.zihenx.dscatalog.repositories.RoleRepository;
import com.zihenx.dscatalog.repositories.UserRepository;
import com.zihenx.dscatalog.services.exceptions.DatabaseException;
import com.zihenx.dscatalog.services.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    // classe para colocar no console mensagens padronizadas (worm, error, etc... )
    private static Logger logger = LoggerFactory.getLogger(UserService.class);

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
        entity.setPassword(passwordEncoder.encode(dto.getPassword())); // mascarando a senha com o encoder
        entity = repository.save(entity);

        return new UserDTO(entity);
    }

    @Transactional
    public UserDTO update(Long id, UserUpdateDTO dto) {
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

    /**
     * pesquisa um usuario no banco por email
     * 
     * @param username -> username definido na implementação do UserDetails lá na
     *                 criação do User
     * @return user -> um usuario do tipo User já que esse implementa UserDetails
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByEmail(username);

        if (user == null) {
            logger.error("User not found: " + username);
            throw new UsernameNotFoundException("Email not found");
        }

        logger.info("User found: " + username);
        return user;
    }
}
