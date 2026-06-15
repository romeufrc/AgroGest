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

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @GetMapping
    public String exibirPropriedades(Model model, HttpSession session) {
        // 1. Resgato o usuário que está logado na sessão
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // Se a sessão caiu, chuta de volta pro login
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // 2. Filtro a lista para mostrar APENAS as propriedades do usuário logado
        List<Propriedade> todasPropriedades = propriedadeRepository.findAll();
        List<Propriedade> minhasPropriedades = new ArrayList<>();

        for (Propriedade p : todasPropriedades) {
            if (p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                minhasPropriedades.add(p);
            }
        }

        model.addAttribute("propriedades", minhasPropriedades);

        if (!model.containsAttribute("novaPropriedade")) {
            model.addAttribute("novaPropriedade", new Propriedade());
        }

        return "propriedades";
    }

    @PostMapping("/salvar")
    public String salvarPropriedade(@ModelAttribute Propriedade propriedade, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado != null) {
            // 3. Amarra a nova fazenda ao usuário antes de salvar no banco!
            propriedade.setUsuario(usuarioLogado);
            propriedadeRepository.save(propriedade);
        }

        return "redirect:/propriedades-view";
    }

    @GetMapping("/editar/{id}")
    public String editarPropriedade(@PathVariable Long id, Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        Propriedade propriedade = propriedadeRepository.findById(id).orElse(null);

        // Regra de Segurança: Só deixa editar se a propriedade existir e pertencer a ele
        if (propriedade != null && propriedade.getUsuario() != null && propriedade.getUsuario().getId().equals(usuarioLogado.getId())) {

            // Refaz o filtro para a tabela continuar isolada
            List<Propriedade> todas = propriedadeRepository.findAll();
            List<Propriedade> minhas = new ArrayList<>();
            for (Propriedade p : todas) {
                if (p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                    minhas.add(p);
                }
            }

            model.addAttribute("novaPropriedade", propriedade);
            model.addAttribute("propriedades", minhas);

            return "propriedades";
        }

        return "redirect:/propriedades-view";
    }

    @GetMapping("/excluir/{id}")
    public String excluirPropriedade(@PathVariable Long id, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        Propriedade propriedade = propriedadeRepository.findById(id).orElse(null);

        // Regra de Segurança: Só deixa apagar se for dono da propriedade
        if (propriedade != null && propriedade.getUsuario() != null && propriedade.getUsuario().getId().equals(usuarioLogado.getId())) {
            propriedadeRepository.deleteById(id);
        }

        return "redirect:/propriedades-view";
    }
}