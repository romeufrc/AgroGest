package com.example.AgroGestao.service;

import com.example.AgroGestao.model.Atividade;
import com.example.AgroGestao.model.Insumos;
import com.example.AgroGestao.repository.AtividadeRepository;
import com.example.AgroGestao.repository.InsumosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AtividadeService {

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private InsumosRepository insumoRepository;

    public Atividade salvarEAtualizarEstoque(Atividade atividade) {
        // 1. Salva a atividade normalmente no banco
        Atividade atividadeSalva = atividadeRepository.save(atividade);

        // 2. Verifica se a atividade é um Plantio ou Adubação (onde gastamos insumos)
        String tipo = atividade.getTipo().toLowerCase();
        if (tipo.contains("plantio") || tipo.contains("adubacao") || tipo.contains("adubação")) {

            // Busca os insumos cadastrados no banco
            List<Insumos> estoque = insumoRepository.findAll();

            for (Insumos insumo : estoque) {
                // Regra simples: se a descrição da atividade contiver o nome do insumo (ex: "Plantio de Soja" e insumo "Soja")
                if (atividade.getDescricao().toLowerCase().contains(insumo.getNome().toLowerCase()) ||
                        atividade.getNome().toLowerCase().contains(insumo.getNome().toLowerCase())) {

                    // Definimos uma quantidade padrão a ser abatida (ex: 50 unidades)
                    // Em um sistema real, você poderia receber a "quantidadeUsada" dentro do JSON da Atividade
                    double quantidadeUsada = 50.0;

                    if (insumo.getQuantidade() >= quantidadeUsada) {
                        insumo.setQuantidade(insumo.getQuantidade() - quantidadeUsada);
                        insumoRepository.save(insumo); // Atualiza o estoque automaticamente!
                        System.out.println("Estoque atualizado! Foram abatidas " + quantidadeUsada + " unidades de " + insumo.getNome());
                    } else {
                        System.out.println("Aviso: Estoque de " + insumo.getNome() + " é insuficiente para abater automaticamente.");
                    }
                    break;
                }
            }
        }

        return atividadeSalva;
    }
}