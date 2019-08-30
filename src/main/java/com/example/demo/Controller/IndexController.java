package com.example.demo.Controller;

import com.example.demo.Model.Cliente;
import com.example.demo.Model.LoginForm;
import com.example.demo.Model.Usuario;
import com.example.demo.Service.ClienteService;
import com.example.demo.Service.PagoService;
import com.example.demo.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class IndexController extends ControlBaseController{

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    ClienteService clienteService;

    @Autowired
    PagoService pagoService;

    @Autowired
    public IndexController(){

    }

    @GetMapping("/home")
    public String home(Model model,HttpSession session) throws Exception{
        if(session.getAttribute("usuario") != null){
            Usuario us = (Usuario) session.getAttribute("usuario");
            model.addAttribute("rol", us.getRole().getRol());
            model.addAttribute("nombreUsuario",us.getNombre()+' '+us.getApellido());
            return "home";
        }else{
            return "redirect:/";
        }

    }

    @GetMapping("/")
    public String index(LoginForm loginForm,HttpSession session){
         if(session.getAttribute("usuario") != null){
            return "redirect:/home";
        }
        return "index";
    }

    @PostMapping("/login")
    public String login(Model model, @Valid LoginForm loginForm, BindingResult bindingResult, RedirectAttributes flash, HttpSession session) throws Exception{

        if(bindingResult.hasErrors()){
            return "index";
        }
        Usuario us = usuarioService.findByCorreo(loginForm.getCorreo());
        if(us != null){
            System.out.println("rol : " + us.getRole().getRol());
            String pass = ControlBaseController.Desencriptar(us.getPassword());
            if(loginForm.getPassword().equals(pass)){
                creandoUsuarioSession(us);
                return "redirect:/home";
            }else{
                flash.addAttribute("messageError","Contraseña invalida");
                return "index";
            }

        }else{
            flash.addAttribute("messageError","Correo invalida");
            return "index";
        }

    }


    @GetMapping("/logout")
    public String logout(Model model,HttpServletRequest request, HttpServletResponse response){
        HttpSession session = request.getSession(false);
        if(request.isRequestedSessionIdValid() && session != null)
        {
            session.invalidate();
        }
        return "redirect:/";
    }

    @GetMapping("/autorizacion")
    public String paginaError(){
        return "nopermiso";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model,HttpSession session){
        if(session.getAttribute("usuario") == null){
            return "redirect:/";
        }
        Usuario us = (Usuario) session.getAttribute("usuario");
        if(!us.getRole().getRol().equals("ADMIN")){
            return "redirect:/autorizacion";
        }

        Date date =  new Date();
        DateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<Cliente> cl = clienteService.ingresados(dataFormat.format(date));
        int pagos_recibidos = pagoService.pagosRealizado(dataFormat.format(date));
        int tamanio = cl.size();
        model.addAttribute("rol", us.getRole().getRol());
        model.addAttribute("nombreUsuario",us.getNombre()+' '+us.getApellido());
        model.addAttribute("cantiaClientes",tamanio);
        model.addAttribute("pagosRecibidos",pagos_recibidos);
        return "Dashboard/dashboard";
    }



}
