package br.com.convite.entrypoint.api.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PresenteResponse {
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
