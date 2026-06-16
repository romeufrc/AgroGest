package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Gasto;
import com.example.AgroGestao.model.Propriedade;
import com.example.AgroGestao.model.Usuario;
import com.example.AgroGestao.repository.GastoRepository;
import com.example.AgroGestao.repository.PropriedadeRepository;
import com.example.AgroGestao.service.GastoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller // Define esta classe como controladora das páginas de gastos
@RequestMapping("/gastos-view") // Mapeia as requisições para /gastos-view
public class GastoController {

    @Autowired
    private GastoRepository gastoRepository; // Repositório de gastos

    @Autowired
    private PropriedadeRepository propriedadeRepository; // Repositório de propriedades

    @Autowired
    private GastoService gastoService; // Service responsável pelas regras de negócio dos gastos

    // Meu controlador de interface para a parte financeira. Tudo eh isolado pelo ID da sessao.
    @GetMapping
    public String exibirGastos(@RequestParam(name = "propriedadeId", required = false) Long propriedadeId, Model model, HttpSession session) {

        // Recupera o usuário logado na sessão
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // Impede acesso sem autenticação
        if (usuarioLogado == null) return "redirect:/login";

        // Carrego as propriedades relativas apenas a mim para o formulario
        List<Propriedade> minhasPropriedades = new ArrayList<>();

        // Filtra apenas as propriedades pertencentes ao usuário logado
        for (Propriedade p : propriedadeRepository.findAll()) {
            if (p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                minhasPropriedades.add(p);
            }
        }

        // Envia as propriedades para a tela
        model.addAttribute("propriedades", minhasPropriedades);

        List<Gasto> meusGastos = new ArrayList<>();
        double total = 0.0;

        // Calculo o total de despesas somando apenas os meus registros
        for (Gasto g : gastoRepository.findAll()) {

            // Verifica se o gasto pertence ao usuário logado
            if (g.getPropriedade() != null &&
                    g.getPropriedade().getUsuario() != null &&
                    g.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {

                // Caso exista filtro por propriedade
                if (propriedadeId != null) {

                    if (propriedadeId.equals(g.getPropriedade().getId())) {
                        meusGastos.add(g);

                        // Soma o valor do gasto ao total
                        if (g.getValor() != null) total += g.getValor();
                    }

                } else {

                    // Adiciona todos os gastos do usuário
                    meusGastos.add(g);

                    // Soma o valor do gasto ao total
                    if (g.getValor() != null) total += g.getValor();
                }
            }
        }

        // Envia os gastos e o total para a página
        model.addAttribute("gastos", meusGastos);
        model.addAttribute("totalGasto", total);

        // Verifica se uma propriedade foi selecionada
        if (propriedadeId != null) {

            Propriedade p = propriedadeRepository.findById(propriedadeId).orElse(null);

            // Garante que a propriedade pertence ao usuário
            if (p != null && p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId())) {
                model.addAttribute("propSelecionada", p);
            } else {
                return "redirect:/gastos-view";
            }

        } else {

            // Nenhuma propriedade filtrada
            model.addAttribute("propSelecionada", null);
        }

        // Cria um objeto vazio para o formulário de cadastro
        if (!model.containsAttribute("novoGasto")) {
            model.addAttribute("novoGasto", new Gasto());
        }

        return "gastos";
    }

    // Processamento do salvamento de contas passando pela validacao de usuario
    @PostMapping("/salvar")
    public String salvarGasto(@ModelAttribute Gasto gasto, HttpSession session) {

        // Recupera o usuário logado
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // Impede acesso sem login
        if (usuarioLogado == null) return "redirect:/login";

        // Verifica se a propriedade informada pertence ao usuário
        if (gasto.getPropriedade() != null && gasto.getPropriedade().getId() != null) {

            Propriedade prop = propriedadeRepository.findById(gasto.getPropriedade().getId()).orElse(null);

            if (prop != null &&
                    prop.getUsuario() != null &&
                    prop.getUsuario().getId().equals(usuarioLogado.getId())) {

                gasto.setPropriedade(prop);

            } else {
                return "redirect:/gastos-view";
            }
        }

        // Meu servico cuida de negar valores negativos
        gastoService.registrarGasto(gasto);

        return "redirect:/gastos-view";
    }

    // Busco os dados de uma despesa garantindo o isolamento
    @GetMapping("/editar/{id}")
    public String editarGasto(@PathVariable Long id, Model model, HttpSession session) {

        // Recupera o usuário logado
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // Impede acesso sem login
        if (usuarioLogado == null) return "redirect:/login";

        // Busca o gasto pelo ID
        Gasto gasto = gastoRepository.findById(id).orElse(null);

        // Verifica se o gasto pertence ao usuário logado
        if (gasto != null &&
                gasto.getPropriedade() != null &&
                gasto.getPropriedade().getUsuario() != null &&
                gasto.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {

            List<Propriedade> minhasProp = new ArrayList<>();

            // Carrega apenas as propriedades do usuário
            for (Propriedade p : propriedadeRepository.findAll()) {
                if (p.getUsuario() != null && p.getUsuario().getId().equals(usuarioLogado.getId()))
                    minhasProp.add(p);
            }

            List<Gasto> meusGastos = new ArrayList<>();
            double total = 0.0;

            // Carrega os gastos do usuário
            for (Gasto g : gastoRepository.findAll()) {
                if (g.getPropriedade() != null &&
                        g.getPropriedade().getUsuario() != null &&
                        g.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {

                    meusGastos.add(g);

                    if (g.getValor() != null)
                        total += g.getValor();
                }
            }

            // Envia os dados para preencher o formulário de edição
            model.addAttribute("novoGasto", gasto);

            // Atualiza os dados exibidos na tela
            model.addAttribute("gastos", meusGastos);
            model.addAttribute("propriedades", minhasProp);
            model.addAttribute("propSelecionada", null);
            model.addAttribute("totalGasto", total);

            return "gastos";
        }

        return "redirect:/gastos-view";
    }

    // Exclusao bloqueada para terceiros
    @GetMapping("/excluir/{id}")
    public String excluirGasto(@PathVariable Long id, HttpSession session) {

        // Recupera o usuário logado
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // Impede acesso sem login
        if (usuarioLogado == null) return "redirect:/login";

        // Busca o gasto pelo ID
        Gasto gasto = gastoRepository.findById(id).orElse(null);

        // Verifica se o gasto pertence ao usuário antes de excluir
        if (gasto != null &&
                gasto.getPropriedade() != null &&
                gasto.getPropriedade().getUsuario() != null &&
                gasto.getPropriedade().getUsuario().getId().equals(usuarioLogado.getId())) {

            gastoRepository.deleteById(id);
        }

        return "redirect:/gastos-view";
    }
}