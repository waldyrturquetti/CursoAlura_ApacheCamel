package br.com.caelum.camel.desafios.capitulo6;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.validation.SchemaValidationException;
import org.xml.sax.SAXParseException;

public class RotaPedidosTratamento {

	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				//deve ser configurado antes de qualquer rota
				onException(SchemaValidationException.class)
						.handled(false)
						.maximumRedeliveries(3)
						.redeliveryDelay(4000)
						.onRedelivery(new Processor() {

							@Override
							public void process(Exchange exchange) throws Exception {
								int counter = (int) exchange.getIn().getHeader(Exchange.REDELIVERY_COUNTER);
								int max = (int) exchange.getIn().getHeader(Exchange.REDELIVERY_MAX_COUNTER);
								System.out.println("Redelivery - " + counter + "/" + max );;
							}
						});

				from("file:pedidos?delay=5s&noop=true")
						.routeId("rota-pedidos")
						.to("validator:pedido.xsd") //Validação
				.log("Processing file ${file:name}");

			}
		});

		context.start();
		Thread.sleep(2000);
		context.stop();
	}	
}
