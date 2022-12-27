package br.com.alurafood.pagamentos.model;

import lombok.Getter;

import java.util.List;

@Getter
public class Pedido {

    private List<ItemDoPedido> itens;
}
