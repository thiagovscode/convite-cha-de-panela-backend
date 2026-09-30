package br.com.convite.entrypoint.api;

import br.com.convite.domain.Fornecedor;
import br.com.convite.domain.MembroEquipeFornecedor;
import br.com.convite.usecase.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/fornecedores")
@RequiredArgsConstructor
public class AdminFornecedorController {

    private final ListarFornecedoresUseCase listarFornecedoresUseCase;
    private final CadastrarFornecedorUseCase cadastrarFornecedorUseCase;
    private final AtualizarFornecedorUseCase atualizarFornecedorUseCase;
    private final ExcluirFornecedorUseCase excluirFornecedorUseCase;
    private final AdicionarMembroFornecedorUseCase adicionarMembroFornecedorUseCase;
    private final RemoverMembroFornecedorUseCase removerMembroFornecedorUseCase;

    @GetMapping
    public ResponseEntity<List<Fornecedor>> listar() {
        return ResponseEntity.ok(listarFornecedoresUseCase.executar());
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Fornecedor req) {
        Fornecedor salvo = cadastrarFornecedorUseCase.executar(req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Fornecedor cadastrado com sucesso!",
                "fornecedor", salvo
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable String id, @RequestBody Fornecedor req) {
        Fornecedor salvo = atualizarFornecedorUseCase.executar(id, req);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Fornecedor atualizado com sucesso!",
                "fornecedor", salvo
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable String id) {
        excluirFornecedorUseCase.executar(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Fornecedor removido com sucesso."));
    }

    @PostMapping("/{id}/membros")
    public ResponseEntity<?> adicionarMembro(@PathVariable String id, @RequestBody MembroEquipeFornecedor novoMembro) {
        Fornecedor salvo = adicionarMembroFornecedorUseCase.executar(id, novoMembro);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Membro adicionado à equipe.",
                "fornecedor", salvo
        ));
    }

    @DeleteMapping("/{fornecedorId}/membros/{membroId}")
    public ResponseEntity<?> removerMembro(@PathVariable String fornecedorId, @PathVariable String membroId) {
        Fornecedor salvo = removerMembroFornecedorUseCase.executar(fornecedorId, membroId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Membro removido da equipe.",
                "fornecedor", salvo
        ));
    }
}
