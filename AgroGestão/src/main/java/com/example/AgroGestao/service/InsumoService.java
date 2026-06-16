package com.example.AgroGestao.service;

import com.example.AgroGestao.model.Insumos;
import com.example.AgroGestao.repository.InsumosRepository;
import com.example.AgroGestao.exception.ExceptionController.RegraNegocioException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InsumoService {

    // Configuração do Logger para rastrear movimentações no galpão
    private static final Logger logger = LogManager.getLogger(InsumoService.class);

    @Autowired
    private InsumosRepository insumoRepository;

    // Meu método para salvar insumos com validação de regras do armazém
    public Insumos salvarInsumo(Insumos insumo) {

        logger.info("Iniciando o cadastro/atualização do insumo: '{}'", insumo.getNome());

        // Regra 1: Valido se o usuário digitou o nome do produto
        if (insumo.getNome() == null || insumo.getNome().trim().isEmpty()) {
            logger.error("Falha no cadastro: Tentativa de salvar insumo sem nome.");
            throw new RegraNegocioException("O nome do insumo não pode ficar em branco.");
        }

        // Regra 2: Bloqueio a entrada de estoque negativo
        if (insumo.getQuantidade() != null && insumo.getQuantidade() < 0) {
            logger.error("Falha no estoque: O usuário tentou colocar quantidade negativa para '{}'.", insumo.getNome());
            throw new RegraNegocioException("A quantidade em estoque não pode ser menor que zero.");
        }

        // Regra 3: Se o estoque estiver abaixo do limite configurado para a propriedade, emito um
        // alerta no log (Apenas aviso, não trava o sistema). Usa o mesmo limite configurável já
        // aplicado no restante do sistema (Dashboard), em vez de um valor fixo de 10 unidades.
        int limiteEstoqueBaixo = (insumo.getPropriedade() != null && insumo.getPropriedade().getLimiteEstoqueBaixo() != null)
                ? insumo.getPropriedade().getLimiteEstoqueBaixo()
                : 10;

        if (insumo.getQuantidade() != null && insumo.getQuantidade() <= limiteEstoqueBaixo) {
            logger.warn("Atenção: O estoque de '{}' está crítico ({} unidades, limite configurado: {}).",
                    insumo.getNome(), insumo.getQuantidade(), limiteEstoqueBaixo);
        }

        Insumos insumoSalvo = insumoRepository.save(insumo);
        logger.info("Insumo '{}' salvo com sucesso no banco. Saldo atual: {} un.", insumoSalvo.getNome(), insumoSalvo.getQuantidade());

        return insumoSalvo;
    }

    // Meu método de exclusão com rastro de segurança
    public void deletarInsumo(Long id) {
        logger.warn("Solicitação recebida para excluir o insumo de ID: {}", id);

        // Verifico se o insumo existe no banco antes de tentar deletar para evitar erro 500
        if (!insumoRepository.existsById(id)) {
            logger.error("Erro ao deletar: Insumo de ID {} não localizado.", id);
            throw new RegraNegocioException("Insumo não encontrado para exclusão.");
        }

        insumoRepository.deleteById(id);
        logger.info("Insumo ID {} excluído permanentemente do sistema.", id);
    }
}