package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.*;
import com.example.AgroGestao.repository.*;
import jakarta.servlet.http.HttpSession;
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

    // Injeto todos os repositorios que eu preciso para varrer o banco de dados e montar o meu dashboard
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

    // Meu metodo principal que constroi a tela inicial. Ele recebe o id da propriedade para filtro e a sessao para seguranca.
    @GetMapping("/")
    public String exibirDashboard(@RequestParam(name = "propriedadeId", required = false) Long id, Model model, HttpSession session) {

        // Primeiro passo da minha seguranca: resgato quem e o usuario logado. Se a sessao for nula, derrubo para o login.
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Filtro o menu dropdown superior para carregar apenas as fazendas que pertencem ao meu usuario logado
        List<Propriedade> todasProp = propriedadeRepository.findAll();
        List<Propriedade> minhasPropriedades = new ArrayList<>();

        for (Propriedade p : todasProp) {
            if (p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                minhasPropriedades.add(p);
            }
        }
        model.addAttribute("propriedades", minhasPropriedades);

        // Inicializo os meus contadores e acumuladores que vao preencher os cards do dashboard
        long qtdPropriedades = minhasPropriedades.size();
        long qtdAtividades = 0;
        long qtdInsumos = 0;
        long qtdSafras = 0;
        double totalGastoCalculado = 0.0;
        List<Atividade> atividadesRecentes = new ArrayList<>();
        List<Safra> safrasFiltradas = new ArrayList<>();

        // Gatilhos para o meu painel de alertas reativos
        boolean alertaOrcamentoEstourado = false;
        boolean alertaSemSafrasAtivas = false;
        boolean alertaColheitaProxima = false;
        boolean alertaEstoqueBaixo = false;
        boolean alertaSemInsumos = false;

        // Variaveis de texto para guardar os nomes dos itens que estao com problemas
        String nomeSafraUrgente = "";
        String itensEstoqueBaixo = "";
        double faturamentoProjetado = 0.0;

        // CENARIO A: O usuario ativou o filtro para visualizar apenas uma fazenda especifica
        if (id != null) {
            // Regra de seguranca: busco a fazenda e verifico se ela realmente pertence a quem esta logado
            Propriedade p = propriedadeRepository.findById(id).orElse(null);
            if (p != null && p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                model.addAttribute("propSelecionada", p);

                // Puxo a margem de seguranca de estoque que o usuario definiu para esta fazenda. O padrao de fabrica e 10.
                int margemSegurancaEstoque = (p.getLimiteEstoqueBaixo() != null) ? p.getLimiteEstoqueBaixo() : 10;

                // Varredura de atividades filtrando pela propriedade escolhida
                List<Atividade> todasAtiv = atividadeRepository.findAll();
                if (todasAtiv != null) {
                    for (Atividade a : todasAtiv) {
                        if (a != null && a.getPropriedade() != null && id.equals(a.getPropriedade().getId())) {
                            qtdAtividades++;
                            atividadesRecentes.add(a);
                        }
                    }
                }

                // Varredura financeira somando apenas os gastos desta propriedade
                List<Gasto> todosGastos = gastoRepository.findAll();
                if (todosGastos != null) {
                    for (Gasto g : todosGastos) {
                        if (g != null && g.getPropriedade() != null && id.equals(g.getPropriedade().getId()) && g.getValor() != null) {
                            totalGastoCalculado += g.getValor();
                        }
                    }
                }

                // Varredura de estoque cruzando a quantidade atual com a margem de seguranca
                List<Insumos> todosInsumos = insumosRepository.findAll();
                if (todosInsumos != null) {
                    for (Insumos ins : todosInsumos) {
                        if (ins != null && ins.getPropriedade() != null && id.equals(ins.getPropriedade().getId())) {
                            qtdInsumos++;
                            if (ins.getQuantidade() != null && ins.getQuantidade() < margemSegurancaEstoque) {
                                alertaEstoqueBaixo = true;
                                // Se ja tiver um item na string, eu concateno com virgula. Senao, eu insiro o primeiro.
                                if (itensEstoqueBaixo.isEmpty()) {
                                    itensEstoqueBaixo = ins.getNome() + " (" + ins.getQuantidade() + " un)";
                                } else {
                                    itensEstoqueBaixo += ", " + ins.getNome() + " (" + ins.getQuantidade() + " un)";
                                }
                            }
                        }
                    }
                }

                // Varredura de safras avaliando o status de colheita
                List<Safra> todasSafras = safraRepository.findAll();
                if (todasSafras != null) {
                    for (Safra s : todasSafras) {
                        if (s != null && s.getPropriedade() != null && id.equals(s.getPropriedade().getId())) {
                            qtdSafras++;
                            safrasFiltradas.add(s);

                            // Calculo a projeção de ganhos: estimativa de sacas vezes o valor esperado por saca
                            if (s.getProducaoEstimadaSacas() != null && s.getPrecoSacaEsperado() != null) {
                                faturamentoProjetado += (s.getProducaoEstimadaSacas() * s.getPrecoSacaEsperado());
                            }

                            // Gatilho de alerta: Se a safra estiver em maturação, eu junto o nome dela na minha lista de avisos
                            if (s.getStatus() != null && (s.getStatus().equalsIgnoreCase("EM MATURAÇÃO") || s.getStatus().equalsIgnoreCase("MATURAÇÃO"))) {
                                alertaColheitaProxima = true;
                                if (nomeSafraUrgente.isEmpty()) {
                                    nomeSafraUrgente = s.getNome() + " [" + s.getCultura() + "]";
                                } else {
                                    nomeSafraUrgente += ", " + s.getNome() + " [" + s.getCultura() + "]";
                                }
                            }
                        }
                    }
                }

                // Valido se as despesas cadastradas ultrapassaram o teto estipulado para a fazenda
                if (p.getLimiteGasto() != null && totalGastoCalculado > p.getLimiteGasto()) {
                    alertaOrcamentoEstourado = true;
                }

                alertaSemSafrasAtivas = (qtdSafras == 0);
                alertaSemInsumos = (qtdInsumos == 0);
            } else {
                // Caso alguem tente forcar um ID de fazenda que nao lhe pertence pela URL
                return "redirect:/";
            }
        } else {
            // CENARIO B: Visao Global. O usuario quer ver os dados de todas as suas fazendas somados.

            // Calculo os totais globais cruzando cada registro com o ID do usuario logado para manter o isolamento
            List<Atividade> todasAtiv = atividadeRepository.findAll();
            for(Atividade a : todasAtiv){
                if(a.getPropriedade() != null && a.getPropriedade().getUsuario() != null && a.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())){
                    qtdAtividades++;
                    atividadesRecentes.add(a);
                }
            }

            // Varredura global de estoque avaliando o limite de seguranca especifico de cada fazenda vinculada
            List<Insumos> todosInsumosGlobais = insumosRepository.findAll();
            for(Insumos ins : todosInsumosGlobais){
                if(ins.getPropriedade() != null && ins.getPropriedade().getUsuario() != null && ins.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())){
                    qtdInsumos++;
                    int limiteEspecifico = (ins.getPropriedade().getLimiteEstoqueBaixo() != null) ? ins.getPropriedade().getLimiteEstoqueBaixo() : 10;
                    if (ins.getQuantidade() != null && ins.getQuantidade() < limiteEspecifico) {
                        alertaEstoqueBaixo = true;
                        if (itensEstoqueBaixo.isEmpty()) {
                            itensEstoqueBaixo = ins.getNome() + " (" + ins.getQuantidade() + " un)";
                        } else {
                            itensEstoqueBaixo += ", " + ins.getNome() + " (" + ins.getQuantidade() + " un)";
                        }
                    }
                }
            }

            // Varredura global de safras, somando faturamento e avaliando status de colheita em todas as fazendas do usuario
            List<Safra> todasSafrasGlobais = safraRepository.findAll();
            for(Safra s : todasSafrasGlobais){
                if(s.getPropriedade() != null && s.getPropriedade().getUsuario() != null && s.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())){
                    qtdSafras++;
                    safrasFiltradas.add(s);
                    if (s.getProducaoEstimadaSacas() != null && s.getPrecoSacaEsperado() != null) {
                        faturamentoProjetado += (s.getProducaoEstimadaSacas() * s.getPrecoSacaEsperado());
                    }

                    if (s.getStatus() != null && (s.getStatus().equalsIgnoreCase("EM MATURAÇÃO") || s.getStatus().equalsIgnoreCase("MATURAÇÃO"))) {
                        alertaColheitaProxima = true;
                        if (nomeSafraUrgente.isEmpty()) {
                            nomeSafraUrgente = s.getNome() + " [" + s.getCultura() + "]";
                        } else {
                            nomeSafraUrgente += ", " + s.getNome() + " [" + s.getCultura() + "]";
                        }
                    }
                }
            }

            // Somatorio de gastos isolados pelo dono da conta
            List<Gasto> listaGastos = gastoRepository.findAll();
            for (Gasto g : listaGastos) {
                if (g.getPropriedade() != null && g.getPropriedade().getUsuario() != null && g.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId()) && g.getValor() != null) {
                    totalGastoCalculado += g.getValor();
                }
            }
        }

        // Construcao da minha lista de logs virtuais que serao renderizados na interface do Dashboard
        List<Map<String, Object>> logsSimulados = new ArrayList<>();
        Map<String, Object> log1 = new HashMap<>();
        log1.put("dataEvento", LocalDateTime.now().minusHours(1));
        log1.put("tipoAlerta", "SISTEMA");
        log1.put("mensagem", "Sessao iniciada com sucesso. Dashboard isolado ativado.");
        logsSimulados.add(log1);

        if (alertaEstoqueBaixo) {
            Map<String, Object> log2 = new HashMap<>();
            log2.put("dataEvento", LocalDateTime.now());
            log2.put("tipoAlerta", "ESTOQUE");
            log2.put("mensagem", "Aviso emitido: Itens operando abaixo da margem minima estabelecida: " + itensEstoqueBaixo);
            logsSimulados.add(log2);
        }

        if (alertaColheitaProxima) {
            Map<String, Object> log3 = new HashMap<>();
            log3.put("dataEvento", LocalDateTime.now());
            log3.put("tipoAlerta", "SAFRA");
            log3.put("mensagem", "Aviso emitido: Safras proximas da colheita detectadas: " + nomeSafraUrgente);
            logsSimulados.add(log3);
        }

        // Mapeamento final entregando todas as metricas calculadas para o arquivo HTML
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

        // Minha formula para encontrar o Lucro Liquido subtraindo as despesas totais do faturamento projetado
        model.addAttribute("lucroLiquidoProjetado", (faturamentoProjetado - totalGastoCalculado));
        model.addAttribute("logsAuditoria", logsSimulados);

        return "index";
    }
}