package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Insumos;
import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.repository.InsumosRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import com.example.AgroGestao.service.InsumoService; // Importei a minha classe Service aqui
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/insumos-view")
public class InsumosController {

    @Autowired
    private InsumosRepository insumosRepository;

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private InsumoService insumoService; // Injetei o Service para usar as regras do armazém e disparar os logs

    //Meu método para listar os insumos na tela com filtro por fazenda
    @GetMapping
    public String exibirInsumos(@RequestParam(name = "propriedadeId", required = false) Long propriedadeId, Model model) {
        model.addAttribute("propriedades", propriedadeRepository.findAll());

        List<Insumos> todosInsumos = insumosRepository.findAll();
        List<Insumos> filtrados = new ArrayList<>();

        if (propriedadeId != null) {
            Propriedade p = propriedadeRepository.findById(propriedadeId).orElse(null);
            if (p != null) {
                model.addAttribute("propSelecionada", p);
                for (Insumos ins : todosInsumos) {
                    if (ins.getPropriedade() != null && propriedadeId.equals(ins.getPropriedade().getId())) {
                        filtrados.add(ins);
                    }
                }
                model.addAttribute("insumos", filtrados);
            } else {
                model.addAttribute("insumos", todosInsumos);
            }
        } else {
            model.addAttribute("propSelecionada", null);
            model.addAttribute("insumos", todosInsumos);
        }

        if (!model.containsAttribute("novoInsumo")) {
            model.addAttribute("novoInsumo", new Insumos());
        }
        return "insumos";
    }

    // Método que o formulário chama ao clicar em salvar um produto novo ou atualizado
    @PostMapping("/salvar")
    public String salvarInsumo(@ModelAttribute Insumos insumo) {
        //Troquei a chamada direta ao banco pelo meu Service.
        // Agora, se o cara tentar colocar quantidade negativa ou sem nome, o Service barra e avisa no Log!
        insumoService.salvarInsumo(insumo);
        return "redirect:/insumos-view";
    }

    //Puxa o produto pelo ID para carregar os dados no botão de editar da tabela
    @GetMapping("/editar/{id}")
    public String editarInsumo(@PathVariable Long id, Model model) {
        Insumos insumo = insumosRepository.findById(id).orElse(null);
        if (insumo != null) {
            model.addAttribute("novoInsumo", insumo);
            model.addAttribute("insumos", insumosRepository.findAll());
            model.addAttribute("propriedades", propriedadeRepository.findAll());
            return "insumos";
        }
        return "redirect:/insumos-view";
    }

    //Deleta o produto pelo ID quando clica na lixeira
    @GetMapping("/excluir/{id}")
    public String excluirInsumo(@PathVariable Long id) {
        insumoService.deletarInsumo(id);
        return "redirect:/insumos-view";
    }
}