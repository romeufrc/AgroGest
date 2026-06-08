package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Gasto;
import com.example.AgroGestao.repository.AtividadeRepository;
import com.example.AgroGestao.repository.InsumosRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import com.example.AgroGestao.repository.GastoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.AgroGestao.repository.SafraRepository;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private PropriedadeRepository propriedadeRepository;

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private InsumosRepository insumosRepository;

    @Autowired
    private GastoRepository gastoRepository;

    @Autowired
    private SafraRepository safraRepository;

    // ==========================================
    // ROTA PRINCIPAL: DASHBOARD DINÂMICO
    // ==========================================
    @GetMapping("/")
    public String exibirDashboard(Model model) {
        // 1. Coleta os contadores globais diretamente do banco de dados
        long qtdPropriedades = propriedadeRepository.count();
        long qtdAtividades = atividadeRepository.count();
        long qtdInsumos = insumosRepository.count();
        long qtdSafras = safraRepository.count();


        // 2. Recupera todos os gastos lançados e calcula a soma total
        List<Gasto> listaGastos = gastoRepository.findAll();
        double totalGasto = 0.0;
        if (listaGastos != null) {
            for (Gasto g : listaGastos) {
                if (g.getValor() != null) {
                    totalGasto += g.getValor();
                }
            }
        }

        // 3. Alimenta as variáveis do Thymeleaf na tela inicial (index.html)
        model.addAttribute("qtdPropriedades", qtdPropriedades);
        model.addAttribute("qtdAtividades", qtdAtividades);
        model.addAttribute("qtdInsumos", qtdInsumos);
        model.addAttribute("totalGasto", totalGasto);
        model.addAttribute("qtdSafras", qtdSafras);

        // 4. Lista de histórico rápido para a tabela de feeds recentes
        model.addAttribute("atividadesRecentes", atividadeRepository.findAll());

        return "index";
    }
}