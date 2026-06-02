package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Usuario;
import com.example.AgroGestao.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Exibe a tela de login
    @GetMapping("/login")
    public String login() {
        // Se nao houver nenhum usuario no banco, cria o seu perfil automaticamente
        if (usuarioRepository.count() == 0) {
            Usuario usuarioPadrao = new Usuario();
            usuarioPadrao.setNome("Romeu Rodrigues");
            usuarioPadrao.setTelefone("83991360945");
            usuarioPadrao.setSenha("agro123");
            usuarioRepository.save(usuarioPadrao); // Salva direto no MySQL
        }
        return "login";
    }

    // Processa os dados digitados na tela
    @PostMapping("/login")
    public String fazerLogin(@RequestParam String telefone,
                             @RequestParam String senha,
                             HttpSession session,
                             Model model) {

        // SEGURANÇA: Remove espaços em branco que o teclado pode colocar sem querer
        String telefoneLimpo = (telefone != null) ? telefone.trim() : "";
        String senhaLimpa = (senha != null) ? senha.trim() : "";

        // Procura no banco de dados o usuario comparando os textos limpos (.trim())
        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getTelefone() != null && telefoneLimpo.equals(u.getTelefone().trim()))
                .findFirst()
                .orElse(null);

        // Valida se o usuario existe e se a senha confere (também usando trim)
        if (usuario != null && usuario.getSenha() != null && usuario.getSenha().trim().equals(senhaLimpa)) {
            session.setAttribute("usuarioLogado", usuario); // Salva o usuario na sessao
            return "redirect:/"; // Sucesso -> Vai para o painel principal
        }

        // Se falhar, manda a mensagem de erro para o HTML
        model.addAttribute("erro", "Telefone ou senha incorretos!");
        return "login";
    }

    // Rota para o botao de Sair limpar a sessao
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Apaga a sessao do navegador
        return "redirect:/login";
    }
}