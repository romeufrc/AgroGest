package com.example.AgroGestao.service;

import com.example.AgroGestao.model.Gasto;
import com.example.AgroGestao.repository.GastoRepository;
import com.example.AgroGestao.exception.ExceptionController.RegraNegocioException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GastoService {

    // Configuração do Logger para rastreamento financeiro
    private static final Logger logger = LogManager.getLogger(GastoService.class);

    @Autowired
    private GastoRepository gastoRepository;

    //Método para validar a entrada de despesas na fazenda
    public Gasto registrarGasto(Gasto gasto) {

        logger.info("Processando registro financeiro. Descrição: '{}'", gasto.getDescricao());

        //O valor do gasto não pode ser zerado ou negativo
        if (gasto.getValor() == null || gasto.getValor() <= 0) {
            logger.error("Falha financeira: Tentativa de registrar gasto com valor zerado ou negativo.");
            throw new RegraNegocioException("O valor do gasto financeiro deve ser maior que zero.");
        }

        Gasto gastoSalvo = gastoRepository.save(gasto);
        logger.info("Despesa '{}' no valor de R$ {} registrada com sucesso.", gastoSalvo.getDescricao(), gastoSalvo.getValor());

        return gastoSalvo;
    }
}