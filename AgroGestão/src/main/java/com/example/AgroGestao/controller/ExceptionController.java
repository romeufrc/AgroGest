package com.example.AgroGestao.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class ExceptionController {

    private static final Logger logger = LogManager.getLogger(ExceptionController.class);

    public static class RegraNegocioException extends RuntimeException {
        public RegraNegocioException(String mensagem) {
            super(mensagem);
        }
    }

    //Método para capturar erros de estoque e regras de negócio
    @ExceptionHandler(RegraNegocioException.class)
    public String tratarErroDeNegocio(RegraNegocioException ex, Model model) {
        logger.warn("Aviso de regra de negócio interceptado: {}", ex.getMessage());
        model.addAttribute("erro", ex.getMessage());
        return "atividades";
    }

    //Captura a ausência do favicon e trata como DEBUG/INFO leve, limpando o console
    @ExceptionHandler(NoResourceFoundException.class)
    public void tratarRecursoAusente(NoResourceFoundException ex) {
        logger.debug("Recurso estático não localizado pelo navegador: {}", ex.getResourcePath());
        // Não retorna nada e não quebra a tela do usuário
    }

    //Escudo para qualquer outro erro interno real e inesperado
    @ExceptionHandler(Exception.class)
    public String tratarErroInesperado(Exception ex, Model model) {
        logger.error("💥 Erro crítico pego no ExceptionController: ", ex);
        model.addAttribute("erro", "Ocorreu um erro interno no servidor. Tente reiniciar a sessão.");
        return "login";
    }
}