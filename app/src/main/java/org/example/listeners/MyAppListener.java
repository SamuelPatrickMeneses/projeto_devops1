package org.example.listeners;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class MyAppListener implements ServletContextListener {

    


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Este código roda quando a aplicação é iniciada
        System.out.println(">>> Aplicação iniciada! Context Path: " 
                            + sce.getServletContext().getContextPath());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Este código roda quando a aplicação é interrompida ou sofre redeploy

        System.out.println(">>> Aplicação encerrada. Limpando recursos...");
    }
}

