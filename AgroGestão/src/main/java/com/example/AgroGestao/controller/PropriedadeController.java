package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.model.Usuario;
import com.example.AgroGestao.repository.PropriedadeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/propriedades-view")
public class PropriedadeController {

    // Injeta o repositório responsável pelas operações no banco de dados
    @Autowired
    private PropriedadeRepository propriedadeRepository;

    // Exibe a página de propriedades
    @GetMapping
    public String exibirPropriedades(Model model, HttpSession session) {

        // Recupera o usuário armazenado na sessão
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // Caso não exista usuário logado, redireciona para a tela de login
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Busca todas as propriedades cadastradas
        List<Propriedade> todasPropriedades = propriedadeRepository.findAll();

        // Lista que armazenará apenas as propriedades do usuário logado
        List<Propriedade> minhasPropriedades = new ArrayList<>();

        // Filtra as propriedades pertencentes ao usuário atual
        for (Propriedade p : todasPropriedades) {
            if (p.getUsuario() != null &&
                    p.getUsuario().getId().equals(usuarioLogado.getId())) {
                minhasPropriedades.add(p);
            }
        }

        // Envia a lista filtrada para a view
        model.addAttribute("propriedades", minhasPropriedades);

        // Cria um objeto vazio para o formulário caso não exista um em edição
        if (!model.containsAttribute("novaPropriedade")) {
            model.addAttribute("novaPropriedade", new Propriedade());
        }

        // Retorna a página propriedades.html
        return "propriedades";
    }

    // Salva uma nova propriedade
    @PostMapping("/salvar")
    public String salvarPropriedade(@ModelAttribute Propriedade propriedade,
                                    HttpSession session) {

        // Recupera o usuário logado
        Usuario usuarioLogado =
                (Usuario) session.getAttribute("usuarioLogado");

        // Associa a propriedade ao usuário antes de salvar
        if (usuarioLogado != null) {
            propriedade.setUsuario(usuarioLogado);
            propriedadeRepository.save(propriedade);
        }

        // Atualiza a página após o cadastro
        return "redirect:/propriedades-view";
    }

    // Carrega os dados de uma propriedade para edição
    @GetMapping("/editar/{id}")
    public String editarPropriedade(@PathVariable Long id,
                                    Model model,
                                    HttpSession session) {

        // Verifica se existe usuário autenticado
        Usuario usuarioLogado =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Busca a propriedade pelo ID
        Propriedade propriedade =
                propriedadeRepository.findById(id).orElse(null);

        // Permite editar apenas propriedades do próprio usuário
        if (propriedade != null &&
                propriedade.getUsuario() != null &&
                propriedade.getUsuario().getId().equals(usuarioLogado.getId())) {

            // Recarrega a lista filtrada de propriedades
            List<Propriedade> todas = propriedadeRepository.findAll();
            List<Propriedade> minhas = new ArrayList<>();

            for (Propriedade p : todas) {
                if (p.getUsuario() != null &&
                        p.getUsuario().getId().equals(usuarioLogado.getId())) {
                    minhas.add(p);
                }
            }

            // Envia a propriedade selecionada para o formulário
            model.addAttribute("novaPropriedade", propriedade);

            // Envia a lista de propriedades para a tabela
            model.addAttribute("propriedades", minhas);

            return "propriedades";
        }

        // Se não for o dono da propriedade, volta para a listagem
        return "redirect:/propriedades-view";
    }

    // Remove uma propriedade
    @GetMapping("/excluir/{id}")
    public String excluirPropriedade(@PathVariable Long id,
                                     HttpSession session) {

        // Recupera o usuário da sessão
        Usuario usuarioLogado =
                (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Busca a propriedade a ser removida
        Propriedade propriedade =
                propriedadeRepository.findById(id).orElse(null);

        // Exclui apenas se a propriedade pertencer ao usuário logado
        if (propriedade != null &&
                propriedade.getUsuario() != null &&
                propriedade.getUsuario().getId().equals(usuarioLogado.getId())) {

            propriedadeRepository.deleteById(id);
        }

        // Atualiza a listagem após a exclusão
        return "redirect:/propriedades-view";
    }
}