package com.example.AgroGestao.service;

import com.example.AgroGestao.model.Safra;
import com.example.AgroGestao.repository.SafraRepository;
import com.example.AgroGestao.exception.ExceptionController.RegraNegocioException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SafraService {

    //Configuração do Logger para rastrear o planejamento agrícola
    private static final Logger logger = LogManager.getLogger(SafraService.class);

    @Autowired
    private SafraRepository safraRepository;

    //Método para validar e registrar o planejamento de uma nova safra
    public Safra salvarSafra(Safra safra) {

        logger.info("Iniciando o registro/atualização da safra: '{}' (Cultura: {})", safra.getNome(), safra.getCultura());

        //Regra 1: Nome e Cultura são obrigatórios para não sujar o banco
        if (safra.getNome() == null || safra.getNome().trim().isEmpty() ||
                safra.getCultura() == null || safra.getCultura().trim().isEmpty()) {
            logger.error("Falha no registro: Tentativa de salvar safra sem nome ou sem especificar a cultura.");
            throw new RegraNegocioException("O nome da safra e o tipo de cultura não podem ficar em branco.");
        }

        //Regra 2: A produção estimada de sacas não pode ser negativa
        if (safra.getProducaoEstimadaSacas() != null && safra.getProducaoEstimadaSacas() < 0) {
            logger.error("Falha de regra de negócio: Produção estimada negativa informada para a safra '{}'.", safra.getNome());
            throw new RegraNegocioException("A estimativa de produção de sacas não pode ser um valor negativo.");
        }

        //Regra 3: O preço esperado da saca também não pode ser negativo
        if (safra.getPrecoSacaEsperado() != null && safra.getPrecoSacaEsperado() < 0) {
            logger.error("Falha financeira: Preço da saca negativo informado para a safra '{}'.", safra.getNome());
            throw new RegraNegocioException("O preço esperado por saca não pode ser menor que zero.");
        }

        Safra safraSalva = safraRepository.save(safra);
        logger.info("Safra '{}' salva com sucesso. Status atual: {}", safraSalva.getNome(), safraSalva.getStatus());

        return safraSalva;
    }

    //Método de exclusão
    public void deletarSafra(Long id) {
        logger.warn("Solicitação recebida para excluir a safra de ID: {}", id);

        if (!safraRepository.existsById(id)) {
            logger.error("Erro ao deletar: Safra de ID {} não localizada no banco.", id);
            throw new RegraNegocioException("Safra não encontrada para exclusão.");
        }

        safraRepository.deleteById(id);
        logger.info("Safra ID {} excluída permanentemente.", id);
    }
}