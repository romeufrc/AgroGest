package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Atividade;
import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.model.Usuario;
import com.example.AgroGestao.repository.AtividadeRepository;
import com.example.AgroGestao.repository.InsumosRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import com.example.AgroGestao.service.AtividadeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller // Define esta classe como um Controller do Spring MVC
@RequestMapping("/atividades-view") // Mapeia as requisições para /atividades-view
public class AtividadeController {

    @Autowired
    private AtividadeRepository atividadeRepository; // Repositório de atividades

    @Autowired
    private PropriedadeRepository propriedadeRepository; // Repositório de propriedades

    @Autowired
    private InsumosRepository insumoRepository; // Repositório de insumos

    @Autowired
    private AtividadeService atividadeService; // Service responsável pelas regras de negócio

    @GetMapping
    public String exibirAtividades(@RequestParam(name = "propriedadeId", required = false) Long propriedadeId, Model model, HttpSession session) {

        // Recupera o usuário armazenado na sessão
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // Se não existir usuário logado, redireciona para a tela de login
        if (usuarioLogado == null) return "redirect:/login";

        // Busca todas as propriedades do usuário logado
        List<Propriedade> minhasPropriedades = propriedadeRepository.findByUsuarioId(usuarioLogado.getId());
        model.addAttribute("propriedades", minhasPropriedades);

        // Carrega os insumos do usuário
        model.addAttribute("listaInsumos", insumoRepository.findByUsuarioId(usuarioLogado.getId()));

        List<Atividade> minhasAtividades;

        // Verifica se foi selecionada uma propriedade específica
        if (propriedadeId != null) {

            // Busca apenas as atividades da propriedade selecionada
            minhasAtividades = atividadeRepository.findByPropriedadeId(propriedadeId);

            Propriedade p = propriedadeRepository.findById(propriedadeId).orElse(null);

            // Garante que a propriedade pertence ao usuário logado
            if (p != null && p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                model.addAttribute("propSelecionada", p);
            } else {
                return "redirect:/atividades-view";
            }
        } else {

            // Caso nenhuma propriedade seja selecionada, exibe todas as atividades do usuário
            minhasAtividades = atividadeRepository.findByUsuarioId(usuarioLogado.getId());
            model.addAttribute("propSelecionada", null);
        }

        // Envia a lista de atividades para a página
        model.addAttribute("atividades", minhasAtividades);

        // Cria um objeto vazio para o formulário caso não exista um objeto em edição
        if (!model.containsAttribute("novaAtividade")) {
            model.addAttribute("novaAtividade", new Atividade());
        }

        return "atividades";
    }

    @PostMapping("/salvar")
    public String salvarAtividade(@ModelAttribute Atividade atividade,
                                  @RequestParam(required = false) Long insumoId,
                                  @RequestParam(required = false) Integer quantidadeUsada, // Quantidade utilizada informada pelo usuário
                                  HttpSession session) {

        // Recupera o usuário logado
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // Verifica se existe usuário autenticado
        if (usuarioLogado == null) return "redirect:/login";

        // Verifica se a propriedade informada pertence ao usuário logado
        if (atividade.getPropriedade() != null && atividade.getPropriedade().getId() != null) {

            Propriedade prop = propriedadeRepository.findById(atividade.getPropriedade().getId()).orElse(null);

            if (prop != null && prop.getUsuario() != null && prop.getUsuario().getId().equals(usuarioLogado.getId())) {
                atividade.setPropriedade(prop);
            } else {
                return "redirect:/atividades-view";
            }
        }

        // Define um tipo padrão caso o usuário não informe
        if (atividade.getTipo() == null || atividade.getTipo().isEmpty()) {
            atividade.setTipo("Campo");
        }

        // Chama o Service para salvar a atividade e realizar a baixa de estoque
        atividadeService.salvarComBaixaEstoque(atividade, insumoId, quantidadeUsada);

        return "redirect:/atividades-view";
    }

    @GetMapping("/editar/{id}")
    public String editarAtividade(@PathVariable Long id, Model model, HttpSession session) {

        // Recupera o usuário logado
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // Caso não esteja autenticado, redireciona para login
        if (usuarioLogado == null) return "redirect:/login";

        // Busca a atividade pelo ID
        Atividade atividade = atividadeRepository.findById(id).orElse(null);

        // Verifica se a atividade pertence ao usuário logado
        if (atividade != null && atividade.getPropriedade() != null && atividade.getPropriedade().getUsuario() != null && atividade.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {

            // Envia a atividade para preencher o formulário de edição
            model.addAttribute("novaAtividade", atividade);

            // Recarrega os dados necessários para a tela
            model.addAttribute("atividades", atividadeRepository.findByUsuarioId(usuarioLogado.getId()));
            model.addAttribute("propriedades", propriedadeRepository.findByUsuarioId(usuarioLogado.getId()));
            model.addAttribute("listaInsumos", insumoRepository.findByUsuarioId(usuarioLogado.getId()));
            model.addAttribute("propSelecionada", null);

            return "atividades";
        }

        return "redirect:/atividades-view";
    }

    @GetMapping("/excluir/{id}")
    public String excluirAtividade(@PathVariable Long id, HttpSession session) {

        // Recupera o usuário logado
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // Impede acesso sem autenticação
        if (usuarioLogado == null) return "redirect:/login";

        // Busca a atividade pelo ID
        Atividade atividade = atividadeRepository.findById(id).orElse(null);

        // Verifica se a atividade pertence ao usuário antes de excluir
        if (atividade != null && atividade.getPropriedade() != null && atividade.getPropriedade().getUsuario() != null && atividade.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {
            atividadeRepository.deleteById(id);
        }

        return "redirect:/atividades-view";
    }
}