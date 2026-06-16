package com.example.AgroGestao.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice // Classe global responsável por capturar e tratar exceções da aplicação
public class ExceptionController {

    // Logger utilizado para registrar avisos e erros no sistema
    private static final Logger logger = LogManager.getLogger(ExceptionController.class);

    // Exceção personalizada para regras de negócio da aplicação
    public static class RegraNegocioException extends RuntimeException {

        // Construtor que recebe a mensagem de erro
        public RegraNegocioException(String mensagem) {
            super(mensagem);
        }
    }

    // Método para capturar erros relacionados às regras de negócio
    @ExceptionHandler(RegraNegocioException.class)
    public String tratarErroDeNegocio(RegraNegocioException ex, Model model) {

        // Registra o aviso no log
        logger.warn("Aviso de regra de negócio interceptado: {}", ex.getMessage());

        // Envia a mensagem de erro para a tela
        model.addAttribute("erro", ex.getMessage());

        // Retorna para a página de atividades
        return "atividades";
    }

    // Captura recursos não encontrados, como favicon.ico
    @ExceptionHandler(NoResourceFoundException.class)
    public void tratarRecursoAusente(NoResourceFoundException ex) {

        // Registra apenas uma mensagem de debug sem interromper o sistema
        logger.debug("Recurso estático não localizado pelo navegador: {}", ex.getResourcePath());

        // Não retorna nada e não gera erro para o usuário
    }

    // Captura qualquer erro inesperado da aplicação
    @ExceptionHandler(Exception.class)
    public String tratarErroInesperado(Exception ex, Model model) {

        // Registra o erro completo no log para análise
        logger.error("💥 Erro crítico pego no ExceptionController: ", ex);

        // Exibe uma mensagem amigável para o usuário
        model.addAttribute("erro", "Ocorreu um erro interno no servidor. Tente reiniciar a sessão.");

        // Redireciona para a tela de login
        return "login";
    }
}