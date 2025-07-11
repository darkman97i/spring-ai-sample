//package com.openkm.test.client;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@Slf4j
//@RestController
//public class TestController {
//
//	@Autowired
//	private MCPClientService mcpClientService;
//
//	@GetMapping(value = "/test", produces = "text/plain")
//	public String performSelfTest() {
//		log.info("Starting MCP client self-test with Spring AI...");
//		return mcpClientService.performSelfTest();
//	}
//}
