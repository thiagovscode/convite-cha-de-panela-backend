package br.com.convite.gateway.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "presentes")
public class PresenteEntity {
    @Id
    private Long id;
    
    private String nome;
    private String imagem;
    private String categoria;
    private String cor;
    private String descricao;
    private String linkCompra;
    private Integer maxQuantity;
    private Integer activeReservationsCount;
    private Boolean esgotado;
}
