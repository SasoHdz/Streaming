/**
 * 
 */
package com.soa.jms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import com.google.gson.Gson;
import com.soa.business.StreamingBusiness;
import com.soa.dto.DatosRenta;
import com.soa.dto.Renta;
import com.soa.dto.Respuesta;

/**
 * Class for receiving messages in an artemis queue.
 */
@Component
public class ArtemisListenerStreaming {
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    private StreamingBusiness business;

    @Autowired
    private JmsSender sender;

    /** Nombre de la cola de respuesta del microservicio. */
    @Value("${streaming.queue.name.out}")
    private String outQueueName;

    @JmsListener(destination = "${streaming.queue.name.in}")
    public void receive(String message) {
        System.out.println(String.format("Received message: %s",
                message));
        Gson gson = new Gson();
        Renta renta = gson.fromJson(message, Renta.class);
        Respuesta respuesta = business.transmitir(renta);
        System.out.println("Resultado de consulta: "+respuesta);
        try {
            sender.sendMessage(message, outQueueName); 
            System.out.println(String.format("Mensaje enviado: %s",respuesta.toString()));
            
            scheduler.schedule(() -> {
                sender.sendMessage("Tiempo excedido....",outQueueName);
                Respuesta terminarTransmision = new Respuesta("Transmision finalizada", false);
                business.detenerTransmision(renta.getTitulo());
                System.out.println(String.format("Mensaje enviado: %s", terminarTransmision));
            },renta.getTime(), TimeUnit.SECONDS);
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
