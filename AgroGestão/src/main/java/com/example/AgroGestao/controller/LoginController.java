package com.example.AgroGestao.controller;

import com.example.AgroGestao.model.Usuario;
import com.example.AgroGestao.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
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

    @GetMapping("/login")
    public String login() {
        //Se o banco estiver vazio, eu crio o perfil inicial já com a senha criptografada
        if (usuarioRepository.count() == 0) {
            Usuario usuarioPadrao = new Usuario();
            usuarioPadrao.setNome("Romeu Rodrigues");
            usuarioPadrao.setTelefone("83991360945");

            //Criptografo a senha "agro123" usando o algoritmo BCrypt antes de salvar no banco
            String senhaCriptografada = BCrypt.hashpw("agro123", BCrypt.gensalt());
            usuarioPadrao.setSenha(senhaCriptografada);

            usuarioRepository.save(usuarioPadrao);
        }
        return "login";
    }

    @PostMapping("/login")
    public String fazerLogin(@RequestParam String telefone,
                             @RequestParam String senha,
                             HttpSession session,
                             Model model) {

        String telefoneLimpo = (telefone != null) ? telefone.trim() : "";
        String senhaLimpa = (senha != null) ? senha.trim() : "";

        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getTelefone() != null && telefoneLimpo.equals(u.getTelefone().trim()))
                .findFirst()
                .orElse(null);

        // Validacao de seguranca: Uso o metodo checkpw do BCrypt para comparar a senha limpa digitada com o hash salvo no banco
        if (usuario != null && usuario.getSenha() != null && BCrypt.checkpw(senhaLimpa, usuario.getSenha())) {

            session.setAttribute("usuarioId", usuario.getId());
            session.setAttribute("usuarioLogado", usuario);

            return "redirect:/";
        }

        model.addAttribute("erro", "Telefone ou senha incorretos!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}