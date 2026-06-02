package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Insumos;
import com.example.AgroGestao.repository.InsumosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/insumos-view")
public class InsumosController {

    @Autowired
    private InsumosRepository insumosRepository;

    // Exibe a tela com a lista de insumos no estoque
    @GetMapping
    public String exibirInsumos(Model model) {
        model.addAttribute("insumos", insumosRepository.findAll());

        // Se nao estiver editando, envia um objeto novo para o form
        if (!model.containsAttribute("novoInsumo")) {
            model.addAttribute("novoInsumo", new Insumos());
        }
        return "insumos";
    }

    // Salva um novo insumo ou atualiza um existente
    @PostMapping("/salvar")
    public String salvarInsumo(@ModelAttribute Insumos insumo) {
        insumosRepository.save(insumo);
        return "redirect:/insumos-view";
    }

    // Busca o insumo pelo ID e joga de volta no form para editar
    @GetMapping("/editar/{id}")
    public String editarInsumo(@PathVariable Long id, Model model) {
        Insumos insumo = insumosRepository.findById(id).orElse(null);
        if (insumo != null) {
            model.addAttribute("novoInsumo", insumo); // Coloca os dados no form
            model.addAttribute("insumos", insumosRepository.findAll()); // Mantem a tabela carregada
            return "insumos";
        }
        return "redirect:/insumos-view";
    }

    // Exclui o insumo do estoque usando o ID
    @GetMapping("/excluir/{id}")
    public String excluirInsumo(@PathVariable Long id) {
        insumosRepository.deleteById(id);
        return "redirect:/insumos-view";
    }
}