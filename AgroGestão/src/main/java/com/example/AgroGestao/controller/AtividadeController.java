package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Atividade;
import com.example.AgroGestao.model.Insumos;
import com.example.AgroGestao.repository.AtividadeRepository;
import com.example.AgroGestao.repository.InsumosRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/atividades-view")
public class AtividadeController {

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private InsumosRepository insumosRepository;

    // Exibe o historico de operacoes e preenche os selects do form
    @GetMapping
    public String exibirAtividades(Model model) {
        model.addAttribute("atividades", atividadeRepository.findAll());
        model.addAttribute("propriedades", propriedadeRepository.findAll());
        model.addAttribute("insumos", insumosRepository.findAll()); // Lista os insumos no form

        // Se nao estiver editando, envia uma atividade vazia
        if (!model.containsAttribute("novaAtividade")) {
            model.addAttribute("novaAtividade", new Atividade());
        }
        return "atividades";
    }

    // Salva a atividade e reduz a quantidade usada do estoque automaticamente
    @PostMapping("/salvar")
    public String salvarNovaAtividade(@ModelAttribute Atividade atividade,
                                      @RequestParam(required = false) Long insumoId,
                                      @RequestParam(defaultValue = "0") double quantidadeUsada) {

        atividadeRepository.save(atividade); // Salva ou atualiza a atividade

        // REGRA DE NEGOCIO: Da baixa automatica no estoque do insumo selecionado
        if (insumoId != null && quantidadeUsada > 0) {
            Insumos insumo = insumosRepository.findById(insumoId).orElse(null);
            if (insumo != null) {
                double estoqueAtual = insumo.getQuantidade();
                if (estoqueAtual >= quantidadeUsada) {
                    insumo.setQuantidade(estoqueAtual - quantidadeUsada);
                    insumosRepository.save(insumo); // Salva o novo estoque reduzido
                }
            }
        }
        return "redirect:/atividades-view";
    }

    // Busca a atividade pelo ID e joga no form para editar
    @GetMapping("/editar/{id}")
    public String editarAtividade(@PathVariable Long id, Model model) {
        Atividade atividade = atividadeRepository.findById(id).orElse(null);
        if (atividade != null) {
            model.addAttribute("novaAtividade", atividade); // Preenche o form
            model.addAttribute("atividades", atividadeRepository.findAll()); // Mantem a lista carregada
            model.addAttribute("propriedades", propriedadeRepository.findAll());
            model.addAttribute("insumos", insumosRepository.findAll());
            return "atividades";
        }
        return "redirect:/atividades-view";
    }

    // Remove uma atividade registrada incorretamente
    @GetMapping("/excluir/{id}")
    public String excluirAtividade(@PathVariable Long id) {
        atividadeRepository.deleteById(id);
        return "redirect:/atividades-view";
    }
}