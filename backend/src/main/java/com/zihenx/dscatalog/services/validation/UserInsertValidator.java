package com.zihenx.dscatalog.services.validation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.zihenx.dscatalog.dto.UserInsertDTO;
import com.zihenx.dscatalog.entities.User;
import com.zihenx.dscatalog.repositories.UserRepository;
import com.zihenx.dscatalog.resources.exceptions.FieldMessage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * criando uma nova validação personalizada, aproveitando o ciclo do bean validation
 * a validação busca no banco se o email existe para algum usuario
 *  - ConstraintValidator = (tipo da anotation, o tipo da classe que vai receber a anotation)
 */
public class UserInsertValidator implements ConstraintValidator<UserInsertValid, UserInsertDTO> {

    @Autowired
    private UserRepository repository;

    @Override
    public void initialize(UserInsertValid ann) {
    }

    /**
     * testa se é valido ou não
     * @param dto objeto que será validado
     * @param context context in which the constraint is evaluated
     *  verifica no banco se existe um usuario com este email
     *  se retornar algo ele adiciona um novo fildmessage a lista, caso ela esteja vazia no fim passa
     * @return boolean se true é valido
     */
    @Override
    public boolean isValid(UserInsertDTO dto, ConstraintValidatorContext context) {

        List<FieldMessage> list = new ArrayList<>();

        User user = repository.findByEmail(dto.getEmail());
        if (user != null) {
            list.add(new FieldMessage("email", "Email já existe"));
        }

        /**
         * Alimentando o campo de erro do beanValidation
         */
        for (FieldMessage e : list) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName())
                    .addConstraintViolation();
        }
        return list.isEmpty();
    }
}