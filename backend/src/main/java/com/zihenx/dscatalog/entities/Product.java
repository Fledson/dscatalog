package com.zihenx.dscatalog.entities;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tb_product")
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String name;

    @Column(columnDefinition = "TEXT") // mapeando como TEXT para textos longos
    private String description;
    private double price;
    private String imgUrl;

    @Column(columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private Instant date;

    @ManyToMany //-> muitos pra muitos
    @JoinTable( //-> mapeando no banco de dados - criando uma terceira tabela com as ligações
        name = "tb_product_category", // nome da tabela que fará a associação das entidades
        joinColumns = @JoinColumn(name = "product_id"), // -> nome da coluna que refenrencia a entidade que esta sendo mapeada
        inverseJoinColumns = @JoinColumn(name = "category_id") // nome da coluna que referencia a outra entidade
    )
    Set<Category> categories = new HashSet<>(); // set para conjunto que não permite repetições

    public Product() {

    }

    public Product(Long id, String name, String description, double price, String imgUrl, Instant date) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imgUrl = imgUrl;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return getId().equals(product.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", imgUrl='" + imgUrl + '\'' +
                ", date=" + date +
                ", categories=" + categories +
                '}';
    }
}
