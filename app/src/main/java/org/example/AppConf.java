package org.example;


import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("")
@ApplicationScoped
public class AppConf extends jakarta.ws.rs.core.Application {


    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<>();
        // Desativa a proteção CSRF que está causando o erro de classe não encontrada
        props.put("jakarta.mvc.security.CsrfProtection", "OFF");
        //props.put("jakarta.mvc.view.ViewEngine.viewFolder", "/WEB-INF/views/");
        return props;
    }

    @Produces
    @Dependent // O logger terá o ciclo de vida da classe que o injeta
    public Logger produceLogger(InjectionPoint injectionPoint) {
        // Obtém o nome da classe onde o @Inject está sendo feito
        String className = injectionPoint.getMember().getDeclaringClass().getName();
        return Logger.getLogger(className);
    }
}

