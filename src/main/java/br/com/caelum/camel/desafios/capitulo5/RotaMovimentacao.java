package br.com.caelum.camel.desafios.capitulo5;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.impl.DefaultCamelContext;

public class RotaMovimentacao {

	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				from("direct:entrada")
//						.transform(body().regexReplaceAll("tipo", "tipoEntrada")) //Substituimos no body da mensagem cada ocorrência tipo por tipoEntrada.
						.log("Corpo antes do XSLT: ${body}")
						.to("xslt:movimentacao-para-soap.xslt")
						.log("Resultado do Template Movimentação: ${body}")
						.setHeader(Exchange.FILE_NAME, constant("movimentacoes.html"))
					.to("file:saida");

			}
		});

		context.start();
		ProducerTemplate producer = context.createProducerTemplate();
		producer.sendBody(
				"direct:entrada",
				"<movimentacoes>" +
						"<movimentacao><valor>2314.4</valor><data>11/12/2015</data><tipo>ENTRADA</tipo></movimentacao>" +
						"<movimentacao><valor>546.98</valor><data>11/12/2015</data><tipo>SAIDA</tipo></movimentacao>" +
						"<movimentacao><valor>314.1</valor><data>12/12/2015</data><tipo>SAIDA</tipo></movimentacao>" +
						"<movimentacao><valor>56.99</valor><data>13/12/2015</data><tipo>SAIDA</tipo></movimentacao>" +
						"</movimentacoes>"
		);

		Thread.sleep(2000);
		context.stop();
	}	
}
