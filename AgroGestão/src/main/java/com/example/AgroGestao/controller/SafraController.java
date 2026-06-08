package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Safra;
import com.example.AgroGestao.repository.SafraRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/safras-view")
public class SafraController {

    @Autowired
    private SafraRepository safraRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    // Exibe a tela de gerenciamento de safras
    @GetMapping
    public String exibirSafras(Model model) {
        model.addAttribute("safras", safraRepository.findAll());
        model.addAttribute("propriedades", propriedadeRepository.findAll());

        if (!model.containsAttribute("novaSafra")) {
            model.addAttribute("novaSafra", new Safra());
        }
        return "safra"; // Aponta corretamente para o arquivo safra.html no singular
    }

    // Salva uma nova safra ou atualiza uma existente
    @PostMapping("/salvar")
    public String salvarSafra(@ModelAttribute Safra safra) {
        safraRepository.save(safra);
        return "redirect:/safras-view";
    }

    // Busca a safra pelo ID e joga de volta no formulário para edição
    @GetMapping("/editar/{id}")
    public String editarSafra(@PathVariable Long id, Model model) {
        Safra safra = safraRepository.findById(id).orElse(null);
        if (safra != null) {
            model.addAttribute("novaSafra", safra);
            model.addAttribute("safras", safraRepository.findAll());

            // CORRIGIDO: Vinculado perfeitamente ao nome da variável declarada no topo (propriedadeRepository)
            model.addAttribute("propriedades", propriedadeRepository.findAll());

            return "safra"; // Aponta corretamente para o arquivo safra.html no singular
        }
        return "redirect:/safras-view";
    }

    // Exclui a safra usando o ID
    @GetMapping("/excluir/{id}")
    public String excluirSafra(@PathVariable Long id) {
        safraRepository.deleteById(id);
        return "redirect:/safras-view";
    }
}