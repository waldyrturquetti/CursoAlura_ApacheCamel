package br.com.caelum.camel.desafios.capitulo3;

import br.com.caelum.camel.pojos.Negociacao;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.thoughtworks.xstream.XStream;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.xstream.XStreamDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;

import java.text.SimpleDateFormat;

public class Capitulo3Desafio3RotaHttpPollingNegociacoes {

    public static void main(String[] args) throws Exception {

        final XStream xStream = new XStream();
        xStream.alias("negociacao", Negociacao.class);

        SimpleRegistry registro = new SimpleRegistry();
        registro.put("mysql", criaDataSource());

        CamelContext context = new DefaultCamelContext(registro);//construtor recebe registro

        context.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("timer://negociacoes?fixedRate=true&delay=3s&period=360s")
                        .to("http4://argentumws-spring.herokuapp.com/negociacoes")
                    .convertBodyTo(String.class)
                    .unmarshal(new XStreamDataFormat(xStream))
                    .split(body())
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                Negociacao negociacao = exchange.getIn().getBody(Negociacao.class);
                                exchange.setProperty("preco", negociacao.getPreco());
                                exchange.setProperty("quantidade", negociacao.getQuantidade());
                                String data = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(negociacao.getData().getTime());
                                exchange.setProperty("data", data);
                            }
                        })
                        .setBody(simple("insert into negociacao(preco, quantidade, data) values (${property.preco}, ${property.quantidade}, '${property.data}')"))
                        .log("${body}") //logando o comando esql
                        .delay(1000) //esperando 1s para deixar a execução mais fácil de entender
                        .to("jdbc:mysql");
            }
        });

        context.start();
        Thread.sleep(10000);
        context.stop();
    }

    private static MysqlConnectionPoolDataSource criaDataSource() {
        MysqlConnectionPoolDataSource mysqlDs = new MysqlConnectionPoolDataSource();
        mysqlDs.setDatabaseName("camel");
        mysqlDs.setServerName("127.0.0.1");
        mysqlDs.setPort(3306);
        mysqlDs.setUser("root");
        mysqlDs.setPassword("root");
        return mysqlDs;
    }
}
