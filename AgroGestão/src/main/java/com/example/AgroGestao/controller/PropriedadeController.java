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

    // Exibe a tela de listagem de propriedades
    @GetMapping
    public String exibirPropriedades(Model model) {
        model.addAttribute("propriedades", propriedadeRepository.findAll());

        // Se nao estiver editando, envia um objeto vazio para o form
        if (!model.containsAttribute("novaPropriedade")) {
            model.addAttribute("novaPropriedade", new Propriedade());
        }
        return "propriedades";
    }

    // Salva uma nova fazenda ou atualiza uma existente
    @PostMapping("/salvar")
    public String salvarNovaPropriedade(@ModelAttribute Propriedade propriedade) {
        propriedadeRepository.save(propriedade);
        return "redirect:/propriedades-view";
    }

    // Busca a propriedade e joga de volta no form da esquerda para editar
    @GetMapping("/editar/{id}")
    public String editarPropriedade(@PathVariable Long id, Model model) {
        Propriedade propriedade = propriedadeRepository.findById(id).orElse(null);
        if (propriedade != null) {
            model.addAttribute("novaPropriedade", propriedade); // Preenche o form
            model.addAttribute("propriedades", propriedadeRepository.findAll()); // Mantem a lista da direita
            return "propriedades";
        }
        return "redirect:/propriedades-view";
    }

    // Remove a fazenda do banco usando o ID
    @GetMapping("/excluir/{id}")
    public String excluirPropriedade(@PathVariable Long id) {
        try {
            propriedadeRepository.deleteById(id);
        } catch (Exception e) {
            // Caso tenha atividades vinculadas, retorna com aviso de erro na URL
            return "redirect:/propriedades-view?erroExclusao";
        }
        return "redirect:/propriedades-view";
    }
}