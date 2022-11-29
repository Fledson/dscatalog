package com.zihenx.dscatalog.services.validation;

import com.zihenx.dscatalog.dto.UserInsertDTO;
import com.zihenx.dscatalog.dto.UserUpdateDTO;
import com.zihenx.dscatalog.entities.User;
import com.zihenx.dscatalog.repositories.UserRepository;
import com.zihenx.dscatalog.resources.exceptions.FieldMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * criando uma nova validação personalizada, aproveitando o ciclo do bean validation
 * a validação busca no banco se o email existe para algum usuario
 *  - ConstraintValidator = (tipo da anotation, o tipo da classe que vai receber a anotation)
 */
public class UserUpdateValidator implements ConstraintValidator<UserUpdateValid, UserUpdateDTO> {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserRepository repository;

    @Override
    public void initialize(UserUpdateValid ann) {
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
    public boolean isValid(UserUpdateDTO dto, ConstraintValidatorContext context) {

        // pegando as variaveis passadas via url
        var uriVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        // pegando o id dentro das variaveis
        long userId = Long.parseLong(uriVars.get("id"));


        List<FieldMessage> list = new ArrayList<>();

        // consultando no banco se existe um usuario com este email
        User user = repository.findByEmail(dto.getEmail());

        // verificando se user existe e se ele é diferente do usuario passado
        if (user != null && userId != user.getId()) {
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