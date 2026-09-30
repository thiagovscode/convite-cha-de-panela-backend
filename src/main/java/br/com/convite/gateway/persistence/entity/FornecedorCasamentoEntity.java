package br.com.convite.gateway.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fornecedores")
public class FornecedorCasamentoEntity {
    @Id
    private String id;

    private String nome; // Responsável principal
    private String responsavel; // Alias para compatibilidade
    private String papel; // "Fornecedor"
    private String categoria; // Música & Som, Foto & Vídeo, Buffet & Gastronomia, Decoração, Cerimonial, etc.
    private String servico; // Orquestra, Fotografia, Som e DJ, Cerimonial, Buffet, etc.
    private String empresa; // Ex: Harmonia Musical
    private String telefone; // Telefone / WhatsApp para contato de emergência
    private String horarioPrevisto; // Ex: "14:00"
    private String instrucaoChegada; // Ex: "Chegada antecipada para afinação e montagem dos instrumentos"
    private Boolean chegadaAntecipada; // true para destacar que precisa entrar antes
    private java.util.List<MembroEquipeFornecedorEntity> equipe;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
