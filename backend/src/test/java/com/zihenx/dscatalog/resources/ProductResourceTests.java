package com.zihenx.dscatalog.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zihenx.dscatalog.dto.ProductDTO;
import com.zihenx.dscatalog.services.ProductService;
import com.zihenx.dscatalog.services.exceptions.DatabaseException;
import com.zihenx.dscatalog.services.exceptions.ResourceNotFoundException;
import com.zihenx.dscatalog.tests.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductResource.class)
public class ProductResourceTests {

    //
    @Autowired
    private MockMvc mockMvc;

    // converte objeto java em json
    @Autowired
    private ObjectMapper objectMapper;

    // instanciando o service mockado
    @MockBean
    private ProductService service;
    private Long existingId, nonExistingId, dependentId;
    private ProductDTO productDTO;
    private PageImpl<ProductDTO> page; // importando o productDTO com um tipo concreto => impl -> implementação

    /**
     * simulando comportamentos
     *
     * @throws Exception
     */
    @BeforeEach
    void setUp() throws Exception {
        // instancias
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;
        productDTO = Factory.createProductDTO();
        page = new PageImpl<>(List.of(productDTO));

        /**
         * quando chamar o findAllPaged com qualquer argumento tem que retornar um pageimpl do tipo productDTO
         * o any ⇾ qualquer argumento (metodoto do mockito)
         */
        when(service.findAllPaged(0L, any())).thenReturn(page);

        /**
         * quando chamar o findById e o id existir deve trazer um productDTO
         */
        when(service.findById(existingId)).thenReturn(productDTO);
        /**
         * quando chamar o findById e o id não existir trazer um ResourceNotFoundException
         */
        when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);

        /**
         * Retorna um productDto quando criar o produto
         */
        when(service.insert(any())).thenReturn(productDTO);

        /**
         * quando chamar o update e o id existir tem que atualizar e retornar o dto
         */
        when(service.update(eq(existingId), any())).thenReturn(productDTO);
        /**
         * quando chamar o update e o id não existir tem que atualizar e retornar uma exceção do tipo ResourceNotFoundException
         */
        when(service.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);

        /**
         * Não faz nada quando chamar o delete e o id existir
         */
        doNothing().when(service).delete(existingId);
        /**
         * Retorna um ResourceNotFoundException quando o id não existir
         */
        doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);
        /**
         * Retorna um DatabaseException quando o id do item depender de outro e ferir a integridade do banco
         */
        doThrow(DatabaseException.class).when(service).delete(dependentId);

    }

    @Test
    public void findAllShouldReturnPage() throws Exception {
        /**
         *  forma direta
         *   mockMvc.perform(get("/products").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
         */

        // Chamada
        ResultActions result =
                mockMvc.perform(get("/products")
                        .accept(MediaType.APPLICATION_JSON));

        // Assertion
        result.andExpect(status().isOk());
    }

    @Test
    public void findByIdShouldReturnProductDtoWhenIdExists() throws Exception {
        ResultActions result =
                mockMvc.perform(get("/products/{id}", existingId)
                        .accept(MediaType.APPLICATION_JSON));

        // verificando o status code
        result.andExpect(status().isOk());
        // verificando se existe os campos do json
        result.andExpect(jsonPath("$.id").exists()); // => o $ é equivalente ao objeto de retorno
        result.andExpect(jsonPath("$.name").exists());
        result.andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        ResultActions result = mockMvc.perform(get("/products/{id}", nonExistingId)
                .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
    }

    @Test
    public void insertShouldReturnCreatedAndProductDtoWhenIdExists() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(post("/products")
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isCreated());
        // verificando se existe os campos do json
        result.andExpect(jsonPath("$.id").exists()); // => o $ é equivalente ao objeto de retorno
        result.andExpect(jsonPath("$.name").exists());
        result.andExpect(jsonPath("$.description").exists());
    }

    @Test
    public void updateShouldReturnProductDtoWhenIdExists() throws Exception {

        String jsonBody = objectMapper.writeValueAsString(productDTO);

        ResultActions result = mockMvc.perform(put("/products/{id}", existingId)
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // verificando o status code
        result.andExpect(status().isOk());
        // verificando se existe os campos do json
        result.andExpect(jsonPath("$.id").exists()); // => o $ é equivalente ao objeto de retorno
        result.andExpect(jsonPath("$.name").exists());
        result.andExpect(jsonPath("$.description").exists());

    }

    @Test
    public void updateSholdReturnNotFouldWhenIdDoesNotExist() throws Exception {

        String jsonBody = objectMapper.writeValueAsString(productDTO); // convertendo objeto java para objeto json(String)

        ResultActions result = mockMvc.perform(put("/products/{id}", nonExistingId)
                .content(jsonBody) // corpo da requisição
                .contentType(MediaType.APPLICATION_JSON) // tipo do conteudo
                .accept(MediaType.APPLICATION_JSON)); // tipo do retorno

        result.andExpect(status().isNotFound());
    }

    @Test
    public void deleteShouldReturnNotContentWhenIdExists() throws Exception {

        ResultActions result = mockMvc.perform(delete("/products/{id}", existingId).accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNoContent());
    }

    @Test
    public void deleteShouldReturnNotFouldWhenIdDoesNotExist() throws Exception {
        ResultActions result = mockMvc.perform(delete("/products/{id}", nonExistingId).accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
    }

    @Test
    public void deleteShoulReturnBadRequestWhenIdIsVinculed() throws Exception {
        ResultActions result = mockMvc.perform(delete("/products/{id}", dependentId).accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isBadRequest());
    }
}
