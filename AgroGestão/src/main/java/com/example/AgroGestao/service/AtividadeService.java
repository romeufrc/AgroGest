package com.example.AgroGestao.service;

import com.example.AgroGestao.model.Atividade;
import com.example.AgroGestao.model.Insumos;
import com.example.AgroGestao.repository.AtividadeRepository;
import com.example.AgroGestao.repository.InsumosRepository;
import com.example.AgroGestao.exception.ExceptionController.RegraNegocioException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // Indica que esta classe é responsável pelas regras de negócio
public class AtividadeService {

    // Logger utilizado para registrar informações e erros durante a execução
    private static final Logger logger = LogManager.getLogger(AtividadeService.class);

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private InsumosRepository insumoRepository;

    @Transactional // Garante que todas as operações sejam executadas em uma única transação
    public Atividade salvarComBaixaEstoque(Atividade atividade, Long insumoId, Integer quantidadeUsada) {

        // Inicia o registro da operação no log
        logger.info("Iniciando processamento da atividade: '{}'", atividade.getNome());

        // Verifica se a descrição da atividade foi informada
        if (atividade.getDescricao() == null || atividade.getDescricao().isEmpty()) {
            throw new RegraNegocioException("A descrição ou tipo da atividade precisa ser informada.");
        }

        // Só realiza a baixa no estoque caso o insumo e a quantidade tenham sido informados
        if (insumoId != null && quantidadeUsada != null) {

            // Verifica se a quantidade utilizada é válida
            if (quantidadeUsada <= 0) {
                throw new RegraNegocioException("A quantidade utilizada deve ser maior que zero.");
            }

            // Busca o insumo no banco pelo ID informado
            Insumos insumo = insumoRepository.findById(insumoId)
                    .orElseThrow(() -> new RegraNegocioException("Insumo selecionado não encontrado."));

            // Confere se existe quantidade suficiente em estoque
            if (insumo.getQuantidade() != null && insumo.getQuantidade() >= quantidadeUsada) {

                // Realiza a baixa do estoque
                insumo.setQuantidade(insumo.getQuantidade() - quantidadeUsada);

                // Salva a nova quantidade no banco
                insumoRepository.save(insumo);

                logger.info("Baixa dinâmica confirmada: {} unidades de '{}'", quantidadeUsada, insumo.getNome());

            } else {
                // Caso não haja estoque suficiente, gera uma exceção
                logger.error("Saldo insuficiente. Requerido: {}, Disponível: {}", quantidadeUsada, insumo.getQuantidade());

                throw new RegraNegocioException(
                        "Estoque insuficiente! O saldo atual é de apenas "
                                + insumo.getQuantidade() + " unidades."
                );
            }
        }

        // Salva a atividade no banco de dados
        Atividade salva = atividadeRepository.save(atividade);

        // Registra no log que a atividade foi salva com sucesso
        logger.info("Atividade '{}' salva com sucesso.", salva.getNome());

        // Retorna a atividade salva
        return salva;
    }
}