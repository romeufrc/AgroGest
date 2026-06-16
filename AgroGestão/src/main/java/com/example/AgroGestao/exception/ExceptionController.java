package com.example.AgroGestao.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Classe responsável pelo tratamento global de exceções da aplicação.
 * Qualquer exceção capturada aqui evita que o sistema apresente erros
 * diretamente para o usuário.
 */
@ControllerAdvice
public class ExceptionController {

    /**
     * Logger utilizado para registrar mensagens de aviso,
     * informações de depuração e erros críticos.
     */
    private static final Logger logger = LogManager.getLogger(ExceptionController.class);

    /**
     * Exceção personalizada para validações e regras de negócio.
     * Deve ser utilizada quando alguma operação não puder ser concluída
     * por uma regra específica do sistema.
     */
    public static class RegraNegocioException extends RuntimeException {

        /**
         * Construtor da exceção personalizada.
         *
         * @param mensagem descrição do erro ocorrido
         */
        public RegraNegocioException(String mensagem) {
            super(mensagem);
        }
    }

    /**
     * Trata exceções relacionadas às regras de negócio da aplicação.
     *
     * CORREÇÃO: antes este handler sempre retornava a view "atividades",
     * mesmo quando a exceção era lançada a partir de Gasto, Insumo ou
     * Safra (que usam o mesmo GastoService/InsumoService/SafraService
     * para bloquear valores inválidos). Isso fazia a página de atividades
     * ser renderizada sem os atributos de modelo que ela espera, quebrando
     * o template. Agora o handler redireciona de volta para a página de
     * onde a requisição partiu, preservando o fluxo de quem disparou o erro.
     *
     * @param ex exceção capturada
     * @param request usado para identificar a página de origem da requisição
     * @param redirectAttributes envia a mensagem de erro através do redirect
     * @return redirecionamento para a página de origem, com a mensagem de erro
     */
    @ExceptionHandler(RegraNegocioException.class)
    public String tratarErroDeNegocio(RegraNegocioException ex,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {

        // Registra o aviso no arquivo de log
        logger.warn("Aviso de regra de negócio interceptado: {}", ex.getMessage());

        // Disponibiliza a mensagem de erro para a próxima requisição (pós-redirect)
        redirectAttributes.addFlashAttribute("erro", ex.getMessage());

        // Identifica a página de onde a requisição partiu (ex: /gastos-view, /insumos-view)
        String referer = request.getHeader("Referer");

        // Volta para a página de origem; se não for possível identificá-la, vai para a home
        return "redirect:" + (referer != null && !referer.isEmpty() ? referer : "/");
    }

    /**
     * Trata solicitações de recursos inexistentes.
     * Exemplo: favicon.ico ou arquivos estáticos ausentes.
     *
     * @param ex exceção capturada
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public void tratarRecursoAusente(NoResourceFoundException ex) {

        // Apenas registra a ocorrência sem impactar o usuário
        logger.debug("Recurso estático não localizado pelo navegador: {}", ex.getResourcePath());
    }

    /**
     * Captura qualquer exceção não tratada especificamente.
     *
     * @param ex exceção capturada
     * @param model objeto utilizado para enviar dados à view
     * @return página de login com mensagem amigável
     */
    @ExceptionHandler(Exception.class)
    public String tratarErroInesperado(Exception ex, Model model) {

        // Registra o erro completo para análise futura
        logger.error("Erro crítico pego no ExceptionController: ", ex);

        // Mensagem amigável exibida ao usuário
        model.addAttribute(
                "erro",
                "Ocorreu um erro interno no servidor. Tente reiniciar a sessão."
        );

        // Redireciona para a tela de login
        return "login";
    }
}