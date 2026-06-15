package com.example.AgroGestao.service;

import com.example.AgroGestao.model.Atividade;
import com.example.AgroGestao.model.Insumos;
import com.example.AgroGestao.repository.AtividadeRepository;
import com.example.AgroGestao.repository.InsumosRepository;
import com.example.AgroGestao.exception.ExceptionController.RegraNegocioException;import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AtividadeService {

    // Configurando o Log4j2 para substituir o System.out.println
    private static final Logger logger = LogManager.getLogger(AtividadeService.class);

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private InsumosRepository insumoRepository;

    // Método que salva a atividade e atualiza o estoque sozinho
    public Atividade salvarEAtualizarEstoque(Atividade atividade) {

        // Log de informação: rastreia o início da operação
        logger.info("Salvando atividade: '{}'", atividade.getNome());

        // Se o tipo vier vazio, eu lanço erro e paro aqui
        if (atividade.getTipo() == null) {
            logger.warn("Erro: Tipo de atividade não foi informado.");
            throw new RegraNegocioException("O tipo da atividade precisa ser informado.");
        }

        String tipo = atividade.getTipo().toLowerCase();

        // Regra: só mexe no estoque se for Plantio ou Adubação
        if (tipo.contains("plantio") || tipo.contains("adubacao") || tipo.contains("adubação")) {

            // Log de depuração: avisa que começou a buscar os produtos no galpão
            logger.debug("Manejo de plantio/adubação detectado. Verificando estoque...");

            List<Insumos> estoque = insumoRepository.findAll();
            String descricao = (atividade.getDescricao() != null) ? atividade.getDescricao().toLowerCase() : "";
            String nomeAtividade = (atividade.getNome() != null) ? atividade.getNome().toLowerCase() : "";

            for (Insumos insumo : estoque) {

                // Se o nome do insumo estiver no texto da atividade, eu vinculo os dois
                if (descricao.contains(insumo.getNome().toLowerCase()) || nomeAtividade.contains(insumo.getNome().toLowerCase())) {

                    int quantidadeUsada = 50; // Padrão de gasto do manejo

                    logger.debug("Insumo encontrado: {}. Saldo atual: {} un.", insumo.getNome(), insumo.getQuantidade());

                    // Se tiver estoque suficiente, dá a baixa
                    if (insumo.getQuantidade() != null && insumo.getQuantidade() >= quantidadeUsada) {

                        insumo.setQuantidade(insumo.getQuantidade() - quantidadeUsada);
                        insumoRepository.save(insumo); // UPDATE no banco

                        // Log de sucesso: avisa quanta quantidade foi retirada do estoque
                        logger.info("Baixa automática: 50 un. retiradas de '{}'.", insumo.getNome());
                    } else {

                        // Se NÃO tiver estoque, grava log de erro e dispara a nossa exceção na tela
                        logger.error("Erro: Saldo insuficiente de '{}'. Operação cancelada.", insumo.getNome());
                        throw new RegraNegocioException("Estoque insuficiente! Faltam produtos para esse manejo.");
                    }

                    break; // Sai do laço porque já tratou o produto certo
                }
            }
        }

        // Salva a atividade de campo no banco de dados e confirma o sucesso
        Atividade atividadeSalva = atividadeRepository.save(atividade);
        logger.info("Atividade '{}' salva com sucesso.", atividadeSalva.getNome());

        return atividadeSalva;
    }
}