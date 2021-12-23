package br.com.caelum.camel.desafios;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class Capitulo3Desafio1RotaHttpPollingNegociacoes {

    public static void main(String[] args) throws Exception {

        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("timer://negociacoes?fixedRate=true&delay=1s&period=360s")
                        .to("http4://argentumws-spring.herokuapp.com/negociacoes")
                        .convertBodyTo(String.class)
                        .setHeader("CamelFileName", constant("negociações.xml"))
                        .log("${body}")
                .to("file:saida");
            }
        });

        context.start();
        Thread.sleep(2000);
        context.stop();
    }
}
