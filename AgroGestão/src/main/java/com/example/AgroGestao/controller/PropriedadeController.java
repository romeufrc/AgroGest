package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.repository.PropriedadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/propriedades-view")
public class PropriedadeController {

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @GetMapping
    public String exibirPropriedades(Model model) {
        model.addAttribute("propriedades", propriedadeRepository.findAll());

        if (!model.containsAttribute("novaPropriedade")) {
            model.addAttribute("novaPropriedade", new Propriedade());
        }
        // SE SEU ARQUIVO FOR "propriedades.html", RETORNE COM O "s" AQUI:
        return "propriedades";
    }

    @PostMapping("/salvar")
    public String salvarPropriedade(@ModelAttribute Propriedade propriedade) {
        propriedadeRepository.save(propriedade);
        return "redirect:/propriedades-view";
    }

    @GetMapping("/editar/{id}")
    public String editarPropriedade(@PathVariable Long id, Model model) {
        Propriedade propriedade = propriedadeRepository.findById(id).orElse(null);
        if (propriedade != null) {
            model.addAttribute("novaPropriedade", propriedade);
            model.addAttribute("propriedades", propriedadeRepository.findAll());
            // SE SEU ARQUIVO FOR "propriedades.html", RETORNE COM O "s" AQUI TAMBÉM:
            return "propriedades";
        }
        return "redirect:/propriedades-view";
    }

    @GetMapping("/excluir/{id}")
    public String excluirPropriedade(@PathVariable Long id) {
        propriedadeRepository.deleteById(id);
        return "redirect:/propriedades-view";
    }
}