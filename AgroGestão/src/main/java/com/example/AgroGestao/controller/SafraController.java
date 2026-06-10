package com.example.AgroGestao.controller;

// AQUI ESTÃO OS IMPORTS QUE REOLVEM O SEU ERRO DE BUILD:
// Eu importo explicitamente as entidades do pacote de modelos para o Java reconhecer
import com.example.AgroGestao.model.Safra;
import com.example.AgroGestao.model.Propriedade; // <-- Esse aqui mata o erro!

// Importo os repositórios para comunicação com o MySQL
import com.example.AgroGestao.repository.SafraRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;

// Importações padrão do Spring Boot e coleções
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/safras-view")
public class SafraController {

    @Autowired
    private SafraRepository safraRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    // Meu método de listagem com filtragem segura por fazenda
    @GetMapping
    public String exibirSafras(@RequestParam(name = "propriedadeId", required = false) Long propriedadeId, Model model) {
        model.addAttribute("propriedades", propriedadeRepository.findAll());

        List<Safra> todasSafras = safraRepository.findAll();
        List<Safra> filtradas = new ArrayList<>();

        if (propriedadeId != null) {
            Propriedade p = propriedadeRepository.findById(propriedadeId).orElse(null);
            if (p != null) {
                model.addAttribute("propSelecionada", p);
                for (Safra s : todasSafras) {
                    if (s.getPropriedade() != null && propriedadeId.equals(s.getPropriedade().getId())) {
                        filtradas.add(s);
                    }
                }
                model.addAttribute("safras", filtradas);
            } else {
                model.addAttribute("safras", todasSafras);
            }
        } else {
            model.addAttribute("propSelecionada", null);
            model.addAttribute("safras", todasSafras);
        }

        if (!model.containsAttribute("novaSafra")) {
            model.addAttribute("novaSafra", new Safra());
        }
        return "safra";
    }

    // Método que faz o INSERT ou UPDATE no banco de dados do MySQL
    @PostMapping("/salvar")
    public String salvarSafra(@ModelAttribute Safra safra) {
        safraRepository.save(safra);
        return "redirect:/safras-view";
    }

    // Recupera a safra selecionada para carregar no formulário premium
    @GetMapping("/editar/{id}")
    public String editarSafra(@PathVariable Long id, Model model) {
        Safra safra = safraRepository.findById(id).orElse(null);
        if (safra != null) {
            model.addAttribute("novaSafra", safra);
            model.addAttribute("safras", safraRepository.findAll());
            model.addAttribute("propriedades", propriedadeRepository.findAll());
            return "safra";
        }
        return "redirect:/safras-view";
    }

    // Exclui o registro da safra do banco pelo ID
    @GetMapping("/excluir/{id}")
    public String excluirSafra(@PathVariable Long id) {
        safraRepository.deleteById(id);
        return "redirect:/safras-view";
    }
}