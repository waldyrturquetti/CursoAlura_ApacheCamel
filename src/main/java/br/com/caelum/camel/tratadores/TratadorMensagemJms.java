package br.com.caelum.camel.tratadores;

import javax.jms.Message;
import javax.jms.MessageListener;


public class TratadorMensagemJms implements MessageListener {
    public void onMessage(Message jmsMessage) {
        // código que processa a mensagem JMS
    }
}
