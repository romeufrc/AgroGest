package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.*;
import com.example.AgroGestao.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    //Injeção de todos os meus repositórios para puxar os dados do dashboard
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

    //Meu método principal que monta a tela inicial (Dashboard) com todos os KPIs
    @GetMapping("/")
    public String exibirDashboard(@RequestParam(name = "propriedadeId", required = false) Long id, Model model) {

        model.addAttribute("propriedades", propriedadeRepository.findAll());

        //Variáveis que vão armazenar os totais para os "Cards" da tela
        long qtdPropriedades = propriedadeRepository.count();
        long qtdAtividades = 0;
        long qtdInsumos = 0;
        long qtdSafras = 0;
        double totalGastoCalculado = 0.0;
        List<Atividade> atividadesRecentes = new ArrayList<>();
        List<Safra> safrasFiltradas = new ArrayList<>();

        //Gatilhos de regras de negócio (Alertas na tela principal)
        boolean alertaOrcamentoEstourado = false;
        boolean alertaSemSafrasAtivas = false;
        boolean alertaColheitaProxima = false;
        boolean alertaEstoqueBaixo = false;
        boolean alertaSemInsumos = false;

        String nomeSafraUrgente = "";
        String itensEstoqueBaixo = "";
        double faturamentoProjetado = 0.0;

        // ====================================================================
        // CENÁRIO A: O usuário selecionou uma Fazenda específica no filtro
        // ====================================================================
        if (id != null) {
            Propriedade p = propriedadeRepository.findById(id).orElse(null);
            if (p != null) {
                model.addAttribute("propSelecionada", p);

                //Regra dinâmica: Pego o limite de estoque que o usuário cadastrou para esta fazenda. Se for vazio, uso 10 por padrão.
                int margemSegurancaEstoque = (p.getLimiteEstoqueBaixo() != null) ? p.getLimiteEstoqueBaixo() : 10;

                //1. Varredura de Atividades: Conto apenas as atividades desta propriedade
                List<Atividade> todasAtiv = atividadeRepository.findAll();
                if (todasAtiv != null) {
                    for (Atividade a : todasAtiv) {
                        if (a != null && a.getPropriedade() != null && id.equals(a.getPropriedade().getId())) {
                            qtdAtividades++;
                            atividadesRecentes.add(a);
                        }
                    }
                }

                // 2. Varredura Financeira: Somatório de gastos apenas desta fazenda
                List<Gasto> todosGastos = gastoRepository.findAll();
                if (todosGastos != null) {
                    for (Gasto g : todosGastos) {
                        if (g != null && g.getPropriedade() != null && id.equals(g.getPropriedade().getId()) && g.getValor() != null) {
                            totalGastoCalculado += g.getValor();
                        }
                    }
                }

                // 3. Varredura Logística: Avalio o estoque cruzando com a margem dinâmica do usuário
                List<Insumos> todosInsumos = insumosRepository.findAll();
                if (todosInsumos != null) {
                    for (Insumos ins : todosInsumos) {
                        if (ins != null && ins.getPropriedade() != null && id.equals(ins.getPropriedade().getId())) {
                            qtdInsumos++;

                            // Disparo de alerta reativo se o estoque estiver menor que o limite da fazenda
                            if (ins.getQuantidade() != null && ins.getQuantidade() < margemSegurancaEstoque) {
                                alertaEstoqueBaixo = true;
                                if (itensEstoqueBaixo.isEmpty()) {
                                    itensEstoqueBaixo = ins.getNome() + " (" + ins.getQuantidade() + " un)";
                                } else {
                                    itensEstoqueBaixo += ", " + ins.getNome() + " (" + ins.getQuantidade() + " un)";
                                }
                            }
                        }
                    }
                }

                // 4. Varredura de Safras e Cálculo de Faturamento e Lucro Projetado
                List<Safra> todasSafras = safraRepository.findAll();
                if (todasSafras != null) {
                    for (Safra s : todasSafras) {
                        if (s != null && s.getPropriedade() != null && id.equals(s.getPropriedade().getId())) {
                            qtdSafras++;
                            safrasFiltradas.add(s);

                            // Calculo o faturamento projetado (Estimativa de Produção x Preço da Saca)
                            if (s.getProducaoEstimadaSacas() != null && s.getPrecoSacaEsperado() != null) {
                                faturamentoProjetado += (s.getProducaoEstimadaSacas() * s.getPrecoSacaEsperado());
                            }

                            // Alerta visual de que a colheita está chegando
                            if (s.getStatus() != null && (s.getStatus().equalsIgnoreCase("EM MATURAÇÃO") || s.getStatus().equalsIgnoreCase("MATURAÇÃO"))) {
                                alertaColheitaProxima = true;
                                nomeSafraUrgente = s.getNome() + " [" + s.getCultura() + "]";
                            }
                        }
                    }
                }

                // Validação Financeira: Verifico se o gasto atual ultrapassou o teto estipulado para a fazenda
                if (p.getLimiteGasto() != null && totalGastoCalculado > p.getLimiteGasto()) {
                    alertaOrcamentoEstourado = true;
                }

                alertaSemSafrasAtivas = (qtdSafras == 0);
                alertaSemInsumos = (qtdInsumos == 0);
            }
        } else {
            // ====================================================================
            // CENÁRIO B: Visão Global (Nenhum filtro de fazenda selecionado)
            // ====================================================================
            qtdAtividades = atividadeRepository.count();
            qtdInsumos = insumosRepository.count();
            qtdSafras = safraRepository.count();
            totalGastoCalculado = obterTotalGastoGlobal();
            atividadesRecentes = atividadeRepository.findAll();
            safrasFiltradas = safraRepository.findAll();

            // No escopo global, o meu sistema avalia cada produto individualmente contra o limite da sua respectiva fazenda
            List<Insumos> todosInsumosGlobais = insumosRepository.findAll();
            if (todosInsumosGlobais != null) {
                for (Insumos ins : todosInsumosGlobais) {
                    if (ins != null && ins.getQuantidade() != null) {
                        int limiteEspecifico = 10;
                        if (ins.getPropriedade() != null && ins.getPropriedade().getLimiteEstoqueBaixo() != null) {
                            limiteEspecifico = ins.getPropriedade().getLimiteEstoqueBaixo();
                        }

                        if (ins.getQuantidade() < limiteEspecifico) {
                            alertaEstoqueBaixo = true;
                            if (itensEstoqueBaixo.isEmpty()) {
                                itensEstoqueBaixo = ins.getNome() + " (" + ins.getQuantidade() + " un)";
                            } else {
                                itensEstoqueBaixo += ", " + ins.getNome() + " (" + ins.getQuantidade() + " un)";
                            }
                        }
                    }
                }
            }

            // Somatório de faturamento global projetado somando todas as fazendas
            List<Safra> todasSafrasGlobais = safraRepository.findAll();
            if (todasSafrasGlobais != null) {
                for (Safra s : todasSafrasGlobais) {
                    if (s != null && s.getProducaoEstimadaSacas() != null && s.getPrecoSacaEsperado() != null) {
                        faturamentoProjetado += (s.getProducaoEstimadaSacas() * s.getPrecoSacaEsperado());
                    }
                }
            }
        }

        // ====================================================================
        // Geração da tabela de Logs Visuais da tela inicial
        // ====================================================================
        List<Map<String, Object>> logsSimulados = new ArrayList<>();
        Map<String, Object> log1 = new HashMap<>();
        log1.put("dataEvento", LocalDateTime.now().minusHours(1));
        log1.put("tipoAlerta", "SISTEMA");
        log1.put("mensagem", "Parâmetros operacionais atualizados: pontos de pedido recalculados dinamicamente.");
        logsSimulados.add(log1);

        if (alertaEstoqueBaixo) {
            Map<String, Object> log2 = new HashMap<>();
            log2.put("dataEvento", LocalDateTime.now());
            log2.put("tipoAlerta", "ESTOQUE");
            log2.put("mensagem", "Aviso emitido: Itens operando abaixo da margem mínima estabelecida: " + itensEstoqueBaixo);
            logsSimulados.add(log2);
        }

        // ====================================================================
        // Envio de todas as variáveis processadas para renderizar no front-end (Thymeleaf)
        // ====================================================================
        model.addAttribute("qtdPropriedades", qtdPropriedades);
        model.addAttribute("qtdAtividades", qtdAtividades);
        model.addAttribute("qtdInsumos", qtdInsumos);
        model.addAttribute("qtdSafras", qtdSafras);
        model.addAttribute("totalGasto", totalGastoCalculado);
        model.addAttribute("atividadesRecentes", atividadesRecentes);
        model.addAttribute("safrasFiltradas", safrasFiltradas);
        model.addAttribute("alertaOrcamento", alertaOrcamentoEstourado);
        model.addAttribute("alertaSafras", alertaSemSafrasAtivas);
        model.addAttribute("alertaColheita", alertaColheitaProxima);
        model.addAttribute("alertaEstoque", alertaEstoqueBaixo);
        model.addAttribute("alertaVazio", alertaSemInsumos);
        model.addAttribute("safraUrgenteNome", nomeSafraUrgente);
        model.addAttribute("produtosFaltaNome", itensEstoqueBaixo);
        model.addAttribute("faturamentoProjetado", faturamentoProjetado);

        // Indicador de Lucro Líquido: Faturamento Projetado menos os Gastos Reais
        model.addAttribute("lucroLiquidoProjetado", (faturamentoProjetado - totalGastoCalculado));
        model.addAttribute("logsAuditoria", logsSimulados);

        return "index";
    }

    // Método auxiliar privado para varrer o banco e somar todas as despesas da conta global
    private double obterTotalGastoGlobal() {
        double total = 0.0;
        List<Gasto> lista = gastoRepository.findAll();
        if (lista != null) {
            for (Gasto g : lista) {
                if (g != null && g.getValor() != null) {
                    total += g.getValor();
                }
            }
        }
        return total;
    }
}