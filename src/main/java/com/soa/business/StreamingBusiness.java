/**
 * 
 */
package com.soa.business;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.soa.dto.Renta;
import com.soa.dto.Respuesta;

/**
 * Clase para concatenación de datos personales.
 */
@Component
public class StreamingBusiness {
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    

    
    public Respuesta transmitir(Renta renta) {
                
        System.out.println("Transmitiendo : " + renta.getTitulo());
        jdbcTemplate.execute("UPDATE Peliculas SET noOcup = noOcup+1 WHERE titulo = '"+renta.getTitulo()+"'");
        scheduler.schedule(() -> {
            System.out.println("Emision terminada");
        },renta.getTime(), TimeUnit.SECONDS);
        
        Respuesta resp = new Respuesta("Transmitiendo ... : "+renta.getTitulo(), true);

        return resp;
        
    }
    
    public void detenerTransmision(String titulo) {
        jdbcTemplate.execute("UPDATE Peliculas SET noOcup = noOcup-1 WHERE titulo = '"+titulo+"'");
    }
   
   
}
