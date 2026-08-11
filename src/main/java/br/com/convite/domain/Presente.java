package br.com.convite.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Presente {
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
    
    public void adicionarReserva() {
        if (this.activeReservationsCount >= this.maxQuantity) {
            throw new br.com.convite.exception.RegraDeNegocioException("O presente já atingiu a quantidade máxima de reservas.");
        }
        this.activeReservationsCount++;
        this.esgotado = this.activeReservationsCount >= this.maxQuantity;
    }

    public void removerReserva() {
        if (this.activeReservationsCount > 0) {
            this.activeReservationsCount--;
            this.esgotado = false;
        }
    }
}
