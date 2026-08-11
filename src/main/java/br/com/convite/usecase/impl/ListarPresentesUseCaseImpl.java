package br.com.convite.usecase.impl;

import br.com.convite.domain.Presente;
import br.com.convite.gateway.PresenteGateway;
import br.com.convite.usecase.ListarPresentesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarPresentesUseCaseImpl implements ListarPresentesUseCase {

    private final PresenteGateway presenteGateway;

    @Override
    public List<Presente> executar() {
        return presenteGateway.listarTodos();
    }
}
