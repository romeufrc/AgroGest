package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Atividade;
import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.repository.AtividadeRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import com.example.AgroGestao.service.AtividadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private AtividadeService atividadeService;

    // 1. Rota para o Dashboard Principal (index.html)
    @GetMapping("/")
    public String exibirDashboard(Model model) {
        // Busca todas as atividades do banco e envia para a tela inicial
        model.addAttribute("atividadesRecentes", atividadeRepository.findAll());
        return "index";
    }

    // 2. Rota para abrir a página de Propriedades (propriedades.html)
    @GetMapping("/propriedades-view")
    public String exibirPropriedades(Model model) {
        model.addAttribute("propriedades", propriedadeRepository.findAll());
        model.addAttribute("novaPropriedade", new Propriedade());
        return "propriedades";
    }

    // 3. Rota para salvar uma nova propriedade através da tela
    @PostMapping("/propriedades-view/salvar")
    public String salvarNovaPropriedade(@ModelAttribute Propriedade propriedade) {
        propriedadeRepository.save(propriedade);
        return "redirect:/propriedades-view";
    }

    // 4. Rota para abrir a página de Atividades (atividades.html)
    @GetMapping("/atividades-view")
    public String exibirAtividades(Model model) {
        model.addAttribute("atividades", atividadeRepository.findAll());
        model.addAttribute("propriedades", propriedadeRepository.findAll());
        model.addAttribute("novaAtividade", new Atividade());
        return "atividades";
    }

    // 5. Rota para salvar uma nova atividade rodando a baixa do estoque
    @PostMapping("/atividades-view/salvar")
    public String salvarNovaAtividade(@ModelAttribute Atividade atividade) {
        atividadeService.salvarEAtualizarEstoque(atividade);
        return "redirect:/atividades-view";
    }

    // 6. Rota para excluir uma atividade direto pela tela
    @GetMapping("/atividades-view/excluir/{id}")
    public String excluirAtividadeVisual(@PathVariable Long id) {
        atividadeRepository.deleteById(id);
        return "redirect:/atividades-view";
    }
}