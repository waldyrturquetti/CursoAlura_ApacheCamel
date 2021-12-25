package br.com.caelum.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.impl.DefaultCamelContext;

public class RotaPedidos {

	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {

				from("file:pedidos?delay=5s&noop=true")
						.routeId("rota-pedidos")
						//Processamento síncrono
//						.multicast()			//Envia o mesmo body pras duas rotas
//						.parallelProcessing()	//Cada subrota é processada paralelamente
//						.timeout(500)			//Define um timeout no processamento
//					.to("direct:http") 			//Sub rota
//					.to("direct:soap");			//Sub rota
					//Processamento assíncrono
					.to("seda:http")
					.to("seda:soap");

				//Chamando o serviço http.
				//Processamento síncrono
//				from("direct:http")
				//Processamento assíncrono
				from("seda:http")
					.routeId("rota-http")
						.setProperty("pedidoId", xpath("/pedido/id/text()"))
						.setProperty("clienteId", xpath("/pedido/pagamento/email-titular/text()"))
						.split().xpath("/pedido/itens/item")
							.log("${id} - ${body}")
						.filter().xpath("/item/formato[text()='EBOOK']")
						.setProperty("ebookId", xpath("/item/livro/codigo/text()"))
							.log("${id} - ${body}")
						.marshal().xmljson()
							.log("${id} - ${body}")
						.setHeader(Exchange.HTTP_METHOD, HttpMethods.GET)
						.setHeader(Exchange.HTTP_QUERY,
								simple("ebookId=${property.ebookId}&pedidoId=${property.pedidoId}&clienteId=${property.clienteId}"))
					.to("http4://localhost:8080/webservices/ebook/item");

				//Chamando o serviço soap.
				//Processamento síncrono
//				from("direct:soap")
				//Processamento assíncrono
				from("seda:soap")
					.routeId("rota-soap")
						.log("${body}")
						.setBody(constant("<envelope>teste<envelope>"))
						.log("${body}")
					.to("mock:soap");//O mock simula um endpoint.

			}
		});

		context.start();

		//Podemos enviar uma mensagem programaticamente para um rota que começa com direct.
//		ProducerTemplate producer = context.createProducerTemplate();
//		producer.sendBody("direct:soap", "<pedido> ... </pedido>");

		Thread.sleep(2000);
		context.stop();
	}	
}
