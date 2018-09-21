package com.je.websocket.endpoint.sockjs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class SubController {

	@Autowired
	public SimpMessagingTemplate template;
	
	@MessageMapping("/subscribe")
	public void subscribe(ReceiveMessage rm) {
		// 广播使用convertAndSend方法，第一个参数为目的地，和js中订阅的目的地要一致
		template.convertAndSend("/topic/getResponse", rm.getName());

	}

	@MessageMapping("/queue")
	public void queuw(ReceiveMessage rm) {
		/*
		 * 广播使用convertAndSendToUser方法，第一个参数为用户id，此时js中的订阅地址为 "/user/" + 用户Id +
		 * "/message",其中"/user"是固定的
		 */
		template.convertAndSendToUser(rm.getName(), "/message", rm.getUser() + ":" + rm.getMessage());
	}

}
