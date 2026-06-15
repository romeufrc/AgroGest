package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Atividade;
import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.repository.AtividadeRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import com.example.AgroGestao.service.AtividadeService; // Importei o meu novo Service aqui
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/atividades-view")
public class AtividadeController {

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private AtividadeService atividadeService; // Injetei a minha classe Service para usar a regra de estoque

    //Método para listar as atividades na tela com filtro
    @GetMapping
    public String exibirAtividades(@RequestParam(name = "propriedadeId", required = false) Long propriedadeId, Model model) {

        List<Atividade> todasAtividades = atividadeRepository.findAll();
        List<Atividade> atividadesFiltradas = new ArrayList<>();

        model.addAttribute("propriedades", propriedadeRepository.findAll());

        if (propriedadeId != null) {
            Propriedade prop = propriedadeRepository.findById(propriedadeId).orElse(null);
            if (prop != null) {
                model.addAttribute("propSelecionada", prop);

                if (todasAtividades != null) {
                    for (Atividade a : todasAtividades) {
                        if (a != null && a.getPropriedade() != null && propriedadeId.equals(gethId(a.getPropriedade()))) {
                            atividadesFiltradas.add(a);
                        }
                    }
                }
                model.addAttribute("atividades", atividadesFiltradas);
            } else {
                model.addAttribute("propSelecionada", null);
                model.addAttribute("atividades", todasAtividades);
            }
        } else {
            model.addAttribute("propSelecionada", null);
            model.addAttribute("atividades", todasAtividades);
        }

        if (!model.containsAttribute("novaAtividade")) {
            model.addAttribute("novaAtividade", new Atividade());
        }
        return "atividades";
    }

    // Método que o formulário chama ao clicar em salvar
    @PostMapping("/salvar")
    public String salvarAtividade(@ModelAttribute Atividade atividade) {
        //Vinculo a atividade com a fazenda certa antes de salvar
        if (atividade.getPropriedade() != null && atividade.getPropriedade().getId() != null) {
            Propriedade prop = propriedadeRepository.findById(atividade.getPropriedade().getId()).orElse(null);
            atividade.setPropriedade(prop);
        }

        //Se o tipo vier em branco, coloco "Campo" como padrão
        if (atividade.getTipo() == null || atividade.getTipo().isEmpty()) {
            atividade.setTipo("Campo");
        }

        //Mudei aqui para chamar o Service em vez do repository direto.
        //Agora, o salvamento passa pela validação de estoque e gera os logs automáticos.
        atividadeService.salvarEAtualizarEstoque(atividade);

        return "redirect:/atividades-view";
    }

    //Puxa a atividade pelo ID para carregar no botão de editar
    @GetMapping("/editar/{id}")
    public String editarAtividade(@PathVariable Long id, Model model) {
        Atividade atividade = atividadeRepository.findById(id).orElse(null);
        if (atividade != null) {
            model.addAttribute("novaAtividade", atividade);
            model.addAttribute("atividades", atividadeRepository.findAll());
            model.addAttribute("propriedades", propriedadeRepository.findAll());
            model.addAttribute("propSelecionada", null);
            return "atividades";
        }
        return "redirect:/atividades-view";
    }

    //Deleta a atividade pelo ID do banco
    @GetMapping("/excluir/{id}")
    public String excluirAtividade(@PathVariable Long id) {
        atividadeRepository.deleteById(id);
        return "redirect:/atividades-view";
    }

    //Funçãozinha auxiliar para pegar o ID da fazenda sem dar erro de nulo
    private Long gethId(Propriedade p) {
        return p != null ? p.getId() : null;
    }
}