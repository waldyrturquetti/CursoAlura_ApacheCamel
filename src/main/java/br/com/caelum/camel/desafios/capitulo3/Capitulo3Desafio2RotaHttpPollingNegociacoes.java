package br.com.caelum.camel.desafios.capitulo3;

import br.com.caelum.camel.pojos.Negociacao;
import com.thoughtworks.xstream.XStream;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.xstream.XStreamDataFormat;
import org.apache.camel.impl.DefaultCamelContext;

public class Capitulo3Desafio2RotaHttpPollingNegociacoes {

    public static void main(String[] args) throws Exception {

        final XStream xStream = new XStream();
        xStream.alias("negociacao", Negociacao.class);

        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("timer://negociacoes?fixedRate=true&delay=3s&period=360s")
                        .to("http4://argentumws-spring.herokuapp.com/negociacoes")
                    .convertBodyTo(String.class)
                    .unmarshal(new XStreamDataFormat(xStream))
                    .split(body())
                        .log("${body}")
                    .end();
            }
        });

        context.start();
        Thread.sleep(10000);
        context.stop();
    }
}
