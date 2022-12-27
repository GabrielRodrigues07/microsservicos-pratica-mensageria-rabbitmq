package br.com.alurafood.pagamentos.service;

import br.com.alurafood.pagamentos.dto.PagamentoDto;
import br.com.alurafood.pagamentos.http.PedidoClient;
import br.com.alurafood.pagamentos.model.Pagamento;
import br.com.alurafood.pagamentos.model.Pedido;
import br.com.alurafood.pagamentos.model.enums.Status;
import br.com.alurafood.pagamentos.repository.PagamentoRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PagamentoService {

    private PagamentoRepository pagamentoRepository;
    private ModelMapper modelMapper;
    private PedidoClient pedido;


    public Page<PagamentoDto> obterTodos(Pageable paginacao) {
        return pagamentoRepository
                .findByAtivoTrue(paginacao)
                .map(p -> modelMapper.map(p, PagamentoDto.class));
    }

    public PagamentoDto obterPorId(Long id) {
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado. Id: " + id));

        PagamentoDto pagamentoDto = modelMapper.map(pagamento, PagamentoDto.class);
        pagamentoDto.setItens(pedido.buscarPedido(pagamento.getPedidoId()).getItens());
        return pagamentoDto;
    }

    public PagamentoDto criarPagamento(PagamentoDto dto) {
        Pagamento pagamento = modelMapper.map(dto, Pagamento.class);
        pagamento.setStatus(Status.CRIADO);
        pagamento.setAtivo(true);
        pagamentoRepository.save(pagamento);

        return modelMapper.map(pagamento, PagamentoDto.class);
    }

    @Transactional
    public void atualizarPagamento(Long id, PagamentoDto dto) {
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado. Id: " + id));
        dto.setId(id);
        dto.setAtivo(true);
        modelMapper.map(dto, pagamento);
    }

    @Transactional
    public void excluirPagamento(Long id) {
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado. Id: " + id));
        if (!pagamento.isAtivo()) {
            throw new EntityNotFoundException("Pagamento não encontrado. Id: " + id);
        }
        pagamento.setAtivo(false);
    }

    public void confirmarPagamento(Long id){
        Optional<Pagamento> pagamento = pagamentoRepository.findById(id);

        if (!pagamento.isPresent()) {
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO);
        pagamentoRepository.save(pagamento.get());
        pedido.atualizaPagamento(pagamento.get().getPedidoId());
    }

    public void alterarStatus(Long id) {
        Optional<Pagamento> pagamento = pagamentoRepository.findById(id);

        if (!pagamento.isPresent()) {
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO_SEM_INTEGRACAO);
        pagamentoRepository.save(pagamento.get());
    }
}
